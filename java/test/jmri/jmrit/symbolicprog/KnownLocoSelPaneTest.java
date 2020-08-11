package jmri.jmrit.symbolicprog;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.progsupport.ProgModePane;

import javax.swing.JLabel;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class KnownLocoSelPaneTest {

    @Test
    public void testCTor() {
        JLabel jl = new JLabel("test selector");
        ProgModePane pmp = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
        KnownLocoSelPane t = new KnownLocoSelPane(jl, false, pmp){
            protected void startProgrammer(DecoderFile decoderFile, RosterEntry r,
                                        String programmerName) {
                log.error("Should have not been invoked, even in test");
            }
            
        };
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testBooleanCTor() {
        KnownLocoSelPane t = new KnownLocoSelPane(false){
            protected void startProgrammer(DecoderFile decoderFile, RosterEntry r,
                                        String programmerName) {
                log.error("Should have not been invoked, even in test");
            }
            
        };
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
        jmri.util.JUnitUtil.tearDown();

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KnownLocoSelPaneTest.class);
}
