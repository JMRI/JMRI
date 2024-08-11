package jmri.jmrix.openlcb.swing.networktree;

import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromFile;
import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromSample;

import java.awt.GraphicsEnvironment;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.jmrix.openlcb.SampleFactory;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.openlcb.cdi.swing.CdiPanel;

/**
 * NOTE: This file actually Demonstrates the openLCB CdiPanel class.
 *
 * @author Bob Jacobsen Copyright 2012
 *
 */
public class CdiPanelDemo {

    @Test
    public void testCtor() {
        CdiPanel m = new CdiPanel();
        Assert.assertNotNull(m);
    }

    @Test
    public void testDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = new JFrame();
        f.setTitle("Configuration Demonstration");
        CdiPanel m = new CdiPanel();

        m.initComponents(demoRepFromSample(SampleFactory.getBasicSample()),
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
        f.pack();
        f.setVisible(true);
        f.dispose();
    }

    @Test
    public void testDisplaySample1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = makeFrameFromFile("java/test/jmri/jmrix/openlcb/sample1.xml");
        f.setTitle("Sample1 XML");
        f.setVisible(true);
        f.dispose();
    }

    @Test
    public void testDisplaySample2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = makeFrameFromFile("java/test/jmri/jmrix/openlcb/sample2.xml");
        f.setTitle("Sample 2 XML");
        f.setVisible(true);
        f.dispose();
    }

    @Test
    public void testLocoCdiDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = makeFrameFromFile("java/test/jmri/jmrix/openlcb/NMRAnetDatabaseTrainNode.xml");
        f.setTitle("Locomotive CDI Demonstration");
        f.setVisible(true);
        f.dispose();
    }

    JFrame makeFrameFromFile(String fileName) {
        JFrame f = new JFrame();
        CdiPanel m = new CdiPanel();

        m.initComponents(demoRepFromFile(new File(fileName)),
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
        jmri.util.JUnitUtil.resetWindows(false, false);
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
