// QualifierAdderTest.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import java.util.*;
import javax.swing.*;
import java.awt.Component;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;

import jmri.progdebugger.*;

/**
 * 
 * @author	Bob Jacobsen, Copyright 2014
 * @version     $Revision$
 */
public class QualifierAdderTest extends TestCase {

    // Service routine for tests
    
    ProgDebugger p;
    CvTableModel cvtable;
    VariableTableModel model;
    VariableValue v1;
    VariableValue v2;
    VariableValue v3;


    VariableValue makeVar(String label, String comment, String cvName,
                          boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                          String cvNum, String mask, int minVal, int maxVal,
                          HashMap<String, CvValue> v, JLabel status, String item) {
        VariableValue var = new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
        
        return var;
    }

    class TestArithmeticQualifier extends ArithmeticQualifier {
        TestArithmeticQualifier(VariableValue watchedVal, int value, String relation) {
            super(watchedVal, value, relation);
        }
        public void setWatchedAvailable(boolean t) {}
        public boolean currentAvailableState() {return true;}
    }
    
    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    /**
     * If there are any modifier elements, process them
     * by e.g. setting attributes on the VariableValue
     */
    protected QualifierAdder processModifierElements(final Element e, final VariableValue v) {
        QualifierAdder qa = new QualifierAdder() {        
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new ValueQualifier(v, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                v.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(e, model);
        
        return qa;
    }


    // Tests
    
    public void testSetup() {
        Assert.assertNotNull(v1);
        Assert.assertNotNull(v2);
        Assert.assertNotNull(v3);
        
        v1.setIntValue(4);
        Assert.assertEquals(4, v1.getIntValue());
        v1.setIntValue(5);
        Assert.assertEquals(5, v1.getIntValue());
        
    }
    
    public void testSingleQualifierOk() {
        Element e = new Element("variable").addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("one"))
                            .addContent(new Element("relation").addContent("eq"))
                            .addContent(new Element("value").addContent("3"))
                    );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(e)
                )
            );
            
        // test equal value qualifier
        
        processModifierElements(e, v2);
        
        v1.setIntValue(3);
        Assert.assertTrue("should be true for 3", v2.getAvailable());
        
        v1.setIntValue(5);
        Assert.assertFalse("should be false for 5", v2.getAvailable());
        
    }

    public void testExistsOk1() {
        Element e = new Element("variable").addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("one"))
                            .addContent(new Element("relation").addContent("exists"))
                            .addContent(new Element("value").addContent("1"))
                    );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(e)
                )
            );
            
        // print JDOM tree, to check
        //org.jdom.output.XMLOutputter fmt 
        //    = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
        //try {
        //	 fmt.output(doc, System.out);
        //} catch (Exception ex) { log.error("error writing XML", ex);}

        // test Exists
        
        processModifierElements(e, v2);
        
        Assert.assertTrue(v2.getAvailable());        
    }

    public void testExistsOk0() {
        Element e = new Element("variable").addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("none"))
                            .addContent(new Element("relation").addContent("exists"))
                            .addContent(new Element("value").addContent("0"))
                    );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(e)
                )
            );
            
        // test Exists
        
        processModifierElements(e, v2);
        
        Assert.assertTrue(v2.getAvailable());        
    }

    public void testNotExistsOk1() {
        Element e = new Element("variable").addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("none"))
                            .addContent(new Element("relation").addContent("exists"))
                            .addContent(new Element("value").addContent("1"))
                    );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(e)
                )
            );
            
        // test Exists
        
        processModifierElements(e, v2);
        
        Assert.assertFalse(v2.getAvailable());        
    }

    public void testNotExistsOk0() {
        Element e = new Element("variable").addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("one"))
                            .addContent(new Element("relation").addContent("exists"))
                            .addContent(new Element("value").addContent("0"))
                    );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(e)
                )
            );
            
        // test Exists
        
        processModifierElements(e, v2);
        
        Assert.assertFalse(v2.getAvailable());        
    }
    
    public void testExistsProtectsEq() {
        Element e = new Element("variable").addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("none"))
                            .addContent(new Element("relation").addContent("exists"))
                            .addContent(new Element("value").addContent("1"))
                    ).addContent(
                      new Element("qualifier")
                            .addContent(new Element("variableref").addContent("none"))
                            .addContent(new Element("relation").addContent("eq"))
                            .addContent(new Element("value").addContent("3"))
                    );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(e)
                )
            );
            
        // test Exists
        
        processModifierElements(e, v2);
        
        Assert.assertFalse(v2.getAvailable());  
        jmri.util.JUnitAppender.assertErrorMessage("Arithmetic EQ operation when watched value doesn't exist");      
    }



    // from here down is testing infrastructure

    public QualifierAdderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", QualifierAdderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite(QualifierAdderTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(QualifierAdderTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
    
        p = new ProgDebugger();
        cvtable = new CvTableModel(new JLabel(""), p);
        model = new VariableTableModel(
                                    new JLabel(""),
                                    new String[] {"Name", "Value"},
                                    cvtable,
                                    new IndexedCvTableModel(new JLabel(""), p)
                               );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // add some elements
        Element el1, el2, el3;
        root.addContent(new Element("decoder")		// the sites information here lists all relevant
            .addContent(new Element("variables")
                .addContent(el1 = new Element("variable")
                    .setAttribute("CV","1")
                    .setAttribute("item", "one")
                    .addContent( new Element("decVal")
                        .setAttribute("max","31")
                        .setAttribute("min","1")
                        )
                    )
                .addContent(el2 = new Element("variable")
                    .setAttribute("CV","2")
                    .setAttribute("item","two")
                    .addContent( new Element("decVal")
                        .setAttribute("max","31")
                        .setAttribute("min","1")
                        )
                    )
                .addContent(el3 = new Element("variable")
                    .setAttribute("CV","3")
                    .setAttribute("item","three")
                    .addContent( new Element("decVal")
                        .setAttribute("max","31")
                        .setAttribute("min","1")
                        )
                    )
                )	// variables element
            ) // decoder element
            ; // end of adding contents

        // and test reading this
        model.setRow(0, el1);
        model.setRow(1, el2);
        model.setRow(1, el3);
        
        v1 = model.findVar("one");
        v2 = model.findVar("two");
        v3 = model.findVar("three");
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}

