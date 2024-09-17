# Coding Challenge
## What's Provided
A simple [Spring Boot](https://projects.spring.io/spring-boot/) web application has been created and bootstrapped 
with data. The application contains information about all employees at a company. On application start-up, an in-memory 
Mongo database is bootstrapped with a serialized snapshot of the database. While the application runs, the data may be
accessed and mutated in the database without impacting the snapshot.

### How to Run
The application may be executed by running `gradlew bootRun`.

### How to Use
The following endpoints are available to use:
```
* CREATE
    * HTTP Method: POST 
    * URL: localhost:8080/employee
    * PAYLOAD: Employee
    * RESPONSE: Employee
* READ
    * HTTP Method: GET 
    * URL: localhost:8080/employee/{id}
    * RESPONSE: Employee
* UPDATE
    * HTTP Method: PUT 
    * URL: localhost:8080/employee/{id}
    * PAYLOAD: Employee
    * RESPONSE: Employee
```
? running the update function in postman is breaking my subsequent READ calls. Maybe I'm missing something.

The Employee has a JSON schema of:
```json
{
  "type":"Employee",
  "properties": {
    "employeeId": {
      "type": "string"
    },
    "firstName": {
      "type": "string"
    },
    "lastName": {
          "type": "string"
    },
    "position": {
          "type": "string"
    },
    "department": {
          "type": "string"
    },
    "directReports": {
      "type": "array",
      "items" : "string"
    }
  }
}
```
For all endpoints that require an "id" in the URL, this is the "employeeId" field.

## What to Implement
Clone or download the repository, do not fork it.

### Task 1
Create a new type, ReportingStructure, that has two properties: employee and numberOfReports.

For the field "numberOfReports", this should equal the total number of reports under a given employee. The number of 
reports is determined to be the number of directReports for an employee and all of their distinct reports. For example, 
given the following employee structure:
```
                    John Lennon
                /               \
         Paul McCartney         Ringo Starr
                               /        \
                          Pete Best     George Harrison
```
The numberOfReports for employee John Lennon (employeeId: 16a596ae-edd3-4847-99fe-c4518e82c86f) would be equal to 4.

This new type should have a new REST endpoint created for it. This new endpoint should accept an employeeId and return 
the fully filled out ReportingStructure for the specified employeeId. The values should be computed on the fly and will 
not be persisted.

ok, so this is a BFS. John lennon knows his direct reports, they know their direct reports, this goes on. We do this until we no longer find new employees.
-add a new endpoint that takes an input and doesn't crash
-change this endpoint to take an input and return the directreports array of that employee
-change this endpoint to implement the solution

edge case: cycles. The example they give is a tree, and you'd have to be very intentional to produce something other than a tree (I think) but I don't think there's anything stopping someone from pointing paul back at john.

### Task 2
Create a new type, Compensation. A Compensation has the following fields: employee, salary, and effectiveDate. Create 
two new Compensation REST endpoints. One to create and one to read by employeeId. These should persist and query the 
Compensation from the persistence layer.

assumption: the employee already exists?
	what if not? we would create it when this is called.
assumption: When we call createCompensation, a user already exists, and will not change(?)
we can't really assume that, but to start
-add a function that just puts the employee in the database
-The compensation has an employee. That is the field we are returning. The compensation coming in also contains an employee. The whole object. The dao (as far as I can tell) doesn't allow me to do anything I'd like, (put the employee in the employee collection, store the eID in the compensation collection, where I can use it as a foreign key in the getter). 
-so ok. That leaves me one path as far as I can tell. We create an employee. We create a compensation, and that employee must exist? Surely, because why would someone make up their own UUID. I can enforce that. Now, this leaves me a duplicated field, the employee. Now, we can update employees. So when we update an employee we need to update the compensation as well. As far as I can tell, this must be all together. The employee gets updated, and the compensation as well.
-in short, Compensation (employee inside) for the controller + service returns, proxyCompensation (employeeId in lieu of the object) for the DAO calls, so that way the service can just look into employee, always find the most up to date record bc no duplication
-but really, we already see strange stuff in the get with the directreports. not gonna worry too much about this
-iteration one: implement assuming no update stuff
eh lemme go understand this DAO pattern
ok so, Spring MongoDB offers automatic implementation of interfaces like employeeRepository, that's why there's no implementation of the repo.
well ok, so we can probably just handle this with the repo and the serviceimpl when the time comes, lets roll with this for now

so I had issues where update wasn't working, so I did some digging and found using the @Id tag can help mark the field for identity

TODO: compensate for update issues
t1: updating allows you to introduce cycles
t2: updates to employee need to propogate to Compensation

confirm, yeah todo for t2 breaks it
so, in employeeServiceImpl, when we update an employee, get the Compensation, and replace it with the new one
hey! that worked immediately.

t1 cycle detection in the bfs bc we're not guaranteed it's a tree

outstanding:
-validation for t1
-validation for t2
-they include a test, do some test stuff
-can UPDATE calls break the state of the compensation? compensation should hold the salary and effectivedate, but employee info should live in employee 

caveat: persist like persist when the db turns off? it's in memory so I assume not.
https://www.mongodb.com/resources/basics/databases/in-memory-database
so when I ctrl c the db and lose my compensations that's ok

## Delivery
Please upload your results to a publicly accessible Git repo. Free ones are provided by Github and Bitbucket.
