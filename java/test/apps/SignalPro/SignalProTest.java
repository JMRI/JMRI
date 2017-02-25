package apps.SignalPro;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SignalProTest {

    @Test
    @Ignore("Causes Exception")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SignalPro t = new SignalPro(new JFrame());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SignalProTest.class.getName());

}
