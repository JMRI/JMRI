package jmri.jmrix.loconet.hexfile;

import java.awt.GraphicsEnvironment;

import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HexFileFrameTest {

    @Test
    @SuppressWarnings("deprecation")        // Thread.stop()
    public void testCTor() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnHexFilePort p = new LnHexFilePort();

        HexFileFrame f = new HexFileFrame();

        ThreadingUtil.runOnGUI( ()-> {
            f.setAdapter(p);
            f.initComponents();
            f.configure();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assertions.assertNotNull(jfo);

        ThreadingUtil.runOnGUI( ()-> {
            f.dispose();
       });

        p.dispose();
        f.sourceThread.stop();
        f.sourceThread.join();
        f.packets.terminateThreads();
        f.dispose();
 }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("LnPowerManager LnTrackStatusUpdateThread");
        JUnitUtil.removeMatchingThreads("LnSensorUpdateThread");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HexFileFrameTest.class);

}
