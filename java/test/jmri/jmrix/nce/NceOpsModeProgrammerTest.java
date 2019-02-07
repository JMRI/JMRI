package jmri.jmrix.nce;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private NceTrafficControlScaffold tcis = null;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        NceOpsModeProgrammer t = new NceOpsModeProgrammer(tcis,1024,true);
        programmer = t;
    }

    @After
    public void tearDown() {
        programmer = null;
        tcis = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceOpsModeProgrammerTest.class);

}
