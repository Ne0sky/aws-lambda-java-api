package com.akash.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import java.util.Map;

/**
 * Lambda handler for the "PUT /todos/{id}/complete" route.
 */
public class UpdateTodoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TodoRepository repository = new TodoRepository();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        try {
            // 1. Get the 'id' from the URL path
            // This is a new, important step!
            // 1. Get the 'id' from the query string parameters
            String todoId = input.getQueryStringParameters().get("id");

// 2. Add a check in case the 'id' is missing
            if (todoId == null || todoId.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400) // 400 Bad Request
                        .withBody("{\"error\":\"'id' query parameter is missing\"}");
            }

            // 2. Call the repository to update the item
            Todo updatedTodo = repository.updateTodoAsComplete(todoId);

            // 3. Check if the update was successful
            if (updatedTodo != null) {
                // Success! Send 200 OK
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withHeaders(Map.of("Content-Type", "application/json"))
                        .withBody(gson.toJson(updatedTodo));
            } else {
                // Item not found
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404) // 404 Not Found
                        .withBody("{\"error\":\"Todo not found\"}");
            }

        } catch (Exception e) {
            // 5. Handle any other errors
            context.getLogger().log("Error updating todo: " + e.getMessage());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Failed to update todo\"}");
        }
    }
}