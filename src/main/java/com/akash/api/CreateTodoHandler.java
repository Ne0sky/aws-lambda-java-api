package com.akash.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import java.util.Map;

/**
 * Lambda handler for the "POST /todos" route.
 */
public class CreateTodoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TodoRepository repository = new TodoRepository();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        try {
            // 1. Get the JSON string from the request body
            String requestBody = input.getBody();

            // 2. Use Gson to turn the JSON into a Todo object
            // We're expecting a simple JSON like {"title": "My new task"}
            Todo newTodo = gson.fromJson(requestBody, Todo.class);

            // 3. Save the new todo using the repository
            Todo createdTodo = repository.createTodo(newTodo);

            // 4. Return a successful 201 Created response
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201) // 201 means "Created"
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(gson.toJson(createdTodo)); // Send back the new todo

        } catch (Exception e) {
            // 5. Handle any errors
            context.getLogger().log("Error creating todo: " + e.getMessage());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Failed to create todo\"}");
        }
    }
}