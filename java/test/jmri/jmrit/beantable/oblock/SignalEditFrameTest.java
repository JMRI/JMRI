package jmri.jmrit.beantable.oblock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.InstanceManager;
import jmri.implementation.VirtualSignalMast;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SignalEditFrameTest {

    @Test
    public void testCTor() {
        SignalEditFrame sef = new SignalEditFrame(
                "Edit Signal-1",
                null,
                null,
                null);
        assertNotNull(sef, "New SEF exists");
        JUnitUtil.dispose(sef);
    }

    @Test
    public void testCTorSignal() {
        VirtualSignalMast m = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "mast1");
        TableFrames tf = new TableFrames();
        SignalTableModel model = new SignalTableModel(tf);
        Portal p1 = new Portal("OP1");
        OBlock b1 = new OBlock("OB1");
        OBlock b2 = new OBlock("OB2");
        SignalTableModel.SignalRow sr = new SignalTableModel.SignalRow(m, b1, p1, b2, 0.0f, false);

        SignalEditFrame sef = new SignalEditFrame("Edit mast1", m, sr, model);
        assertNotNull(sef, "Mast SEF exists");
        sef.initComponents();
        JUnitUtil.dispose(sef);
    }

    @Test
    public void testOpenThenOkSignalEditFrame() {

        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        OBlockManager oBlockMgr = InstanceManager.getDefault(OBlockManager.class);

        VirtualSignalMast m = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "mast1");
        TableFrames tf = new TableFrames();
        SignalTableModel model = new SignalTableModel(tf);
        Portal p1 = portalMgr.providePortal("OP1");
        portalMgr.providePortal("OP2");
        OBlock b1 = oBlockMgr.provide("OB1");
        OBlock b2 = oBlockMgr.provide("OB2");
        SignalTableModel.SignalRow sr = new SignalTableModel.SignalRow(m, b1, p1, b2, 0.0f, false);

        SignalEditFrame t = new SignalEditFrame("Edit mast2", m, sr, model);
        assertNotNull(t, "Mast SEF exists");

        t.initComponents();

        ThreadingUtil.runOnGUI( () -> t.setVisible(true) );
        JFrameOperator jfo = new JFrameOperator(t.getTitle());
        assertNotNull(jfo);

        JButtonOperator jbo = new JButtonOperator(jfo, Bundle.getMessage("ButtonOK"));
        jbo.doClick();
        jfo.waitClosed();

        oBlockMgr.dispose();

        JUnitAppender.assertWarnMessage("Portal OP1 needs an OBlock on each side");
        JUnitAppender.assertWarnMessage("Portal OP2 needs an OBlock on each side");
        JUnitAppender.assertWarnMessage("Portal OP1 needs an OBlock on each side");
        JUnitAppender.assertWarnMessage("Portal OP2 needs an OBlock on each side");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalEditFrameTest.class);

}
