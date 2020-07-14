package jmri.util;

import javax.swing.JMenu;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MenuScrollerTest {

    @Test
    public void testCTor() {
        MenuScroller t = new MenuScroller(new JMenu());
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

    // private final static Logger log = LoggerFactory.getLogger(MenuScrollerTest.class);

}
