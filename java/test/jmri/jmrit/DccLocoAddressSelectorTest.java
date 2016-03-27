package jmri.jmrit;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of DccLocoAddressSelector
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version	$Revision$
 */
public class DccLocoAddressSelectorTest extends TestCase {

    public void testCtor() {
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        Assert.assertNotNull("exists", sel);
    }

    // you can ask for the text field, and set the number using it
    public void testSetNumByField() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        f.setText("123");
        Assert.assertEquals("check number ", 123, sel.getAddress().getNumber());
    }

    // you can ask for the text field once, and only once
    String reportedError;

    public void testReqNumByField() {
        setThrottleManager();
        reportedError = null;
        DccLocoAddressSelector sel = new DccLocoAddressSelector() {
            void reportError(String msg) {
                reportedError = msg;
            }
        };
        JTextField f = sel.getTextField();
        Assert.assertTrue("1st OK", f != null);
        Assert.assertTrue("no msg from 1st", reportedError == null);
        Assert.assertTrue("2nd null", sel.getTextField() == null);
        Assert.assertTrue("msg from 2nd", reportedError != null);
    }

    // you can ask for the text field & combo box, and set using both (long addr)
    public void testSetTypeLongBySel() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        f.setText("323");
        b.setSelectedIndex(2);
        Assert.assertEquals("check number ", 323, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", true, sel.getAddress().isLongAddress());
    }

    // you can ask for the text field & combo box, and set using both (short addr)
    public void testSetTypeShortBySel() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        f.setText("23");
        b.setSelectedIndex(1);
        Assert.assertEquals("check number ", 23, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", false, sel.getAddress().isLongAddress());
    }

    // can leave selector box alone, and get sensical answers
    public void testLetTypeSitLong() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        Assert.assertNotNull("exists", b);
        f.setText("2023");
        Assert.assertEquals("check number ", 2023, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", true, sel.getAddress().isLongAddress());
    }

    public void testLetTypeSitShort() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        Assert.assertNotNull("exists", b);
        f.setText("23");
        Assert.assertEquals("check number ", 23, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", false, sel.getAddress().isLongAddress());
    }

    // if address not set, don't get a address object
    public void testNotSet() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        Assert.assertEquals("no object ", null, sel.getAddress());
    }

    // try setting the address after creation
    public void testSetNumByField1() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        f.setText("123");
        Assert.assertEquals("check initial number ", 123, sel.getAddress().getNumber());
        sel.setAddress(new DccLocoAddress(2000, true));
        Assert.assertEquals("check updated number ", 2000, sel.getAddress().getNumber());
        Assert.assertEquals("check updated type ", true, sel.getAddress().isLongAddress());
    }

    public void testSetNumByField2() {
        setThrottleManager();
        DccLocoAddressSelector sel = new DccLocoAddressSelector();
        JTextField f = sel.getTextField();
        f.setText("1220");
        Assert.assertEquals("check initial number ", 1220, sel.getAddress().getNumber());
        sel.setAddress(new DccLocoAddress(20, false));
        Assert.assertEquals("check updated number ", 20, sel.getAddress().getNumber());
        Assert.assertEquals("check updated type ", false, sel.getAddress().isLongAddress());
    }

    // from here down is testing infrastructure
    protected void setThrottleManager() {
        ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
        return;
    }

    public DccLocoAddressSelectorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DccLocoAddressSelectorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DccLocoAddressSelectorTest.class);
        return suite;
    }

}
