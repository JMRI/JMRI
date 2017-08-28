package jmri.jmrix.openlcb.swing.networktree;

import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromFile;
import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromSample;

import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.jmrix.openlcb.SampleFactory;
import jmri.util.JUnitUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.cdi.swing.CdiPanel;

/**
 * NOTE: This file actually Demonstrates the openLCB CdiPanel class.
 *
 * @author Bob Jacobsen Copyright 2012
 *
 */
public class CdiPanelDemo {

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

    Element getRootFromFile(String name) {
        Element root = null;
        try {
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);  // argument controls validation
            Document doc = builder.build(new BufferedInputStream(new FileInputStream(new File(name))));
            root = doc.getRootElement();
        } catch (Exception e) {
            System.out.println("While reading file: " + e);
        }
        return root;
    }

    JFrame makeFrameFromFile(String fileName) {
        JFrame f = new JFrame();
        CdiPanel m = new CdiPanel();

        m.initComponents(demoRepFromFile(new File(fileName)),
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

        f.pack();
        return f;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

}
