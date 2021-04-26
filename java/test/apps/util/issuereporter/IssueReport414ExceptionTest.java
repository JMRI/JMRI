package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Minimal test skeleton for IssueReport414Exception class
 */
public class IssueReport414ExceptionTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("IssueReport414Exception constructor", new IssueReport414Exception());
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

