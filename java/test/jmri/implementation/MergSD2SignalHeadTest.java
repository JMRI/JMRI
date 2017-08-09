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
public class MergSD2SignalHeadTest {
=======
public class MergSD2SignalHeadTest extends AbstractSignalHeadTestBase {
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
        MergSD2SignalHead t = new MergSD2SignalHead("testsys","testuser",3,green,yellow,red,false,false);
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
        return new MergSD2SignalHead("testsys","testuser",3,green,yellow,red,false,false);
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
    private final static Logger log = LoggerFactory.getLogger(MergSD2SignalHeadTest.class.getName());
=======
    //private final static Logger log = LoggerFactory.getLogger(MergSD2SignalHeadTest.class.getName());
>>>>>>> JMRI/master

}
