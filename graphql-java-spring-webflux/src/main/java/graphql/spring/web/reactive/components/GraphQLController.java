package graphql.spring.web.reactive.components;


import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.Internal;
import graphql.spring.web.reactive.ExecutionResultHandler;
import graphql.spring.web.reactive.GraphQLInvocation;
import graphql.spring.web.reactive.GraphQLInvocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@Internal
public class GraphQLController {

    @Autowired
    GraphQLInvocation graphQLInvocation;

    @Autowired
    ExecutionResultHandler executionResultHandler;

    @Autowired
    ObjectMapper objectMapper;

    @RequestMapping(value = "${graphql.url:graphql}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object graphqlPOST(@RequestBody GraphQLRequestBody body,
                              ServerWebExchange serverWebExchange) {
        String query = body.getQuery();
        if (query == null) {
            query = "";
        }
        Mono<ExecutionResult> executionResult = graphQLInvocation.invoke(new GraphQLInvocationData(query, body.getOperationName(), body.getVariables()), serverWebExchange);
        return executionResultHandler.handleExecutionResult(executionResult, serverWebExchange.getResponse());
    }

    @RequestMapping(value = "${graphql.url:graphql}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object graphqlGET(
            @RequestParam("query") String query,
            @RequestParam(value = "operationName", required = false) String operationName,
            @RequestParam(value = "variables", required = false) String variablesJson,
            ServerWebExchange serverWebExchange
    ) {
        Mono<ExecutionResult> executionResult = graphQLInvocation.invoke(new GraphQLInvocationData(query, operationName, convertVariablesJson(variablesJson)), serverWebExchange);
        return executionResultHandler.handleExecutionResult(executionResult, serverWebExchange.getResponse());
    }

    private Map<String, Object> convertVariablesJson(String jsonMap) {
        if (jsonMap == null) return Collections.emptyMap();
        try {
            return objectMapper.readValue(jsonMap, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert variables GET parameter: expected a JSON map", e);
        }

    }


}
