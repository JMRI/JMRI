package jmri.jmrix.roco.z21;

import java.util.ArrayList;
import java.util.List;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21XNetTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2004
 * @author	Paul Bender Copyright 2016
 */
public class Z21XNetTurnoutManagerTest extends jmri.jmrix.lenz.XNetTurnoutManagerTest {

    @Override
    @After
    public void tearDown() {
	lnis = null;
	l = null;
        JUnitUtil.tearDown();
    }

    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        // create and register the manager object
        l = new Z21XNetTurnoutManager(lnis, "X");
        jmri.InstanceManager.setTurnoutManager(l);
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetTurnoutManagerTest.class);

}
