/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.neo4j.bolt;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Logging;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Configuration for Bolt Neo4j driver.
 *
 * @author graemerocher
 * @since 1.0
 */
@ConfigurationProperties(Neo4jBoltSettings.PREFIX)
public class Neo4jBoltConfiguration implements Neo4jBoltSettings {
    /**
     * The default retry count value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_RETRYCOUNT = 3;

    /**
     * The default retry delay value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_RETRYDELAY_SECONDS = 1;

    @ConfigurationBuilder(prefixes = "with", allowZeroArgs = true)
    protected Config.ConfigBuilder config = Config.builder();

    private URI uri = URI.create(DEFAULT_URI);
    private AuthToken authToken;
    private String username;
    private String password;
    private int retryCount = DEFAULT_RETRYCOUNT;
    private Duration retryDelay = Duration.of(DEFAULT_RETRYDELAY_SECONDS, ChronoUnit.SECONDS);

    /**
     * Constructor.
     */
    public Neo4jBoltConfiguration() {
        config.withLogging(Logging.slf4j());
    }

    /**
     * @return The Neo4j URIs
     */
    @NonNull
    public URI getUri() {
        return uri;
    }

    /**
     * Set a single {@link URI}.
     *
     * @param uri A single Neo4j URI
     */
    public void setUri(@NonNull @NotNull URI uri) {
        this.uri = uri;
    }

    /**
     * @return The number of times to retry establishing a connection to the server
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Default value ({@value #DEFAULT_RETRYCOUNT}).
     * @param retryCount The retry count
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * @return The delay between retry attempts
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * Default value ({@value #DEFAULT_RETRYDELAY_SECONDS}).
     * @param retryDelay The delay between retry attempts
     */
    public void setRetryDelay(Duration retryDelay) {
        if (retryDelay != null) {
            this.retryDelay = retryDelay;
        }
    }

    /**
     * @return The configuration
     */
    public Config getConfig() {
        return config.build();
    }

    /**
     * @return The configuration builder used
     */
    public Config.ConfigBuilder getConfigBuilder() {
        return config;
    }

    /**
     * @return The auth token to use
     * @see org.neo4j.driver.AuthTokens
     */
    public Optional<AuthToken> getAuthToken() {
        if (authToken != null) {
            return Optional.of(authToken);
        } else if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            return Optional.of(AuthTokens.basic(username, password));
        }
        return Optional.empty();
    }

    /**
     * @param authToken The {@link AuthToken}
     */
    @Inject
    public void setAuthToken(@Nullable AuthToken authToken) {
        this.authToken = authToken;
    }

    /**
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param trustStrategy The {@link org.neo4j.driver.Config.TrustStrategy}
     */
    @Inject
    public void setTrustStrategy(@Nullable Config.TrustStrategy trustStrategy) {
        if (trustStrategy != null) {
            this.config.withTrustStrategy(trustStrategy);
        }
    }
}
