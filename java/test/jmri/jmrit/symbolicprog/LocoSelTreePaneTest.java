package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.progsupport.ProgModePane;
import javax.swing.JLabel;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoSelTreePaneTest {

    @Test
    public void testCTor() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        LocoSelTreePane t = new LocoSelTreePane(jl,pmp);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(ProgrammerConfigManager.class,new ProgrammerConfigManager());
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(LocoSelTreePaneTest.class);

}
