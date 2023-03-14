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
package io.micronaut.neo4j.bolt;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import jakarta.inject.Inject;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Logging;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
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
    private Neo4jEmbeddedSettings embeddedSettings = new Neo4jEmbeddedSettings();

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

    /**
     * @return The settings for the embedded Neo4j server
     */
    public Neo4jEmbeddedSettings getEmbeddedSettings() {
        return embeddedSettings;
    }

    /**
     * @param embeddedSettings The {@link Neo4jEmbeddedSettings}
     */
    @Inject
    public void setEmbeddedSettings(Neo4jEmbeddedSettings embeddedSettings) {
        this.embeddedSettings = embeddedSettings;
    }

    /**
     * The configuration settings for the embedded Neo4j.
     */
    @ConfigurationProperties("embedded")
    public static class Neo4jEmbeddedSettings implements Toggleable {
        /**
         * The default enable value.
         */
        @SuppressWarnings("WeakerAccess")
        public static final boolean DEFAULT_ENABLED = true;

        /**
         * The default ephemeral value.
         */
        @SuppressWarnings("WeakerAccess")
        public static final boolean DEFAULT_EPHEMERAL = false;

        /**
         * The default drop data value.
         */
        @SuppressWarnings("WeakerAccess")
        public static final boolean DEFAULT_DROPDATA = false;

        private Map<String, Object> options = Collections.emptyMap();
        private String directory;
        private boolean dropData = DEFAULT_DROPDATA;
        private boolean ephemeral = DEFAULT_EPHEMERAL;
        private boolean enabled = DEFAULT_ENABLED;

        /**
         * @return Whether the embedded sever is enabled
         */
        @Override
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Default value ({@value #DEFAULT_ENABLED}).
         * @param enabled enable the server
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return Options to pass to the embedded server
         */
        public Map<String, Object> getOptions() {
            return options;
        }

        /**
         * @param options The options to pass to the embedded server
         */
        public void setOptions(Map<String, Object> options) {
            if (options != null) {
                this.options = options;
            }
        }

        /**
         * @return The directory to store embedded data
         */
        public Optional<String> getDirectory() {
            return Optional.ofNullable(directory);
        }

        /**
         * @param directory The directory
         */
        public void setDirectory(String directory) {
            this.directory = directory;
        }

        /**
         * @return Whether to drop existing data
         */
        public boolean isDropData() {
            return dropData;
        }

        /**
         * Default value ({@value #DEFAULT_DROPDATA}).
         * @param dropData drop the existing data
         */
        public void setDropData(boolean dropData) {
            this.dropData = dropData;
        }

        /**
         * @return Whether to create the database in a temp directory and deleted on shutdown
         */
        public boolean isEphemeral() {
            return ephemeral;
        }

        /**
         * Default value ({@value #DEFAULT_EPHEMERAL}).
         * @param ephemeral define the embedded ser as ephemeral
         */
        public void setEphemeral(boolean ephemeral) {
            this.ephemeral = ephemeral;
        }
    }
}
