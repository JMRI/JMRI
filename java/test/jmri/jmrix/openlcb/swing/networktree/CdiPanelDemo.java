package jmri.jmrix.openlcb.swing.networktree;

import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromFile;
import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromSample;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.InstanceManager;
import jmri.jmrix.openlcb.OlcbEventNameStore;
import jmri.jmrix.openlcb.SampleFactory;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JFrameOperator;

import org.openlcb.cdi.swing.CdiPanel;

/**
 * NOTE: This file actually Demonstrates the openLCB CdiPanel class.
 *
 * @author Bob Jacobsen Copyright 2012
 *
 */
public class CdiPanelDemo {

    private CdiPanel m;
    private OlcbEventNameStore ens;

    @Test
    public void testCtor() {
        m = new CdiPanel();
        Assert.assertNotNull(m);
    }

    @Test
    @DisabledIfHeadless
    public void testDisplay() {
        JFrame f = new JFrame();
        f.setTitle("Configuration Demonstration");
        m = new CdiPanel();

        var rep = demoRepFromSample(SampleFactory.getBasicSample());
        ens = new OlcbEventNameStore();
        rep.eventNameStore = ens;

        m.initComponents( rep,
                new CdiPanel.GuiItemFactory() {
            @Override
            public JButton handleReadButton(JButton button) {
                //System.out.println("process button");
                button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                return button;
            }
        }
        );

        f.add(m);

        // show
        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });
        new QueueTool().waitEmpty(); // pause for CDI to render
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assertions.assertNotNull(jfo);

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testDisplaySample1() {
        JFrame f = makeFrameFromFile("java/test/jmri/jmrix/openlcb/sample1.xml");
        f.setTitle("Sample1 XML");
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(true);
        });
        new QueueTool().waitEmpty(); // pause for CDI to render
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assertions.assertNotNull(jfo);
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testDisplaySample2() {
        JFrame f = makeFrameFromFile("java/test/jmri/jmrix/openlcb/sample2.xml");
        f.setTitle("Sample 2 XML");
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(true);
        });
        new QueueTool().waitEmpty(); // pause for CDI to render
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assertions.assertNotNull(jfo);
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testLocoCdiDisplay() {
        JFrame f = makeFrameFromFile("java/test/jmri/jmrix/openlcb/NMRAnetDatabaseTrainNode.xml");
        f.setTitle("Locomotive CDI Demonstration");
        ThreadingUtil.runOnGUI( () -> f.setVisible(true));
        new QueueTool().waitEmpty(); // pause for CDI to render
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assertions.assertNotNull(jfo);
        JUnitUtil.dispose(f);
    }

    JFrame makeFrameFromFile(String fileName) {
        JFrame f = new JFrame();
        m = new CdiPanel();

        var rep = demoRepFromFile(new File(fileName));
        ens = new OlcbEventNameStore();
        rep.eventNameStore = ens;

        m.initComponents( rep,
                new CdiPanel.GuiItemFactory() {
            @Override
            public JButton handleReadButton(JButton button) {
                button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                return button;
            }
        }
        );

        f.add(m);

        f.pack();
        return f;
    }

    @BeforeAll
    static public void checkSeparate() {
       // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        if ( ens != null ) {
            ens.deregisterShutdownTask();
            ens = null;
        }
        if ( m != null ) {
            m.release();
        }
        m = null;
        InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        JUnitUtil.tearDown();
    }

    public static void main(String args[]) throws InterruptedException {
        var tests = new CdiPanelDemo();

        tests.setUp();
        tests.testDisplaySample1();

        Thread.sleep(360000);

        tests.tearDown();
    }
}
