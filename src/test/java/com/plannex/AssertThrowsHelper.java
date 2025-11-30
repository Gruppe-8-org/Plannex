package com.plannex;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertThrowsHelper {
    public <T, S extends Exception> void verifyExceptionThrownWithMessage(String expectedMessage, Class<S> c, Callable<T> function) {
        Exception thrown = assertThrows(c,
                function::call,
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }
}
