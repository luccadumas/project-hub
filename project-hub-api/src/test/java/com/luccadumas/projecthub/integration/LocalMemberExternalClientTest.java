package com.luccadumas.projecthub.integration;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.service.ExternalMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalMemberExternalClientTest {

    @Mock
    private ExternalMemberService externalMemberService;

    @InjectMocks
    private LocalMemberExternalClient client;

    @Test
    @DisplayName("Should delegate findById to external member service")
    void shouldDelegateFindById() {
        when(externalMemberService.findById(1L)).thenReturn(
                MemberResponse.builder().id(1L).name("Ana Silva").role("manager").build()
        );

        MemberResponse response = client.findById(1L);

        assertThat(response.getName()).isEqualTo("Ana Silva");
        verify(externalMemberService).findById(1L);
    }

    @Test
    @DisplayName("Should delegate findAll to external member service")
    void shouldDelegateFindAll() {
        when(externalMemberService.findAll()).thenReturn(List.of(
                MemberResponse.builder().id(2L).name("Bruno Costa").role("employee").build()
        ));

        assertThat(client.findAll()).hasSize(1);
        verify(externalMemberService).findAll();
    }
}
