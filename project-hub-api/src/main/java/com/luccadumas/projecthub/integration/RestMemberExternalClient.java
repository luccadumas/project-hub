package com.luccadumas.projecthub.integration;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Optional HTTP adapter for remote member APIs.
 * The application uses {@link LocalMemberExternalClient} by default.
 */
public class RestMemberExternalClient implements MemberExternalClient {

    private final RestClient restClient;
    private final String baseUrl;

    public RestMemberExternalClient(RestClient restClient, String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public MemberResponse findById(Long id) {
        try {
            MemberResponse response = restClient.get()
                    .uri(baseUrl + "/external/members/{id}", id)
                    .retrieve()
                    .body(MemberResponse.class);
            if (response == null) {
                throw new ResourceNotFoundException("Member not found with id: " + id);
            }
            return response;
        } catch (RestClientException ex) {
            throw new ResourceNotFoundException("Member not found with id: " + id);
        }
    }

    @Override
    public List<MemberResponse> findAll() {
        return restClient.get()
                .uri(baseUrl + "/external/members")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
