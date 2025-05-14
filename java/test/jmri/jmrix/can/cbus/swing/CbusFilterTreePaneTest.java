package jmri.jmrix.can.cbus.swing;

import java.io.File;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for CbusFilterFrame.
 * @author Steve Young Copyright(C) 2025
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusFilterTreePaneTest {

    @Test
    public void testTree() {

        assertNotNull(frame);
        frame.initComponents();

        ThreadingUtil.runOnGUI( () -> frame.setVisible(true));
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );

        sendAllOpcs(false, false);
        sendAllOpcs(false, true);


        JTreeOperator jto = new JTreeOperator(jfo);
        Assertions.assertNotNull(jto);

        ((jmri.util.swing.JCheckBoxTree)frame.ftp.cbt).treePathClicked(jto.getPathForRow(0), true);

        JSpinnerOperator jso = new JSpinnerOperator(jfo, 0); // min event
        jso.setValue(123);

        jso = new JSpinnerOperator(jfo, 1); // max event
        jso.setValue(456);

        sendAllOpcs(false, false);
        sendAllOpcs(false, true);

        ((jmri.util.swing.JCheckBoxTree)frame.ftp.cbt).treePathClicked(jto.getPathForRow(1), true);
        ((jmri.util.swing.JCheckBoxTree)frame.ftp.cbt).treePathClicked(jto.getPathForRow(2), true);
        JButtonOperator jbo = new JButtonOperator(jfo, Bundle.getMessage("ButtonResetCounts"));
        jbo.doClick();

        sendAllOpcs(false, false);
        sendAllOpcs(false, true);

        send200Nodes();

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    private void send200Nodes() {
        CanMessage m = new CanMessage(0x01);
        m.setNumDataElements(4);
        m.setElement(0, CbusConstants.CBUS_ACON);
        // m.setElement(2, nn);
        m.setElement(4, 77);
        for ( int i=1; i<200; i++) {
            m.setElement(2, i);
            frame.filter(m);
        }
    }

    private void sendAllOpcs( boolean shouldFilter, boolean reply) {
        for ( int i=0; i<= 0xFF; i++) {
            if ( shouldFilter ) {
                sendFrameOpc(i, reply);
            } else {
                sendFrameOpc(i, reply);
            }
        }
    }

    private boolean sendFrameOpc(int opc, boolean reply ) {
        if ( reply ) {
            CanReply r = new CanReply();
            r.setOpCode(opc);
            return frame.filter(r);
        } else {
            CanMessage r = new CanMessage(0x01);
            r.setOpCode(opc);
            return frame.filter(r);
        }
    }

    private CbusFilterFrame frame;
    private CbusFilterFrameTest.FtTestConsole _testConsole;

    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        JUnitUtil.setUp();
        Assertions.assertDoesNotThrow( () ->
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir)));
        _testConsole = new CbusFilterFrameTest.FtTestConsole();
        frame = new CbusFilterFrame(_testConsole,null);
    }

    @AfterEach
    public void tearDown() {
        if( _testConsole !=null){
            _testConsole.dispose();
        }
        if ( frame != null ) {
            frame.setDefaultCloseOperation(CbusFilterFrame.DISPOSE_ON_CLOSE);
            JUnitUtil.dispose(frame);
            frame = null;
        }
        JUnitUtil.tearDown();
    }

}
