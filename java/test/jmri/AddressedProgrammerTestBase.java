package jmri;

import java.util.List;
import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Base JUnit tests for the Operations Mode Programmers derived from
 * the AddressedProgrammer interface
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 * @author Paul Bender copyright (C) 2018 
 */
abstract public class AddressedProgrammerTestBase extends ProgrammerTestBase {

    @Test
    @Override
    public void testDefault() {
        Assume.assumeTrue(programmer instanceof AddressedProgrammer);
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBYTEMODE,
                programmer.getMode());        
    }

    @Test
    @Override
    public void testGetCanRead() {
        Assume.assumeTrue(programmer instanceof AddressedProgrammer);
        Assert.assertFalse("can read", programmer.getCanRead());
    }
    
    @Test(expected=java.lang.IllegalArgumentException.class)
    @Override
    public void testSetGetMode() {
        Assume.assumeTrue(programmer instanceof AddressedProgrammer);
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }

    @Test
    public void testGetLongAddress(){
        Assume.assumeTrue(programmer instanceof AddressedProgrammer);
        Assert.assertNotNull("long/short address boolean",((AddressedProgrammer)programmer).getLongAddress());
    }

    @Test
    public void testGetAddressNumber(){
        Assume.assumeTrue(programmer instanceof AddressedProgrammer);
        Assert.assertNotNull("Numeric Address",((AddressedProgrammer)programmer).getAddressNumber());
    }

    @Test
    public void testGetAddress(){
        Assume.assumeTrue(programmer instanceof AddressedProgrammer);
        Assert.assertNotNull("String Address",((AddressedProgrammer)programmer).getAddress());
    }

    
    // must set the value of programmer in setUp.
    @Before
    @Override
    abstract public void setUp();

}
