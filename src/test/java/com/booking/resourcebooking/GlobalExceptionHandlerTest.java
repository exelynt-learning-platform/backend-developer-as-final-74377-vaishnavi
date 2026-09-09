
package com.booking.resourcebooking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.exception.GlobalExceptionHandler;
import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.exception.UnauthorizedException;

import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;



class GlobalExceptionHandlerTest {

    @Test
    void shouldReturn404WhenResourceNotFound() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Resource not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Resource not found", response.getBody().get("message"));
    }
    
  
    @Test
    void shouldReturn400WhenBadRequest() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BadRequestException exception =
                new BadRequestException("Invalid request");

        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Invalid request", response.getBody().get("message"));
    }
    

    
    @Test
    void shouldReturn401WhenUnauthorized() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        UnauthorizedException exception =
                new UnauthorizedException("Unauthorized access");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnauthorized(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Unauthorized access", response.getBody().get("message"));
    }
   
    @Test
    void shouldReturn400WhenValidationFails() throws Exception {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Method method =
                GlobalExceptionHandlerTest.class.getDeclaredMethod(
                        "dummyMethod", String.class);

        MethodParameter parameter =
                new MethodParameter(method, 0);

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(
                        new Object(),
                        "dummy");

        bindingResult.addError(
                new FieldError(
                        "dummy",
                        "username",
                        "Username is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        parameter,
                        bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode());

        assertEquals(
                400,
                response.getBody().get("status"));

        Map<String, String> errors =
                (Map<String, String>) response.getBody().get("errors");

        assertEquals(
                "Username is required",
                errors.get("username"));
    }
    private void dummyMethod(String username) {
        // Used only to create MethodParameter for the test
    }

}
