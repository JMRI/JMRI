package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
        assumeTrue(programmer instanceof AddressedProgrammer);
        assertEquals( ProgrammingMode.OPSBYTEMODE, programmer.getMode(),
            "Check Default");
    }

    @Test
    @Override
    public void testGetCanRead() {
        assumeTrue(programmer instanceof AddressedProgrammer);
        assertFalse( programmer.getCanRead(), "can read");
    }
    
    @Test
    @Override
    public void testSetGetMode() {
        assumeTrue(programmer instanceof AddressedProgrammer);
        var ex = assertThrows(IllegalArgumentException.class, () ->
            programmer.setMode(ProgrammingMode.REGISTERMODE));
        assertNotNull(ex);
    }

    @Test
    public void testGetLongAddress(){
        assumeTrue(programmer instanceof AddressedProgrammer);
        assertNotNull(((AddressedProgrammer)programmer).getLongAddress(), "long/short address boolean");
    }

    @Test
    public void testGetAddressNumber(){
        assumeTrue(programmer instanceof AddressedProgrammer);
        assertNotNull( ((AddressedProgrammer)programmer).getAddressNumber(), "Numeric Address");
    }

    @Test
    public void testGetAddress(){
        assumeTrue(programmer instanceof AddressedProgrammer);
        assertNotNull( ((AddressedProgrammer)programmer).getAddress(), "String Address");
    }

    // must set the value of programmer in setUp.
    @Override
    abstract public void setUp();

}
