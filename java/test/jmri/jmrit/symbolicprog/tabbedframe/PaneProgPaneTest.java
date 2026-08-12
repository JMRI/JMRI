package jmri.jmrit.symbolicprog.tabbedframe;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen Copyright 2001, 2002, 2003, 2004
 */
@DisabledIfHeadless
public class PaneProgPaneTest {

    private final ProgDebugger p = new ProgDebugger();

    // test creating columns in a pane
    @Test
    public void testColumn() {
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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

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
        assertNotNull( pane, "exists");
        assertEquals( 2, colCount, "column count");
        JUnitUtil.dispose(pFrame);
    }

    // test specifying variables in columns
    @Test
    public void testVariables() {
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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

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
        assertNotNull(pane, "exists");
        assertEquals(7, varCount, "variable defn count");
        JUnitUtil.dispose(pFrame);
    }

    // test storage of programming info in list
    @Test
    public void testVarListFill() {
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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

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
        assertEquals(2, pane.varList.size(), "variable list length");
        assertEquals(Integer.valueOf(1), pane.varList.get(0), "1st variable index ");
        assertEquals(Integer.valueOf(0), pane.varList.get(1), "2nd variable index ");
        JUnitUtil.dispose(pFrame);
    }

    // test storage of programming info in list
    @Test
    public void testPaneRead() {
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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

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

        assertEquals("20", varModel.getValString(0), "CV 2 value ");
        assertEquals("30", varModel.getValString(1), "CV 3 value ");

        log.debug("testPaneRead ends ok");
        JUnitUtil.dispose(pFrame);
    }

    @Test
    public void testPaneWrite() {
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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

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

        assertEquals(20, p.getCvVal(2), "CV 2 value ");
        assertEquals(30, p.getCvVal(3), "CV 3 value ");

        log.debug("testPaneWrite ends ok");
        JUnitUtil.dispose(pFrame);
    }

    // test counting of read operations needed
    @Test
    public void testPaneReadOpCount() {
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
        JUnitUtil.waitFor(()->{return pFrame.threadCount.get() == 0;}, "PaneProgFrame threads done");

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
        assertEquals(29, progPane.countOpsNeeded(true, false), "number of all CVs to read ");
        assertEquals(29, progPane.countOpsNeeded(false, false), "number of all CVs to write ");

        assertEquals(0, progPane.countOpsNeeded(true, true), "number of changed CVs to read ");
        assertEquals(0, progPane.countOpsNeeded(false, true), "number of changed CVs to write ");

        // mark some as needing to be written
        var getKey1 = cvModel.allCvMap().get("1");
        assertNotNull(getKey1);
        getKey1.setValue(12);

        assertEquals(29, progPane.countOpsNeeded(true, false), "modified all CVs to read ");
        assertEquals(29, progPane.countOpsNeeded(false, false), "modified all CVs to write ");

        assertEquals(1, progPane.countOpsNeeded(true, true), "modified changed CVs to read ");
        assertEquals(1, progPane.countOpsNeeded(false, true), "modified changed CVs to write ");

        var getKey69 = cvModel.allCvMap().get("69");
        assertNotNull(getKey69);
        getKey69.setValue(12);
        // careful - might change more than one CV!

        assertEquals(29, progPane.countOpsNeeded(true, false), "spdtbl all CVs to read ");
        assertEquals(29, progPane.countOpsNeeded(false, false), "spdtbl all CVs to write ");

        assertEquals(2, progPane.countOpsNeeded(true, true), "spdtbl changed CVs to read ");
        assertEquals(2, progPane.countOpsNeeded(false, true), "spdtbl changed CVs to write ");

        log.debug("testPaneReadOpCount ends ok");
        JUnitUtil.dispose(pFrame);
    }

    // variables for internal classes to report their interpretations
    // private String result = null; // currently unused
    private int colCount = -1;
    private int varCount = -1;

    // static variables for the test XML structures
    private Element root = null;
    private Element pane1 = null;
    private Element pane2 = null;
    private Element pane3 = null;
    private Document doc = null;

    // provide a test document in the above static variables
    void setupDoc() {
        // assertNull(result);
        assertEquals(-1,colCount);
        assertEquals(-1,varCount);
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
        assertNotNull(pane2);
        assertNotNull(pane3);
        log.debug("setupDoc complete");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        // result = null;
        colCount = -1;
        varCount = -1;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false, false); // Detachable frame : "Comments : test frame"
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaneProgPaneTest.class);

}
