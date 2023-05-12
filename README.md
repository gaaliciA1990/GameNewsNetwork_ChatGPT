# Game News Network 

[![Release](https://github.com/gaaliciA1990/GameNewsNetwork_ChatGPT/actions/workflows/Release.yml/badge.svg)](https://github.com/gaaliciA1990/GameNewsNetwork_ChatGPT/actions/workflows/Release.yml)

# Getting Started

## Prerequisites for the Backend
- Configure your GitHub account to [use ssh authentication](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent).
### For the Backend
- Download and install [MongoDB](https://www.mongodb.com/try/download/community). We will use this for local development and testing.
- Install [JDK 17](https://www.oracle.com/java/technologies/downloads/).
- Install [IntelliJ 2022.1.3](https://www.jetbrains.com/idea/download/) or later.

## Configuring your dev environment

To ease with development, here are some tools that you may want to install:
- [IntelliJ Detekt Plugin](https://plugins.jetbrains.com/plugin/10761-detekt)
- [IntelliJ OpenApi Editor Plugin](https://plugins.jetbrains.com/plugin/14837-openapi-swagger-editor)

## Launching the server

The server can be run using `./gradlew run`. To run the server as a service, run the [install.sh](https://github.com/gaaliciA1990/GameNewsNetwork_ChatGPT/blob/main/install.sh) script.

## Updating packages

After adding a new package to the `build.gradle` file, you will need to perform a manual step to migrate the dependency to use the `refreshVersions` plugin.
To do so, run this command: `./gradlew refreshVersionsMigrate --mode=VersionsPropertiesOnly`.
