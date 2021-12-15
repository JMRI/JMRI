package jmri.jmrit.beantable.turnout;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Steve Young Copyright (C) 2021
 */
public class TurnoutOperationEditorDialogTest {
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCTor() {
        Turnout testedTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IS1");
        TurnoutOperation proto = InstanceManager.getDefault(TurnoutOperationManager.class).getMatchingOperationAlways(testedTurnout);
        Assert.assertNotNull("proto exists",proto);
        
        TurnoutOperationEditorDialog t = new TurnoutOperationEditorDialog(proto,testedTurnout,null);
        Assert.assertNotNull("exists",t);
        
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
