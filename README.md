# Exchange API

Exchange API is a REST API providing exchange rates and conversions for many currencies.

## Table of Contents

1. [Technologies](#technologies)
2. [Running](#running)
3. [Configuration](#configuration)
4. [Documentation](#documentation)
5. [Development and Testing](#development-and-testing)
6. [Contributing](#contributing)
7. [License](#license)

## Technologies

Exchange API is a [Spring Boot](https://spring.io/projects/spring-boot) project written in [Java 24](https://openjdk.java.net/projects/jdk/24) and it uses following:

* [fixer.io](https://fixer.io) as exchange rate provider
* [Spring Data](https://spring.io/projects/spring-data) for data persistence
* [H2](https://www.h2database.com) as database
* [Spring Doc](https://springdoc.org) for API documentation
* [JUnit 5](https://junit.org/junit5) for testing
* [Gradle](https://gradle.org) as build tool

## Running

You can Exchange API as a regular Java application in your favorite IDE with [`Main.java`](src/main/java/dev/akif/exchange/Main.java) as your main class. You can also run it with Gradle as following:

```bash
gradle bootRun
```

By default, Exchange API runs on `localhost:8080`.

## Configuration

Exchange API can run out-of-the-box as long as credentials for rate provider is defined as `FIXER_IO_KEY` environment variable or in [`application.yml`](src/main/resources/application.yml) file.

## Documentation

API documentation is managed by [Spring Doc](https://springdoc.org). To access [Swagger UI](https://swagger.io/tools/swagger-ui), open [`/swagger.html`](http://localhost:8080/swagger.html) in a web browser after running Exchange API. You can also find Open API specification at [`/docs`](http://localhost:8080/docs) as a Json.

## Development and Testing

Exchange API is built with Gradle. You can use regular Gradle tasks such as `clean`, `compile`, `test` tasks for development and testing.

## Contributing

All contributions are welcome, including requests to feature your project utilizing Exchange API. Please feel free to send a pull request. Thank you.

## License

Exchange API is licensed with [MIT License](LICENSE.md).
