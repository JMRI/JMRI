package jmri.jmrix.ecos.utilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.ecos.EcosInterfaceScaffold;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AddRosterEntryToEcosTest {

    @Test
    public void testCTor() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        AddRosterEntryToEcos t = new AddRosterEntryToEcos("Add Roster Entry Test",memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AddRosterEntryToEcosTest.class.getName());

}
