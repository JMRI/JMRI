package jmri.jmrit.throttle;

import javax.swing.JInternalFrame;

import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WindowPreferencesTest {

    @Test
    public void testGetPreferencesJInternalFrame() {

        JInternalFrame f = new JInternalFrame("my_Frame");
        Element e = WindowPreferences.getPreferences(f);
        Assertions.assertNotNull(e, "exists");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WindowPreferencesTest.class);
}
