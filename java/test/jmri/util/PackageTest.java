package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
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
        TestSuite suite = new TestSuite("jmri.util.PackageTest");   // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(FileUtilTest.class));
        suite.addTest(JUnitAppenderTest.suite());
        suite.addTest(IntlUtilitiesTest.suite());
        suite.addTest(Log4JUtilTest.suite());
        suite.addTest(NamedBeanHandleTest.suite());
        suite.addTest(OrderedHashtableTest.suite());
        suite.addTest(PreferNumericComparatorTest.suite());
        suite.addTest(StringUtilTest.suite());
        suite.addTest(ThreadingUtilTest.suite());
        suite.addTest(ThreadingDemoAndTest.suite());
        suite.addTest(I18NTest.suite());
        suite.addTest(AlphanumComparatorTest.suite());
        suite.addTest(ColorUtilTest.suite());
        suite.addTest(MathUtilTest.suite());
        suite.addTest(SwingTestCaseTest.suite());
        suite.addTest(jmri.util.docbook.PackageTest.suite());
        suite.addTest(jmri.util.exceptionhandler.PackageTest.suite());
        suite.addTest(jmri.util.jdom.PackageTest.suite());
        suite.addTest(jmri.util.swing.PackageTest.suite());

        suite.addTest(jmri.util.WaitHandlerTest.suite());
        suite.addTest(jmri.util.PropertyChangeEventQueueTest.suite());
        suite.addTest(jmri.util.zeroconf.PackageTest.suite());
        suite.addTest(jmri.util.DateUtilTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.prefs.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.javamail.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.davidflanagan.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.datatransfer.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.com.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.table.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.iharder.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BareBonesBrowserLaunchTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionNameFromSystemNameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DnDStringImportHandlerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DnDTableExportHandlerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DnDTableImportExportHandlerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(FileUtilSupportTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(FontUtilTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GetArgumentListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GetClassPathTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GetJavaPropertyTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(HelpUtilTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JTextPaneAppenderTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriInsetsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriJFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriLocalEntityResolverTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriNullEntityResolverTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoAddressComparatorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MouseInputAdapterInstallerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NamedBeanComparatorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NonNullArrayListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NoArchiveFileFilterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OrderedPropertiesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PhysicalLocationPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PhysicalLocationTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PortNameMapperTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ResizableImagePanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RuntimeUtilTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialUtilTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SocketUtilTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SystemNameComparatorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SystemTypeTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlFilenameFilterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.xml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriJFrameActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JLogoutputFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(WindowMenuTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.usb.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(FileChooserFilterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JTreeWithPopupTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MenuScrollerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ExternalLinkContentViewerUITest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PipeListenerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(IterableEnumerationTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BusyGlassPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MultipartMessageTest.class));

        // deliberately at end
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.util.Log4JErrorIsErrorTest.class));

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
