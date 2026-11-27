package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MergSD2SignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testCTor() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle",it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT3");
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle",it2);
        Turnout it3 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2");
        NamedBeanHandle<Turnout> yellow = new NamedBeanHandle<>("yellow handle",it3);
        MergSD2SignalHead t = new MergSD2SignalHead("testsys","testuser",3,green,yellow,red,false,false);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInvalidAppearanceIsNotStored() {
        SignalHead h = getHeadToTest(); // three aspects: red, yellow, green
        h.setAppearance(SignalHead.GREEN);
        h.setAppearance(SignalHead.LUNAR);
        Assert.assertEquals("still shows green", SignalHead.GREEN, h.getAppearance());
        h.setAppearance(SignalHead.FLASHRED);
        Assert.assertEquals("still shows green", SignalHead.GREEN, h.getAppearance());
        // a later change of lit state redraws the stored appearance, without complaint
        h.setLit(false);
        h.setLit(true);
        Assert.assertEquals(SignalHead.GREEN, h.getAppearance());
    }

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private static final Logger log = LoggerFactory.getLogger(MergSD2SignalHeadTest.class);

}
