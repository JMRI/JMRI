// PaneProgPaneTest.java

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

import jmri.*;
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

/**
 * @author	Bob Jacobsen Copyright 2001, 2002, 2003, 2004
 * @version         $Revision: 1.8 $
 */
public class PaneProgPaneTest extends TestCase {

    ProgDebugger p = new ProgDebugger();

    // test creating columns in a pane
    public void testColumn() {
        setupDoc();
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        if (log.isDebugEnabled()) log.debug("CvTableModel ctor complete");
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        if (log.isDebugEnabled()) log.debug("VariableTableModel ctor complete");

        // create test object with special implementation of the newColumn(String) operation
        colCount = 0;
        PaneProgPane p = new PaneProgPane("name", pane1, cvModel, varModel, null) {
                public JPanel newColumn(Element e, boolean a, Element el) { colCount++; return new JPanel();}
            };

        assertEquals("column count", 2, colCount);
    }

    // test specifying variables in columns
    public void testVariables() {
        setupDoc();  // make sure XML document is ready
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        if (log.isDebugEnabled()) log.debug("VariableTableModel ctor complete");

        // create test object with special implementation of the newVariable(String) operation
        varCount = 0;
        PaneProgPane p = new PaneProgPane("name", pane1, cvModel, varModel, null) {
                public void newVariable(Element e, JComponent p, GridBagLayout g, GridBagConstraints c, boolean a)
                { varCount++; }
            };

        assertEquals("variable defn count", 7, varCount);
    }

    // test storage of programming info in list
    public void testVarListFill() {
        setupDoc();  // make sure XML document is ready
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        if (log.isDebugEnabled()) log.debug("VariableTableModel ctor complete");
        // have to add a couple of defined variables
        Element el0 = new Element("variable")
            .addAttribute("CV","17")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Start voltage")
            .addContent( new Element("longAddressVal"));
        if (log.isDebugEnabled()) log.debug("First element created");
        varModel.setRow(0, el0);
        if (log.isDebugEnabled()) log.debug("First element loaded");
        Element el1 = new Element("variable")
            .addAttribute("CV","17")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Primary Address")
            .addContent( new Element("decVal"));
        if (log.isDebugEnabled()) log.debug("Second element created");
        varModel.setRow(1, el1);
        if (log.isDebugEnabled()) log.debug("Two elements loaded");

        // test by invoking
        PaneProgPane p = new PaneProgPane("name", pane1, cvModel, varModel, null);
        assertEquals("variable list length", 2, p.varList.size());
        assertEquals("1st variable index ", new Integer(1), p.varList.get(0));
        assertEquals("2nd variable index ", new Integer(0), p.varList.get(1));
    }

    // test storage of programming info in list
    public void testPaneRead() {
        if (log.isDebugEnabled()) log.debug("testPaneRead starts");
        // initialize the system
        setupDoc();  // make sure XML document is ready

        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        if (log.isDebugEnabled()) log.debug("VariableTableModel ctor complete");
        // have to add a couple of defined variables
        Element el0 = new Element("variable")
            .addAttribute("CV","2")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Start voltage")
            .addContent( new Element("decVal"));
        varModel.setRow(0, el0);
        Element el1 = new Element("variable")
            .addAttribute("CV","1")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Primary Address")
            .addContent( new Element("decVal"));
        varModel.setRow(1, el1);

        PaneProgPane progPane = new PaneProgPane("name", pane1, cvModel, varModel, null);

        // test by invoking
        progPane.readPaneAll();

        // wait for reply (normally, done by callback; will check that later)
        if (log.isDebugEnabled()) log.debug("Start to wait for reply");
        int i = 0;
        while ( progPane.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i);
        assertTrue("busy period ends before timeout ", i<=100);

        Assert.assertEquals("last cv read ", 2, p.lastReadCv());

        if (log.isDebugEnabled()) log.debug("testPaneRead ends ok");
    }

    public void testPaneWrite() {
        if (log.isDebugEnabled()) log.debug("testPaneWrite starts");
        // initialize the system
        setupDoc();  // make sure XML document is ready

        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);
        if (log.isDebugEnabled()) log.debug("VariableTableModel ctor complete");
        // have to add a couple of defined variables
        Element el0 = new Element("variable")
            .addAttribute("CV","2")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Start voltage")
            .addContent( new Element("decVal"));
        varModel.setRow(0, el0);
        Element el1 = new Element("variable")
            .addAttribute("CV","1")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Primary Address")
            .addContent( new Element("decVal"));
        varModel.setRow(1, el1);
        if (log.isDebugEnabled()) log.debug("Two elements loaded");

        PaneProgPane progPane = new PaneProgPane("name", pane1, cvModel, varModel, null);

        // test by invoking
        progPane.writePaneAll();

        // wait for reply (normally, done by callback; will check that later)
        if (log.isDebugEnabled()) log.debug("Start to wait for reply");
        int i = 0;
        while ( progPane.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i);
        assertTrue("busy period ends before timeout ", i<=100);

