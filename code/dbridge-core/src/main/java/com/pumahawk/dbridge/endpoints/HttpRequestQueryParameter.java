package com.pumahawk.dbridge.endpoints;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.UriTemplate;

import com.pumahawk.dbridge.services.QueryParameter;

public class HttpRequestQueryParameter implements QueryParameter {

    private UriTemplate contextTemplate = new UriTemplate("{path:.*}");

    private final ServerHttpRequest request;

    public HttpRequestQueryParameter(ServerHttpRequest request) {
        this.request = request;
    }

    @Override
    public String path() {
        return match("path");
    }

    @Override
    public Map<String, String> params() {
        return request
            .getQueryParams()
            .toSingleValueMap();
    }

    private String match(String key) {
        return Optional
            .ofNullable(request.getPath())
            .map(RequestPath::value)
            .map(contextTemplate::match)
            .map(m -> m.get(key))
            .orElse("");
    }

}
