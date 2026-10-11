package apps.util.issuereporter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Minimal test skeleton for EnhancementRequest class
 */
public class EnhancementRequestTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new EnhancementRequest("title", "body"),
            "EnhancementRequest constructor");
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

