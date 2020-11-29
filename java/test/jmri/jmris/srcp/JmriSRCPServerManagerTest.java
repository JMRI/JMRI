package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerManager class 
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerManagerTest {

    @Test
    public void testGetInstance() {
        JmriSRCPServerManager a = JmriSRCPServerManager.getInstance();
        assertThat(a).isNotNull();
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
