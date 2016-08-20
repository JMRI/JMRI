package jmri.jmrix.openlcb.swing.networktree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.openlcb.cdi.swing.CdiPanel;

/**
 * @author Bob Jacobsen Copyright 2012
 * @version $Revision: 2175 $
 */
public class CdiPanelDemo extends TestCase {

    // from here down is testing infrastructure
    public CdiPanelDemo(String s) {
        super(s);
    }

    public void testDisplay() {
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

    public void testDisplaySample1() {
        JFrame f = makeFrame(getRootFromFile("java/test/jmri/jmrix/openlcb/sample1.xml"));
        f.setTitle("Sample1 XML");
        f.setVisible(true);
    }

    public void testDisplaySample2() {
        JFrame f = makeFrame(getRootFromFile("java/test/jmri/jmrix/openlcb/sample2.xml"));
        f.setTitle("Sample 2 XML");
        f.setVisible(true);
    }

    public void testLocoCdiDisplay() {
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

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CdiPanelDemo.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CdiPanelDemo.class);
        return suite;
    }
}
