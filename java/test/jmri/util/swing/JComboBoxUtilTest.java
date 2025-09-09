package jmri.util.swing;

import javax.swing.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class JComboBoxUtilTest {

    @Test
    public void testCall() {
        JComboBox<String> c = new JComboBox<>(new String[]{"A", "B"});
        JComboBoxUtil.setupComboBoxMaxRows(c);
        assertTrue( c.getMaximumRowCount() > 7, "Max Row Count");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBoxTest.class);

}
