package com.coreservice.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest {

    @Test
    void shouldCreateResourceWhenValid() {
        Resource r = new Resource("id-123", "My Resource", "desc");
        assertEquals("id-123", r.getId());
        assertEquals("My Resource", r.getName());
        assertEquals("desc", r.getDescription());
    }

    @Test
    void shouldFailWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Resource("id", "", "desc"));
    }

    @Test
    void shouldFailWhenNameIsTooLong() {
        String longName = "a".repeat(101);
        assertThrows(IllegalArgumentException.class,
                () -> new Resource("id", longName, "desc"));
    }
}

