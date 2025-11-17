package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2018
 */
public class TextAreaFIFOTest {

    // String newLine = System.getProperty("line.separator");
    
    @Test
    public void testCTor() {
        TextAreaFIFO t = new TextAreaFIFO(2);
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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TextAreaFIFOTest.class);

}
