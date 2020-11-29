package jmri.util;

import org.junit.Assume;
import org.junit.jupiter.api.*;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HelpUtilTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        
        JMenuBar menuBar = new JMenuBar();
        int initialMenuCount = menuBar.getMenuCount();
        HelpUtil.helpMenu(menuBar,"test",true);
        menuBar.getMenu(0);
        assertThat(menuBar.getMenuCount()).withFailMessage("Help Menu not created")
                .isGreaterThan(initialMenuCount);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HelpUtilTest.class);

}
