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

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class QuadOutputSignalHeadTest {

    @Test
    public void testCTor() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3");
        NamedBeanHandle<Turnout> red = new NamedBeanHandle("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2");
        NamedBeanHandle<Turnout> yellow = new NamedBeanHandle("yellow handle",it3);
        Turnout it4 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT4");
        NamedBeanHandle<Turnout> lunar = new NamedBeanHandle("lunar handle",it4);
        QuadOutputSignalHead t = new QuadOutputSignalHead("Test Head",green,red,yellow,lunar);
        Assert.assertNotNull("exists",t);
    }

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

    //private final static Logger log = LoggerFactory.getLogger(QuadOutputSignalHeadTest.class.getName());

}
