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
public class CombinedLocoSelPaneTest {

    @Test
    public void testCTor() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl,pmp);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.setDefault(ProgrammerConfigManager.class,new ProgrammerConfigManager());
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CombinedLocoSelPaneTest.class);

}
