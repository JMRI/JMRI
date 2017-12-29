package jmri.jmrit.withrottle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        ConsistControllerTest.class,
        ConsistFunctionControllerTest.class,
        DeviceServerTest.class,
        FacelessServerTest.class,
        MultiThrottleControllerTest.class,
        MultiThrottleTest.class,
        RouteControllerTest.class,
        ThrottleControllerTest.class,
        TrackPowerControllerTest.class,
        TurnoutControllerTest.class,
        WiFiConsistFileTest.class,
        WiFiConsistTest.class,
        WiFiConsistManagerTest.class,
        WiThrottleManagerTest.class,
        WiThrottlePreferencesTest.class,
        WiThrottlesListModelTest.class,
        WiThrottlePrefsPanelTest.class,
        ControllerFilterActionTest.class,
        ControllerFilterFrameTest.class,
        UserInterfaceTest.class,
        WiThrottleCreationActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.withrottle tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest  {
}
