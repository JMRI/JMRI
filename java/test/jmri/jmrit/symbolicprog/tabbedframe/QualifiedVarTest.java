package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JPanel;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test PaneProg with qualified variables.
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class QualifiedVarTest {

    // show me a specially-created frame
    @Test
    public void testFrame() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // run all following on Swing thread
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                setupDoc();
                PaneProgFrame p = new PaneProgFrame(null, new RosterEntry(),
                        "test qualified var", "programmers/Basic.xml",
                        new jmri.progdebugger.ProgDebugger(), false) {
                    // dummy implementations
                    @Override
                    protected JPanel getModePane() {
                        return null;
                    }
                    // prevent this test from prompting to save file
                    @Override
                    protected boolean checkDirtyFile() {
                        return false;
                    }
                };

                // get the sample info
                try {
                    jmri.jmrit.XmlFile file = new jmri.jmrit.XmlFile() {
                    };
                    org.jdom2.Element el = file.rootFromFile(new java.io.File("java/test/jmri/jmrit/symbolicprog/tabbedframe/pass/DecoderWithQualifier.xml"));

                    DecoderFile df = new DecoderFile();  // used as a temporary
                    df.loadVariableModel(el.getChild("decoder"), p.variableModel);
                } catch (IOException | JDOMException e) {
                    log.error("Exception during setup", e);
                }
                p.readConfig(root, new RosterEntry());
                p.pack();
                p.setVisible(true);

                // close the window for cleanliness
                p.dispatchEvent(new WindowEvent(p, WindowEvent.WINDOW_CLOSING));
        }
        });
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
                .setAttribute("showFnLanelPane", "no")
                .setAttribute("showRosterMediaPane", "no")
                .addContent(new Element("pane")
                        .setAttribute("name", "Test")
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "Primary Address")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV2")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV3")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV4")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV5")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV6")
                                )
                                .addContent(new Element("separator"))
                                .addContent(new Element("label")
                                        .setAttribute("label", "set cv3 >= 100 to see CV4")
                                )
                                .addContent(new Element("label")
                                        .setAttribute("label", "set cv3 <=100 to see CV5, CV6")
                                )
                        )
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "Minor Version Number")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "Major Version Number")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "iCV53.5.0")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "iCV55.92.0")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "iCV55.92.1")
                                )
                                .addContent(new Element("separator"))
                                .addContent(new Element("label")
                                        .setAttribute("label", "set cv3 >= 100 to see iCV53.5.0")
                                )
                                .addContent(new Element("label")
                                        .setAttribute("label", "set minor >= 100 to see iCV55.92.0")
                                )
                                .addContent(new Element("label")
                                        .setAttribute("label", "set minor, major >= 100 to see iCV55.92.1")
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
                        .setAttribute("name", "CV3>50")
                        .addContent(new Element("qualifier")
                                .addContent(new Element("variableref")
                                        .addContent("CV3")
                                )
                                .addContent(new Element("relation")
                                        .addContent("gt")
                                )
                                .addContent(new Element("value")
                                        .addContent("50")
                                )
                        )
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV3")
                                )
                                .addContent(new Element("display")
                                        .setAttribute("item", "CV4")
                                )
                                .addContent(new Element("label")
                                        .setAttribute("label", "Pane visible with CV3>100")
                                )
                        )
                )
        ); // end of adding contents
    }

    private final static Logger log = LoggerFactory.getLogger(QualifiedVarTest.class);

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
