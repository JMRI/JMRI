package jmri.jmrit;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import org.junit.*;

/**
 * Test simple functioning of DccLocoAddressSelector
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @author      Paul Bender Copyright (C) 2018
 */
public class DccLocoAddressSelectorTest {
        
    private DccLocoAddressSelector sel = null;
 
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", sel);
    }

    // you can ask for the text field, and set the number using it
    @Test
    public void testSetNumByField() {
        JTextField f = sel.getTextField();
        f.setText("123");
        Assert.assertEquals("check number ", 123, sel.getAddress().getNumber());
    }

    // you can ask for the text field once, and only once
    String reportedError;

    @Test
    public void testReqNumByField() {
        reportedError = null;
        sel = new DccLocoAddressSelector() {
            @Override
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
    @Test
    public void testSetTypeLongBySel() {
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        f.setText("323");
        b.setSelectedIndex(2);
        Assert.assertEquals("check number ", 323, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", true, sel.getAddress().isLongAddress());
    }

    // you can ask for the text field & combo box, and set using both (short addr)
    @Test
    public void testSetTypeShortBySel() {
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        f.setText("23");
        b.setSelectedIndex(1);
        Assert.assertEquals("check number ", 23, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", false, sel.getAddress().isLongAddress());
    }

    // can leave selector box alone, and get sensical answers
    @Test
    public void testLetTypeSitLong() {
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        Assert.assertNotNull("exists", b);
        f.setText("2023");
        Assert.assertEquals("check number ", 2023, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", true, sel.getAddress().isLongAddress());
    }

    @Test
    public void testLetTypeSitShort() {
        JTextField f = sel.getTextField();
        JComboBox<String> b = sel.getSelector();
        Assert.assertNotNull("exists", b);
        f.setText("23");
        Assert.assertEquals("check number ", 23, sel.getAddress().getNumber());
        Assert.assertEquals("check type  ", false, sel.getAddress().isLongAddress());
    }

    // if address not set, don't get a address object
    @Test
    public void testNotSet() {
        Assert.assertEquals("no object ", null, sel.getAddress());
    }

    // try setting the address after creation
    @Test
    public void testSetNumByField1() {
        JTextField f = sel.getTextField();
        f.setText("123");
        Assert.assertEquals("check initial number ", 123, sel.getAddress().getNumber());
        sel.setAddress(new DccLocoAddress(2000, true));
        Assert.assertEquals("check updated number ", 2000, sel.getAddress().getNumber());
        Assert.assertEquals("check updated type ", true, sel.getAddress().isLongAddress());
    }

    @Test
    public void testSetNumByField2() {
        JTextField f = sel.getTextField();
        f.setText("1220");
        Assert.assertEquals("check initial number ", 1220, sel.getAddress().getNumber());
        sel.setAddress(new DccLocoAddress(20, false));
        Assert.assertEquals("check updated number ", 20, sel.getAddress().getNumber());
        Assert.assertEquals("check updated type ", false, sel.getAddress().isLongAddress());
    }

    @Before 
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        sel = new DccLocoAddressSelector();
    }

    @After
    public void tearDown() {
	sel = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
