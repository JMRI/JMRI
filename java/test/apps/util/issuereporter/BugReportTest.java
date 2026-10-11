package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Minimal test skeleton for BugReport class
 */
public class BugReportTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new BugReport("title", "body", false, false, false),
            "BugReport constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

