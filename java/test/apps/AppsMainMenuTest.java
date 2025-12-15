package apps;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Dave Sand Copyright (C) 2021
 */
public class AppsMainMenuTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        AppsMainMenu t = new AppsMainMenu();
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AppsMainMenuTest.class);
}
