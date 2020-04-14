package jmri.jmrit.symbolicprog.tabbedframe;

import static org.junit.Assert.*;

import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author	Bob Jacobsen Copyright 2001, 2002, 2003, 2004
 */
public class PaneProgPaneTest {

    ProgDebugger p = new ProgDebugger();

    // test creating columns in a pane
    @Test
    public void testColumn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupDoc();
        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        log.debug("CvTableModel ctor complete");
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        log.debug("VariableTableModel ctor complete");

        // create test object with special implementation of the newColumn(String) operation
        colCount = 0;
        PaneProgPane pane = new PaneProgPane(pFrame, "name", pane1, cvModel, varModel, null, null) {
            @Override
            public JPanel newColumn(Element e, boolean a, Element el) {
                colCount++;
                return new JPanel();
            }
        };
        assertNotNull("exists", pane);
        assertEquals("column count", 2, colCount);
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    // test specifying variables in columns
    @Test
    public void testVariables() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupDoc();  // make sure XML document is ready
        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        log.debug("VariableTableModel ctor complete");

        // create test object with special implementation of the newVariable(String) operation
        varCount = 0;
        PaneProgPane pane = new PaneProgPane(pFrame, "name", pane1, cvModel, varModel, null, null) {
            @Override
            public void newVariable(Element e, JComponent p, GridBagLayout g, GridBagConstraints c, boolean a) {
                varCount++;
            }
        };
        assertNotNull("exists", pane);
        assertEquals("variable defn count", 7, varCount);
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    // test storage of programming info in list
    @Test
    public void testVarListFill() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupDoc();  // make sure XML document is ready
        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        log.debug("VariableTableModel ctor complete");
        // have to add a couple of defined variables
        Element el0 = new Element("variable")
                .setAttribute("CV", "17")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Start voltage")
                .addContent(new Element("longAddressVal"));
        log.debug("First element created");
        varModel.setRow(0, el0);
        log.debug("First element loaded");
        Element el1 = new Element("variable")
                .setAttribute("CV", "17")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Primary Address")
                .addContent(new Element("decVal"));
        log.debug("Second element created");
        varModel.setRow(1, el1);
        log.debug("Two elements loaded");

