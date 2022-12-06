package com.pumahawk.dbridge.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pumahawk.dbridge.services.QueryResult;
import com.pumahawk.dbridge.services.QueryService;

@RestController
public class QueryController {

    @Autowired
    private QueryService queryService;

    @RequestMapping(
        path = "/**")
    public ResponseEntity<? extends Object> main(ServerHttpRequest request) {
        return generateReponse(queryService.query(new HttpRequestQueryParameter(request)));
    }

    public ResponseEntity<? extends Object> generateReponse(QueryResult response) {
        return ResponseEntity.ok().body(response);
    }

}
