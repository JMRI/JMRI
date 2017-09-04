package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LsDecSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testCTor() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3"); // deliberately changed names
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2"); // deliberately changed names
        NamedBeanHandle<Turnout> yellow = new NamedBeanHandle<>("yellow handle",it3);
        Turnout it4 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT4");
        NamedBeanHandle<Turnout> flashgreen = new NamedBeanHandle<>("flash green handle",it4);
        Turnout it5 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT5");
        NamedBeanHandle<Turnout> flashred = new NamedBeanHandle<>("flash red handle",it5);
        Turnout it6 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT6");
        NamedBeanHandle<Turnout> flashyellow = new NamedBeanHandle<>("yellow handle",it6);
        Turnout it7 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT7");
        NamedBeanHandle<Turnout> off = new NamedBeanHandle<>("off handle",it7);
        LsDecSignalHead t = new LsDecSignalHead("testSys","testUser",green,1,red,2,yellow,3,flashgreen,4,flashred,5,flashyellow,6,off,7);
        Assert.assertNotNull("exists",t);
    }

    @Override
    public SignalHead getHeadToTest() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3"); // deliberately changed names
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2"); // deliberately changed names
        NamedBeanHandle<Turnout> yellow = new NamedBeanHandle<>("yellow handle",it3);
        Turnout it4 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT4");
        NamedBeanHandle<Turnout> flashgreen = new NamedBeanHandle<>("flash green handle",it4);
        Turnout it5 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT5");
        NamedBeanHandle<Turnout> flashred = new NamedBeanHandle<>("flash red handle",it5);
        Turnout it6 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT6");
        NamedBeanHandle<Turnout> flashyellow = new NamedBeanHandle<>("yellow handle",it6);
        Turnout it7 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT7");
        NamedBeanHandle<Turnout> off = new NamedBeanHandle<>("off handle",it7);
        return new LsDecSignalHead("testSys","testUser",green,1,red,2,yellow,3,flashgreen,4,flashred,5,flashyellow,6,off,7);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(LsDecSignalHeadTest.class);

}
