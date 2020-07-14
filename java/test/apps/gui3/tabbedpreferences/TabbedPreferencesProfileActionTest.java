package apps.gui3.tabbedpreferences;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TabbedPreferencesProfileActionTest {

    @Test
    public void testCTor() {
        TabbedPreferencesProfileAction t = new TabbedPreferencesProfileAction();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesProfileActionTest.class);

}