        // test by invoking
        PaneProgPane pane = new PaneProgPane(pFrame, "name", pane1, cvModel, varModel, null, null);
        assertEquals("variable list length", 2, pane.varList.size());
        assertEquals("1st variable index ", Integer.valueOf(1), pane.varList.get(0));
        assertEquals("2nd variable index ", Integer.valueOf(0), pane.varList.get(1));
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    // test storage of programming info in list
    @Test
    public void testPaneRead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        log.debug("testPaneRead starts");
        // initialize the system
        setupDoc();  // make sure XML document is ready

        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };

        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        log.debug("VariableTableModel ctor complete");
        // have to add a couple of defined variables
        Element el0 = new Element("variable")
                .setAttribute("CV", "2")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Start voltage")
                .addContent(new Element("decVal"));
        Element el1 = new Element("variable")
                .setAttribute("CV", "3")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Primary Address")
                .addContent(new Element("decVal"));
        varModel.setRow(0, el0);
        varModel.setRow(1, el1);

        PaneProgPane progPane = new PaneProgPane(pFrame, "name", pane1, cvModel, varModel, null, null);

        p.resetCv(2, 20);
        p.resetCv(3, 30);

        // test by invoking
        progPane.readAllButton.setSelected(true);

        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(() -> {
            return !progPane.isBusy();
        }, "progPane.isBusy");

        Assert.assertEquals("CV 2 value ", "20", varModel.getValString(0));
        Assert.assertEquals("CV 3 value ", "30", varModel.getValString(1));

        log.debug("testPaneRead ends ok");
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    @Test
    public void testPaneWrite() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        log.debug("testPaneWrite starts");
        // initialize the system
        setupDoc();  // make sure XML document is ready

        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        log.debug("VariableTableModel ctor complete");
        // have to add a couple of defined variables
        Element el0 = new Element("variable")
                .setAttribute("CV", "2")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("default", "20")
                .setAttribute("label", "Start voltage")
                .addContent(new Element("decVal"));
        Element el1 = new Element("variable")
                .setAttribute("CV", "3")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("default", "30")
                .setAttribute("label", "Primary Address")
                .addContent(new Element("decVal"));
        varModel.setRow(0, el0);
        varModel.setRow(1, el1);
        log.debug("Two elements loaded");

        PaneProgPane progPane = new PaneProgPane(pFrame, "name", pane1, cvModel, varModel, null, null);

        p.resetCv(2, -1);
        p.resetCv(3, -1);

        // test by invoking
        progPane.writeAllButton.setSelected(true);

        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(() -> {
            return !progPane.isBusy();
        }, "progPane.isBusy");

        Assert.assertEquals("CV 2 value ", 20, p.getCvVal(2));
        Assert.assertEquals("CV 3 value ", 30, p.getCvVal(3));

        log.debug("testPaneWrite ends ok");
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    // test counting of read operations needed
    @Test
    public void testPaneReadOpCount() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        log.debug("testPaneReadOpCount starts");
        // initialize the system
        setupDoc();  // make sure XML document is ready

        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);

        // have to add a couple of defined variables
        int row = 0;

        // note these +have+ to be on this pane, e.g. named in setupDoc
        Element el0 = new Element("variable")
                .setAttribute("CV", "1")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Start voltage")
                .addContent(new Element("decVal"));
        varModel.setRow(row++, el0);

        Element el1 = new Element("variable")
                .setAttribute("CV", "1")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Primary Address")
                .addContent(new Element("decVal"));
        varModel.setRow(row++, el1);

        Element el2 = new Element("variable")
                .setAttribute("CV", "67")
                .setAttribute("label", "Normal direction of motion")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .addContent(new Element("speedTableVal"));
        varModel.setRow(row++, el2);

        Element el3 = new Element("variable")
                .setAttribute("CV", "68")
                .setAttribute("readOnly", "no")
                .setAttribute("mask", "VVVVVVVV")
                .setAttribute("label", "Address")
                .addContent(new Element("decVal"));
        varModel.setRow(row++, el3);

        PaneProgPane progPane = new PaneProgPane(pFrame, "name", pane1, cvModel, varModel, null, null);

        // start actual testing
        Assert.assertEquals("number of all CVs to read ", 29, progPane.countOpsNeeded(true, false));
        Assert.assertEquals("number of all CVs to write ", 29, progPane.countOpsNeeded(false, false));

        Assert.assertEquals("number of changed CVs to read ", 0, progPane.countOpsNeeded(true, true));
        Assert.assertEquals("number of changed CVs to write ", 0, progPane.countOpsNeeded(false, true));

        // mark some as needing to be written
        (cvModel.allCvMap().get("1")).setValue(12);

        Assert.assertEquals("modified all CVs to read ", 29, progPane.countOpsNeeded(true, false));
        Assert.assertEquals("modified all CVs to write ", 29, progPane.countOpsNeeded(false, false));

        Assert.assertEquals("modified changed CVs to read ", 1, progPane.countOpsNeeded(true, true));
        Assert.assertEquals("modified changed CVs to write ", 1, progPane.countOpsNeeded(false, true));

        (cvModel.allCvMap().get("69")).setValue(12);
        // careful - might change more than one CV!

        Assert.assertEquals("spdtbl all CVs to read ", 29, progPane.countOpsNeeded(true, false));
        Assert.assertEquals("spdtbl all CVs to write ", 29, progPane.countOpsNeeded(false, false));

        Assert.assertEquals("spdtbl changed CVs to read ", 2, progPane.countOpsNeeded(true, true));
        Assert.assertEquals("spdtbl changed CVs to write ", 2, progPane.countOpsNeeded(false, true));

        log.debug("testPaneReadOpCount ends ok");
        pFrame.dispatchEvent(new WindowEvent(pFrame, WindowEvent.WINDOW_CLOSING));
    }

    // static variables for internal classes to report their interpretations
    static String result = null;
    static int colCount = -1;
    static int varCount = -1;

    // static variables for the test XML structures
    Element root = null;
    Element pane1 = null;
    Element pane2 = null;
    Element pane3 = null;
    Document doc = null;

    // provide a test document in the above static variables
    void setupDoc() {
        // create a JDOM tree with just some elements
        root = new Element("programmer-config");
        doc = new Document(root);
        doc.setDocType(new DocType("programmer-config", "programmer-config.dtd"));

        // add some elements
        root.addContent(new Element("programmer")
                .addContent(pane1 = new Element("pane")
                        .setAttribute("name", "Basic")
                        .addContent(new Element("column")
                                .addContent(new Element("display")
                                        .setAttribute("item", "Primary Address")
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
                .addContent(pane2 = new Element("pane")
                        .setAttribute("name", "CV")
                        .addContent(new Element("column")
                                .addContent(new Element("cvtable"))
                        )
                )
                .addContent(pane3 = new Element("pane")
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

        log.debug("setupDoc complete");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PaneProgPaneTest.class);

}
