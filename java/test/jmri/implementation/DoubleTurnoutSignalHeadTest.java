package jmri.implementation;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import jmri.util.MockPropertyChangeListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 * @author Balazs Racz Copyright (C) 2017
 */
public class DoubleTurnoutSignalHeadTest extends AbstractSignalHeadTestBase {

    protected MockPropertyChangeListener l = new MockPropertyChangeListener();

    @Test
    public void testCTor() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle", it);
        Turnout it2 = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1"); // deliberately use same system name?
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle", it2);
        new DoubleTurnoutSignalHead("Test Head", green, red);
        //Assert.assertNotNull("exists",t);
    }

    void createHead() {
        mGreenTurnout = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NamedBeanHandle<Turnout> green = new NamedBeanHandle<>("green handle", mGreenTurnout);
        mRedTurnout = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT2");
        NamedBeanHandle<Turnout> red = new NamedBeanHandle<>("red handle", mRedTurnout);
        mHead = new DoubleTurnoutSignalHead("Test Head", green, red);
    }

    void waitForTimer() {
        if (mHead.readUpdateTimer == null) return;
        while (mHead.readUpdateTimer.isRunning()) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
            }
        }
        // Makes sure that the timer's callback is not still pending in the Swing execution
        // thread by scheduling an execution there and waiting for it.
        try {
            SwingUtilities.invokeAndWait(() -> {
            });
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Turnout mRedTurnout;
    private Turnout mGreenTurnout;
    private DoubleTurnoutSignalHead mHead;

    @Test
    public void testSetAppearance() {
        createHead();

        mHead.setAppearance(SignalHead.RED);
        Assert.assertEquals(Turnout.THROWN, mRedTurnout.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, mGreenTurnout.getKnownState());
        mHead.setAppearance(SignalHead.GREEN);
        Assert.assertEquals(Turnout.CLOSED, mRedTurnout.getKnownState());
        Assert.assertEquals(Turnout.THROWN, mGreenTurnout.getKnownState());
        mHead.setAppearance(SignalHead.YELLOW);
        Assert.assertEquals(Turnout.THROWN, mRedTurnout.getKnownState());
        Assert.assertEquals(Turnout.THROWN, mGreenTurnout.getKnownState());
        mHead.setAppearance(SignalHead.RED);
        Assert.assertEquals(Turnout.THROWN, mRedTurnout.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, mGreenTurnout.getKnownState());
    }

    @Test
    public void testNotify() {
        createHead();
        mHead.setAppearance(SignalHead.RED);
        mHead.addPropertyChangeListener(l);

        mHead.setAppearance(SignalHead.YELLOW);
        verify(l.m).onChange("Appearance", SignalHead.YELLOW);
        verifyNoMoreInteractions(l.m);

        waitForTimer();
        verifyNoMoreInteractions(l.m);

        mHead.setAppearance(SignalHead.GREEN);
        verify(l.m).onChange("Appearance", SignalHead.GREEN);
        verifyNoMoreInteractions(l.m);

        waitForTimer();
        verifyNoMoreInteractions(l.m);
    }

    @Test
    public void testReadOutput() {
        createHead();

        mHead.setAppearance(SignalHead.RED);
        mHead.addPropertyChangeListener(l);
        Assert.assertEquals(SignalHead.RED, mHead.getAppearance());

        mRedTurnout.setCommandedState(Turnout.CLOSED);
        mGreenTurnout.setCommandedState(Turnout.THROWN);
        Assert.assertEquals(SignalHead.RED, mHead.getAppearance());
        verifyNoMoreInteractions(l.m);
        Assert.assertNotNull(mHead.readUpdateTimer); // Should be running.

        waitForTimer();
        verify(l.m).onChange("Appearance", SignalHead.GREEN);
        Assert.assertEquals(SignalHead.GREEN, mHead.getAppearance());
        reset(l.m);

        mRedTurnout.setCommandedState(Turnout.THROWN);
        verifyNoMoreInteractions(l.m);
        waitForTimer();
        verify(l.m).onChange("Appearance", SignalHead.YELLOW);
        Assert.assertEquals(SignalHead.YELLOW, mHead.getAppearance());
        verifyNoMoreInteractions(l.m);

        mRedTurnout.setCommandedState(Turnout.CLOSED);
        mGreenTurnout.setCommandedState(Turnout.CLOSED);
        verifyNoMoreInteractions(l.m);
        waitForTimer();
        verify(l.m).onChange("Appearance", SignalHead.DARK);
    }

    @Test
    public void testFlashingIgnoresTurnoutFeedback() {
        createHead();
        mHead.setAppearance(SignalHead.FLASHRED);
        mHead.addPropertyChangeListener(l);
        Assert.assertEquals(Turnout.THROWN, mRedTurnout.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, mGreenTurnout.getKnownState());
        Assert.assertEquals(SignalHead.FLASHRED, mHead.getAppearance());
        // Should not be running, since all commands came from us.
        Assert.assertNull(mHead.readUpdateTimer);
        verifyNoMoreInteractions(l.m);

        // Wait for the flash
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        Assert.assertEquals(SignalHead.FLASHRED, mHead.getAppearance()); // hasn't changed
        verifyNoMoreInteractions(l.m); // also no notification

        Assert.assertEquals(Turnout.CLOSED, mRedTurnout.getKnownState());
        Assert.assertEquals(Turnout.CLOSED, mGreenTurnout.getKnownState());

        mGreenTurnout.setCommandedState(Turnout.THROWN);
        verifyNoMoreInteractions(l.m);
        Assert.assertNotNull(mHead.readUpdateTimer); // now it's started
        waitForTimer();
        verifyNoMoreInteractions(l.m);
        Assert.assertEquals(SignalHead.FLASHRED, mHead.getAppearance()); // hasn't changed
    }    
    
    @Override
    public SignalHead getHeadToTest() {
        createHead();
        return mHead;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(DoubleTurnoutSignalHeadTest.class);

}
