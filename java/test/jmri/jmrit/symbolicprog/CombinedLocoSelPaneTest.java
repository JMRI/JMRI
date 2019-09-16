package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.progsupport.ProgModePane;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CombinedLocoSelPaneTest {

    @Test
    public void testCTor() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testLayoutRosterSelection() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        JPanel panel = t.layoutRosterSelection();
        Assert.assertNotNull("Roster Selection Panel Created", panel);
    }

    @Test
    public void testCreateProgrammerSelection() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        JPanel panel = t.createProgrammerSelection();
        Assert.assertNotNull("Programmer Selection Panel Created", panel);
    }

    @Test
    public void testSelectLocoAddressNotInRoster() {
        // This method was put in place to catch a missing resource bundle
        // key. in the selectLoco method.
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        t.selectLoco(1234);
        JUnitAppender.assertWarnMessage("Read address 1234, but no such loco in roster");
    }

    @Test
    public void testStartIdentifyLoco() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        t.startIdentifyLoco();
        JUnitAppender.assertWarnMessage("Selector did not provide a programmer, use default");
    }

    @Test
    public void testStartIdentifyDecoder() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        t.startIdentifyDecoder();
        JUnitAppender.assertWarnMessage("Selector did not provide a programmer, use default");
    }

    @Test
    public void testSelectDecoder() {
        // This method was put in place to catch a missing resource bundle
        // key. in the selectLoco method.
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        CombinedLocoSelPane t = new CombinedLocoSelPane(jl, pmp);
        t.selectDecoder(13, 123, -1);
        JUnitAppender.assertWarnMessage("Found mfg 13 (Public-domain and DIY) version 123; no such decoder defined");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        jmri.InstanceManager.setDefault(ProgrammerConfigManager.class, new ProgrammerConfigManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CombinedLocoSelPaneTest.class);
}
