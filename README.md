http://employee-service.herokuapp.com

Since everything is handled by the framework and the actual codebase consists of a few declarative classes, the actual "units" for unit testing are missing.

Thus a set of integration tests were written to ensure that the whole things wires correctly and the API behaves as expected.

In case some business logic layer would be introduced in the future, it could be a great candidate for unit tests.

It would also be beneficial to have E2E smoketests though UI for core business functionality, but setting those up requires a lot of infra effort and would probably be too much work for the scope of this task.
