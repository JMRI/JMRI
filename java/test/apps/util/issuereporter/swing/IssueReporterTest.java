package apps.util.issuereporter.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Minimal test skeleton for IssueReporter class
 */
public class IssueReporterTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("IssueReporter constructor", new IssueReporter());
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

