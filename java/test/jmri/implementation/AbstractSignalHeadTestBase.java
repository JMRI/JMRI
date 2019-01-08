package jmri.implementation;

import java.util.Arrays;
import jmri.SignalHead;
import org.junit.Assert;
import org.junit.Test;

/**
 * Base support for testing SignalHead implementations
 *
 * @author	Bob Jacobsen Copyright (C) 2017
 */
public abstract class AbstractSignalHeadTestBase {

    abstract SignalHead getHeadToTest();
    
    @Test
    final public void testLit() {
        SignalHead s = getHeadToTest();

        Assert.assertTrue("initial", s.getLit());
        
        s.setLit(false);
        Assert.assertTrue("set", !s.getLit());

        s.setLit(true);
        Assert.assertTrue("set again", s.getLit());
    }

    private boolean validAppearance(int appearance, SignalHead s) {
        for (int item : s.getValidStates() ) if (item == appearance ) return true;
        return false;
    }
    
    @Test
    final public void testHeld() {
        SignalHead s = getHeadToTest();

        Assert.assertTrue("initial", ! s.getHeld());
        
        s.setHeld(true);
        Assert.assertTrue("set", s.getHeld());

        s.setHeld(false);
        Assert.assertTrue("set again", ! s.getHeld());
    }

    @Test
    final public void testLitDarkIndependent() {
        SignalHead s = getHeadToTest();

        int appearance = s.getValidStates()[0];
        if (appearance == SignalHead.DARK) appearance = s.getValidStates()[1];
        
        s.setAppearance(appearance);
        s.setLit(false);
        Assert.assertTrue("not Lit", ! s.getLit());
        Assert.assertEquals("not Lit", appearance, s.getAppearance());

        appearance = s.getValidStates()[1];
        if (validAppearance(SignalHead.DARK, s)) appearance = SignalHead.DARK;
        
        s.setLit(true);
        s.setAppearance(appearance);
        Assert.assertTrue("Lit", s.getLit());
        Assert.assertEquals("Lit", appearance, s.getAppearance());
    }

    @Test
    final public void testBaseAppearanceStop() {
        SignalHead s = getHeadToTest();
        if (! validAppearance(SignalHead.RED, s)) return;
        s.setAppearance(SignalHead.RED);
        Assert.assertTrue( !s.isCleared() );
        Assert.assertTrue(  s.isAtStop() );
        Assert.assertTrue( !s.isShowingRestricting() );
    }
    @Test
    final public void testBaseAppearanceClearYellow() {
        SignalHead s = getHeadToTest();
        if (! validAppearance(SignalHead.YELLOW, s)) return;
        s.setAppearance(SignalHead.YELLOW);
        Assert.assertTrue(  s.isCleared() );
        Assert.assertTrue( !s.isAtStop() );
        Assert.assertTrue( !s.isShowingRestricting() );
    }
    @Test
    final public void testBaseAppearanceClearGreen() {
        SignalHead s = getHeadToTest();
        if (! validAppearance(SignalHead.GREEN, s)) return;
        s.setAppearance(SignalHead.GREEN);
        Assert.assertTrue(  s.isCleared() );
        Assert.assertTrue( !s.isAtStop() );
        Assert.assertTrue( !s.isShowingRestricting() );
    }
    @Test
    final public void testBaseAppearanceRestrictingFlashRed() {
        SignalHead s = getHeadToTest();
        if (! validAppearance(SignalHead.FLASHRED, s)) return;
        s.setAppearance(SignalHead.FLASHRED);
        Assert.assertTrue( !s.isCleared() );
        Assert.assertTrue( !s.isAtStop() );
        Assert.assertTrue(  s.isShowingRestricting() );
    }
    @Test
    final public void testBaseAppearanceRestrictingLunar() {
        SignalHead s = getHeadToTest();
        if (! validAppearance(SignalHead.LUNAR, s)) return;
        s.setAppearance(SignalHead.LUNAR);
        Assert.assertTrue( !s.isCleared() );
        Assert.assertTrue( !s.isAtStop() );
        Assert.assertTrue(  s.isShowingRestricting() );
    }
    

    @Test
    final public void testClearStopRestrictingOverlaps() {
        SignalHead s = getHeadToTest();
        for (int appearance : s.getValidStates()) {
            checkOverlaps(appearance);
        }
    }
    
    final void checkOverlaps(int appearance) {
        SignalHead s = getHeadToTest();
        s.setAppearance(appearance);
        
        Assert.assertTrue(! (s.isCleared() && s.isAtStop()));
        Assert.assertTrue(! (s.isAtStop() && s.isShowingRestricting()));
        Assert.assertTrue(! (s.isShowingRestricting() && s.isCleared()));
    }

    @Test
    public void testGetBeanType(){
         SignalHead s = getHeadToTest();
         Assert.assertEquals("bean type",s.getBeanType(),Bundle.getMessage("BeanNameSignalHead"));
    }

}
