package jmri.jmrix.loconet.sdf;

import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.sdf.InitiateSound class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class InitiateSoundTest {

    @Test
    public void testCtor() {
        new InitiateSound((byte) 0, (byte) 0);
    }

}
