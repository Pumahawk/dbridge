package com.pumahawk.dbridge.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = HttpRequestQueryParameterTests.RequestWrapperController.class
)
@EnableAutoConfiguration
public class HttpRequestQueryParameterTests {

    @Autowired
    private WebTestClient client;

    @Test
    public void loadContext() {
    }

    @Test
    public void successRequest() {
        client.get().uri("/echo").exchange().expectStatus().is2xxSuccessful();
    }

    public static Stream<Arguments> pathMatch() {
        return Stream.of(
            arguments("/", "/"),
            arguments("/query", "/query"),
            arguments("/users/1", "/users/1")
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void pathMatch(String uri, String path) {
        client.get().uri(uri).exchange();
        assertEquals(path, wr().path());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parameterEmpty() {
        client.get().uri("/p/params/noparam").exchange();
        assertEquals(1, wr().params().size());
        assertEquals(0, ((Map<String, Object>) wr().params().get("_s")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parameterMultiple() {
        client.get().uri(b -> b
            .path("p/t/withparams")
            .queryParam("param1", "value1")
            .queryParam("param1", "value3")
            .queryParam("param2", "value2")
            .build()).exchange();
        Map<String, Object> parameters = wr().params();
        assertEquals("value1", parameters.get("param1"));
        assertEquals("value2", parameters.get("param2"));
        assertEquals("value1", ((Map<String, List<Object>>) parameters.get("_s")).get("param1").get(0));
        assertEquals("value3", ((Map<String, List<Object>>) parameters.get("_s")).get("param1").get(1));
        assertNotNull(((Map<String, List<Object>>) parameters.get("_s")).get("param3"));
    }

    @Test
    public void getMethod_GET() {
        client.get().uri("/path")
        .exchange();
        assertEquals(HttpMethod.GET, wr().method());
    }

    @Test
    public void getMethod_POST() {
        client.post().uri("/path")
        .bodyValue(Collections.singletonMap("message", "hello-world"))
        .exchange();
        assertEquals(HttpMethod.POST, wr().method());
    }

    @Test
    public void getBody() {
        client.post().uri("/path")
        .bodyValue(Collections.singletonMap("message", "hello-world"))
        .exchange();
        JsonNode body = (JsonNode) wr().params().get("_b");
        assertEquals("hello-world", body.get("message").asText());
    }

    private HttpRequestQueryParameter wr() {
        return RequestWrapperController.wrapper;
    }

    @RestController
    public static class RequestWrapperController {

        public static HttpRequestQueryParameter wrapper;

        @GetMapping("/**")
        public Mono<Void> echo(ServerHttpRequest request) {
            return echo(request, null);
        }

        @PostMapping("/**")
        public Mono<Void> echo(ServerHttpRequest request, @RequestBody JsonNode body) {
            wrapper = new HttpRequestQueryParameter(request, body);
            return Mono.empty();
        }

    }
    
}
