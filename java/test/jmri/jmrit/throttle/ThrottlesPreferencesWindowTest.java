package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of ThrottlesPreferencesWindow
 *
 * @author Lionel Jeanson
 */
public class ThrottlesPreferencesWindowTest {

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCtor() {
        try {
            ThrottlesPreferencesWindow w = new ThrottlesPreferencesWindow("ThrottlesPreferencesWindowTest");
            Assert.assertNotNull("exists", w);
        } catch (IndexOutOfBoundsException e) {
            Assert.fail("IndexOutOfBoundsException\n"+e);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();

    }
}

