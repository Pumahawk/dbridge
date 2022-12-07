package com.pumahawk.dbridge.endpoints;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpMethod;
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
    public Map<String, Object> params() {
        HashMap<String, Object> maps = new HashMap<>();
        maps.putAll(request
        .getQueryParams()
        .toSingleValueMap());
        maps.put("_s", new NotNullListMap<>(request.getQueryParams()));
        return maps;
    }

    @Override
    public HttpMethod method() {
        return request.getMethod();
    }

    private String match(String key) {
        return Optional
            .ofNullable(request.getPath())
            .map(RequestPath::value)
            .map(contextTemplate::match)
            .map(m -> m.get(key))
            .orElse("");
    }

    private static class NotNullListMap<T extends Object> extends HashMap<String, List<? extends Object>> {

        public NotNullListMap(Map<? extends String, ? extends List<? extends Object>> arg0) {
            super(arg0);
        }

        @Override
        public List<? extends Object> get(Object key) {
            List<? extends Object> l = super.get(key);
            return l != null
                ? l
                : Collections.emptyList();
        }
        
    }

}
