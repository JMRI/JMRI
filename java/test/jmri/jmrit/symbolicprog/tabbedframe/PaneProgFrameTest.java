package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for PaneProgFrame.
 *
 * @author	Bob Jacobsen
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
