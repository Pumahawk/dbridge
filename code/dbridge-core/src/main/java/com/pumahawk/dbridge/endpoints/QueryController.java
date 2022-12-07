package com.pumahawk.dbridge.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.pumahawk.dbridge.services.QueryResult;
import com.pumahawk.dbridge.services.QueryService;

@RestController
public class QueryController {

    @Autowired
    private QueryService queryService;

    @GetMapping(
        path = "/**")
    public ResponseEntity<? extends Object> main(ServerHttpRequest request) {
        return main(request, null);
    }

    @PostMapping(
        path = "/**")
    public ResponseEntity<? extends Object> main(ServerHttpRequest request, @RequestBody JsonNode body) {
        return generateReponse(queryService.query(new HttpRequestQueryParameter(request, body)));
    }

    public ResponseEntity<? extends Object> generateReponse(QueryResult response) {
        return ResponseEntity.ok().body(response);
    }

}
