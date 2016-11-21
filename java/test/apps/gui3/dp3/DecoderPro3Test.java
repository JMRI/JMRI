package apps.gui3.dp3;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;

/**
 *
 * Description: Tests for the DecoderPro3 application.
 *
 * @author  Paul Bender Copyright (C) 2016
 */
public class DecoderPro3Test {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());        
        String[] args = {"DecoderProConfig3.xml"};
        apps.AppsBase a = new DecoderPro3(args){
            // force the application to not actually start.  
            // Just checking construction.
            @Override 
            protected void start(){}
           @Override
            protected void configureProfile(){
                 JUnitUtil.resetInstanceManager();
            }
            @Override
            protected void installConfigurationManager(){
                 JUnitUtil.initConfigureManager();
                 JUnitUtil.initDefaultUserMessagePreferences();
            }
            @Override
            protected void installManagers(){
                 JUnitUtil.initInternalTurnoutManager();
                 JUnitUtil.initInternalLightManager();
                 JUnitUtil.initInternalSensorManager();
                 JUnitUtil.initRouteManager();
                 JUnitUtil.initMemoryManager();
                 JUnitUtil.initDebugThrottleManager();
            }
            @Override
            protected void installShutDownManager(){
                 JUnitUtil.initShutDownManager();
            }
        };
        Assert.assertNotNull(a);
        // shutdown the application
        a.handleQuit();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.resetApplication();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetApplication();
        apps.tests.Log4JFixture.tearDown();
    }


}
