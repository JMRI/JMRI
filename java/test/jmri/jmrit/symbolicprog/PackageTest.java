package jmri.jmrit.symbolicprog;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the jmri.jmrix.symbolicprog package.
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class PackageTest extends TestCase {

    // check configuring the programmer
    public void testConfigProgrammer() {
        // initialize the system
        Programmer p = new ProgDebugger();
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        assertTrue(InstanceManager.getDefault(jmri.ProgrammerManager.class).getGlobalProgrammer() == p);
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.jmrit.symbolicprog.ArithmeticQualifierTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ValueQualifierTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.QualifierAdderTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.FnMapPanelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CompositeVariableValueTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(Pr1ImporterTest.class));
        suite.addTest(jmri.jmrit.symbolicprog.ComboCheckBoxTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ComboRadioButtonsTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.DecVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.EnumVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.HexVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.LongAddrVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.SplitVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CvValueTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(CvTableModelTest.class));
        suite.addTest(jmri.jmrit.symbolicprog.VariableTableModelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CombinedLocoSelListPaneTest.suite());

        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.symbolicprog.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.symbolicprog.autospeed.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.symbolicprog.symbolicframe.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(QualifierCombinerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConstantValueTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoSelPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NameFileTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NewLocoSelPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ProgDefaultTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ProgrammerConfigManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ProgrammerConfigPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpeedTableVarValueTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ValueEditorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ValueRendererTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CombinedLocoSelPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CombinedLocoSelTreePaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(KnownLocoSelPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoSelTreePaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ResetTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(FactoryResetActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CsvExportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CsvImportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GenericImportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LokProgImportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Pr1ImportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Pr1ExportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Pr1WinExportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(QuantumCvMgrImportActionTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
