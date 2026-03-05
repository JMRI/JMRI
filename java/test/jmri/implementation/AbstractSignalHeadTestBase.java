package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import jmri.SignalHead;

import org.junit.jupiter.api.*;

/**
 * Base support for testing SignalHead implementations
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public abstract class AbstractSignalHeadTestBase {

    abstract SignalHead getHeadToTest();

    @Test
    public final void testLit() {
        SignalHead s = getHeadToTest();

        assertTrue(s.getLit(), "initial");

        s.setLit(false);
        assertFalse(s.getLit(), "set");

        s.setLit(true);
        assertTrue(s.getLit(), "set again");
    }

    private boolean validAppearance(int appearance, SignalHead s) {
        for (int item : s.getValidStates() ) if (item == appearance ) return true;
        return false;
    }

    @Test
    public final void testHeld() {
        SignalHead s = getHeadToTest();

        assertFalse(s.getHeld(), "initial");

        s.setHeld(true);
        assertTrue(s.getHeld(), "set");

        s.setHeld(false);
        assertFalse(s.getHeld(), "set again");
    }

    @Test
    public final void testLitDarkIndependent() {
        SignalHead s = getHeadToTest();

        int appearance = s.getValidStates()[0];
        if (appearance == SignalHead.DARK) appearance = s.getValidStates()[1];

        s.setAppearance(appearance);
        s.setLit(false);
        assertFalse(s.getLit(), "not Lit");
        assertEquals(appearance, s.getAppearance(), "not Lit");

        appearance = s.getValidStates()[1];
        if (validAppearance(SignalHead.DARK, s)) appearance = SignalHead.DARK;

        s.setLit(true);
        s.setAppearance(appearance);
        assertTrue(s.getLit(), "Lit");
        assertEquals(appearance, s.getAppearance(), "Lit");
    }

    @Test
    public final void testBaseAppearanceStop() {
        SignalHead s = getHeadToTest();
        assumeTrue(validAppearance(SignalHead.RED, s), "Head does not have RED");
        s.setAppearance(SignalHead.RED);
        assertFalse(s.isCleared());
        assertTrue(s.isAtStop());
        assertFalse(s.isShowingRestricting());
    }

    @Test
    public final void testBaseAppearanceClearYellow() {
        SignalHead s = getHeadToTest();
        assumeTrue(validAppearance(SignalHead.YELLOW, s), "Head does not have YELLOW");
        s.setAppearance(SignalHead.YELLOW);
        assertTrue(s.isCleared());
        assertFalse(s.isAtStop());
        assertFalse(s.isShowingRestricting());
    }

    @Test
    public final void testBaseAppearanceClearGreen() {
        SignalHead s = getHeadToTest();
        assumeTrue(validAppearance(SignalHead.GREEN, s), "Head does not have GREEN");
        s.setAppearance(SignalHead.GREEN);
        assertTrue(s.isCleared());
        assertFalse(s.isAtStop());
        assertFalse(s.isShowingRestricting());
    }

    @Test
    public final void testBaseAppearanceRestrictingFlashRed() {
        SignalHead s = getHeadToTest();
        assumeTrue(validAppearance(SignalHead.FLASHRED, s), "Head does not have FLASHRED");
        s.setAppearance(SignalHead.FLASHRED);
        assertFalse(s.isCleared());
        assertFalse(s.isAtStop());
        assertTrue(s.isShowingRestricting());
    }

    @Test
    public final void testBaseAppearanceRestrictingLunar() {
        SignalHead s = getHeadToTest();
        assumeTrue(validAppearance(SignalHead.LUNAR, s), "Head does not have LUNAR");
        s.setAppearance(SignalHead.LUNAR);
        assertFalse(s.isCleared());
        assertFalse(s.isAtStop());
        assertTrue(s.isShowingRestricting());
    }

    @Test
    public final void testClearStopRestrictingOverlaps() {
        SignalHead s = getHeadToTest();
        for (int appearance : s.getValidStates()) {
            checkOverlaps(appearance);
        }
    }

    final void checkOverlaps(int appearance) {
        SignalHead s = getHeadToTest();
        s.setAppearance(appearance);
        
        assertFalse((s.isCleared() && s.isAtStop()));
        assertFalse((s.isAtStop() && s.isShowingRestricting()));
        assertFalse((s.isShowingRestricting() && s.isCleared()));
    }

    @Test
    public void testGetBeanType(){
        SignalHead s = getHeadToTest();
        assertEquals(s.getBeanType(),Bundle.getMessage("BeanNameSignalHead"), "bean type");
    }

    @Test
    public void testDescribeState() {
        SignalHead s = getHeadToTest();
        assertEquals(Bundle.getMessage("SignalHeadStateDark"), s.describeState(SignalHead.DARK) );
        assertEquals(Bundle.getMessage("SignalHeadStateRed"), s.describeState(SignalHead.RED) );
        assertEquals(Bundle.getMessage("SignalHeadStateFlashingRed"), s.describeState(SignalHead.FLASHRED) );
        assertEquals(Bundle.getMessage("SignalHeadStateYellow"), s.describeState(SignalHead.YELLOW) );
        assertEquals(Bundle.getMessage("SignalHeadStateFlashingYellow"), s.describeState(SignalHead.FLASHYELLOW) );
        assertEquals(Bundle.getMessage("SignalHeadStateGreen"), s.describeState(SignalHead.GREEN) );
        assertEquals(Bundle.getMessage("SignalHeadStateFlashingGreen"), s.describeState(SignalHead.FLASHGREEN) );
        assertEquals(Bundle.getMessage("SignalHeadStateLunar"), s.describeState(SignalHead.LUNAR) );
        assertEquals(Bundle.getMessage("SignalHeadStateFlashingLunar"), s.describeState(SignalHead.FLASHLUNAR) );
        assertEquals(Bundle.getMessage("SignalHeadStateHeld"), s.describeState(SignalHead.HELD) );
        assertEquals(
            Bundle.getMessage("SignalHeadStateYellow") + " " + Bundle.getMessage("SignalHeadStateHeld"),
            s.describeState( SignalHead.YELLOW + SignalHead.HELD ) );
        assertEquals(
            Bundle.getMessage("SignalHeadStateRed") + " " + Bundle.getMessage("SignalHeadStateFlashingRed"),
            s.describeState( SignalHead.RED + SignalHead.FLASHRED ) );
    }

}
