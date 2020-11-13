# Micronaut Neo4j

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.neo4j/micronaut-neo4j-bolt.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.neo4j%22%20AND%20a:%22micronaut-neo4j-bolt%22)
[![Build Status](https://github.com/micronaut-projects/micronaut-neo4j/workflows/Java%20CI/badge.svg)](https://github.com/micronaut-projects/micronaut-neo4j/actions)

Integrates Micronaut and Neo4j.

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-neo4j/latest/guide) for more information.

## Snapshots and Releases

Snaphots are automatically published to JFrog OSS using [Github Actions](https://github.com/micronaut-projects/micronaut-neo4j/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to JCenter and Maven Central via [Github Actions](https://github.com/micronaut-projects/micronaut-neo4j/actions).

A release is performed with the following steps:

- [Edit the version](https://github.com/micronaut-projects/micronaut-neo4j/edit/master/gradle.properties) specified by `projectVersion` in `gradle.properties` to a semantic, unreleased version. Example `1.0.0`
- [Create a new release](https://github.com/micronaut-projects/micronaut-neo4j/releases/new). The Git Tag should start with `v`. For example `v1.0.0`.
- [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-neo4j/actions?query=workflow%3ARelease) to check it passed successfully.
- Celebrate!
