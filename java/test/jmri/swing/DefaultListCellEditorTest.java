package jmri.swing;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultListCellEditorTest {

    @Test
    public void testCTorCheckBox() {
        DefaultListCellEditor<String> t = new DefaultListCellEditor<>(new JCheckBox());
        assertNotNull( t, "exists");
    }

    @Test
    public void testCTorComboBox() {
        DefaultListCellEditor<String> t = new DefaultListCellEditor<>(new JComboBox<String>());
        assertNotNull( t, "exists");
    }

    @Test
    public void testCTorTextField() {
        DefaultListCellEditor<String> t = new DefaultListCellEditor<>(new JTextField());
        assertNotNull( t, "exists");
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
