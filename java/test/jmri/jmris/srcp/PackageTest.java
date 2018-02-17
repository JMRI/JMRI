package jmri.jmris.srcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JmriSRCPServerTest.class,
    jmri.jmris.srcp.parser.PackageTest.class,
    JmriSRCPTurnoutServerTest.class,
    JmriSRCPSensorServerTest.class,
    JmriSRCPPowerServerTest.class,
    JmriSRCPProgrammerServerTest.class,
    JmriSRCPTimeServerTest.class,
    BundleTest.class,
    JmriSRCPServerFrameTest.class,
    JmriSRCPServerActionTest.class,
    JmriSRCPServerManagerTest.class,
    JmriSRCPThrottleServerTest.class,
	JmriSRCPServerMenuTest.class,
	JmriSRCPServerPreferencesPanelTest.class,
	JmriSRCPServerPreferencesTest.class,
	JmriSRCPServiceHandlerTest.class,
	TimeStampedOutputTest.class
})

/**
 * Tests for the jmri.jmris.srcp package
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class PackageTest {
}
