package jmri.util.swing;

import javax.swing.*;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class JComboBoxUtilTest {

    @Test
    public void testCall() {
        JComboBox<String> c = new JComboBox<>(new String[]{"A", "B"});
        JComboBoxUtil.setupComboBoxMaxRows(c);
        Assert.assertTrue("Max Row Count", c.getMaximumRowCount() > 7);  // NOI18N
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBoxTest.class);

}
