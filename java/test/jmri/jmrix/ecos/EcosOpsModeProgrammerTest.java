package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    @Override
    @Test
    public void testGetCanRead() {
        // Ecos supports railcom
        Assert.assertTrue("can read", programmer.getCanRead());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosOpsModeProgrammer t = new EcosOpsModeProgrammer(tc,25,false);
        programmer = t;
    }

    @AfterEach
    @Override
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosProgrammerTest.class);

}
