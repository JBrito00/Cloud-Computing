# Lab 4 - Google Firestore service

## Objective
Use the Java API to access the Google Firestore service.
Consider the attached CSV file (OcupacaoEspacosPublicos.csv) with data extracted from a real case (temporary occupation of spaces for events in the city of Lisbon).
1. Using the Java Firestore API, create a Java application that creates a collection of documents (one document for each line of the CSV file) in the Firestore database, filling it with the data from the attached file. Consider that the data of each line is represented in the following class structure represented in the figure. Consider that each document is assigned an identifier "Lab4-" + ID, where ID is the integer number that identifies a temporary occupation.

## Assignment
Add the following functionalities to the application:

a. Display the content of a document based on its identifier (e.g., Lab4-2017).

b. Delete a field from a document, given its identifier and the name of the field to be deleted.

c. Perform a simple query to retrieve all documents from a specific parish.

d. Perform a compound query to retrieve documents with the following criteria:

    - With the ID field greater than a value

    - From a specific parish

    - Of a specific event type

e. Perform a query to retrieve documents with events that started in the month of February 2017 (start date (dtInicio) greater than 31/01/2017 and less than 01/03/2017).

f. Perform a query to retrieve documents with events that were fully completed in the month of February 2017 (start date (dtInicio) greater than 31/01/2017 and end date (dtFinal) less than 01/03/2017).

## Resources
[Google Firestore Java API Documentation](https://firebase.google.com/docs/firestore/quickstart)

[Java Documentation](https://docs.oracle.com/en/java/)