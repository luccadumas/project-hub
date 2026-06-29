package com.luccadumas.projecthub.integration;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestMemberExternalClientTest {

    private static final String BASE_URL = "http://members-api";

    private MockRestServiceServer server;
    private RestMemberExternalClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestMemberExternalClient(builder.build(), BASE_URL);
    }

    @Test
    @DisplayName("Should fetch member by id from remote API")
    void shouldFetchMemberById() {
        server.expect(requestTo(BASE_URL + "/external/members/1"))
                .andRespond(withSuccess("""
                        {"id":1,"name":"Ana Silva","role":"manager"}
                        """, MediaType.APPLICATION_JSON));

        MemberResponse response = client.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Ana Silva");
        server.verify();
    }

    @Test
    @DisplayName("Should throw when remote API returns empty body for member lookup")
    void shouldThrowWhenMemberBodyIsEmpty() {
        server.expect(requestTo(BASE_URL + "/external/members/99"))
                .andRespond(withNoContent());

        assertThatThrownBy(() -> client.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        server.verify();
    }

    @Test
    @DisplayName("Should throw when remote API fails for member lookup")
    void shouldThrowWhenRemoteLookupFails() {
        server.expect(requestTo(BASE_URL + "/external/members/2"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findById(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("2");
        server.verify();
    }

    @Test
    @DisplayName("Should list members from remote API")
    void shouldListMembers() {
        server.expect(requestTo(BASE_URL + "/external/members"))
                .andRespond(withSuccess("""
                        [
                          {"id":1,"name":"Ana Silva","role":"manager"},
                          {"id":2,"name":"Bruno Costa","role":"employee"}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<MemberResponse> members = client.findAll();

        assertThat(members).hasSize(2);
        assertThat(members.getFirst().getName()).isEqualTo("Ana Silva");
        server.verify();
    }
}
