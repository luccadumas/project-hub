package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.integration.MemberExternalClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberDirectoryTest {

    @Mock
    private MemberExternalClient memberExternalClient;

    @InjectMocks
    private MemberDirectory memberDirectory;

    @Test
    @DisplayName("Should resolve manager names in a single external lookup")
    void shouldResolveManagerNamesInSingleLookup() {
        when(memberExternalClient.findAll()).thenReturn(List.of(
                MemberResponse.builder().id(1L).name("Ana Silva").role("manager").build(),
                MemberResponse.builder().id(2L).name("Bruno Costa").role("employee").build()
        ));

        Map<Long, String> names = memberDirectory.resolveManagerNames(Set.of(1L, 2L));

        assertThat(names).containsEntry(1L, "Ana Silva");
        assertThat(names).containsEntry(2L, "Bruno Costa");
        verify(memberExternalClient).findAll();
    }

    @Test
    @DisplayName("Should return empty map when no manager ids are requested")
    void shouldReturnEmptyMapWhenNoManagerIds() {
        assertThat(memberDirectory.resolveManagerNames(Set.of())).isEmpty();
    }

    @Test
    @DisplayName("Should fetch required member by id")
    void shouldFetchRequiredMemberById() {
        when(memberExternalClient.findById(1L)).thenReturn(
                MemberResponse.builder().id(1L).name("Ana Silva").role("manager").build()
        );

        MemberResponse member = memberDirectory.getRequired(1L);

        assertThat(member.getName()).isEqualTo("Ana Silva");
        verify(memberExternalClient).findById(1L);
    }
}
