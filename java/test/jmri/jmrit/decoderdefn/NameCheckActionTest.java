package jmri.jmrit.decoderdefn;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NameCheckActionTest {

    @Test
    public void testCTor() {
        NameCheckAction t = new NameCheckAction("Test",new javax.swing.JPanel());
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NameCheckActionTest.class);

}
