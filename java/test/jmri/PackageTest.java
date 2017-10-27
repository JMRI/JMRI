package jmri;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.PackageTest");  // no tests in this class itself

        suite.addTest(jmri.BeanSettingTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.NamedBeanHandleManagerTest.suite());
        suite.addTest(jmri.BlockTest.suite());
        suite.addTest(jmri.BlockManagerTest.suite());
        suite.addTest(jmri.DccLocoAddressTest.suite());
        suite.addTest(jmri.InstanceManagerTest.suite());
        suite.addTest(jmri.NamedBeanTest.suite());
        suite.addTest(jmri.LightTest.suite());
        suite.addTest(new JUnit4TestAdapter(NmraPacketTest.class));
        suite.addTest(jmri.ConditionalVariableTest.suite());
        suite.addTest(jmri.PathTest.suite());
        suite.addTest(jmri.PathLengthTest.suite());
        suite.addTest(jmri.PushbuttonPacketTest.suite());
        suite.addTest(new JUnit4TestAdapter(SectionTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.SignalGroupTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.SignalMastLogicTest.class));
        suite.addTest(new JUnit4TestAdapter(TransitTest.class));
        suite.addTest(new JUnit4TestAdapter(TransitSectionTest.class));
        suite.addTest(new JUnit4TestAdapter(TransitSectionActionTest.class));
        suite.addTest(jmri.TurnoutTest.suite());
        suite.addTest(jmri.TurnoutOperationTest.suite());
        suite.addTest(jmri.ApplicationTest.suite());
        suite.addTest(jmri.AudioTest.suite());
        suite.addTest(jmri.IdTagTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.SchemaTest.class));
        suite.addTest(jmri.ProgrammingModeTest.suite());
        suite.addTest(new JUnit4TestAdapter(VersionTest.class));
        suite.addTest(jmri.beans.PackageTest.suite());
        suite.addTest(jmri.progdebugger.PackageTest.suite());
        suite.addTest(jmri.configurexml.PackageTest.suite());
        suite.addTest(jmri.implementation.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.managers.PackageTest.class));
        suite.addTest(jmri.jmrix.PackageTest.suite());
        suite.addTest(jmri.jmrit.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.swing.PackageTest.class));
        suite.addTest(jmri.util.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.web.PackageTest.class));
        suite.addTest(jmri.jmris.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.profile.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.server.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.plaf.PackageTest.class));
        suite.addTest(jmri.script.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(AudioExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(ProgrammerExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(ProgReadExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(ProgWriteExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(TimebaseRateExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.spi.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriPluginTest.class));
        suite.addTest(new JUnit4TestAdapter(MetadataTest.class));
        suite.addTest(new JUnit4TestAdapter(NoFeedbackTurnoutOperationTest.class));
        suite.addTest(new JUnit4TestAdapter(RawTurnoutOperationTest.class));
        suite.addTest(new JUnit4TestAdapter(ScaleTest.class));
        suite.addTest(new JUnit4TestAdapter(SectionManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(SensorTurnoutOperationTest.class));
        suite.addTest(new JUnit4TestAdapter(TransitManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(TurnoutOperationManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(EntryPointTest.class));
        suite.addTest(new JUnit4TestAdapter(RunCucumberTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
