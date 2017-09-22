package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JLabel;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DccAddressPanelTest {

    @Test
    public void testCTor() {
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.ProgrammerManager.class).getGlobalProgrammer();
        VariableTableModel vtm = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                new CvTableModel(new JLabel(""), p)
        );
        DccAddressPanel t = new DccAddressPanel(vtm);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DccAddressPanelTest.class.getName());

}
