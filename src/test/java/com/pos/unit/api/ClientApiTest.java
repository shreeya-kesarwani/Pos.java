package com.pos.unit.api;

import com.pos.api.ClientApi;
import com.pos.dao.ClientDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiTest {

    @InjectMocks
    private ClientApi clientApi;

    @Mock
    private ClientDao clientDao;

    // ---------------- get / getCheck ----------------

    @Test
    void get_shouldDelegateToDao() {
        Client c = new Client();
        when(clientDao.selectById(1)).thenReturn(c);

        Client out = clientApi.get(1);

        assertSame(c, out);
        verify(clientDao).selectById(1);
        verifyNoMoreInteractions(clientDao);
    }

    @Test
    void getCheck_shouldReturn_whenFound() throws ApiException {
        Client c = new Client();
        when(clientDao.selectById(10)).thenReturn(c);

        Client out = clientApi.getCheck(10);

        assertSame(c, out);
        verify(clientDao).selectById(10);
    }

    @Test
    void getCheck_shouldThrow_whenNotFound() {
        when(clientDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.getCheck(99));
        assertTrue(ex.getMessage().contains(CLIENT_ID_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));

        verify(clientDao).selectById(99);
    }

    // ---------------- getByName / getCheckByName ----------------

    @Test
    void getByName_shouldDelegateToDao() {
        Client c = new Client();
        when(clientDao.selectByName("abc")).thenReturn(c);

        Client out = clientApi.getByName("abc");

        assertSame(c, out);
        verify(clientDao).selectByName("abc");
    }

    @Test
    void getCheckByName_shouldReturnNull_whenNameNull() throws ApiException {
        Client out = clientApi.getCheckByName(null);
        assertNull(out);
        verifyNoInteractions(clientDao);
    }

    @Test
    void getCheckByName_shouldReturn_whenFound() throws ApiException {
        Client c = new Client();
        when(clientDao.selectByName("c1")).thenReturn(c);

        Client out = clientApi.getCheckByName("c1");

        assertSame(c, out);
        verify(clientDao).selectByName("c1");
    }

    @Test
    void getCheckByName_shouldThrow_whenNotFound() {
        when(clientDao.selectByName("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.getCheckByName("missing"));
        assertTrue(ex.getMessage().contains(CLIENT_NAME_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("missing"));

        verify(clientDao).selectByName("missing");
    }

    // ---------------- add ----------------

    @Test
    void add_shouldInsert_whenNameNotExists() throws ApiException {
        Client c = new Client();
        c.setName("new");

        when(clientDao.selectByName("new")).thenReturn(null);

        clientApi.add(c);

        verify(clientDao).selectByName("new");
        verify(clientDao).insert(c);
        verifyNoMoreInteractions(clientDao);
    }

    @Test
    void add_shouldThrow_whenClientAlreadyExists() {
        Client c = new Client();
        c.setName("dup");

        when(clientDao.selectByName("dup")).thenReturn(new Client());

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.add(c));
        assertTrue(ex.getMessage().contains(CLIENT_ALREADY_EXISTS.value()));
        assertTrue(ex.getMessage().contains("dup"));

        verify(clientDao).selectByName("dup");
        verify(clientDao, never()).insert(any());
    }

    // ---------------- update ----------------

    @Test
    void update_shouldUpdateFields_whenNameFreeOrSameClient() throws ApiException {
        Client existing = new Client();
        existing.setId(1);
        existing.setName("old");
        existing.setEmail("old@mail");

        when(clientDao.selectById(1)).thenReturn(existing);

        // No other client has the new name
        when(clientDao.selectByName("newName")).thenReturn(null);

        Client incoming = new Client();
        incoming.setName("newName");
        incoming.setEmail("new@mail");

        clientApi.update(1, incoming);

        assertEquals("newName", existing.getName());
        assertEquals("new@mail", existing.getEmail());

        verify(clientDao).selectById(1);
        verify(clientDao).selectByName("newName");
    }

    @Test
    void update_shouldAllowSameName_ifOtherIsSameClientId() throws ApiException {
        Client existing = new Client();
        existing.setId(1);
        existing.setName("same");

        when(clientDao.selectById(1)).thenReturn(existing);

        Client other = new Client();
        other.setId(1); // same id => allowed

        when(clientDao.selectByName("same")).thenReturn(other);

        Client incoming = new Client();
        incoming.setName("same");
        incoming.setEmail("x@mail");

        clientApi.update(1, incoming);

        assertEquals("same", existing.getName());
        assertEquals("x@mail", existing.getEmail());

        verify(clientDao).selectById(1);
        verify(clientDao).selectByName("same");
    }

    @Test
    void update_shouldThrow_whenNameTakenByOtherClient() {
        Client existing = new Client();
        existing.setId(1);

        when(clientDao.selectById(1)).thenReturn(existing);

        Client other = new Client();
        other.setId(2); // different client has same name

        when(clientDao.selectByName("taken")).thenReturn(other);

        Client incoming = new Client();
        incoming.setName("taken");

        ApiException ex = assertThrows(ApiException.class, () -> clientApi.update(1, incoming));
        assertTrue(ex.getMessage().contains(CLIENT_NAME_TAKEN.value()));
        assertTrue(ex.getMessage().contains("taken"));

        verify(clientDao).selectById(1);
        verify(clientDao).selectByName("taken");
    }

    @Test
    void update_shouldThrow_whenClientIdNotFound() {
        when(clientDao.selectById(404)).thenReturn(null);

        Client incoming = new Client();
        incoming.setName("x");

        assertThrows(ApiException.class, () -> clientApi.update(404, incoming));

        verify(clientDao).selectById(404);
        verify(clientDao, never()).selectByName(any());
    }

    // ---------------- search / count passthrough ----------------

    @Test
    void search_shouldDelegateToDao() {
        List<Client> expected = List.of(new Client());
        when(clientDao.searchByParams(1, "n", "e", 0, 10)).thenReturn(expected);

        List<Client> out = clientApi.search(1, "n", "e", 0, 10);

        assertSame(expected, out);
        verify(clientDao).searchByParams(1, "n", "e", 0, 10);
    }

    @Test
    void getCount_shouldDelegateToDao() {
        when(clientDao.getCount(1, "n", "e")).thenReturn(5L);

        Long out = clientApi.getCount(1, "n", "e");

        assertEquals(5L, out);
        verify(clientDao).getCount(1, "n", "e");
    }

    // ---------------- getByNames / getByIds ----------------

    @Test
    void getByNames_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(List.of(), clientApi.getByNames(null));
        assertEquals(List.of(), clientApi.getByNames(List.of()));
        verifyNoInteractions(clientDao);
    }

    @Test
    void getByNames_shouldDelegate_whenNonEmpty() {
        List<String> names = List.of("a", "b");
        List<Client> expected = List.of(new Client(), new Client());

        when(clientDao.selectByNames(names)).thenReturn(expected);

        List<Client> out = clientApi.getByNames(names);

        assertSame(expected, out);
        verify(clientDao).selectByNames(names);
    }

    @Test
    void getByIds_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(List.of(), clientApi.getByIds(null));
        assertEquals(List.of(), clientApi.getByIds(List.of()));
        verifyNoInteractions(clientDao);
    }

    @Test
    void getByIds_shouldDelegate_whenNonEmpty() {
        List<Integer> ids = List.of(1, 2);
        List<Client> expected = List.of(new Client());

        when(clientDao.selectByIds(ids)).thenReturn(expected);

        List<Client> out = clientApi.getByIds(ids);

        assertSame(expected, out);
        verify(clientDao).selectByIds(ids);
    }
}
