package dev.akif.exchange.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControllerTest {
  private static class TestController extends Controller {}

  private static final Controller controller = new TestController();

  @Test
  @DisplayName("responding")
  void testResponding() {
    Exception e =
        assertThrows(
            Exception.class,
            () ->
                controller.respond(
                    () -> {
                      throw new Exception("test");
                    }));
    assertEquals("test", e.getMessage());

    ResponseEntity<String> response = controller.respond(() -> "test");

    assertEquals(200, response.getStatusCode().value());
    assertEquals("test", response.getBody());
  }

  @Test
  @DisplayName("responding with custom status code")
  void testRespondingWithCustomStatusCode() {
    Exception e =
        assertThrows(
            Exception.class,
            () ->
                controller.respond(
                    () -> {
                      throw new Exception("test");
                    },
                    HttpStatus.CREATED));
    assertEquals("test", e.getMessage());

    ResponseEntity<String> response = controller.respond(() -> "test", HttpStatus.CREATED);

    assertEquals(201, response.getStatusCode().value());
    assertEquals("test", response.getBody());
  }
}
