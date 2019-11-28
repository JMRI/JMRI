package jmri.jmrit.beantable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BlockTableActionTest.class,
        LightTableWindowTest.class,
        LogixTableActionTest.class,
        LRouteTableActionTest.class,
        OBlockTableActionTest.class,
        RouteTableActionTest.class,
        SensorTableWindowTest.class,
        SignalGroupTableActionTest.class,
        SignalHeadTableActionTest.class,
        TurnoutTableWindowTest.class,
        BundleTest.class,
        jmri.jmrit.beantable.signalmast.PackageTest.class,
        jmri.jmrit.beantable.sensor.PackageTest.class,
        jmri.jmrit.beantable.oblock.PackageTest.class,
        jmri.jmrit.beantable.beanedit.PackageTest.class,
        jmri.jmrit.beantable.usermessagepreferences.PackageTest.class,
        MemoryTableActionTest.class,
        AudioTableActionTest.class,
        BeanTableFrameTest.class,
        BeanTablePaneTest.class,
        EnablingCheckboxRendererTest.class,
        IdTagTableActionTest.class,
        IdTagTableTabActionTest.class,
        LightTableActionTest.class,
        LightTableTabActionTest.class,
        ListedTableActionTest.class,
        ListedTableFrameTest.class,
        MaintenanceTest.class,
        RailComTableActionTest.class,
        ReporterTableActionTest.class,
        ReporterTableTabActionTest.class,
        SectionTableActionTest.class,
        SensorTableActionTest.class,
        SensorTableTabActionTest.class,
        SignalGroupSubTableActionTest.class,
        SignalMastLogicTableActionTest.class,
        SignalMastTableActionTest.class,
        TransitTableActionTest.class,
        TurnoutTableActionTest.class,
        TurnoutTableTabActionTest.class,
        SetPhysicalLocationActionTest.class,
        AudioTablePanelTest.class,
        AudioTableFrameTest.class,
        AddNewBeanPanelTest.class,
        AddNewDevicePanelTest.class,
        AddNewHardwareDevicePanelTest.class,
})

/**
 * Tests for classes in the jmri.jmrit.beantable package
 *
 * @author	Bob Jacobsen Copyright 2004
 */
public class PackageTest {
}
