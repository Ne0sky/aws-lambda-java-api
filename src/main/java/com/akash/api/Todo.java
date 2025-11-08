package com.akash.api;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean // 1. Tells the client this class maps to a table
public class Todo {
    private String id;
    private String title;
    private boolean completed;

    // IMPORTANT: You MUST have an empty, no-args constructor
    public Todo() {
    }

    // Your original constructor is still useful
    public Todo(String id, String title) {
        this.id = id;
        this.title = title;
        this.completed = false;
    }

    // 2. Tells the client that 'id' is the primary key
    @DynamoDbPartitionKey
    public String getId() { return id; }

    // All your other getters and setters remain the same
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}