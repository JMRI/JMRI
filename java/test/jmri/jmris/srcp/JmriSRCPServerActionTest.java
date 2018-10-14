package jmri.jmris.srcp;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerAction class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerActionTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerAction a = new JmriSRCPServerAction();
        Assert.assertNotNull(a);
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerAction a = new JmriSRCPServerAction("Hello World");
        Assert.assertNotNull(a);
    }

    @Before public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
