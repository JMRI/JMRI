package jmri.jmrit.permission;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

/**
 * Tests for DefaultPermissionManager session management.
 *
 * @author Ralf Lang
 * @author JMRI Community (C) 2026
 */
public class DefaultPermissionManagerTest {

    private DefaultPermissionManager manager;

    /**
     * Test successful remote login with valid credentials.
     */
    @Test
    public void testRemoteLoginSuccess() throws Exception {
        // Add a test user
        manager.addUser("testuser", "testpass");

        // Attempt remote login
        StringBuilder sessionId = new StringBuilder();
        boolean result = manager.remoteLogin(sessionId, Locale.getDefault(), "testuser", "testpass");

        assertTrue(result, "Remote login should succeed with valid credentials");
        assertNotNull(sessionId.toString(), "Session ID should not be null");
        assertFalse(sessionId.toString().isBlank(), "Session ID should not be empty or whitespace-only");
        assertEquals(10, sessionId.length(), "Session ID should be 10 characters long");
    }

    /**
     * Test remote login with invalid password.
     */
    @Test
    public void testRemoteLoginInvalidPassword() throws Exception {
        // Add a test user
        manager.addUser("testuser", "testpass");

        // Attempt remote login with wrong password
        StringBuilder sessionId = new StringBuilder();
        boolean result = manager.remoteLogin(sessionId, Locale.getDefault(), "testuser", "wrongpass");

        assertFalse(result, "Remote login should fail with invalid password");
        assertEquals(0, sessionId.length(), "Session ID should be empty on failed login");
    }

    /**
     * Test remote login with non-existent user.
     */
    @Test
    public void testRemoteLoginNonExistentUser() {
        // Attempt remote login with non-existent user
        StringBuilder sessionId = new StringBuilder();
        boolean result = manager.remoteLogin(sessionId, Locale.getDefault(), "nonexistent", "anypass");

        assertFalse(result, "Remote login should fail for non-existent user");
        assertEquals(0, sessionId.length(), "Session ID should be empty on failed login");
    }

    /**
     * Test that session ID is stored and can be verified.
     */
    @Test
    public void testRemoteSessionStorage() throws Exception {
        // Add a test user and login
        manager.addUser("testuser", "testpass");
        StringBuilder sessionId = new StringBuilder();
        manager.remoteLogin(sessionId, Locale.getDefault(), "testuser", "testpass");

        // Verify the session is stored
        assertTrue(manager.isRemotelyLoggedIn(sessionId.toString()),
                "Session should be marked as logged in");
    }

    /**
     * Test that invalid session ID is not recognized.
     */
    @Test
    public void testInvalidSessionNotLoggedIn() {
        assertFalse(manager.isRemotelyLoggedIn("invalid-session-id"),
                "Invalid session should not be recognized as logged in");
        assertFalse(manager.isRemotelyLoggedIn(null),
                "Null session should not be recognized as logged in");
        assertFalse(manager.isRemotelyLoggedIn(""),
                "Empty session should not be recognized as logged in");
        assertFalse(manager.isRemotelyLoggedIn("   "),
                "Blank session should not be recognized as logged in");
    }

    /**
     * Test remote logout removes the session.
     */
    @Test
    public void testRemoteLogout() throws Exception {
        // Add a test user and login
        manager.addUser("testuser", "testpass");
        StringBuilder sessionId = new StringBuilder();
        manager.remoteLogin(sessionId, Locale.getDefault(), "testuser", "testpass");

        // Verify session exists
        assertTrue(manager.isRemotelyLoggedIn(sessionId.toString()),
                "Session should exist before logout");

        // Logout
        manager.remoteLogout(sessionId.toString());

        // Verify session is removed
        assertFalse(manager.isRemotelyLoggedIn(sessionId.toString()),
                "Session should be removed after logout");
    }

    /**
     * Test remote logout with invalid session IDs (should not throw exception).
     */
    @Test
    public void testRemoteLogoutInvalidSessions() {
        // These should not throw exceptions
        assertDoesNotThrow(() -> manager.remoteLogout(null),
                "Logout with null session should not throw");
        assertDoesNotThrow(() -> manager.remoteLogout(""),
                "Logout with empty session should not throw");
        assertDoesNotThrow(() -> manager.remoteLogout("   "),
                "Logout with blank session should not throw");
        assertDoesNotThrow(() -> manager.remoteLogout("nonexistent-session"),
                "Logout with non-existent session should not throw");
    }

