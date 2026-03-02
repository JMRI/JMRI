package apps.systemconsole;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SystemConsolePreferencesManagerTest {

    @Test
    public void testCTor() {
        SystemConsolePreferencesManager t = new SystemConsolePreferencesManager();
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

    // private final static Logger log = LoggerFactory.getLogger(SystemConsolePreferencesManagerTest.class);

}
