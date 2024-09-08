package jmri.jmrix.loconet.sdf;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.sdf.InitiateSound class.
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class InitiateSoundTest {

    @Test
    public void testInitiateSoundCtor() {
        Assertions.assertNotNull( new InitiateSound((byte) 0, (byte) 0) );
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
