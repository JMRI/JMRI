package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Minimal test skeleton for BugReport class
 */
public class BugReportTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("BugReport constructor", new BugReport("title", "body", false, false, false));
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

