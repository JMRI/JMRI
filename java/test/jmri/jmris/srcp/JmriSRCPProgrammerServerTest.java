package jmri.jmris.srcp;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPProgrammerServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPProgrammerServerTest{

    @Test
    public void testCtor() {
        OutputStream output = new ByteArrayOutputStream();
        GlobalProgrammerManager programmerManager = Mockito.mock(GlobalProgrammerManager.class);
        InstanceManager.setDefault(GlobalProgrammerManager.class,programmerManager);
        Programmer programmer = Mockito.mock(Programmer.class);
        Mockito.when(programmerManager.getGlobalProgrammer()).thenReturn(programmer);
        JmriSRCPProgrammerServer a = new JmriSRCPProgrammerServer(output);
        assertThat(a).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
