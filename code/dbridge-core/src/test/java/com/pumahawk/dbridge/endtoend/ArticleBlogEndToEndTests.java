package com.pumahawk.dbridge.endtoend;

import org.junit.jupiter.api.Test;

@EndToEndTest
public class ArticleBlogEndToEndTests extends BlogPostgresEndToEndTests {

    @Test
    public void getArticles() {
        String response = getResource("response/articles/list.json");
        client().get().uri("/articles")
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void getArticlesByTitle() {
        String response = getResource("response/articles/listByTitle.json");
        client().get().uri(b -> b
                .path("/articles")
                .queryParam("title", "fir")
                .build())
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void getById() {
        String response = getResource("response/articles/byId.json");
        client().get().uri("/articles/1")
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void multipleFilterById() {
        String response = getResource("response/articles/listByMultipleId.json");
        client().get().uri(b -> b
                .path("/articles")
                .queryParam("id", "1", "2")
                .build())
            .exchange()
            .expectBody()
            .json(response, false);
    }
}
