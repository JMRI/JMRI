package jmri.util.table;

import javax.swing.JButton;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ButtonEditorTest {

    @Test
    public void testCTor() {
        ButtonEditor t = new ButtonEditor(new JButton("Test"));
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

    // private final static Logger log = LoggerFactory.getLogger(ButtonEditorTest.class);

}
