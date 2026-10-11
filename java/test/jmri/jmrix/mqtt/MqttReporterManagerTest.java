package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * MqttReporterManagerTest.java
 *
 * Test for the CbusReporterManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 * @author Steve Young Copyright (C) 2019
 */
public class MqttReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }

    @Override
    public String getSystemName(String i) {
        return "MR" + i;
    }

    @Test
    @Disabled
    @Override
    public void testRegisterDuplicateSystemName() {}

    @Test
    @Disabled
    @Override
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Disabled
    @Override
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    private MqttAdapterScaffold adapter = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        adapter = new MqttAdapterScaffold(true);

        Assertions.assertNotNull(adapter.getSystemConnectionMemo());
        l = new MqttReporterManager(adapter.getSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        l = null;
        Assertions.assertNotNull(adapter);
        adapter.dispose();
        JUnitUtil.tearDown();
    }

}
