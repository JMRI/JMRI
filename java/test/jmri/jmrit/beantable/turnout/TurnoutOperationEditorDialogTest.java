package jmri.jmrit.beantable.turnout;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author Steve Young Copyright (C) 2021
 */
public class TurnoutOperationEditorDialogTest {
    
    @Test
    public void testCTor() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
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
