package jmri.jmrit.roster;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class PrintRosterActionTest {

    @Test
    public void testCTor() {
        JmriJFrame jf = new JmriJFrame("TestPrintWindow");
        jmri.util.swing.WindowInterface wi = jf;
        PrintRosterAction t = new PrintRosterAction("test print roster",wi);
        Assert.assertNotNull("exists",t);

        // add a RosterEntry
        RosterEntry r = RosterEntryImplementations.id1();
        Roster.getDefault().addEntry(r);

        t.setPreview(true);

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            t.actionPerformed(null);
        } );
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleDecoderProRoster")+" "+Bundle.getMessage("ALLENTRIES"));
        Assert.assertNotNull(jfo);
        jfo.requestClose();
        jfo.waitClosed();

        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintRosterActionTest.class);

}
