package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.EcosInterfaceScaffold;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosLocoToRosterTest.class);

}
