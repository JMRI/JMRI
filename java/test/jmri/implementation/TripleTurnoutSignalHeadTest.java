package jmri.implementation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.TurnoutManager;
import jmri.Turnout;
import jmri.NamedBeanHandle;
<<<<<<< HEAD
=======
import jmri.SignalHead;
>>>>>>> JMRI/master

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
<<<<<<< HEAD
public class TripleTurnoutSignalHeadTest {
=======
public class TripleTurnoutSignalHeadTest extends AbstractSignalHeadTestBase {
>>>>>>> JMRI/master

    @Test
    public void testCTor() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
<<<<<<< HEAD
        NamedBeanHandle green = new NamedBeanHandle("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3");
        NamedBeanHandle red = new NamedBeanHandle("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2");
        NamedBeanHandle yellow = new NamedBeanHandle("yellpw handle",it3);
=======
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3");
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2");
        NamedBeanHandle<Turnout> yellow = new NamedBeanHandle<>("yellow handle",it3);
>>>>>>> JMRI/master
        TripleTurnoutSignalHead t = new TripleTurnoutSignalHead("Test Head",green,red,yellow);
        Assert.assertNotNull("exists",t);
    }

<<<<<<< HEAD
=======
    @Override
    public SignalHead getHeadToTest() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3");
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2");
        NamedBeanHandle<Turnout> yellow = new NamedBeanHandle<>("yellow handle",it3);
        return new TripleTurnoutSignalHead("Test Head",green,red,yellow);
    }

>>>>>>> JMRI/master
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

<<<<<<< HEAD
    private final static Logger log = LoggerFactory.getLogger(TripleTurnoutSignalHeadTest.class.getName());
=======
    //private final static Logger log = LoggerFactory.getLogger(TripleTurnoutSignalHeadTest.class.getName());
>>>>>>> JMRI/master

}
