/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.neo4j.bolt

import io.micronaut.context.DefaultApplicationContext
import io.micronaut.context.env.MapPropertySource
import org.neo4j.driver.Driver
import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.socket.SocketUtils
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author graemerocher
 * @since 1.0
 */
class Neo4jServerSpec extends Specification {

    void "test neo4j testcontainer"() {
        given:
        Neo4jContainer neo4jContainer = new Neo4jContainer(DockerImageName.parse("neo4j:4.4.24"))
                .withoutAuthentication()
        neo4jContainer.start()

        ApplicationContext applicationContext = new DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(MapPropertySource.of(
                'test',
                ['neo4j.uri' : "bolt://localhost:${neo4jContainer.firstMappedPort}"]
        ))
        applicationContext.start()

        when:
        Driver driver = applicationContext.getBean(Driver)

        then:
        driver.session().run('MATCH (n) RETURN n').size() == 0

        cleanup:
        applicationContext?.stop()
    }
}
