package apps.gui3.tabbedpreferences;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TabbedPreferencesActionTest {

    @Test
    public void testCTor() {
        TabbedPreferencesAction t = new TabbedPreferencesAction();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesActionTest.class);

}
