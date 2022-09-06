package jmri.server.web.spi;

import javax.annotation.CheckForNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class AngularRouteTest {

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

    @Test
    public void testGetConstructor() throws Exception {

        Exception exc = assertThrows(NullPointerException.class, () -> {
            checkCtorThrowsException(null,"b","c","d");
        } );
        assertNotNull(exc);

        exc = assertThrows(IllegalArgumentException.class, () -> {
            checkCtorThrowsException("a", "b", "c", "d");
        } );
        assertNotNull(exc);

        exc = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            assertNotNull(new AngularRoute("a", null, null, null));
        } );
        assertNotNull(exc);

        exc = assertThrows(IllegalArgumentException.class, () -> {
            assertNotNull(new AngularRoute("a", null, "c", "d"));
        } );
        assertNotNull(exc);

        exc = assertThrows(IllegalArgumentException.class, () -> {
            assertNotNull(new AngularRoute("a", "b", null, "d"));
        } );
        assertNotNull(exc);

        Assertions.assertDoesNotThrow(() -> {
            assertNotNull(new AngularRoute("a", null, null, "d"));
        } );

        Assertions.assertDoesNotThrow(() -> {
            assertNotNull(new AngularRoute("a", "b", "c", null));
        } );

    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = {"NP_NULL_PARAM_DEREF_NONVIRTUAL","NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE"},
        justification = "testing exception when null passed")
    private void checkCtorThrowsException(@CheckForNull String a, @CheckForNull String b, @CheckForNull String c, @CheckForNull String d) throws Exception {
        Assertions.assertNotNull(new AngularRoute(a, b, c, d));
        Assertions.fail("Should have thrown exception");
    }

    @Test
    public void testGetRedirection() {
        AngularRoute ar = new AngularRoute("a", null, null, "d");
        assertEquals("d", ar.getRedirection());
        ar = new AngularRoute("a", "b", "c", null);
        assertNull(ar.getRedirection());
    }

    @Test
    public void testGetWhen() {
        AngularRoute ar = new AngularRoute("a", null, null, "d");
        assertEquals("a", ar.getWhen());
    }

    @Test
    public void testGetTemplate() {
        AngularRoute ar = new AngularRoute("a", null, null, "d");
        assertNull(ar.getTemplate());
        ar = new AngularRoute("a", "b", "c", null);
        assertEquals("b", ar.getTemplate());
    }

    @Test
    public void testGetController() {
        AngularRoute ar = new AngularRoute("a", null, null, "d");
        assertNull(ar.getController());
        ar = new AngularRoute("a", "b", "c", null);
        assertEquals("c", ar.getController());
    }

}
