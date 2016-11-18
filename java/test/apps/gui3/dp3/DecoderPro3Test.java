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
    @Ignore("can only set application name once in jmri.Application.  We need a way to reset this value for testing")
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

        };
        Assert.assertNotNull(a);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
