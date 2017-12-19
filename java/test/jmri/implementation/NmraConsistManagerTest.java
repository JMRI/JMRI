package jmri.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NmraConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Test
    public void testCtor2(){
        Assert.assertNotNull("NmraConsistManager Default Constructor",new NmraConsistManager());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new NmraConsistManager(jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class));
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NmraConsistManagerTest.class);
}
