/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.neo4j.bolt.health;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.Collections;
import java.util.concurrent.CompletionStage;

import javax.inject.Singleton;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.summary.ServerInfo;
import org.reactivestreams.Publisher;

/**
 * A Health Indicator for Neo4j.
 *
 * @author graemerocher
 * @since 1.0
 */
@Requires(classes = HealthIndicator.class)
@Singleton
public class Neo4jHealthIndicator implements HealthIndicator {

    public static final String NAME = "neo4j";
    private final Driver boltDriver;

    private static final SessionConfig DEFAULT_SESSION_CONFIG = SessionConfig.builder()
        .withDefaultAccessMode(AccessMode.WRITE)
        .build();

    /**
     * Constructor.
     * @param boltDriver driver
     */
    public Neo4jHealthIndicator(Driver boltDriver) {
        this.boltDriver = boltDriver;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        try {
            AsyncSession session = boltDriver.asyncSession(DEFAULT_SESSION_CONFIG);

            Single<HealthResult> healthResultSingle = Single.create(emitter -> {
                CompletionStage<ResultCursor> query =
                    session.runAsync("RETURN 1 AS result");

                query
                    .thenComposeAsync(ResultCursor::consumeAsync)
                    .handleAsync((resultSummaryStage, throwable) -> {
                        if (throwable != null) {
                            return buildErrorResult(throwable);
                        } else {
                            HealthResult.Builder status = HealthResult.builder(NAME, HealthStatus.UP);
                            ServerInfo serverInfo = resultSummaryStage.server();
                            status.details(Collections.singletonMap(
                                "server", serverInfo.version() + "@" + serverInfo.address()));
                            return status.build();
                        }
                    })
                    .thenComposeAsync(status -> session.closeAsync().thenApply(signal -> status))
                    .thenAccept(emitter::onSuccess);
            });

            return healthResultSingle.toFlowable().subscribeOn(Schedulers.io());
        } catch (Throwable e) {
            return Flowable.just(buildErrorResult(e));
        }
    }

    private HealthResult buildErrorResult(Throwable throwable) {
        return HealthResult.builder(NAME, HealthStatus.DOWN).exception(throwable).build();
    }
}
