
# 🚀 Project Study Guide: Serverless Java API

This note contains all key concepts, technical decisions, and interview-worthy points from our "To-Do List API" project.

---

## 1. Core Architecture

We built a **Serverless CRUD (Create, Read, Update, Delete) API** for a to-do list.

* **API Layer:** AWS Lambda Function URLs
* **Compute Layer:** AWS Lambda (Java 21 Runtime)
* **Database Layer:** Amazon DynamoDB (NoSQL)

This architecture is **event-driven**, **cost-effective** (pay-per-request), and **infinitely scalable** without managing any servers.

---

## 2. Key Java Concepts & Code

### The Builder Pattern (`.build()`)

This is a modern Java pattern for creating complex objects step-by-step. Think of it as **ordering a custom sandwich**.

* **Why?** It's cleaner and more readable than a massive constructor with 10+ arguments.
* **How it works:**
    1.  `DynamoDbClient.builder()`: Get an empty "order form" (the `Builder`).
    2.  `.region(Region.US_EAST_1)`: Add an option to the form.
    3.  `.build()`: The final "Confirm Order" button. It assembles the object.

```java
// We used this to create our database client
DynamoDbClient ddb = DynamoDbClient.builder()
    .region(Region.US_EAST_1)
    .build();
````

-----

### Lambda Expressions (`r -> r.key(...)`)

A lambda expression is a **short, anonymous function** used to pass a "quick instruction" to another method.

* **The Syntax:** `(input) -> (what to do with input)`
* **Our Code:** `todoTable.deleteItem(r -> r.key(k -> k.partitionValue(todoId)));`
* **The "Long Way" (What it replaces):**
  ```java
  // 1. Build the Key
  Key myKey = Key.builder()
                 .partitionValue(todoId)
                 .build();

  // 2. Build the Request
  DeleteItemRequest myRequest = DeleteItemRequest.builder()
                                     .key(myKey)
                                     .build();

  // 3. Pass the final request
  todoTable.deleteItem(myRequest);
  ```
* **In Plain English:** Our one-line lambda is a shortcut. It says: "Hey `deleteItem`, take your **request builder** (I'll call it `r`) and tell it to set the **key**. To set the key, take your **key builder** (I'll call it `k`) and tell it to set the **partition value** to my `todoId`."

-----

### The `final` Keyword (Lambda Optimization)

In our handlers, we declared our `repository` and `gson` variables as `final` and *outside* the `handleRequest` method.

```java
public class CreateTodoHandler implements RequestHandler<...> {
    
    // These are initialized ONCE at "cold start"
    private final TodoRepository repository = new TodoRepository();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(...) {
        // This code re-uses the SAME repository and gson objects
        // for every "warm" request.
        ...
    }
}
```

* **Why?** This is a **critical performance optimization**. The `final` keyword ensures these objects are created **only once** when the Lambda function "cold starts."
* **Interview Point:** Every subsequent "warm start" request **reuses** these objects. We don't create a new database connection or a new JSON parser for every single API call, which is much faster and more efficient.

-----

## 3\. Key AWS Concepts

### DynamoDB Enhanced Client

This is a "smart translator" library that saves us from writing tons of boilerplate code.

* **The Problem:** The *standard* DynamoDB client doesn't understand your `Todo` class. You'd have to manually convert your Java object to a complex `Map<String, AttributeValue>` every time you save or update.
* **The Solution:** The Enhanced Client **maps your Java class directly to the DynamoDB table**. We just had to "teach" it the rules with annotations:
    * `@DynamoDbBean`: "This class represents a table item."
    * `@DynamoDbPartitionKey`: "This field is the primary key."

It's the magic that lets us write simple code like `todoTable.putItem(myTodoObject)`.

### `RequestHandler` Interface

This is the "contract" that officially turns a Java class into an AWS Lambda function.

```java
public class CreateTodoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> { ... }
```

* **`implements RequestHandler`**: A promise that our class will have a `handleRequest` method, which is the *only* method Lambda knows to call.
* **`<...>` (Generics):** Defines the exact *input* and *output* for the function.
    * **`APIGatewayProxyRequestEvent`**: A pre-built class from AWS that holds all incoming data (body, headers, query parameters).
    * **`APIGatewayProxyResponseEvent`**: The pre-built class we *must* return, allowing us to set the `statusCode`, `body`, and `headers`.

### `context.getLogger().log()`

This is the **`System.out.println()` for AWS Lambda**.

* When your code runs in the cloud, you can't see a local console.
* This method sends your log messages (especially errors) to **Amazon CloudWatch Logs**, which is the *only* way to debug what's happening inside your live function.

-----

## 4\. Project Troubleshooting & Gotchas

* **`UnsupportedClassVersionError`**: This error meant we compiled our code with a **newer** Java version (like Java 21) than the AWS Lambda runtime was configured to use (like Java 17).

    * **Fix:** We matched the Lambda runtime to our compiler version (`Java 21`).

* **`NoClassDefFoundError` / `ClassNotFoundException`**: This error meant our uploaded JAR was "thin"—it only contained *our* code (`.class` files) but was **missing all the libraries** (Gson, AWS SDK).

    * **Fix:** We used the **Maven Shade Plugin** to build a **"fat JAR"** (or "uber-JAR"). This bundles our code AND all its dependencies into one single, large JAR file for upload.

* **`getPathParameters()` vs. `getQueryStringParameters()`**:

    * I initially told you to use `getPathParameters().get("id")`. This was **wrong**.
    * `getPathParameters` is for **API Gateway**, which can handle "clean" routes like `/todos/123`.
    * **Lambda Function URLs** are simpler. They need to use **`getQueryStringParameters()`** to read `id` from a URL like `/todos?id=123`.

-----

## 5\. 💡 Interview "Soundbites"

* **Q:** How did you build your API?

    * **A:** "I built a fully serverless REST API using AWS Lambda for compute and DynamoDB for storage. The API was written in Java 21, and I used Lambda Function URLs to expose the four CRUD endpoints (`GET`, `POST`, `PUT`, `DELETE`)."

* **Q:** How did your Java code talk to the database?

    * **A:** "I used the DynamoDB Enhanced Client. This allowed me to map my Java `Todo` object (a POJO) directly to the database table using `@DynamoDbBean` annotations, which saved me from writing a lot of manual conversion code."

* **Q:** How did you handle dependencies in your Lambda function?

    * **A:** "I used Maven for dependency management and configured the **Maven Shade Plugin** to package the application as a 'fat JAR.' This ensured all necessary libraries, like the AWS SDK and Gson, were bundled with my code and deployed to Lambda."

* **Q:** How did you optimize your Lambda's performance?

    * **A:** "I optimized for 'warm starts' by initializing my database repository and Gson parser as `final` private variables. This means they're only created once during a cold start, and then reused for every subsequent request, which dramatically reduces latency."

<!-- end list -->

```
```