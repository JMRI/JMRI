package jmri.jmrix.mqtt;

import java.util.List;

import jmri.*;
import jmri.Manager.NameValidity;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
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

    public void testRegisterDuplicateSystemName() {}
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    private MqttSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new MqttSystemConnectionMemo();
        memo.setMqttAdapter(new MqttAdapter());
        l = new MqttReporterManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
