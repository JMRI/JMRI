package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link DefaultConditionalExecute}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultConditionalExecuteTest {

    @Test
    public void testToggleLockOnATurnoutWithoutHardwareLockout() {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        DefaultConditionalExecute execute = new DefaultConditionalExecute(new DefaultConditional("IXIC 1"));
        ConditionalAction toggle = new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE,
                Conditional.Action.LOCK_TURNOUT, "IT1", Route.TOGGLE, "");
        Reference<Integer> count = new Reference<>(0);
        List<String> errors = new ArrayList<>();

        execute.lockTurnout(toggle, turnout, count, errors);
        assertTrue( turnout.isLocked(), "toggled to locked");
        turnout.setCommandedState(Turnout.THROWN);
        assertEquals( Turnout.CLOSED, turnout.getCommandedState(), "throw refused while locked");
        JUnitAppender.assertWarnMessageStartsWith("Turnout IT1 not set");

        execute.lockTurnout(toggle, turnout, count, errors);
        assertFalse( turnout.isLocked(), "toggled to unlocked");
        assertEquals( 2, count.get(), "two actions counted");
        assertTrue( errors.isEmpty(), "no errors");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
