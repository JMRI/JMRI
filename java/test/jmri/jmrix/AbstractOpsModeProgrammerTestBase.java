package jmri.jmrix;

import jmri.ProgrammingMode;

import org.junit.jupiter.api.*;

/**
 * Base JUnit tests for the Operations Mode Programmers derived from
 * the AbstractProgammer class
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 * @author Paul Bender copyright (C) 2018 
 */
abstract public class AbstractOpsModeProgrammerTestBase extends jmri.AddressedProgrammerTestBase {

    @Test
    public void testDefaultViaBestMode() {
        Assertions.assertEquals( ProgrammingMode.OPSBYTEMODE,
            ((AbstractProgrammer)programmer).getBestMode(),"Check Default");        
    }

    @Override
    @Test
    public void testGetCanRead() {
        Assertions.assertFalse( programmer.getCanRead(),"can read");
    }
    
    @Override
    @Test
    public void testSetGetMode() {
        IllegalArgumentException iae = Assertions.assertThrows(
            IllegalArgumentException.class, () ->
                programmer.setMode(ProgrammingMode.REGISTERMODE));
        Assertions.assertNotNull(iae);
    }
    
    // must set the value of programmer in setUp.
    @Override
    abstract public void setUp();

}
