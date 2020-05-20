package jmri.jmris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Base set of tests for descendants of the jmri.jmris.AbstractPowerServer class
 *
 * @author Paul Bender Copyright (C) 2018
 */
abstract public class AbstractPowerServerTestBase {

    protected AbstractPowerServer ps = null;

    @Test
    public void testCtor() {
        assertThat(ps).isNotNull();
    }

    // test sending an error message.
    @Test
    public void testSendErrorStatus() throws Exception {
        ps.sendErrorStatus();
        checkErrorStatusSent();
    }

    // test sending an On Status.
    @Test
    public void testSendOnStatus() throws Exception {
        ps.sendStatus(jmri.PowerManager.ON);
        checkPowerOnSent();
    }

    // test sending an OFF Status.
    @Test
    public void testSendOffStatus() throws Exception {
        ps.sendStatus(jmri.PowerManager.OFF);
        checkPowerOffSent();
    }

    // test sending an Unknown Status.
    @Test
    public void testSendUnknownStatus() throws Exception {
        ps.sendStatus(-1);
        checkUnknownStatusSent();
    }

    // test the property change sequence for an ON property change.
    @Test
    public void testPropertyChangeOnStatus() {
         Throwable thrown = catchThrowable( () -> jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.ON));
         checkPowerOnSent();
         assertThat(thrown).withFailMessage("Exception setting Status").isNull();
    }

    // test the property change sequence for an OFF property change.
    @Test
    public void testPropertyChangeOffStatus() {
        Throwable thrown = catchThrowable( () -> jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.OFF));
        assertThat(thrown).withFailMessage("Exception setting Status").isNull();
        checkPowerOffSent();
    }

    /**
     * assert that power on was sent
     */
    abstract public void checkPowerOnSent(); 
    /**
     * assert that power off was sent
     */
    abstract public void checkPowerOffSent();  
    /**
     * assert that an unknown status was sent
     */
    abstract public void checkUnknownStatusSent();  
    /**
     * assert that an error status was sent
     */
    abstract public void checkErrorStatusSent();  

    /**
     * Setup ps and a power manager instance;
     *
     */
    @BeforeEach
    abstract public void setUp(); 

}
