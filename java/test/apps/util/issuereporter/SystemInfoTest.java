package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Minimal test skeleton for SystemInfo class
 */
public class SystemInfoTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SystemInfo constructor", new SystemInfo(true));
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

