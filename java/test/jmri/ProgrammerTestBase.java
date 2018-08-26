package jmri;

import java.util.List;
import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the Programmer interface
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 */
abstract public class ProgrammerTestBase {

    protected Programmer programmer;

    @Test
    public void testCtor() {
        Assert.assertNotNull(programmer);
    }

    @Test
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTMODE,
                programmer.getMode());        
    }

    @Test
    public void testGetCanRead() {
        Assert.assertTrue("can read", programmer.getCanRead());
    }
    
    @Test
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testSetModeNull() {
        programmer.setMode(null);
    }
    
    // The minimal setup for log4J
    @Before
    abstract public void setUp();

    @After
    abstract public void tearDown();

}
