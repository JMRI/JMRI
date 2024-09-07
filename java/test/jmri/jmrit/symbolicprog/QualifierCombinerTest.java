package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;

import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;


/**
 *
 * @author Bob Jacobsen, Copyright 2014
 * @author Paul Bender, Copyright 2017
 */
public class QualifierCombinerTest {

    // Service routine for tests
    private ProgDebugger p = new ProgDebugger();
    private CvTableModel cvtable;
    private VariableTableModel model;
    // VariableValue v1;
    // VariableValue v2;
    // VariableValue v3;

    private static class TestArithmeticQualifier extends ArithmeticQualifier {

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
    public void testVariableGeAndLe() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "ge", "le"
        ArithmeticQualifier aq1 = new TestArithmeticQualifier(variable, 10, "ge");
        ArithmeticQualifier aq2 = new TestArithmeticQualifier(variable, 20, "le");

        ArrayList<Qualifier> q=new ArrayList<>();
        q.add(aq1);
        q.add(aq2);
        QualifierCombiner qc = new QualifierCombiner(q);
        
        Assert.assertEquals(false, qc.currentDesiredState());  // initially 3 above
        
        cv.setValue(10);
        Assert.assertEquals(true, qc.currentDesiredState());
        
        cv.setValue(20);
        Assert.assertEquals(true, qc.currentDesiredState());
        
        cv.setValue(5);
        Assert.assertEquals(false, qc.currentDesiredState());

        cv.setValue(25);
        Assert.assertEquals(false, qc.currentDesiredState());

    }

    @Test
    public void testVariableNeAndNe() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "ge", "le"
        ArithmeticQualifier aq1 = new TestArithmeticQualifier(variable, 1, "ne");
        ArithmeticQualifier aq2 = new TestArithmeticQualifier(variable, 7, "ne");

        ArrayList<Qualifier> q=new ArrayList<>();
        q.add(aq1);
        q.add(aq2);
        QualifierCombiner qc = new QualifierCombiner(q);
        
        Assert.assertEquals(true, qc.currentDesiredState());  // initially 3 above
        
        cv.setValue(2);
        Assert.assertEquals(true, qc.currentDesiredState());
        
        cv.setValue(6);
        Assert.assertEquals(true, qc.currentDesiredState());
        
        cv.setValue(1);
        Assert.assertEquals(false, qc.currentDesiredState());

        cv.setValue(7);
        Assert.assertEquals(false, qc.currentDesiredState());

    }

    @Test
    @Disabled("Test requires further development")
    public void testVariableNeAndNeFromXml() {
    }

    protected HashMap<String, CvValue> createCvMap() {
        return new HashMap<>();
    }

    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    @BeforeEach
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

        // v1 = model.findVar("one");
        // v2 = model.findVar("two");
        // v3 = model.findVar("three");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
