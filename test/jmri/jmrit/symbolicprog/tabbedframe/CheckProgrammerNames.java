// CheckProgrammerNames.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;
import org.jdom.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2007, 2008
 * @version	$Revision: 1.1 $
 * @see jmri.jmrit.XmlFile
 */
public class CheckProgrammerNames extends TestCase {
    
    public void testComprehensive() {
        check(new File("xml/programmers/Comprehensive.xml"));
    }
    
    public void testBasic() {
        check(new File("xml/programmers/Basic.xml"));
    }
    
    public void testTrainShowBasic() {
        check(new File("xml/programmers/TrainShowBasic.xml"));
    }
    
    public void testSampleClub() {
        check(new File("xml/programmers/Sample Club.xml"));
    }
    
    public void testCustom() {
        check(new File("xml/programmers/Custom.xml"));
    }
    
    public void testTutorial() {
        check(new File("xml/programmers/Tutorial.xml"));
    }
    
    public void testRegisters() {
        check(new File("xml/programmers/Registers.xml"));
    }
    
/*     public void testESU() { */
/*         check(new File("xml/programmers/ESU.xml")); */
/*     } */
    
/*     public void testZimo() { */
/*         check(new File("xml/programmers/Zimo.xml")); */
/*     } */
    
    public void check(File file) {
        // handle the file (later should be outside this thread?)
        try {
            Element root = readFile(file);
            if (log.isDebugEnabled()) log.debug("parsing complete");
            
            // check to see if there's a decoder element
            if (root.getChild("programmer")==null) {
                Assert.fail("Does not appear to be a programmer file");
                return;
            }
            List varList = addVariables(root.getChild("programmer"), new ArrayList<Element>());
            if (log.isDebugEnabled()) log.debug("found "+varList.size()+" variables");
            jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();
            
            String warnings = "";
            
            for (int i=0; i<varList.size(); i++) {
                Element varElement = (Element)(varList.get(i));
                // for each variable, see if can find in names file
                Attribute itemAttr = varElement.getAttribute("item");
                String item = null;
                if (itemAttr!=null) item = itemAttr.getValue();
                if (log.isDebugEnabled()) log.debug("Variable called \""
                                                    +((item!=null)?item:"<none>"));
                if (!(item==null ? false : nfile.checkName(item))) {
                    log.warn("Variable not found: item=\""
                             +((item!=null)?item:"<none>")+"\"");
                    warnings += "Variable not found: item=\""
                        +((item!=null)?item:"<none>")+"\"\n";
                }
            }
            
            if (!warnings.equals("")) {
                Assert.fail(warnings);
            }
                        
        } catch (Exception ex) {
            Assert.fail("Error parsing decoder file: "+ex);
            return;
        }
    }
    
    List<Element> addVariables(Element e, List<Element> l) {
        // add all "display" elements
        List d = e.getChildren("display");
        l.addAll(d);

        // process all sub-elements
        d = new ArrayList<Element>();
        d.addAll(e.getChildren("pane"));
        d.addAll(e.getChildren("column"));
        d.addAll(e.getChildren("row"));
        for (Iterator<Element> i = d.iterator(); i.hasNext(); ) {
            addVariables(i.next(), l);
        }

        return l;
    }
    
    /**
     * Ask SAX to read and verify a file
     */
    Element readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
        
        return xf.rootFromFile(file);
        
    }
    
    // from here down is testing infrastructure

    public CheckProgrammerNames(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CheckProgrammerNames.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CheckProgrammerNames.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CheckProgrammerNames.class.getName());
    
}
