package jmri.jmrix.can.cbus.node;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    CbusAllocateNodeNumberTest.class,
    CbusNodeTest.class,
    CbusNodeConstantsTest.class,
    CbusBasicNodeEventTest.class,
    CbusNodeEventTest.class,
    CbusNodeEventTableDataModelTest.class,
    CbusNodeFromBackupTest.class,
    CbusNodeFromFcuTableDataModelTest.class,
    CbusNodeNVTableDataModelTest.class,
    CbusNodeSingleEventTableDataModelTest.class,
    CbusNodeTableDataModelTest.class,
    CbusNodeTrickleFetchTest.class,
    CbusNodeBackupManagerTest.class,
    CbusNodeCanListenerTest.class,
    CbusNodeEventManagerTest.class,
    CbusNodeNVManagerTest.class,
    CbusNodeParameterManagerTest.class,
    CbusNodeStatsTest.class,
    CbusNodeTimerManagerTest.class,
    CbusNodeBackupFileTest.class,
    CbusBasicNodeTest.class,
    CbusBasicNodeWithManagersTest.class,
    CbusBasicNodeWithMgrsCommandStationTest.class,
})

/**
 * Tests for the jmri.jmrix.can.cbus package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest  {
}
