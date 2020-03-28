package jmri.jmris.srcp;

import jmri.InstanceManagerDelegate;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeEvent;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPPowerServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPPowerServerTest extends jmri.jmris.AbstractPowerServerTestBase {
        
    private StringBuilder sb = null;
    private InstanceManagerDelegate instanceManagerDelegate;
    private PowerManager powerManager;

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER ON\n");
        Mockito.verify(powerManager).setPower(PowerManager.ON);
        Mockito.when(powerManager.getPower()).thenReturn(PowerManager.ON);
        ps.propertyChange(new PropertyChangeEvent(powerManager,"Power",PowerManager.OFF,PowerManager.ON));
        assertThat(sb.toString()).endsWith("100 INFO 0 POWER ON\n\r").withFailMessage("status as a result of parsing on");
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER OFF\n");
        Mockito.verify(powerManager).setPower(PowerManager.OFF);
        Mockito.when(powerManager.getPower()).thenReturn(PowerManager.OFF);
        ps.propertyChange(new PropertyChangeEvent(powerManager,"Power",PowerManager.ON,PowerManager.OFF));
        assertThat(sb.toString()).endsWith("100 INFO 0 POWER OFF\n\r").withFailMessage("status as a result of parsing off");
    }

    @Override
    @Test
    public void testPropertyChangeOnStatus() {
        try {
            Mockito.when(powerManager.getPower()).thenReturn(PowerManager.ON);
            ps.propertyChange(new PropertyChangeEvent(powerManager, "Power", PowerManager.OFF, PowerManager.ON));
            assertThat(sb.toString()).endsWith("100 INFO 0 POWER ON\n\r").withFailMessage("status as a result of property change on");
        } catch (JmriException je) {
            //false exception due to mocking
        }
    }

    @Override
    @Test
    public void testPropertyChangeOffStatus()  {
        try {
            Mockito.when(powerManager.getPower()).thenReturn(PowerManager.OFF);
            ps.propertyChange(new PropertyChangeEvent(powerManager, "Power", PowerManager.ON, PowerManager.OFF));
            assertThat(sb.toString()).endsWith("100 INFO 0 POWER OFF\n\r").withFailMessage("status as a result of property change off");
        } catch (JmriException je) {
            //false exception due to mocking
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOnSent(){
            assertThat(sb.toString()).endsWith("100 INFO 0 POWER ON\n\r").withFailMessage("status as a result of on property change");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOffSent(){
        assertThat(sb.toString()).endsWith("100 INFO 0 POWER OFF\n\r").withFailMessage("status as a result of off property change");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent() {
        assertThat(sb.toString()).endsWith("499 ERROR unspecified error\n\r").withFailMessage("sendErrorStatus check");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkUnknownStatusSent() {
        assertThat(sb.toString()).endsWith("411 ERROR unknown value\n\r").withFailMessage("send Unknown Status check");
    }

    // The minimal setup for log4J
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        instanceManagerDelegate = Mockito.mock(InstanceManagerDelegate.class);
        powerManager = Mockito.mock(PowerManager.class);
        Mockito.when(instanceManagerDelegate.getDefault(PowerManager.class)).thenReturn(powerManager);
        Mockito.when(instanceManagerDelegate.getNullableDefault(PowerManager.class)).thenReturn(powerManager);
        sb = new StringBuilder();
        OutputStream output = new OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        };
        ps = new JmriSRCPPowerServer(output,instanceManagerDelegate);
    }

    @AfterEach
    public void tearDown() {
        ps.dispose();
        ps = null;
        sb = null;
        instanceManagerDelegate = null;
        powerManager = null;
        JUnitUtil.tearDown(); // ToDo refactor common teardown so there is a version that doesn't init instance manager.
    }

}
