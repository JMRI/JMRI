package jmri.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        CvUtilTest.class,
        FileUtilTest.class,
        JUnitAppenderTest.class,
        IntlUtilitiesTest.class,
        Log4JUtilTest.class,
        MockShutDownManagerTest.class,
        PreferNumericComparatorTest.class,
        StringUtilTest.class,
        ThreadingUtilTest.class,
        ThreadingDemoAndTest.class,
        I18NTest.class,
        AlphanumComparatorTest.class,
        ColorUtilTest.class,
        MathUtilTest.class,
        JUnitSwingUtilTest.class,
        SwingTestCaseTest.class,
        
        jmri.util.docbook.PackageTest.class,
        jmri.util.exceptionhandler.PackageTest.class,
        jmri.util.jdom.PackageTest.class,
        jmri.util.junit.PackageTest.class,
        jmri.util.swing.PackageTest.class,
        jmri.util.zeroconf.PackageTest.class,

        jmri.util.prefs.PackageTest.class,
        jmri.util.javamail.PackageTest.class,
        jmri.util.davidflanagan.PackageTest.class,
        jmri.util.datatransfer.PackageTest.class,
        jmri.util.com.PackageTest.class,
        jmri.util.table.PackageTest.class,
        jmri.util.iharder.PackageTest.class,
        jmri.util.usb.PackageTest.class,
        jmri.util.xml.PackageTest.class,
        
        WaitHandlerTest.class,
        PropertyChangeEventQueueTest.class,
        DateUtilTest.class,
        BareBonesBrowserLaunchTest.class,
        ConnectionNameFromSystemNameTest.class,
        DnDStringImportHandlerTest.class,
        DnDTableExportHandlerTest.class,
        DnDTableImportExportHandlerTest.class,
        FileUtilSupportTest.class,
        GetArgumentListTest.class,
        GetClassPathTest.class,
        GetJavaPropertyTest.class,
        HelpUtilTest.class,
        JTextPaneAppenderTest.class,
        JmriInsetsTest.class,
        JmriJFrameTest.class,
        JmriLocalEntityResolverTest.class,
        JmriNullEntityResolverTest.class,
        LocoAddressComparatorTest.class,
        MouseInputAdapterInstallerTest.class,
        NamedBeanComparatorTest.class,
        NamedBeanPreferNumericComparatorTest.class,
        NamedBeanUserNameComparatorTest.class,
        NonNullArrayListTest.class,
        NoArchiveFileFilterTest.class,
        OrderedPropertiesTest.class,
        PhysicalLocationPanelTest.class,
        PhysicalLocationTest.class,
        PortNameMapperTest.class,
        SerialUtilTest.class,
        SystemNameComparatorTest.class,
        SystemTypeTest.class,
        TimerUtilTest.class,
        XmlFilenameFilterTest.class,
        JmriJFrameActionTest.class,
        JLogoutputFrameTest.class,
        WindowMenuTest.class,
        FileChooserFilterTest.class,
        JTreeWithPopupTest.class,
        MenuScrollerTest.class,
        ExternalLinkContentViewerUITest.class,
        PipeListenerTest.class,
        IterableEnumerationTest.class,
        BusyGlassPaneTest.class,
        MultipartMessageTest.class,
        NamedBeanExpectedStateTest.class,
        NamedBeanExpectedValueTest.class,
        QuickPromptUtilTest.class,
        UnzipFileClassTest.class,
        AbstractFrameActionTest.class,
        ValidatingInputPaneTest.class,

        // deliberately at end
        jmri.util.Log4JErrorIsErrorTest.class,
})

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
