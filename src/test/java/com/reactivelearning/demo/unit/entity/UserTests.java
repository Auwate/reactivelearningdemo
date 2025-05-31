package com.reactivelearning.demo.unit.entity;

import com.reactivelearning.demo.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class UserTests {

    /**
     * Test successful update sequence, given non-null params
     */
    @Test
    void testUpdateSuccess() {

        User user = new User();
        user.update("Test_User", "Test_Pass", "Test_Email");

        assertEquals("Test_User", user.getUsername());
        assertEquals("Test_Pass", user.getPassword());
        assertEquals("Test_Email", user.getEmail());

    }

    /**
     * Test successful partial update (some non-null params)
     */
    @Test
    void testPartialUpdateSuccess() {

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        user1.update("Test", null, null);
        user2.update(null, "Test2", null);
        user3.update(null, null, "Test3");

        assertEquals("Test", user1.getUsername());
        assertEquals("Test2", user2.getPassword());
        assertEquals("Test3", user3.getEmail());

    }

}
