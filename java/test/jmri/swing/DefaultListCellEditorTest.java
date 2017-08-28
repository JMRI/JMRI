package jmri.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultListCellEditorTest {

    @Test
    public void testCTorCheckBox() {
        DefaultListCellEditor t = new DefaultListCellEditor(new JCheckBox());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorComboBox() {
        DefaultListCellEditor t = new DefaultListCellEditor(new JComboBox());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorTextField() {
        DefaultListCellEditor t = new DefaultListCellEditor(new JTextField());
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

    private final static Logger log = LoggerFactory.getLogger(DefaultListCellEditorTest.class.getName());

}
