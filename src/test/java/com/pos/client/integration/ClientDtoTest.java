package com.pos.client.integration;

import com.pos.api.ClientApi;
import com.pos.dto.ClientDto;
import com.pos.exception.ApiException;
import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.ClientForm;
import com.pos.pojo.Client;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDtoTest {

    @Mock private ClientApi clientApi;
    @Mock private Validator validator;

    @InjectMocks private ClientDto clientDto;

    @Test
    void addNormalizesValidatesAndCallsApi() throws Exception {
        ClientForm form = new ClientForm();
        form.setName("  Acme  ");
        form.setEmail("  a@b.com  ");

        when(validator.validate(any(ClientForm.class))).thenReturn(Set.of());

        clientDto.add(form);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientApi).add(captor.capture());

        assertEquals("Acme", captor.getValue().getName());
        assertEquals("a@b.com", captor.getValue().getEmail());
    }

    @Test
    void updateNormalizesValidatesAndCallsApi() throws Exception {
        ClientForm form = new ClientForm();
        form.setName("  N  ");
        form.setEmail("  e@e.com  ");

        when(validator.validate(any(ClientForm.class))).thenReturn(Set.of());

        clientDto.update(9, form);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientApi).update(eq(9), captor.capture());

        assertEquals("N", captor.getValue().getName());
        assertEquals("e@e.com", captor.getValue().getEmail());
    }

    @Test
    void getClientsNormalizesQueryParams() throws Exception {
        Client c = new Client();
        c.setId(1);
        c.setName("X");
        c.setEmail("x@y.com");

        when(clientApi.search(eq(7), eq("name"), eq("email"), eq(1), eq(10))).thenReturn(List.of(c));
        when(clientApi.getCount(eq(7), eq("name"), eq("email"))).thenReturn(1L);

        PaginatedResponse<ClientData> resp = clientDto.getClients(7, "  name ", "  email ", 1, 10);

        assertEquals(1L, resp.getTotalCount());
        assertEquals(1, resp.getData().size());
        assertEquals("X", resp.getData().getFirst().getName());
    }

    @Test
    void addThrowsWhenValidationFails() {
        ClientForm form = new ClientForm();
        form.setName("x");

        @SuppressWarnings("unchecked")
        ConstraintViolation<ClientForm> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("bad");
        when(validator.validate(any(ClientForm.class))).thenReturn(Set.of(v));

        ApiException ex = assertThrows(ApiException.class, () -> clientDto.add(form));
        assertEquals("bad", ex.getMessage());

        verifyNoInteractions(clientApi);
    }

    @Test
    void updateThrowsWhenValidationFails() {
        ClientForm form = new ClientForm();
        form.setName("x");

        @SuppressWarnings("unchecked")
        ConstraintViolation<ClientForm> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("bad");
        when(validator.validate(any(ClientForm.class))).thenReturn(Set.of(v));

        ApiException ex = assertThrows(ApiException.class, () -> clientDto.update(1, form));
        assertEquals("bad", ex.getMessage());

        verifyNoInteractions(clientApi);
    }
}