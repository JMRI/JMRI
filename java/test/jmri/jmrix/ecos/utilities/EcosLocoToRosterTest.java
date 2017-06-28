package jmri.jmrix.ecos.utilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.ecos.EcosTrafficController;
import jmri.jmrix.ecos.EcosInterfaceScaffold;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EcosLocoToRosterTest {

    @Test
    public void testCTor() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        EcosLocoToRoster t = new EcosLocoToRoster(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EcosLocoToRosterTest.class.getName());

}
