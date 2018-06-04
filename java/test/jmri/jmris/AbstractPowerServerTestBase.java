package jmri.jmris;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Base set of tests for decendents of the jmri.jmris.AbstractPowerServer class
 *
 * @author Paul Bender Copyright (C) 2018
 */
abstract public class AbstractPowerServerTestBase {

    protected AbstractPowerServer ps = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(ps);
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
        try {
            jmri.InstanceManager.getDefault(jmri.PowerManager.class)
                            .setPower(jmri.PowerManager.ON);
            checkPowerOnSent();
        } catch (jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    // test the property change sequence for an OFF property change.
    @Test
    public void testPropertyChangeOffStatus() {
        try {
            jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.OFF);
            checkPowerOffSent();
        } catch (jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
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
    @Before
    abstract public void setUp(); 

}
