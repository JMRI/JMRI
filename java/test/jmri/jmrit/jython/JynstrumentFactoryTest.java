package jmri.jmrit.jython;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JynstrumentFactoryTest {

    // no Constructor test, tested class only supplies static methods

    @Test
    public void testJynstrumentFactoryInvalid() {
        Jynstrument t = JynstrumentFactory.createInstrument("", null);
        Assertions.assertNull(t);
        JUnitAppender.assertErrorMessageStartsWith("File name too short");
        JUnitAppender.assertErrorMessage("Path is null");
        JUnitAppender.assertErrorMessageStartsWith("Invalid Jynstrument,");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JynstrumentFactoryTest.class);

}
