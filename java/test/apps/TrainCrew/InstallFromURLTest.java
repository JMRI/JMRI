package apps.TrainCrew;

import org.junit.jupiter.api.*;

/**
 * Tests for the InstallFromURL class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class InstallFromURLTest  {

    @Test
    public void testCtor() {
        InstallFromURL t = new InstallFromURL();
        Assertions.assertNotNull(t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
