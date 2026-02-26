package apps.InstallTest;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InstallTestTest {

    @Test
    @DisabledIfHeadless
    @Disabled("gives error message about an invalid profile on Travis")
    public void testCTor() {
        InstallTest t = new InstallTest();
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

    // private final static Logger log = LoggerFactory.getLogger(InstallTestTest.class);
}
