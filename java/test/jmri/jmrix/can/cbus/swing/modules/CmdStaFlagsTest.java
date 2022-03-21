package jmri.jmrix.can.cbus.swing.modules;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of Cbus Modules Common Code
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CmdStaFlagsTest {
    
    protected UpdateNV _update;
    String [] flags = {"flag one", "flag two", "flag three", "flag four", "flag five", "flag six", "flag seven", "flag eight"};
    String [] tooltips = {"tt one", "tt two", "tt three", "tt four", "tt five", "tt six", "tt seven", "tt eight"};
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCtor() {

        CmdStaFlags t = new CmdStaFlags(3, "Title", flags, tooltips, _update);
        Assert.assertNotNull("exists",t);
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
