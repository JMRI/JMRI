package jmri.jmrix;

import java.util.List;
import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.*;

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
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }
    
    // must set the value of programmer in setUp.
    @Before
    @Override
    abstract public void setUp();

}
