package jmri.jmris.srcp;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerAction class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerActionTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerAction a = new JmriSRCPServerAction();
        assertThat(a).isNotNull();
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerAction a = new JmriSRCPServerAction("Hello World");
        assertThat(a).isNotNull();
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
