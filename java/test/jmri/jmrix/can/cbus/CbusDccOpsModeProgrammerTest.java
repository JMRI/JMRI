package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusDccOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        CbusDccOpsModeProgrammer t = new CbusDccOpsModeProgrammer(100,true,new TrafficControllerScaffold());
        programmer = t;
    }

    @After
    @Override
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccOpsModeProgrammerTest.class);

}
