Entellect ksql queries service that is creating queries in a given KSQLDB cluster based on an input file.

# Prerequisites

- sbt installed (https://www.scala-sbt.org/download.html)
- Java version 11 (it may not work on java v18)

# Compile

Run from project root folder
> sbt compile 

# Run

Set the following environment variables:

KSQL_DB_HOST - URL of `ksql` endpoint.
KSQL_DB_QUERIES - path to the file with queries

example:
> export KSQL_DB_HOST=https://localhost:80/ksql
> 
> export KSQL_DB_QUERIES=./KSQLDB-persistent-queries.txt

Launch the application from the project root:

> sbt run
 
