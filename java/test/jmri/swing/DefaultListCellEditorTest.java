package jmri.swing;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultListCellEditorTest {

    @Test
    public void testCTorCheckBox() {
        DefaultListCellEditor<String> t = new DefaultListCellEditor<String>(new JCheckBox());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorComboBox() {
        DefaultListCellEditor<String> t = new DefaultListCellEditor<String>(new JComboBox<String>());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorTextField() {
        DefaultListCellEditor<String> t = new DefaultListCellEditor<String>(new JTextField());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultListCellEditorTest.class);

}
