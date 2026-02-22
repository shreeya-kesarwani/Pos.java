package com.pos.utils;

import com.pos.utils.CsvRoleAccessService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvRoleAccessServiceTest {

    private static Path classpathCsvPath() {
        // Maven test classpath includes target/test-classes first.
        return Path.of("target", "test-classes", "access-control.csv");
    }

    private static void writeCsv(String content) throws Exception {
        Path p = classpathCsvPath();
        Files.createDirectories(p.getParent());
        Files.writeString(p, content, StandardCharsets.UTF_8);
    }

    @Test
    void load_and_isAllowed_shouldWork_forNormalizationWildcardAndPathParams() throws Exception {
        writeCsv("""
                # comment line should be ignored

                GET, /orders , supervisor | operator
                POST, orders, SUPERVISOR
                GET, /products/{id}, SUPERVISOR
                *, /public/*, *
                GET, /trail/, SUPERVISOR
                """);

        CsvRoleAccessService svc = new CsvRoleAccessService();
        svc.load();

        // null guards
        assertFalse(svc.isAllowed(null, "/orders", "SUPERVISOR"));
        assertFalse(svc.isAllowed("GET", null, "SUPERVISOR"));
        assertFalse(svc.isAllowed("GET", "/orders", null));

        // method + role normalization (trim + uppercase)
        assertTrue(svc.isAllowed(" get ", "/orders", " supervisor "));
        assertTrue(svc.isAllowed("GET", "/orders", "OPERATOR"));
        assertFalse(svc.isAllowed("GET", "/orders", "GUEST"));

        // path normalization: no leading slash + trailing slash
        assertTrue(svc.isAllowed("POST", "orders", "SUPERVISOR"));
        assertTrue(svc.isAllowed("POST", "/orders/", "SUPERVISOR"));

        // path params: {id} should match a segment
        assertTrue(svc.isAllowed("GET", "/products/123", "SUPERVISOR"));
        assertFalse(svc.isAllowed("GET", "/products/123", "OPERATOR"));

        // wildcard path + wildcard roles
        assertTrue(svc.isAllowed("GET", "/public/anything/here", "ANYROLE"));
        assertTrue(svc.isAllowed("POST", "/public/x", "GUEST"));
        assertTrue(svc.isAllowed("DELETE", "/public/x", "SUPERVISOR"));

        // trailing slash normalization in rule itself ("/trail/" becomes "/trail")
        assertTrue(svc.isAllowed("GET", "/trail", "SUPERVISOR"));
        assertTrue(svc.isAllowed("GET", "/trail/", "SUPERVISOR"));
        assertFalse(svc.isAllowed("GET", "/trail", "OPERATOR"));

        // no matching rule
        assertFalse(svc.isAllowed("PUT", "/orders", "SUPERVISOR"));
    }

    @Test
    void load_shouldThrowRuntimeException_whenCsvLineHasLessThan3Parts() throws Exception {
        writeCsv("""
                GET,/ok,SUPERVISOR
                BADLINE_WITH_TOO_FEW_PARTS
                """);

        CsvRoleAccessService svc = new CsvRoleAccessService();

        RuntimeException ex = assertThrows(RuntimeException.class, svc::load);
        assertTrue(ex.getMessage().contains("Failed to load access rules"));
        assertNotNull(ex.getCause());
    }

    @Test
    void isAllowed_shouldReturnFalse_whenRulesListEmpty() throws Exception {
        // empty file: only comments/blanks => rules remains empty
        writeCsv("""
                # only comment
                  
                """);

        CsvRoleAccessService svc = new CsvRoleAccessService();
        svc.load();

        assertFalse(svc.isAllowed("GET", "/anything", "SUPERVISOR"));
    }
}
