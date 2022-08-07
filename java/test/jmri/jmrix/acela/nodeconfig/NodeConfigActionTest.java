package jmri.jmrix.acela.nodeconfig;

import jmri.InstanceManager;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of NodeConfigAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class NodeConfigActionTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testNodfeConfigStringMemoCtor() {
        NodeConfigAction action = new NodeConfigAction("Acela test Action", new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDefaultCtor() {
        AcelaSystemConnectionMemo memo = new AcelaSystemConnectionMemo();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, memo);
        NodeConfigAction action = new NodeConfigAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
