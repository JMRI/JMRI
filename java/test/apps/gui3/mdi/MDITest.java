package apps.gui3.mdi;

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
 * Description: Tests for the MDI application.
 *
 * @author  Paul Bender Copyright (C) 2016
 */
public class MDITest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String[] args = {"DecoderProConfig3.xml"};
        apps.AppsBase a = new MDI(args){
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
    }

    @After
    public void tearDown() {
    }


}
