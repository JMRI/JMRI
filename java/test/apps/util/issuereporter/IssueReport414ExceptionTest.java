package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Minimal test skeleton for IssueReport414Exception class
 */
public class IssueReport414ExceptionTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new IssueReport414Exception(),
            "IssueReport414Exception constructor");
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

