package com.pumahawk.dbridge.endpoints;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pumahawk.dbridge.exceptions.NotFoundRoute;
import com.pumahawk.dbridge.exceptions.ProjectException;
import com.pumahawk.dbridge.script.ScriptManagerFactory;
import com.pumahawk.dbridge.services.QueryParameter;
import com.pumahawk.dbridge.services.QueryService;
import com.pumahawk.dbridge.services.SimpleQueryResult;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class QueryControllerTests {

  @MockitoBean private QueryService queryService;

  @Autowired private WebTestClient client;

  @Autowired private ScriptManagerFactory scriptManagerFactory;

  @Test
  public void streamTest() {

    SimpleQueryResult qr = new SimpleQueryResult();
    ObjectMapper om = new ObjectMapper();
    qr.setData(om.createObjectNode().put("id", 1));

    when(queryService.query(any(QueryParameter.class))).thenReturn(qr);

    client.get().uri("/get").exchange().expectBody(String.class).isEqualTo("{\"data\":{\"id\":1}}");
  }

  public static Stream<Arguments> notFound() {
    return Stream.of(arguments(new NotFoundRoute(), "Route not found"));
  }

  @ParameterizedTest
  @MethodSource
  public void notFound(ProjectException ex, String message) {
    when(queryService.query(any())).thenThrow(ex);
    client
        .get()
        .uri("/notFound")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.NOT_FOUND)
        .expectBody()
        .jsonPath("message")
        .isEqualTo(message);
  }

  @Test
  public void notFoundFromSpel() {
    Exception ex = null;
    try {
      scriptManagerFactory.getScriptManager().evaluate("#found(null, 'not foud spel')");
    } catch (Exception e) {
      ex = e;
    }
    when(queryService.query(any())).thenThrow(ex);
    client.get().uri("/notFounfSpel").exchange().expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
  }
}
