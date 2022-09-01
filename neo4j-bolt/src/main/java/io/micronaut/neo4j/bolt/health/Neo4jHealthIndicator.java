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

import java.util.Collections;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.neo4j.driver.Driver;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.ServerInfo;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    private final ExecutorService ioExecutor;

    /**
     * Constructor.
     * @param boltDriver driver
     * @param ioExecutor The IO executor
     */
    public Neo4jHealthIndicator(Driver boltDriver, @Named(TaskExecutors.IO) ExecutorService ioExecutor) {
        this.boltDriver = boltDriver;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        try {

            Mono<HealthResult> healthResultSingle = Mono.create(emitter -> {
                AsyncSession session = boltDriver.asyncSession();
                CompletionStage<ResultSummary> query =
                    session.writeTransactionAsync(tx -> tx.runAsync("RETURN 1 AS result").thenCompose(ResultCursor::consumeAsync));
                query
                    .handleAsync((resultSummaryStage, throwable) -> {
                        if (throwable != null) {
                            return buildErrorResult(throwable);
                        } else {
                            HealthResult.Builder status = HealthResult.builder(NAME, HealthStatus.UP);
                            ServerInfo serverInfo = resultSummaryStage.server();
                            status.details(Collections.singletonMap(
                                "server", serverInfo.agent() + "@" + serverInfo.address()));
                            return status.build();
                        }
                    })
                    .thenComposeAsync(status -> session.closeAsync().handle((signal, throwable) -> status))
                    .thenAccept(emitter::success);
            });

            return healthResultSingle.subscribeOn(Schedulers.fromExecutorService(ioExecutor));
        } catch (Throwable e) {
            return Mono.just(buildErrorResult(e));
        }
    }

    private HealthResult buildErrorResult(Throwable throwable) {
        return HealthResult.builder(NAME, HealthStatus.DOWN).exception(throwable).build();
    }
}
