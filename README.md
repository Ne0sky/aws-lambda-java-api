
# 📝 Technical Note: Serverless Java CRUD API

A serverless API providing full CRUD (Create, Read, Update, Delete) functionality for a to-do list.

## 1. Architecture

* **API Layer:** 4x AWS Lambda Function URLs (one for each CRUD operation).
* **Compute Layer:** 4x AWS Lambda Functions (Java 21 runtime).
* **Database:** 1x Amazon DynamoDB Table (`todos`).

---

## 2. Database

* **Table:** `todos`
* **Primary Key:** `id` (String)

---

## 3. Core Code

### `Todo.java` (The Data Model)

A POJO annotated for the DynamoDB Enhanced Client.

```java
@DynamoDbBean
public class Todo {
    private String id;
    private String title;
    private boolean completed;

    @DynamoDbPartitionKey
    public String getId() { return id; }
    
    // Required empty constructor & other getters/setters
}
````

### `TodoRepository.java` (The Data Layer)

Encapsulates all DynamoDB logic using the Enhanced Client.

```java
public class TodoRepository {
    private final DynamoDbTable<Todo> todoTable;

    public TodoRepository() {
        DynamoDbClient ddb = DynamoDbClient.builder().region(Region.US_EAST_1).build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
        this.todoTable = enhancedClient.table("todos", TableSchema.fromBean(Todo.class));
    }

    // CREATE
    public Todo createTodo(Todo todo) {
        todo.setId(UUID.randomUUID().toString());
        todoTable.putItem(todo);
        return todo;
    }
    
    // READ
    public List<Todo> getAllTodos() {
        return todoTable.scan().items().stream().toList();
    }

    // UPDATE
    public Todo updateTodoAsComplete(String todoId) {
        Todo todo = todoTable.getItem(r -> r.key(k -> k.partitionValue(todoId)));
        if (todo != null) {
            todo.setCompleted(true);
            todoTable.updateItem(todo);
            return todo;
        }
        return null;
    }

    // DELETE
    public boolean deleteTodo(String todoId) {
        try {
            todoTable.deleteItem(r -> r.key(k -> k.partitionValue(todoId)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### `*Handler.java` (The API Layer)

Each handler implements `RequestHandler` and parses the HTTP request.

**Key Logic (Example: `UpdateTodoHandler`)**

```java
public class UpdateTodoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final TodoRepository repository = new TodoRepository();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // 1. Get ID from query string
        String todoId = input.getQueryStringParameters().get("id");
        
        // 2. Handle missing ID (return 400)
        if (todoId == null || todoId.isEmpty()) { ... }

        // 3. Call repository
        Todo updatedTodo = repository.updateTodoAsComplete(todoId);

        // 4. Handle "Not Found" (return 404)
        if (updatedTodo == null) { ... }
        
        // 5. Return success (return 200)
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(updatedTodo));
    }
}
```

-----

## 4\. Deployment

1.  **Packaging:** Project is packaged as a "fat JAR" using the **Maven Shade Plugin**.
2.  **Lambda:** Four separate Lambda functions are created (`getTodosFunction`, `createTodoFunction`, etc.).
3.  **Upload:** The **same "fat JAR"** is uploaded to all four functions.
4.  **Handler:** Each function's handler setting is pointed to its specific class:
    * `com.akash.api.GetTodosHandler::handleRequest`
    * `com.akash.api.CreateTodoHandler::handleRequest`
    * `com.akash.api.UpdateTodoHandler::handleRequest`
    * `com.akash.api.DeleteTodoHandler::handleRequest`
5.  **Permissions:** Each function's IAM Role is given the **`AmazonDynamoDBFullAccess_v2`** policy.
6.  **Function URLs:** Each function is exposed with a unique URL, **CORS enabled**, and the correct **`Allow-Methods`** (`GET`, `POST`, `PUT`, `DELETE`).

-----

## 5\. API Endpoints

* **CREATE:** `POST` `https://<...>.lambda-url.us-east-1.on.aws/`
* **READ:** `GET` `https://<...>.lambda-url.us-east-1.on.aws/`
* **UPDATE:** `PUT` `https://<...>.lambda-url.us-east-1.on.aws/?id=...`
* **DELETE:** `DELETE` `https://<...>.lambda-url.us-east-1.on.aws/?id=...`

<!-- end list -->

```
```