package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import javax.swing.JLabel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author	Bob Jacobsen, Copyright 2014
 * @author Paul Bender, Copyright 2017
 */
public class QualifierCombinerTest {

    // Service routine for tests
    ProgDebugger p;
    CvTableModel cvtable;
    VariableTableModel model;
    VariableValue v1;
    VariableValue v2;
    VariableValue v3;

    class TestArithmeticQualifier extends ArithmeticQualifier {

        TestArithmeticQualifier(VariableValue watchedVal, int value, String relation) {
            super(watchedVal, value, relation);
        }

        @Override
        public void setWatchedAvailable(boolean t) {
        }

        @Override
        public boolean currentAvailableState() {
            return true;
        }
    }

    @Test
    public void testCtor(){
       ArrayList<Qualifier> q=new ArrayList<Qualifier>();
       q.add(new TestArithmeticQualifier(v1,5,"gt"));
       q.add(new TestArithmeticQualifier(v1,10,"lt"));
       QualifierCombiner qc = new QualifierCombiner(q);
       Assert.assertNotNull("Exists",qc); 
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        p = new ProgDebugger();
        cvtable = new CvTableModel(new JLabel(""), p);
        model = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                cvtable
        );

        // create a JDOM tree with just some elements
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        Element el1, el2, el3;
        root.addContent(new Element("decoder") // the sites information here lists all relevant
                .addContent(new Element("variables")
                        .addContent(el1 = new Element("variable")
                                .setAttribute("CV", "1")
                                .setAttribute("item", "one")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "31")
                                        .setAttribute("min", "1")
                                )
                        )
                        .addContent(el2 = new Element("variable")
                                .setAttribute("CV", "2")
                                .setAttribute("item", "two")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "31")
                                        .setAttribute("min", "1")
                                )
                        )
                        .addContent(el3 = new Element("variable")
                                .setAttribute("CV", "3")
                                .setAttribute("item", "three")
                                .addContent(new Element("decVal")
                                        .setAttribute("max", "31")
                                        .setAttribute("min", "1")
                                )
                        )
                ) // variables element
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

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
