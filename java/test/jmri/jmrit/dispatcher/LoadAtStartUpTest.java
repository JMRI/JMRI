package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMastManager;
import jmri.ThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.util.FileUtilSupport;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Steve Gigiel 2018
 *
 * Tests the LoadAtStartUp function for Dispatcher
 * In addition it tests auto running of a train.
 */
public class LoadAtStartUpTest {

    @Test
    @SuppressWarnings("null")
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        // load layout file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml");
        cm.load(f);
        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        FileUtilSupport.getDefault().setUserFilesPath("java/test/jmri/jmrit");
        // we need a throttle manager
        ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
        // signal mast manager
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);

        //set sensors inactive
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (Sensor s : sm.getNamedBeanSet()) {
            s.setState(Sensor.INACTIVE);
        }
        // place train on layout
        sm.getSensor("Occ South Platform").setState(Sensor.ACTIVE);

        // and load. only one of 2 trains will load
        d.loadAtStartup();

        Assert.assertTrue("Train Loaded", (d.getActiveTrainsList().size() == 1));

        //takes a while for trains to load.
        try {
            Thread.sleep(3000);
        } catch (Exception ex) {
        }
        // set up loco address
        DccLocoAddress addr = new DccLocoAddress(1000, true);
        // slep milli secs to allow all listeners to fire.
        int sleepMillis = 1000;
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        // check signals and speed
        Assert.assertTrue("1 West End Div Signal Green", smm.getSignalMast("West End Div").getAspect().equals("Clear"));
        Assert.assertTrue("1 West To South  Green", smm.getSignalMast("West To South").getAspect().equals("Clear"));
        Assert.assertTrue("1 South To East Signal Green",
                smm.getSignalMast("South To East").getAspect().equals("Approach"));
        Assert.assertTrue("1 East End Throat Signal Green",
                smm.getSignalMast("East End Throat").getAspect().equals("Stop"));
        float speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.15, speed, 0.01);

        sm.getSensor("Occ West Platform Switch").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return smm.getSignalMast("West End Div").getAspect().equals("Stop");},"Signal Just passed stop");
        Assert.assertTrue("2 West End Div Signal Stop", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("2 West To South  Green", smm.getSignalMast("West To South").getAspect().equals("Clear"));
        Assert.assertTrue("2 South To East Signal Green",
                smm.getSignalMast("South To East").getAspect().equals("Approach"));
        Assert.assertTrue("2 East End Throat Signal Green",
                smm.getSignalMast("East End Throat").getAspect().equals("Stop"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.15, speed, 0.01);

        sm.getSensor("Occ West Block").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South Platform").setState(Sensor.INACTIVE);
        sm.getSensor("Occ West Platform Switch").setState(Sensor.INACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("3 West End Div Signal Stop", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("3 West To South  Green", smm.getSignalMast("West To South").getAspect().equals("Clear"));
        Assert.assertTrue("3 South To East Signal Green",
                smm.getSignalMast("South To East").getAspect().equals("Clear"));
        Assert.assertTrue("3 East End Throat Signal Approach",
                smm.getSignalMast("East End Throat").getAspect().equals("Approach"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.60, speed, 0.01);

        sm.getSensor("Occ South Block").setState(Sensor.ACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("4 West End Div Signal Red", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("4 West To South  Red", smm.getSignalMast("West To South").getAspect().equals("Stop"));
        Assert.assertTrue("4 South To East Signal Green",
                smm.getSignalMast("South To East").getAspect().equals("Clear"));
        Assert.assertTrue("4 East End Throat Signal Green",
                smm.getSignalMast("East End Throat").getAspect().equals("Approach"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.60, speed, 0.01);

        sm.getSensor("Occ West Block").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Block").setState(Sensor.ACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("5 West End Div Signal Green", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("5 West To South  Red", smm.getSignalMast("West To South").getAspect().equals("Stop"));
        Assert.assertTrue("5 South To East Signal Red", smm.getSignalMast("South To East").getAspect().equals("Stop"));
        Assert.assertTrue("5 East End Throat Signal Green",
                smm.getSignalMast("East End Throat").getAspect().equals("Approach"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.15, speed, 0.01);

        sm.getSensor("Occ South Block").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("6 West End Div Signal Red", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("6 West To South  Red", smm.getSignalMast("West To South").getAspect().equals("Stop"));
        Assert.assertTrue("6 South To East Signal Red", smm.getSignalMast("South To East").getAspect().equals("Stop"));
        Assert.assertTrue("6 East End Throat Signal Red",
                smm.getSignalMast("East End Throat").getAspect().equals("Stop"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.15, speed, 0.01);

        sm.getSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("7 West End Div Signal Red", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("7 West To South  Red", smm.getSignalMast("West To South").getAspect().equals("Stop"));
        Assert.assertTrue("7 South To East Signal Red", smm.getSignalMast("South To East").getAspect().equals("Stop"));
        Assert.assertTrue("7 East End Throat Signal Red",
                smm.getSignalMast("East End Throat").getAspect().equals("Stop"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.15, speed, 0.01);

        sm.getSensor("Occ South Platform").setState(Sensor.ACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("8 West End Div Signal Red", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("8 West To South  Red", smm.getSignalMast("West To South").getAspect().equals("Stop"));
        Assert.assertTrue("8 South To East Signal Red", smm.getSignalMast("South To East").getAspect().equals("Stop"));
        Assert.assertTrue("8 East End Throat Signal Red",
                smm.getSignalMast("East End Throat").getAspect().equals("Stop"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.25, speed, 0.01); // yep, I know,  but true. Code needs to fixed.

        sm.getSensor("Occ East Block").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Platform Switch").setState(Sensor.INACTIVE);
        try {
            Thread.sleep(sleepMillis);
        } catch (Exception ex) {
        }
        Assert.assertTrue("9 West End Div Signal Red", smm.getSignalMast("West End Div").getAspect().equals("Stop"));
        Assert.assertTrue("9 West To South  Red", smm.getSignalMast("West To South").getAspect().equals("Stop"));
        Assert.assertTrue("9 South To East Signal Red", smm.getSignalMast("South To East").getAspect().equals("Stop"));
        Assert.assertTrue("9 East End Throat Signal Red",
                smm.getSignalMast("East End Throat").getAspect().equals("Stop"));
        speed = (float) m.getThrottleInfo(addr, "SpeedSetting");
        Assert.assertEquals(0.0, speed, 0.0); //stopped

        // cancel (terminate) the train.
        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();

        Assert.assertTrue("All trains terminated", (d.getActiveTrainsList().size() == 0));

        // cleanup window
        JUnitUtil.dispose(d);
    }

    protected void setThrottleManager() {
        ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
        return;
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();

    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}