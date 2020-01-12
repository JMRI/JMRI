package jmri.jmrit.automat;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JythonSigletTest {

    @Test
    public void testCTor() {
        JythonSiglet t = new JythonSiglet("Test");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSiglet() {
        JythonSiglet js = new JythonSiglet(FileUtil.getAbsoluteFilename("program:java/test/jmri/jmrit/automat/jython-siglet.py"));
        Turnout input = InstanceManager.turnoutManagerInstance().provide("input");
        input.setCommandedState(Turnout.CLOSED);
        Turnout output = InstanceManager.turnoutManagerInstance().provide("output");
        output.setCommandedState(Turnout.THROWN);
        NamedBean[] inputs = {input};
        js.setInputs(inputs);
        js.start();
        assertTrue(js.isRunning());
        JUnitUtil.waitFor(() -> output.getState() == Turnout.CLOSED, "Siglet failed to function");
        js.stop();
        JUnitUtil.waitFor(() -> !js.isRunning(), "Siglet thread failed to stop");
        assertEquals(Turnout.CLOSED, output.getState());
        assertFalse(js.isRunning());
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

    // private final static Logger log = LoggerFactory.getLogger(JythonSigletTest.class);

}
