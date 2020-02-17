package jmri.jmris.srcp;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
