package jmri.jmrix.openlcb.swing.networktree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.openlcb.cdi.swing.CdiPanel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * NOTE: This file actually Demonstrates the openLCB CdiPanel class.
 * @author Bob Jacobsen Copyright 2012
 * 
 */
public class CdiPanelDemo {

    public void testCtor(){
        CdiPanel m = new CdiPanel();
        Assert.assertNotNull(m);
    }

    @Test
    public void testDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = new JFrame();
        f.setTitle("Configuration Demonstration");
        CdiPanel m = new CdiPanel();

        m.initComponents(new CdiPanel.ReadWriteAccess() {
            @Override
            public void doWrite(long address, int space, byte[] data) {
                System.out.println(data.length);
                System.out.println("write " + address + " " + space + ": " + org.openlcb.Utilities.toHexDotsString(data));
            }

            @Override
            public void doRead(long address, int space, int length, CdiPanel.ReadReturn handler) {
                handler.returnData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
                System.out.println("read " + address + " " + space);
            }
        },
                new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        //System.out.println("process button");
                        button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                        return button;
                    }
                }
        );
        m.loadCDI(
                new org.openlcb.cdi.jdom.JdomCdiRep(
                        jmri.jmrix.openlcb.SampleFactory.getBasicSample()
                )
        );

        f.add(m);

        // show
        f.pack();
        f.setVisible(true);
    }

    @Test
    public void testDisplaySample1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = makeFrame(getRootFromFile("java/test/jmri/jmrix/openlcb/sample1.xml"));
        f.setTitle("Sample1 XML");
        f.setVisible(true);
    }

    @Test
    public void testDisplaySample2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = makeFrame(getRootFromFile("java/test/jmri/jmrix/openlcb/sample2.xml"));
        f.setTitle("Sample 2 XML");
        f.setVisible(true);
    }

    @Test
    public void testLocoCdiDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = makeFrame(getRootFromFile("java/test/jmri/jmrix/openlcb/NMRAnetDatabaseTrainNode.xml"));
        f.setTitle("Locomotive CDI Demonstration");
        f.setVisible(true);
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

    JFrame makeFrame(Element root) {
        JFrame f = new JFrame();
        CdiPanel m = new CdiPanel();

        m.initComponents(new CdiPanel.ReadWriteAccess() {
            @Override
            public void doWrite(long address, int space, byte[] data) {
                System.out.println(data.length);
                System.out.println("write " + address + " " + space + ": " + org.openlcb.Utilities.toHexDotsString(data));
            }

            @Override
            public void doRead(long address, int space, int length, CdiPanel.ReadReturn handler) {
                handler.returnData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
                System.out.println("read " + address + " " + space);
            }
        },
                new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        //System.out.println("process button");
                        button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                        return button;
                    }
                }
        );

        m.loadCDI(
                new org.openlcb.cdi.jdom.JdomCdiRep(root)
        );

        f.add(m);

        f.pack();
        return f;
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public  void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
