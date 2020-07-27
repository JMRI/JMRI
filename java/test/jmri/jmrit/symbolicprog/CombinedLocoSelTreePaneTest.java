package jmri.jmrit.symbolicprog;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.progsupport.ProgModePane;

import javax.swing.JLabel;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CombinedLocoSelTreePaneTest {

    @Test
    public void testCTor() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelTreePane t = new CombinedLocoSelTreePane(jl, pmp);
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ProgrammerConfigManager.class, new ProgrammerConfigManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CombinedLocoSelTreePaneTest.class);
}