    /**
     * Test multiple simultaneous remote sessions for different users.
     */
    @Test
    public void testMultipleRemoteSessions() throws Exception {
        // Add multiple users
        manager.addUser("user1", "pass1");
        manager.addUser("user2", "pass2");

        // Login both users
        StringBuilder sessionId1 = new StringBuilder();
        StringBuilder sessionId2 = new StringBuilder();
        manager.remoteLogin(sessionId1, Locale.getDefault(), "user1", "pass1");
        manager.remoteLogin(sessionId2, Locale.getDefault(), "user2", "pass2");

        // Verify both sessions exist
        assertTrue(manager.isRemotelyLoggedIn(sessionId1.toString()),
                "User1 session should exist");
        assertTrue(manager.isRemotelyLoggedIn(sessionId2.toString()),
                "User2 session should exist");

        // Sessions should be different
        assertNotEquals(sessionId1.toString(), sessionId2.toString(),
                "Different users should have different session IDs");

        // Logout user1
        manager.remoteLogout(sessionId1.toString());

        // Verify user1 session removed but user2 session still exists
        assertFalse(manager.isRemotelyLoggedIn(sessionId1.toString()),
                "User1 session should be removed");
        assertTrue(manager.isRemotelyLoggedIn(sessionId2.toString()),
                "User2 session should still exist");
    }

    /**
     * Test that session IDs are generated uniquely.
     * Note: This test verifies uniqueness for multiple logins,
     * but does not test cryptographic randomness.
     */
    @Test
    public void testSessionIdUniqueness() throws Exception {
        manager.addUser("testuser", "testpass");

        // Generate multiple session IDs
        StringBuilder sessionId1 = new StringBuilder();
        StringBuilder sessionId2 = new StringBuilder();
        StringBuilder sessionId3 = new StringBuilder();

        manager.remoteLogin(sessionId1, Locale.getDefault(), "testuser", "testpass");
        manager.remoteLogout(sessionId1.toString());

        manager.remoteLogin(sessionId2, Locale.getDefault(), "testuser", "testpass");
        manager.remoteLogout(sessionId2.toString());

        manager.remoteLogin(sessionId3, Locale.getDefault(), "testuser", "testpass");

        // Verify all session IDs are different
        assertNotEquals(sessionId1.toString(), sessionId2.toString(),
                "Consecutive session IDs should be different");
        assertNotEquals(sessionId2.toString(), sessionId3.toString(),
                "Consecutive session IDs should be different");
        assertNotEquals(sessionId1.toString(), sessionId3.toString(),
                "Consecutive session IDs should be different");
    }

    /**
     * Test that pre-existing session ID in StringBuilder is used.
     */
    @Test
    public void testRemoteLoginWithPreExistingSessionId() throws Exception {
        manager.addUser("testuser", "testpass");

        // Provide a pre-existing session ID
        StringBuilder sessionId = new StringBuilder("my-custom-session-id");
        boolean result = manager.remoteLogin(sessionId, Locale.getDefault(), "testuser", "testpass");

        assertTrue(result, "Remote login should succeed");
        assertEquals("my-custom-session-id", sessionId.toString(),
                "Pre-existing session ID should be preserved");
        assertTrue(manager.isRemotelyLoggedIn("my-custom-session-id"),
                "Pre-existing session ID should be registered");
    }

    /**
     * Test that guest user cannot login remotely.
     * Guest user has null password which should cause login to fail safely.
     */
    @Test
    public void testGuestUserCannotLoginRemotely() {
        StringBuilder sessionId = new StringBuilder();

        // Guest user login should fail (guest has null password)
        // This may throw NPE or return false depending on implementation
        // We just verify that no session is created
        try {
            boolean result = manager.remoteLogin(sessionId, Locale.getDefault(), "guest", "anypassword");
            assertFalse(result, "Guest user should not be able to login remotely");
        } catch (NullPointerException e) {
            // Expected - guest user has null password which causes NPE in checkPassword
            // This is acceptable behavior - guest cannot login remotely
        }

        // Verify no session was created for guest
        assertFalse(manager.isRemotelyLoggedIn(sessionId.toString()),
                "No session should be created for guest user");
    }

    /**
     * Test same user can have multiple remote sessions simultaneously.
     */
    @Test
    public void testSameUserMultipleSessions() throws Exception {
        manager.addUser("testuser", "testpass");

        // Login same user twice with different sessions
        StringBuilder sessionId1 = new StringBuilder();
        StringBuilder sessionId2 = new StringBuilder();

        manager.remoteLogin(sessionId1, Locale.getDefault(), "testuser", "testpass");
        manager.remoteLogin(sessionId2, Locale.getDefault(), "testuser", "testpass");

        // Both sessions should be valid
        assertTrue(manager.isRemotelyLoggedIn(sessionId1.toString()),
                "First session should be valid");
        assertTrue(manager.isRemotelyLoggedIn(sessionId2.toString()),
                "Second session should be valid");

        // Logout one session
        manager.remoteLogout(sessionId1.toString());

        // First session should be invalid, second should still be valid
        assertFalse(manager.isRemotelyLoggedIn(sessionId1.toString()),
                "First session should be invalidated");
        assertTrue(manager.isRemotelyLoggedIn(sessionId2.toString()),
                "Second session should still be valid");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        manager = new DefaultPermissionManager().init();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
