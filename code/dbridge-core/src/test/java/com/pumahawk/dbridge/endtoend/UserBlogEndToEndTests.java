package com.pumahawk.dbridge.endtoend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@EndToEndTest
public class UserBlogEndToEndTests extends BlogPostgresEndToEndTests {

    @Test
    public void getUsers() {
        String response = getResource("response/users/list.json");
        client().get().uri("/users")
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void searchUsers_byName() {
        String response = getResource("response/users/listByName.json");
        client().get().uri(b -> b
                .path("/users")
                .queryParam("name", "mar")
                .build())
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void searchUsers_byId() {
        String response = getResource("response/users/listById.json");
        client().get().uri(b -> b
                .path("/users")
                .queryParam("id", "2")
                .build())
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void searchUsers_byId_badRequest() {
        getResource("response/users/listById.json");
        client().get().uri(b -> b
                .path("/users")
                .queryParam("id", "2", "aa")
                .build())
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    public void searchWithLimitAndOffset() {
        String response = getResource("response/users/listWithOffsetAndLimit.json");
        client().get().uri(b -> b
                .path("/users")
                .queryParam("limit", "1")
                .queryParam("offset", "1")
                .build())
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void getById() {
        String response = getResource("response/users/byId.json");
        client().get().uri(b -> b
                .path("/users/{id}")
                .build("1"))
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void emptySearchUsers() {
        String response = getResource("response/users/listEmpty.json");
        client().get().uri(b -> b
                .path("/users")
                .queryParam("name", "zzzz")
                .build())
            .exchange()
            .expectBody()
            .json(response, false);
    }

    @Test
    public void getById_notFound() {
        String response = getResource("response/users/byId_notFound.json");
        client().get().uri(b -> b
                .path("/users/{id}")
                .build("554454"))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
            .expectBody()
            .json(response, false);
    }

    @Test
    public void getById_idNotNumber() {
        String response = getResource("response/users/byId_idNotNumber.json");
        client().get().uri(b -> b
                .path("/users/{id}")
                .build("id_12343"))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody()
            .json(response, false);
    }

}
