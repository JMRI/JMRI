// QualifiedVarTest.java

package jmri.jmrit.symbolicprog.tabbedframe;

import org.apache.log4j.Logger;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import javax.swing.JPanel;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import junit.framework.*;

/**
 * Test PaneProg with qualified variables
 *
 * @author			Bob Jacobsen    Copyright 2010
 * @version			$Revision$
 */
public class QualifiedVarTest extends TestCase {


    // show me a specially-created frame
    public void testFrame() throws Exception {
        if (System.getProperty("jmri.headlesstest","false").equals("true")) return;

        setupDoc();
        PaneProgFrame p = new PaneProgFrame(null, new RosterEntry(),
                                            "test qualified var", "programmers/Basic.xml",
                                            new jmri.progdebugger.ProgDebugger(), false) {
                // dummy implementations
                protected JPanel getModePane() { return null; }
            };

        // get the sample info
        jmri.jmrit.XmlFile file = new jmri.jmrit.XmlFile(){};
        org.jdom.Element el = file.rootFromFile(new java.io.File("java/test/jmri/jmrit/decoderdefn/DecoderWithQualifier.xml"));
        
        DecoderFile df = new DecoderFile();  // used as a temporary
        df.loadVariableModel(el.getChild("decoder"), p.variableModel);

        p.readConfig(root, new RosterEntry());
        p.pack();
        p.setVisible(true);
        
        // close
        p.dispose();
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
        doc.setDocType(new DocType("programmer-config","programmer-config.dtd"));

        // add some elements
        root.addContent(new Element("programmer")
                .setAttribute("showFnLanelPane","no")
                .setAttribute("showRosterMediaPane","no")
            .addContent(new Element("pane")
                .setAttribute("name","Test")
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
                    .addContent(new Element("separator"))
                    .addContent(new Element("label")
                        .setAttribute("label", "set cv3 >= 100 to see cv4")
                        )
                    .addContent(new Element("label")
                        .setAttribute("label", "set cv3 <=100 to see cv5")
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
                .setAttribute("name", "iCV")
                .addContent(new Element("column")
                    .addContent(new Element("indxcvtable"))
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
            )
            ; // end of adding contents

        return;
    }

    // from here down is testing infrastructure

    public QualifiedVarTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", QualifiedVarTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(QualifiedVarTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(QualifiedVarTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
