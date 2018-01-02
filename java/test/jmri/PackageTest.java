package jmri;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BeanSettingTest.class,
    BundleTest.class,
    NamedBeanHandleManagerTest.class,
    BlockTest.class,
    BlockManagerTest.class,
    DccLocoAddressTest.class,
    InstanceManagerTest.class,
    NamedBeanTest.class,
    LightTest.class,
    ManagerTest.class,
    NmraPacketTest.class,
    ConditionalVariableTest.class,
    PathTest.class,
    PathLengthTest.class,
    PushbuttonPacketTest.class,
    SectionTest.class,
    SignalGroupTest.class,
    SignalMastLogicTest.class,
    TransitTest.class,
    TransitSectionTest.class,
    TransitSectionActionTest.class,
    TurnoutTest.class,
    TurnoutOperationTest.class,
    ApplicationTest.class,
    AudioTest.class,
    IdTagTest.class,
    SchemaTest.class,
    ProgrammingModeTest.class,
    VersionTest.class,
    jmri.beans.PackageTest.class,
    jmri.progdebugger.PackageTest.class,
    jmri.configurexml.PackageTest.class,
    jmri.implementation.PackageTest.class,
    jmri.managers.PackageTest.class,
    jmri.jmrix.PackageTest.class,
    jmri.jmrit.PackageTest.class,
    jmri.swing.PackageTest.class,
    jmri.util.PackageTest.class,
    jmri.web.PackageTest.class,
    jmri.jmris.PackageTest.class,
    jmri.profile.PackageTest.class,
    jmri.server.PackageTest.class,
    jmri.plaf.PackageTest.class,
    jmri.script.PackageTest.class,
    AudioExceptionTest.class,
    JmriExceptionTest.class,
    ProgrammerExceptionTest.class,
    ProgReadExceptionTest.class,
    ProgWriteExceptionTest.class,
    TimebaseRateExceptionTest.class,
    jmri.spi.PackageTest.class,
    JmriPluginTest.class,
    MetadataTest.class,
    NoFeedbackTurnoutOperationTest.class,
    RawTurnoutOperationTest.class,
    ScaleTest.class,
    SectionManagerTest.class,
    SensorTurnoutOperationTest.class,
    TransitManagerTest.class,
    TurnoutOperationManagerTest.class,
    EntryPointTest.class,
    RunCucumberTest.class,})

/**
 * Invoke complete set of tests for the Jmri package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
public class PackageTest {
}
