package jmri;

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
        Assertions.assertNotNull(programmer);
    }

    @Test
    public void testDefault() {
        Assertions.assertEquals( ProgrammingMode.DIRECTMODE,
            programmer.getMode(), "Check Default");
    }

    @Test
    public void testGetCanRead() {
        Assertions.assertTrue( programmer.getCanRead(), "can read");
    }

    @Test
    public void testGetCanWrite() {
        Assertions.assertTrue( programmer.getCanWrite(), "can write");
    }

    @Test
    public void testGetCanReadAddress() {
        Assertions.assertFalse( programmer.getCanRead("1234"), "can read address");
    }

    @Test
    public void testGetCanWriteAddress() {
        Assertions.assertTrue( programmer.getCanWrite("1234"), "can write address");
    }
 
    @Test
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assertions.assertEquals( ProgrammingMode.REGISTERMODE,
            programmer.getMode(), "Check mode matches set");
    }

    @Test
    public void testSetModeNull() {
        Throwable throwable = Assertions.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(null));
        Assertions.assertNotNull(throwable.getMessage());
    }

    @Test
    public void testGetWriteConfirmMode(){
        Assertions.assertEquals( Programmer.WriteConfirmMode.NotVerified,
            programmer.getWriteConfirmMode("1234"), "Write Confirm Mode");
    }

    @Test
    public void testWriteCVNullListener() throws jmri.ProgrammerException {
        programmer.writeCV("1",42,null);
    }

    abstract public void setUp();

    abstract public void tearDown();

}
