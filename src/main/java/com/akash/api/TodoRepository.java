package com.akash.api;
import java.util.UUID;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.util.List;

public class TodoRepository {

    private final DynamoDbTable<Todo> todoTable;

    public TodoRepository() {
        // 1. Create the standard DynamoDB client
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(Region.US_EAST_1) // <-- *** UPDATED REGION ***
                .build();

        // 2. Create the "Enhanced" client
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        // 3. Map your 'Todo' class to the 'todos' table
        this.todoTable = enhancedClient.table("todos", TableSchema.fromBean(Todo.class));
    }

    // This replaces your "return todoList;" logic
    public List<Todo> getAllTodos() {
        // .scan() is the DynamoDB operation for "get everything"
        return todoTable.scan().items().stream().toList();
    }

    // This replaces your "todoList.add(...)" logic
    public Todo createTodo(Todo todo) {
        // Generate a unique ID for the new todo
        String todoId = UUID.randomUUID().toString();
        todo.setId(todoId);

        // Ensure it's not marked as completed
        todo.setCompleted(false);

        // Save the new item to the DynamoDB table
        todoTable.putItem(todo);

        return todo;
    }

    /**
     * Finds a Todo by its ID, marks it as complete, and saves it.
     * @param todoId The ID of the todo to update.
     * @return The updated Todo object, or null if not found.
     */
    public Todo updateTodoAsComplete(String todoId) {

        // 1. Find the item in DynamoDB
        Todo todo = todoTable.getItem(r -> r.key(k -> k.partitionValue(todoId)));

        if (todo != null) {
            // 2. Update the Java object in memory
            todo.setCompleted(true);

            // 3. Save the changes back to DynamoDB
            // The enhanced client knows this is an update because the ID already exists
            todoTable.updateItem(todo);

            return todo;
        } else {
            // We couldn't find the todo to update
            return null;
        }
    }

    /**
     * Deletes a Todo item by its ID.
     * @param todoId The ID of the todo to delete.
     * @return true if the item was found and deleted, false if it was not found.
     */
    public boolean deleteTodo(String todoId) {

        try {
            // 1. Delete the item using its key.
            // This will throw an exception if the item doesn't exist,
            // so we can use a try-catch to see if it worked.
            todoTable.deleteItem(r -> r.key(k -> k.partitionValue(todoId)));

            // 2. If no exception was thrown, it was successful.
            return true;

        } catch (Exception e) {
            // 3. If an exception occurred (like 'ResourceNotFoundException'),
            // it means the item didn't exist.
            return false;
        }
    }

}