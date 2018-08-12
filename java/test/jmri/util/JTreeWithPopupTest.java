package jmri.util;

import javax.swing.tree.DefaultMutableTreeNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JTreeWithPopupTest {

    @Test
    public void testCTor() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        JTreeWithPopup t = new JTreeWithPopup(node);
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(JTreeWithPopupTest.class);

}
