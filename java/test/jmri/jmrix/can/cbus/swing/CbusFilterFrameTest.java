package jmri.jmrix.can.cbus.swing;


import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusFilterType;
import jmri.util.JUnitUtil;
// import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusFilterFrameTest extends jmri.util.JmriJFrameTestBase {

    protected static class FtTestConsole extends jmri.jmrix.can.cbus.swing.console.CbusConsolePane {

        private final ArrayList<String> stringOutputList;

        protected FtTestConsole() {
            super();
            stringOutputList = new ArrayList<>();
        }

        @Override
        public void nextLine(String lineOne, String lineTwo, int filterId){ 
            stringOutputList.add(lineOne);
        }

        protected ArrayList<String> getStringOutputList() {
            return stringOutputList;
        }
    }

    private FtTestConsole _testConsole;

    @Test
    public void testCanFrames(){
        frame.initComponents();
        assertNotNull(frame);

        frame.setVisible(true);
        // ThreadingUtil.runOnGUI( () -> frame.setVisible(true));



        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JTreeOperator jto = new JTreeOperator(jfo);
        Assertions.assertNotNull(jto);

        CbusFilterFrame cff = (CbusFilterFrame) frame;
        
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTON);
        assertFalse(cff.filter(m));

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_TON);
        assertFalse(cff.filter(r));
        
         // Filter Incoming
        ((jmri.util.swing.JCheckBoxTree)cff.ftp.cbt).treePathClicked(jto.getPathForRow(1), true);

        assertTrue(cff.filter(r));
        assertFalse(cff.filter(m));

        // Pass Incoming, Filter Outgoing
        ((jmri.util.swing.JCheckBoxTree)cff.ftp.cbt).treePathClicked(jto.getPathForRow(1), true);
        ((jmri.util.swing.JCheckBoxTree)cff.ftp.cbt).treePathClicked(jto.getPathForRow(2), true);

        assertTrue(cff.filter(m));
        assertFalse(cff.filter(r));

        assertFalse(cff.ftp.filter.isFilterActive(CbusFilterType.CFEVENT.ordinal()));

        // All Events Filtered
        ((jmri.util.swing.JCheckBoxTree)cff.ftp.cbt).treePathClicked(jto.getPathForRow(3), true);
        assertTrue(cff.ftp.filter.isFilterActive(CbusFilterType.CFEVENT.ordinal()));

        // All Events Passed
        ((jmri.util.swing.JCheckBoxTree)cff.ftp.cbt).treePathClicked(jto.getPathForRow(3), true);

        assertFalse(cff.ftp.filter.isFilterActive(CbusFilterType.CFEVENT.ordinal()));

        jto.expandRow(3);
        jto.getQueueTool().waitEmpty();

        // Minimum event number filter
        ((jmri.util.swing.JCheckBoxTree)cff.ftp.cbt).treePathClicked(jto.getPathForRow(4), true);

        JSpinnerOperator spinner = new JSpinnerOperator(jfo, 0);
        JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
        assertEquals( "0", jtfo.getText(),"original Min Event 0");

        jtfo.setText("123");

        // Outgoing event
        CanReply mEvent = new CanReply(0x12);
        mEvent.setNumDataElements(5);
        mEvent.setElement(0, CbusConstants.CBUS_ACON);
        mEvent.setElement(1, 0x01); // Node 257
        mEvent.setElement(2, 0x01); // Node 257
        mEvent.setElement(3, 0x00); // Event > 123
        mEvent.setElement(4, 0xff); // Event > 123

        assertFalse(cff.filter(mEvent));

        mEvent.setElement(4, 0x01); // Event < 123
        assertTrue(cff.filter(mEvent));

        // JUnitUtil.dispose(jfo.getWindow());

      //  assertEquals("Filter ( 1 / 1 ) ",
       //     new JToggleButtonOperator(jfo,4).getText(),"text says pass");

        // JUnitUtil.dispose(jfo.getWindow());
        // jfo.waitClosed();

        // frame.dispose();
        
        
    }

    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;

    @TempDir
    protected File tempDir;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        } catch ( IOException ex ) {
            Assertions.fail("Could not init new Null Profile", ex);
        }
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        _testConsole = new FtTestConsole();
        _testConsole.initComponents(memo, false);
        frame = new CbusFilterFrame(_testConsole,null);
    }

    @AfterEach
    @Override
    public void tearDown() {

        // JFrameOperator jfo = new JFrameOperator(frame);
        // jfo.waitClosed();
        

        if( _testConsole !=null ){
            _testConsole.dispose();
        }

        if(frame!=null) {
           JUnitUtil.dispose(frame);
        }
        frame = null;
        // JUnitUtil.resetWindows(true,true);

        tc.terminateThreads();
        memo.dispose();

        JUnitUtil.tearDown();

        // super.tearDown(); // disposes frame, calls JUnitUtil.shutDown
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventFilterTest.class);

}