        Assert.assertEquals("last cv written ", 2, p.lastWriteCv());

        if (log.isDebugEnabled()) log.debug("testPaneWrite ends ok");
    }

    // test counting of read operations needed
    public void testPaneReadOpCount() {
        if (log.isDebugEnabled()) log.debug("testPaneReadOpCount starts");
        // initialize the system
        setupDoc();  // make sure XML document is ready

        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel);

        // have to add a couple of defined variables
        int row = 0;
        
        // note these +have+ to be on this pane, e.g. named in setupDoc
        Element el0 = new Element("variable")
            .addAttribute("CV","1")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Start voltage")
            .addContent( new Element("decVal"));
        varModel.setRow(row++, el0);

        Element el1 = new Element("variable")
            .addAttribute("CV","1")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Primary Address")
            .addContent( new Element("decVal"));
        varModel.setRow(row++, el1);

        Element el2 = new Element("variable")
            .addAttribute("CV","67")
            .addAttribute("label","Normal direction of motion")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addContent( new Element("speedTableVal"));
        varModel.setRow(row++, el2);
        
        Element el3 = new Element("variable")
            .addAttribute("CV","68")
            .addAttribute("readOnly","no")
            .addAttribute("mask","VVVVVVVV")
            .addAttribute("label","Address")
            .addContent( new Element("decVal"));
        varModel.setRow(row++, el3);

        PaneProgPane progPane = new PaneProgPane("name", pane1, cvModel, varModel, null);

        // start actual testing
        Assert.assertEquals("number of all CVs to read ", 29, progPane.countOpsNeeded(true,false));
        Assert.assertEquals("number of all CVs to write ", 29, progPane.countOpsNeeded(false,false));

        Assert.assertEquals("number of changed CVs to read ", 0, progPane.countOpsNeeded(true,true));
        Assert.assertEquals("number of changed CVs to write ", 0, progPane.countOpsNeeded(false,true));
        
        // mark some as needing to be written
        ((CvValue)cvModel.allCvVector().get(1)).setValue(12);

        Assert.assertEquals("modified all CVs to read ", 29, progPane.countOpsNeeded(true,false));
        Assert.assertEquals("modified all CVs to write ", 29, progPane.countOpsNeeded(false,false));

        Assert.assertEquals("modified changed CVs to read ", 1, progPane.countOpsNeeded(true,true));
        Assert.assertEquals("modified changed CVs to write ", 1, progPane.countOpsNeeded(false,true));
        
        ((CvValue)cvModel.allCvVector().get(69)).setValue(12);
            // careful - might change more than one CV!
            
        Assert.assertEquals("spdtbl all CVs to read ", 29, progPane.countOpsNeeded(true,false));
        Assert.assertEquals("spdtbl all CVs to write ", 29, progPane.countOpsNeeded(false,false));

        Assert.assertEquals("spdtbl changed CVs to read ", 2, progPane.countOpsNeeded(true,true));
        Assert.assertEquals("spdtbl changed CVs to write ", 2, progPane.countOpsNeeded(false,true));

        if (log.isDebugEnabled()) log.debug("testPaneReadOpCount ends ok");
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
        doc.setDocType(new DocType("programmer-config","programmer-config.dtd"));

        // add some elements
        root.addContent(new Element("programmer")
            .addContent(pane1 = new Element("pane")
                .addAttribute("name","Basic")
                .addContent(new Element("column")
                    .addContent(new Element("display")
                        .addAttribute("item", "Primary Address")
                        )
                    .addContent(new Element("display")
                        .addAttribute("item", "Start voltage")
                        )
                    .addContent(new Element("display")
                        .addAttribute("item", "Normal direction of motion")
                        )
                    )
                .addContent(new Element("column")
                    .addContent(new Element("display")
                        .addAttribute("item", "Address")
                        )
                    .addContent(new Element("display")
                        .addAttribute("item", "Normal direction of motion")
                        )
                    .addContent(new Element("display")
                        .addAttribute("item", "Normal direction of motion")
                        .addAttribute("format","checkbox")
                        )
                    .addContent(new Element("display")
                        .addAttribute("item", "Normal direction of motion")
                        .addAttribute("format","radiobuttons")
                        )
                    )
                )
            .addContent(pane2 = new Element("pane")
                .addAttribute("name", "CV")
                .addContent(new Element("column")
                    .addContent(new Element("cvtable"))
                    )
                )
            .addContent(pane3 = new Element("pane")
                .addAttribute("name", "Other")
                .addContent(new Element("column")
                    .addContent(new Element("display")
                        .addAttribute("item", "Address")
                        )
                    .addContent(new Element("display")
                        .addAttribute("item", "Normal direction of motion")
                        )
                    )
                )
            )
            ; // end of adding contents

        if (log.isDebugEnabled()) log.debug("setupDoc complete");
        return;
    }

    // from here down is testing infrastructure

    public PaneProgPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PaneProgPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PaneProgPaneTest.class);
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgPaneTest.class.getName());

}
