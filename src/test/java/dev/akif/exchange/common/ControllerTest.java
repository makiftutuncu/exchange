package dev.akif.exchange.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import e.java.E;
import e.java.EException;
import e.java.EOr;

public class ControllerTest {
    private static class TestController extends Controller {}

    private static final Controller controller = new TestController();

    @Test
    @DisplayName("responding with EOr")
    void testRespondingWithEOr() {
        E e = E.fromName("test").code(400);

        EException ee = assertThrows(EException.class, () -> controller.respond(EOr.from(e)));
        assertEquals(ee.e, e);

        ResponseEntity<String> response = controller.respond(EOr.from("test"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("test", response.getBody());
    }

    @Test
    @DisplayName("responding with EOr with custom status code")
    void testRespondingWithEOrWithCustomStatusCode() {
        E e = E.fromName("test").code(400);

        EException ee = assertThrows(EException.class, () -> controller.respond(EOr.from(e), HttpStatus.CREATED));
        assertEquals(ee.e, e);

        ResponseEntity<String> response = controller.respond(EOr.from("test"), HttpStatus.CREATED);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("test", response.getBody());
    }
}
