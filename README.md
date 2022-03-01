[![CircleCI](https://circleci.com/gh/korzepadawid/url-shortener/tree/master.svg?style=svg)](https://circleci.com/gh/korzepadawid/url-shortener/tree/master)
[![License: CC0-1.0](https://img.shields.io/badge/License-CC0%201.0-lightgrey.svg)](http://creativecommons.org/publicdomain/zero/1.0/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/korzepadawid/url-shortener)](https://github.com/korzepadawid/url-shortener/issues?q=is%3Aissue+is%3Aclosed)
[![GitHub issues](https://img.shields.io/github/issues/korzepadawid/url-shortener)](https://github.com/korzepadawid/url-shortener/issues/)

# URL Shortener ✂

REST API for url-shortener web-app.

## Technologies

- [Spring Boot](https://spring.io/projects/spring-boot) - Server side framework
- [Maven](https://maven.apache.org/) - Build automation tool
- [Hibernate](https://hibernate.org/) - ORM / JPA implementation
- [PostgreSQL](https://www.postgresql.org/docs/) - Object-relational database system
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - The top layer over Hibernate
- [CircleCI](https://circleci.com/) - CI/CD platform
- [Docker](https://www.docker.com/) - Platform for developing, shipping, and running applications

## Business Logic

The application uses [Base62](https://en.wikipedia.org/wiki/Base62) for encoding and decoding URL's
id.

Assume a database with the structure below.

```
ID              URL
1               https://stackoverflow.com/
2               https://www.tensorflow.org/
...
13943437364     https://www.nytimes.com/
```

(rest of fields omitted for brevity)

After saving the requested URL, you will receive an encoded(Base62) database id,
e.g. `13943437364 -> fdDf12`. You can use `fdDf12` to navigate
to [https://www.nytimes.com/](https://www.nytimes.com/). When you enter `localhost:8080/fdDf12`,
the `fdDf12` is going to be decoded to decimal number
(`fdDf12 -> 13943437364`)
in order to navigate you to [https://www.nytimes.com/](https://www.nytimes.com/).

Base62 improves the scalability of the system, let's have a look at the possible combinations
`(62^n; n is length)`:

```
62^3 = 238328
62^4 = 14776336
...
62^7 = 3521614606208
```

## Using REST API

Send `POST` request to `localhost:8080/api/v1/urls` in order to get your shortened URL.

```json
{
  "url": "https://www.tensorflow.org/",
  "expiringAt": "2023-10-29 12:24:23"
}
```

The `expiringAt` field is optional, use the following format `yyyy-MM-dd HH:mm:ss`. Response should
look like this:

```json
{
  "longUrl": "https://www.tensorflow.org/",
  "shortUrl": "/fdDf12",
  "expiringAt": "2023-10-29 12:24:23"
}
```

Right now you can navigate to [https://www.tensorflow.org/](https://www.tensorflow.org/) by
entering `localhost:8080//fdDf12` in your web-browser.

**Important!** You can't save same url with the same `expirationAt` for the second time.

## Launch

## IDE

Clone the repository and you're ready to go! H2-database will be used as your default database.

## Docker

#### Prerequisites

- Java 11 or newer (full JDK not a JRE).
- Docker
- docker-compose

```
$ git clone git@github.com:korzepadawid/url-shortener.git
```

```
$ cd url-shortener
```

```
$ chmod +x mvnw
```

```
$ ./mvnw clean package
```

```
$ docker-compose up
```

## License

[Creative Commons Zero v1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/)

