package jmri.jmrix.can.cbus.swing.configtool;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class ConfigToolPaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testInitComp() {

        assertEquals( 0,tcis.numListeners(), "no listener to start with");

        ((ConfigToolPane)panel).initComponents(memo);

        assertNotNull(tcis);
        assertEquals( 1, tcis.numListeners(), "listening");

        assertNotNull( panel, "exists");
        assertEquals( "CAN " + Bundle.getMessage("CapConfigTitle"),panel.getTitle(), "name with memo");


        // check pane has loaded something
        JmriJFrame f = new JmriJFrame(panel.getTitle());
        f.add(panel);

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            f.pack();
            f.setVisible(true);
        });

        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        assertTrue(getResetButtonEnabled(jfo));

        assertEquals( "",getStringCaptureOne(jfo), "nothing in capture slot 1" );
        assertEquals( "",getStringCaptureTwo(jfo), "nothing in capture slot 2" );

        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x91); // ACOF OPC
        m.setElement(1, 0xd4); // nn
        m.setElement(2, 0x31); // nn
        m.setElement(3, 0x30); // en
        m.setElement(4, 0x39); // en

        ((ConfigToolPane)panel).message(m);
        assertEquals("-n54321e12345",getStringCaptureOne(jfo), "event in capture slot 1");

        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x98); // ASON OPC
        r.setElement(1, 0x00); // nn 0
        r.setElement(2, 0x00); // nn 0
        r.setElement(3, 0xff); // en
        r.setElement(4, 0x39); // en

        ((ConfigToolPane)panel).reply(r);
        assertEquals( "+65337",getStringCaptureTwo(jfo), "event in capture slot 2");

        // Ask to close window
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
    }

    private boolean getResetButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ButtonResetCapture")).isEnabled() );
    }

    private String getStringCaptureOne( JFrameOperator jfo ){
        return ( new JTextFieldOperator(jfo,0).getText() );
    }

    private String getStringCaptureTwo( JFrameOperator jfo ){
        return ( new JTextFieldOperator(jfo,1).getText() );
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        title = Bundle.getMessage("CapConfigTitle");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.configtool.ConfigToolFrame";
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);

        panel = new ConfigToolPane();
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel.dispose();
        panel = null;
        assertNotNull(tcis);
        assertEquals( 0,tcis.numListeners(), "no listener after dispose");
        tcis.terminateThreads();
        assertNotNull(memo);
        memo.dispose();
        tcis = null;
        memo = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConfigToolPaneTest.class);

}
