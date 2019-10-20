package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TamsOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        TamsOpsModeProgrammer t = new TamsOpsModeProgrammer(tc,1234,true);
        programmer = t;
    }

    @After
    @Override
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsOpsModeProgrammerTest.class);

}
