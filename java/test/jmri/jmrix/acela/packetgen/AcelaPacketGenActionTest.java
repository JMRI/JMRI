package jmri.jmrix.acela.packetgen;

import jmri.InstanceManager;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of AcelaPacketGenAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AcelaPacketGenActionTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void testStringMemoCtorAcela() {
        AcelaPacketGenAction action = new AcelaPacketGenAction("Acela test Action", new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void testDefaultCtor() {
        AcelaSystemConnectionMemo memo = new AcelaSystemConnectionMemo();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, memo);
        AcelaPacketGenAction action = new AcelaPacketGenAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
