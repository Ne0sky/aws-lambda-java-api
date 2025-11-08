package com.akash.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Lambda handler for the "DELETE /todos?id=..." route.
 */
public class DeleteTodoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TodoRepository repository = new TodoRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        try {
            // 1. Get the 'id' from the query string
            String todoId = input.getQueryStringParameters().get("id");

            if (todoId == null || todoId.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400) // 400 Bad Request
                        .withBody("{\"error\":\"'id' query parameter is missing\"}");
            }

            // 2. Call the repository to delete the item
            boolean success = repository.deleteTodo(todoId);

            if (success) {
                // 3. Success! Send 204 No Content.
                // This is the standard for a successful DELETE.
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(204);
            } else {
                // 4. Item not found
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404) // 404 Not Found
                        .withBody("{\"error\":\"Todo not found\"}");
            }

        } catch (Exception e) {
            // 5. Handle any other errors
            context.getLogger().log("Error deleting todo: " + e.getMessage());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Failed to delete todo\"}");
        }
    }
}