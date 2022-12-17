package com.pumahawk.dbridge.endtoend;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@EndToEndTest
public class BucketBlogEndToEndTests extends BlogPostgresEndToEndTests {

    @Test
    public void insertInBucket() {

        String id = UUID.randomUUID().toString();

        client().post().uri("/bucket")
            .bodyValue(new ObjectMapper().createObjectNode()
                .put("message", "BucketBlogEndToEndTests")
                .put("id", id))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectBody().json("{\"data\":{\"message\":\"success\"}}", false);

        client().get().uri(b -> b
                .path("/bucket/{id}")
                .build(id))
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody().json("{\"data\":{\"id\":\"" + id + "\"}}", false);
        
    }
    
    
}
