package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Minimal test skeleton for SystemInfo class
 */
public class SystemInfoTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new SystemInfo(true),
            "SystemInfo constructor");
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

