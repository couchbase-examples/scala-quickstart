# Quickstart in Couchbase with Scala

#### Build REST APIs with Couchbase's Scala SDK

> This repo is designed to teach you how to connect to a Couchbase cluster to create, read, update, and delete documents and how to write simple parametrized N1QL queries.
[![Try it now!](https://da-demo-images.s3.amazonaws.com/runItNow_outline.png?couchbase-example=java-springboot-quickstart-repo&source=github)](https://gitpod.io/#https://github.com/couchbase-examples/scala-quickstart)

Full documentation can be found on the [Couchbase Developer Portal](https://developer.couchbase.com/tutorial-quickstart-scala/).
## Prerequisites
To run this prebuilt project, you will need:

- Couchbase Server 7 Installed (version 7.0.0-5247 or higher)
- Scala 2.12 or higher installed
- Code Editor installed (IntelliJ IDEA, Eclipse, or Visual Studio Code)

### Database Server Configuration

All configuration for communication with the database is stored in the `/src/main/resources/application.conf` file.  This includes the connection string, username, and password.  The default username is assumed to be `Administrator` and the default password is assumed to be `password`.  If these are different in your environment you will need to change them before running the application.

## Running The Application

The application can be run via your IDE or from the terminal:

```sh
sbt run
```

The application will keep running until you provide a line of input, after which it will shut down the web servers.

You can launch your browser and go to each web server's Swagger start page: 

* [Akka HTTP server](https://localhost:8081/docs)
* [htt4ps server](https://localhost:8082/docs)
* [Play server](https://localhost:8083/docs)

## Running The Tests

To run the unit tests and integration tests (which requires a running Couchbase Server), use the following command:

```sh
sbt test
```


## Conclusion

Setting up a basic REST API in Scala with Couchbase is fairly simple.  This project when run with Couchbase Server 7 installed creates a bucket in Couchbase, an index for our parameterized [N1QL query](https://docs.couchbase.com/java-sdk/current/howtos/n1ql-queries-with-sdk.html), and showcases basic CRUD operations needed in most applications.
