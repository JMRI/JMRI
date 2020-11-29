package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.util.JUnitUtil;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Tests for PaneProgFrame.
 *
 * @author Bob Jacobsen
 */
public class PaneProgFrameTest {

    // test creating a pane in config file
    @Test
    public void testPane() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupDoc();

        // create test object
        result = null;

        PaneProgFrame p = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                new jmri.progdebugger.ProgDebugger(), false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return new JPanel();
            }
        };

        // invoke
        result = null;
        p.readConfig(root, new RosterEntry());
        Assert.assertEquals("paneList length ", 4, p.paneList.size());
        // three panes in root, plus roster entry pane

        JFrame f = jmri.util.JmriJFrame.getFrame("test frame");
        Assert.assertTrue("found frame", f != null);
        p.dispatchEvent(new WindowEvent(p, WindowEvent.WINDOW_CLOSING));
    }

    // show me the specially-created frame
    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupDoc();
        PaneProgFrame p = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                new jmri.progdebugger.ProgDebugger(), false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };

        // ugly, temporary way to load the decoder info
        jmri.jmrit.decoderdefn.DecoderFileTest t = new jmri.jmrit.decoderdefn.DecoderFileTest();
        t.setupDecoder();
        DecoderFile df = new DecoderFile();  // used as a temporary
        df.loadVariableModel(t.decoder, p.variableModel);

        p.readConfig(root, new RosterEntry());
        p.pack();
        p.setVisible(true);

        JFrame f = jmri.util.JmriJFrame.getFrame("test frame");
        Assert.assertTrue("found frame", f != null);
        p.dispatchEvent(new WindowEvent(p, WindowEvent.WINDOW_CLOSING));
    }

    @Test
    public void testLoadDecoderFileUpdateMaxFnNum() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create test Element
        org.jdom2.Element e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "our id 4")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                        .setAttribute("comment", "decoder comment")
                ); // end create element

        RosterEntry r = new RosterEntry(e) { // a temporary storage, need to override some methods
            @Override
            protected void warnShortLong(String s) {
            }

            @Override
            public void loadFunctions(Element e3, String source) {
            }

            @Override
            public void loadSounds(Element e3, String source) {
            }

            @Override
            public void updateFile() {
            }

            @Override
            public void writeFile(CvTableModel cvModel, VariableTableModel variableModel) {
            }
        };

        org.jdom2.Element o = r.store();

        // check test attributes are loaded
        Assert.assertEquals("XML Element ", e.toString(), o.toString());
        Assert.assertEquals("family ", "91", o.getChild("decoder").getAttribute("family").getValue());
        Assert.assertEquals("model ", "33", o.getChild("decoder").getAttribute("model").getValue());
        Assert.assertEquals("comment", "decoder comment", o.getChild("decoder").getAttribute("comment").getValue());
        Assert.assertEquals("default maxFnNum is loaded", "28", o.getChild("decoder").getAttribute("maxFnNum").getValue());

        // ugly, temporary way to load the decoder info
        jmri.jmrit.decoderdefn.DecoderFileTest t = new jmri.jmrit.decoderdefn.DecoderFileTest();
        t.setupDecoder();
        DecoderFile df = new DecoderFile() { // a temporary storage, need to override some methods
            @Override
            public String getFileName() {
                return "0NMRA_test.xml";
            }

            @Override
            public String getProductID() {
                return getModelElement().getAttributeValue("productID");
            }

            @Override
            public Element getModelElement() {
                return t.model;
            }
        };

        setupDoc();
        PaneProgFrame p = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                new jmri.progdebugger.ProgDebugger(), false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }

            @Override
            protected boolean checkDirtyDecoder() {
                return false;
            }

            @Override
            protected boolean checkDirtyFile() {
                return false;
            }
        };

        df.loadVariableModel(t.decoder, p.variableModel);
        p.loadDecoderFile(df, r);
        o = r.store();

        Assert.assertEquals("model maxFnNum ", "31", t.model.getAttribute("maxFnNum").getValue());
        Assert.assertEquals("roster entry maxFnNum ", "31", o.getChild("decoder").getAttribute("maxFnNum").getValue());

        p.dispatchEvent(new WindowEvent(p, WindowEvent.WINDOW_CLOSING));
    }

    // static variables for internal classes to report their interpretations
    static String result = null;
    static int colCount = -1;
    static int varCount = -1;

    // static variables for the test XML structures
    Element root = null;
    Document doc = null;

    // provide a test document in the above static variables
    void setupDoc() {
        // create a JDOM tree with just some elements
        root = new Element("programmer-config");
        doc = new Document(root);
        doc.setDocType(new DocType("programmer-config", "programmer-config.dtd"));

        // add some elements
        root.addContent(new Element("programmer")
                .setAttribute("showFnLanelPane", "yes")
                .setAttribute("showRosterMediaPane", "yes")
                .addContent(new Element("pane")
                        .setAttribute("name", "Basic")
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "Address")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Start voltage")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Normal direction of motion")
                                )
                        )
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "Address")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Normal direction of motion")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Normal direction of motion")
                                        .setAttribute("format", "checkbox")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Normal direction of motion")
                                        .setAttribute("format", "radiobuttons")
                                )
                        )
                )
                .addContent(new Element("pane")
                        .setAttribute("name", "CV")
                        .addContent(new Element("column")
                                .addContent(new Element("cvtable"))
                        )
                )
                .addContent(new Element("pane")
                        .setAttribute("name", "Other")
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "Address")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Normal direction of motion")
                                )
                        )
                )
        ); // end of adding contents
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
