package jmri.util;

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JTreeWithPopupTest {

    @Test
    public void testCTor() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        JTreeWithPopup t = new JTreeWithPopup(node);
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JTreeWithPopupTest.class);

}
