package apps.util.issuereporter.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Minimal test skeleton for IssueReporter class
 */
public class IssueReporterTest {

    @Test
    public void testCtor(){
      Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
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

