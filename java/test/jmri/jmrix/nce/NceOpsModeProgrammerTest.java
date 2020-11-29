package jmri.jmrix.nce;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private NceTrafficControlScaffold tcis = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        NceOpsModeProgrammer t = new NceOpsModeProgrammer(tcis,1024,true);
        programmer = t;
    }

    @AfterEach
    @Override
    public void tearDown() {
        programmer = null;
        tcis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceOpsModeProgrammerTest.class);

}
