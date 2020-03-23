package jmri.jmris.srcp;

import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManagerDelegate;
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

    private InstanceManagerDelegate instanceManagerDelegate;

    @Test
    public void testCtor() {
        OutputStream output = new ByteArrayOutputStream();
        GlobalProgrammerManager programmerManager = Mockito.mock(GlobalProgrammerManager.class);
        Mockito.when(instanceManagerDelegate.getDefault(GlobalProgrammerManager.class)).thenReturn(programmerManager);
        Mockito.when(instanceManagerDelegate.getNullableDefault(GlobalProgrammerManager.class)).thenReturn(programmerManager);
        Programmer programmer = Mockito.mock(Programmer.class);
        Mockito.when(programmerManager.getGlobalProgrammer()).thenReturn(programmer);
        JmriSRCPProgrammerServer a = new JmriSRCPProgrammerServer(output,instanceManagerDelegate);
        assertThat(a).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUpForMockInstanceManager();
        instanceManagerDelegate = Mockito.mock(InstanceManagerDelegate.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
