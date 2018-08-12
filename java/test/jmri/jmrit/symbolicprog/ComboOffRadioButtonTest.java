package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import jmri.progdebugger.ProgDebugger;
import javax.swing.JLabel;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ComboOffRadioButtonTest {

    // copied from EnumVariableValueTest and modified slightly.
    private EnumVariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        EnumVariableValue v1 = new EnumVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, item);
        v1.nItems(10);
        v1.addItem("0");
        v1.addItem("1");
        v1.addItem("2");
        v1.addItem("3");
        v1.addItem("4");
        v1.addItem("5");
        v1.addItem("6");
        v1.addItem("7");
        v1.addItem("9", 9);
        // values needed for specific tests
        v1.addItem("40000", 40000);

        v1.lastItem();

        return v1;
    }


    @Test
    public void testCTor() {
        ProgDebugger p = new ProgDebugger();
        HashMap<String, CvValue> v = new HashMap<String, CvValue>();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        EnumVariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        String testArray[] = new String[2];
        testArray[0]="";
        testArray[1]="";
        ComboOffRadioButton t = new ComboOffRadioButton(new javax.swing.JComboBox<String>(testArray),variable);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ComboOffRadioButtonTest.class);

}
