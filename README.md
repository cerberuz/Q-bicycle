# Bicycle Licence QLDB Demo

This repository hosts the Java 11 codebase using `Spring Boot` for interacting with an `Amazon QLDB` Ledger.. This prototype was originally developed to highlight some capabilities within `Amazon QLDB` that were showcased during a webinar. It was never intended to be production code, and has no associated tests.

This repository has been put online in order to demonstrate capabilities of the Amazon Q Developer Agent for Code Transformation, and to allow people to try it out for themselves on some legacy code.

## Pre-Requisites

In order for the prototype to run, you need to set up an `Amazon QLDB` ledger called `bicycleLicenceLedger` in the `eu-west-1` region. The ledger also needs to have a table defined called `licence`.

The ledger name is hardcoded in the `LedgerConstants` file. The region is hardcoded in the `BicycleLicenceQLDBRepository` file, and the table name is hardcoded in the queries in the same file.