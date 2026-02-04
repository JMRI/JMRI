package jmri.util.swing;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ValidatedTextFieldTest {

    @Test
    public void testCTor() {
        ValidatedTextField t = new ValidatedTextField(10, false, ".","test error");
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ValidatedTextFieldTest.class.getName());

}
