package jmri.jmrit.jython;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RunJythonScriptTest {

    @Test
    @DisabledIfHeadless
    public void testStringCTor() {
        RunJythonScript t = new RunJythonScript("Test");
        assertNotNull(t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        RunJythonScript t = new RunJythonScript("Test",new jmri.util.JmriJFrame(false,false));
        assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RunJythonScriptTest.class);

}
