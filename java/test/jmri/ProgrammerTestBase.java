package jmri;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    public void testGetCanWrite() {
        Assert.assertTrue("can write", programmer.getCanWrite());
    }

    @Test
    public void testGetCanReadAddress() {
        Assert.assertFalse("can read address", programmer.getCanRead("1234"));
    }

    @Test
    public void testGetCanWriteAddress() {
        Assert.assertTrue("can write address", programmer.getCanWrite("1234"));
    }   
 
    @Test
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }
    
    @Test
    public void testSetModeNull() {
        Assert.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(null));
    }

    @Test
    public void testGetWriteConfirmMode(){
        Assert.assertEquals("Write Confirm Mode",Programmer.WriteConfirmMode.NotVerified,
                programmer.getWriteConfirmMode("1234"));
    }

    @Test
    public void testWriteCVNullListener() throws jmri.ProgrammerException {
                programmer.writeCV("1",42,null);
    }
    
    @BeforeEach
    abstract public void setUp();

    @AfterEach
    abstract public void tearDown();

}
