package com.akash.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

/**
 * Lambda handler for the "GET /todos" route.
 */
public class GetTodosHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TodoRepository repository = new TodoRepository();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        try {
            // 1. Get all todos from the database
            List<Todo> todoList = repository.getAllTodos();

            // 2. Convert the list to a JSON string
            String jsonBody = gson.toJson(todoList);

            // 3. Return a successful 200 OK response
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(jsonBody);

        } catch (Exception e) {
            // 4. Handle any errors
            context.getLogger().log("Error getting todos: " + e.getMessage());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Failed to retrieve todos\"}");
        }
    }
}