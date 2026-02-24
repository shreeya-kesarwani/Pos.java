package com.pos.client.unit;

import com.pos.api.ClientApi;
import com.pos.dao.ClientDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Client;
import com.pos.setup.UnitTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiTest {

    @InjectMocks
    private ClientApi clientApi;

    @Mock
    private ClientDao clientDao;

    private Client existingClient;

    private Integer clientId;
    private String existingName;
    private String existingEmail;

    @BeforeEach
    void setupData() {
        clientId = 1;
        existingName = "old";
        existingEmail = "old@mail";
        existingClient = UnitTestFactory.client(clientId, existingName, existingEmail);
    }

    @Test
    void getShouldDelegateToDao() {
        when(clientDao.selectById(clientId)).thenReturn(existingClient);

        Client out = clientApi.get(clientId);

        assertSame(existingClient, out);
        verify(clientDao).selectById(clientId);
        verifyNoMoreInteractions(clientDao);
    }

    @Test
    void getCheckShouldReturnWhenFound() throws ApiException {
        when(clientDao.selectById(10)).thenReturn(existingClient);

        Client out = clientApi.getCheck(10);

        assertSame(existingClient, out);
        verify(clientDao).selectById(10);
    }

    @Test
    void getCheckShouldThrowWhenNotFound() {
        when(clientDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.getCheck(99));
        assertTrue(ex.getMessage().contains(CLIENT_ID_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));
        verify(clientDao).selectById(99);
    }

    @Test
    void getByNameShouldDelegateToDao() {
        when(clientDao.selectByName("abc")).thenReturn(existingClient);

        Client out = clientApi.getByName("abc");

        assertSame(existingClient, out);
        verify(clientDao).selectByName("abc");
    }

    @Test
    void getCheckByNameShouldReturnNullWhenNameNull() throws ApiException {
        Client out = clientApi.getCheckByName(null);

        assertNull(out);
        verifyNoInteractions(clientDao);
    }

    @Test
    void getCheckByNameShouldReturnWhenFound() throws ApiException {
        when(clientDao.selectByName("c1")).thenReturn(existingClient);

        Client out = clientApi.getCheckByName("c1");

        assertSame(existingClient, out);
        verify(clientDao).selectByName("c1");
    }

    @Test
    void getCheckByNameShouldThrowWhenNotFound() {
        when(clientDao.selectByName("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.getCheckByName("missing"));
        assertTrue(ex.getMessage().contains(CLIENT_NAME_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("missing"));
        verify(clientDao).selectByName("missing");
    }

    @Test
    void addShouldInsertWhenNameNotExists() throws ApiException {
        Client incoming = UnitTestFactory.clientWithName("new");
        when(clientDao.selectByName("new")).thenReturn(null);

        clientApi.add(incoming);

        verify(clientDao).selectByName("new");
        verify(clientDao).insert(incoming);
        verifyNoMoreInteractions(clientDao);
    }

    @Test
    void addShouldThrowWhenClientAlreadyExists() {
        Client incoming = UnitTestFactory.clientWithName("dup");
        when(clientDao.selectByName("dup")).thenReturn(UnitTestFactory.client(2, "dup", "x@mail"));

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.add(incoming));
        assertTrue(ex.getMessage().contains(CLIENT_ALREADY_EXISTS.value()));
        assertTrue(ex.getMessage().contains("dup"));

        verify(clientDao).selectByName("dup");
        verify(clientDao, never()).insert(any());
    }

    @Test
    void updateShouldUpdateFieldsWhenNameFree() throws ApiException {
        when(clientDao.selectById(clientId)).thenReturn(existingClient);
        when(clientDao.selectByName("newName")).thenReturn(null);

        Client incoming = UnitTestFactory.client(null, "newName", "new@mail");

        clientApi.update(clientId, incoming);

        assertEquals("newName", existingClient.getName());
        assertEquals("new@mail", existingClient.getEmail());

        verify(clientDao).selectById(clientId);
        verify(clientDao).selectByName("newName");
    }

    @Test
    void updateShouldAllowSameNameWhenOtherIsSameClientId() throws ApiException {
        when(clientDao.selectById(clientId)).thenReturn(existingClient);

        Client other = UnitTestFactory.client(clientId, "same", "ignore@mail");
        when(clientDao.selectByName("same")).thenReturn(other);

        Client incoming = UnitTestFactory.client(null, "same", "x@mail");

        clientApi.update(clientId, incoming);

        assertEquals("same", existingClient.getName());
        assertEquals("x@mail", existingClient.getEmail());

        verify(clientDao).selectById(clientId);
        verify(clientDao).selectByName("same");
    }

    @Test
    void updateShouldThrowWhenNameTakenByOtherClient() {
        when(clientDao.selectById(clientId)).thenReturn(existingClient);

        Client other = UnitTestFactory.client(2, "taken", "x@mail");
        when(clientDao.selectByName("taken")).thenReturn(other);

        Client incoming = UnitTestFactory.clientWithName("taken");

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.update(clientId, incoming));
        assertTrue(ex.getMessage().contains(CLIENT_NAME_TAKEN.value()));
        assertTrue(ex.getMessage().contains("taken"));

        verify(clientDao).selectById(clientId);
        verify(clientDao).selectByName("taken");
    }

    @Test
    void updateShouldThrowWhenClientIdNotFound() {
        when(clientDao.selectById(404)).thenReturn(null);

        Client incoming = UnitTestFactory.clientWithName("x");

        assertThrows(ApiException.class, () -> clientApi.update(404, incoming));

        verify(clientDao).selectById(404);
        verify(clientDao, never()).selectByName(any());
    }

    @Test
    void searchShouldDelegateToDao() {
        List<Client> expected = List.of(existingClient);
        when(clientDao.searchByParams(1, "n", "e", 0, 10)).thenReturn(expected);

        List<Client> out = clientApi.search(1, "n", "e", 0, 10);

        assertSame(expected, out);
        verify(clientDao).searchByParams(1, "n", "e", 0, 10);
    }

    @Test
    void getCountShouldDelegateToDao() {
        when(clientDao.getCount(1, "n", "e")).thenReturn(5L);

        Long out = clientApi.getCount(1, "n", "e");

        assertEquals(5L, out);
        verify(clientDao).getCount(1, "n", "e");
    }

    @Test
    void getByNamesShouldReturnEmptyWhenNullOrEmpty() {
        assertEquals(List.of(), clientApi.getByNames(null));
        assertEquals(List.of(), clientApi.getByNames(List.of()));
        verifyNoInteractions(clientDao);
    }

    @Test
    void getByNamesShouldDelegateWhenNonEmpty() {
        List<String> names = List.of("a", "b");
        List<Client> expected = List.of(existingClient, UnitTestFactory.client(2, "b", "b@mail"));
        when(clientDao.selectByNames(names)).thenReturn(expected);

        List<Client> out = clientApi.getByNames(names);

        assertSame(expected, out);
        verify(clientDao).selectByNames(names);
    }

    @Test
    void getByIdsShouldReturnEmptyWhenNullOrEmpty() {
        assertEquals(List.of(), clientApi.getByIds(null));
        assertEquals(List.of(), clientApi.getByIds(List.of()));
        verifyNoInteractions(clientDao);
    }

    @Test
    void getByIdsShouldDelegateWhenNonEmpty() {
        List<Integer> ids = List.of(1, 2);
        List<Client> expected = List.of(existingClient);
        when(clientDao.selectByIds(ids)).thenReturn(expected);

        List<Client> out = clientApi.getByIds(ids);

        assertSame(expected, out);
        verify(clientDao).selectByIds(ids);
    }
}