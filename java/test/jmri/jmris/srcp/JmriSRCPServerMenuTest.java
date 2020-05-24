package jmri.jmris.srcp;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerMenu class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerMenuTest {

    @Test public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerMenu a = new JmriSRCPServerMenu();
        assertThat(a).isNotNull();
    }

    @Test public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerMenu a = new JmriSRCPServerMenu("Hello World");
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
