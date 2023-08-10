/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.neo4j.bolt.health

import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultApplicationContext
import io.micronaut.context.env.MapPropertySource
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.neo4j.bolt.Neo4jBoltConfiguration
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Mono
import spock.lang.Specification

/**
 * @author graemerocher
 * @since 1.0
 */
class Neo4jHealthIndicatorSpec extends Specification {

    void "test neo4j health indicator"() {
        given:
        Neo4jContainer neo4jContainer = new Neo4jContainer(DockerImageName.parse("neo4j:latest"))
            .withoutAuthentication()
        neo4jContainer.start()

        ApplicationContext applicationContext = new DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(MapPropertySource.of(
                'test',
                ['neo4j.uri' : "bolt://localhost:${neo4jContainer.firstMappedPort}"]
        ))
        applicationContext.start()

        expect:
        applicationContext.containsBean(Neo4jBoltConfiguration)

        when:
        Neo4jHealthIndicator healthIndicator = applicationContext.getBean(Neo4jHealthIndicator)
        HealthResult result = Mono.from(healthIndicator.result).block()

        then:
        result.status == HealthStatus.UP
        result.details.server instanceof String
        // neo4j-driver 5.10.0 changes server string to embed version,
        // e.g. Neo4j/5.10.0@127.0.0.1:52527 or Neo4j/5.10.0@localhost:52527
        result.details.server.matches "Neo4j/(?:\\d+\\.\\d+\\.\\d+@)?.*:\\d+"

        when:
        neo4jContainer.stop()
        result = Mono.from(healthIndicator.result).block()

        then:
        result.status == HealthStatus.DOWN

        cleanup:
        applicationContext?.stop()
    }
}
