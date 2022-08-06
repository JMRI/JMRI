package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import jmri.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for VetoedBell class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class VetoedBellTest {

    @Test
    public void testConstruction() {
        Assert.assertNotNull( new VetoedBell("veto Sensor", new PhysicalBell("Bell output")));
    }
 
    @Test
    public void testBellStrokeAllowed() throws JmriException {
        Assert.assertNotNull(veto);
        veto.setState(Sensor.INACTIVE);
        
        Bell vbell = new VetoedBell("veto Sensor", new BellScaffold());
        vbell.ring();
        
        Assert.assertTrue(rung);
    }
    
    @Test
    public void testBellStrokeNotAllowed() throws JmriException  {
        Assert.assertNotNull(veto);
        veto.setState(Sensor.ACTIVE);
        
        Bell vbell = new VetoedBell("veto Sensor", new BellScaffold());
        vbell.ring();
        
        Assert.assertFalse(rung);
    }
    
    boolean rung;
    private Sensor veto = null;
    private Turnout bellTurnout = null;

    private class BellScaffold implements Bell {

        BellScaffold(){
            rung = false;
        }

        @Override
        public void ring() {
            rung = true;
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        
        veto = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1"); veto.setUserName("veto Sensor");
        bellTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1"); bellTurnout.setUserName("Bell output");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
