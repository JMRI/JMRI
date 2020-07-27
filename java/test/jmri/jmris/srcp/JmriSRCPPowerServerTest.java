package jmri.jmris.srcp;

import jmri.InstanceManager;
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
    private PowerManager powerManager;

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER ON\n");
        Mockito.verify(powerManager).setPower(PowerManager.ON);
        Mockito.when(powerManager.getPower()).thenReturn(PowerManager.ON);
        ps.propertyChange(new PropertyChangeEvent(powerManager,"Power",PowerManager.OFF,PowerManager.ON));
        assertThat(sb.toString()).withFailMessage("status as a result of parsing on").endsWith("100 INFO 0 POWER ON\n\r");
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER OFF\n");
        Mockito.verify(powerManager).setPower(PowerManager.OFF);
        Mockito.when(powerManager.getPower()).thenReturn(PowerManager.OFF);
        ps.propertyChange(new PropertyChangeEvent(powerManager,"Power",PowerManager.ON,PowerManager.OFF));
        assertThat(sb.toString()).withFailMessage("status as a result of parsing off").endsWith("100 INFO 0 POWER OFF\n\r");
    }

    @Override
    @Test
    public void testPropertyChangeOnStatus() {
        Mockito.when(powerManager.getPower()).thenReturn(PowerManager.ON);
        ps.propertyChange(new PropertyChangeEvent(powerManager, "Power", PowerManager.OFF, PowerManager.ON));
        assertThat(sb.toString()).withFailMessage("status as a result of property change on").endsWith("100 INFO 0 POWER ON\n\r");
    }

    @Override
    @Test
    public void testPropertyChangeOffStatus()  {
        Mockito.when(powerManager.getPower()).thenReturn(PowerManager.OFF);
        ps.propertyChange(new PropertyChangeEvent(powerManager, "Power", PowerManager.ON, PowerManager.OFF));
        assertThat(sb.toString()).withFailMessage("status as a result of property change off").endsWith("100 INFO 0 POWER OFF\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOnSent(){
            assertThat(sb.toString()).withFailMessage("status as a result of on property change").endsWith("100 INFO 0 POWER ON\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOffSent(){
        assertThat(sb.toString()).withFailMessage("status as a result of off property change").endsWith("100 INFO 0 POWER OFF\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent() {
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").endsWith("499 ERROR unspecified error\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkUnknownStatusSent() {
        assertThat(sb.toString()).withFailMessage("send Unknown Status check").endsWith("411 ERROR unknown value\n\r");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        powerManager = Mockito.mock(PowerManager.class);
        InstanceManager.setDefault(PowerManager.class,powerManager);
        sb = new StringBuilder();
        OutputStream output = new OutputStream() {
            @Override
            public void write(int b) {
                sb.append((char) b);
            }
        };
        ps = new JmriSRCPPowerServer(output);
    }

    @AfterEach
    public void tearDown() {
        ps.dispose();
        ps = null;
        sb = null;
        powerManager = null;
        JUnitUtil.tearDown();
    }

}
