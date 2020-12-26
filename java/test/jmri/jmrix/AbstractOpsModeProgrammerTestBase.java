package jmri.jmrix;

import jmri.ProgrammingMode;

import org.junit.Assert;
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
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBYTEMODE,
                ((AbstractProgrammer)programmer).getBestMode());        
    }

    @Override
    @Test
    public void testGetCanRead() {
        Assert.assertFalse("can read", programmer.getCanRead());
    }
    
    @Override
    @Test
    public void testSetGetMode() {
        Assert.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(ProgrammingMode.REGISTERMODE));
    }
    
    // must set the value of programmer in setUp.
    @BeforeEach
    @Override
    abstract public void setUp();

}
