package jmri.jmrit.signalling;

import java.awt.GraphicsEnvironment;
import java.util.Hashtable;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SignallingPanelTest {

    @Test
    public void testNullCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Signalling Panel");
        
        new SignallingPanel(jf);
        // just checking for no exceptions in ctor
        
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Signalling Panel");
        SignallingPanel t = new SignallingPanel(jf);
        
        t.cancelPressed(null);
        JUnitUtil.dispose(jf);
    }


    @Test
    public void testDoubleCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Signalling Panel");
        
        jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        InstanceManager.sensorManagerInstance().provideSensor("IS1");
        InstanceManager.sensorManagerInstance().provideSensor("IS2");
        SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0001)");
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(sm1);
        SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(sm2);
        
        SignalMastLogic sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).newSignalMastLogic(sm1);
        sml.setDestinationMast(sm2);
        sml.allowAutoMaticSignalMastGeneration(false, sm2);
        // add a control sensor
        sml.addSensor("IS1", 1, sm2); // Active
        // add 1 control turnout
        Hashtable<NamedBeanHandle<Turnout>, Integer> hashTurnouts = new Hashtable<NamedBeanHandle<Turnout>, Integer>();
        NamedBeanHandle<Turnout> namedTurnout1 = nbhm.getNamedBeanHandle("IT1", it1);
        hashTurnouts.put(namedTurnout1, 1); // 1 = Closed
        sml.setTurnouts(hashTurnouts, sm2);        
        
        SignallingPanel t = new SignallingPanel(sm1, sm2, jf);
        
        t.applyPressed(null);
        
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultSignalMastManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignallingPanelTest.class);

}
