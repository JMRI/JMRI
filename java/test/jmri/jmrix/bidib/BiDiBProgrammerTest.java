package jmri.jmrix.bidib;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBProgrammer class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBProgrammerTest  extends jmri.jmrix.AbstractProgrammerTest {
    
    private BiDiBSystemConnectionMemo memo;
    
    // should be revised - which modes are really available with BiDiB..
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                ((BiDiBProgrammer)programmer).getBestMode());  
    }

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                programmer.getMode());        
    }

    @Test
    @Override
    public void testSetGetMode() {
        
        Assertions.assertThatThrownBy(() -> programmer.setMode(ProgrammingMode.REGISTERMODE)).isInstanceOf(IllegalArgumentException.class);
        
        Assert.assertEquals("Check mode matches set", ProgrammingMode.DIRECTBYTEMODE,
                programmer.getMode());        
    }

    @Override
    @Test
    public void testGetCanWriteAddress() {
        Assert.assertFalse("can write address", programmer.getCanWrite("1234"));
    }    

    @Override
    @Test
    public void testGetWriteConfirmMode(){
        Assert.assertEquals("Write Confirm Mode",jmri.Programmer.WriteConfirmMode.DecoderReply,
                programmer.getWriteConfirmMode("1234"));
    }

    @Override
    @Test
    @Disabled("we can't make a BidibNode which does not execute synchroneous BiDiB commands")
    public void testWriteCVNullListener() throws jmri.ProgrammerException {
        //programmer.writeCV("1",42,null); //not possible since we can't make a BidibNode which does not execute synchroneous BiDiB commands
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        
        programmer = new BiDiBProgrammer(memo.getBiDiBTrafficController());
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        //programmer = null;
        JUnitUtil.tearDown();
    }

}
