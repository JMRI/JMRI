package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Section;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.implementation.DefaultSection;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.mockito.Mockito;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoTrainActionTest {

    @Test
    public void testCTor() {

        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class,df);

        jmri.Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit,"Train",ActiveTrain.USER);
        AutoActiveTrain aat = new AutoActiveTrain(at);
        AutoTrainAction t = new AutoTrainAction(aat);
        Assertions.assertNotNull(t, "exists");
    }

    private AutoTrainAction newAction() {
        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);
        jmri.Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        return new AutoActiveTrain(at).getAutoTrainAction();
    }

    // A Section entry action that sets sensor IS1 active after a delay.
    private TransitSection sectionWithDelayedSensorSet(int delay) {
        Section section = new DefaultSection("TS1");
        TransitSection ts = new TransitSection(section, 1, Section.FORWARD);
        ts.addAction(new TransitSectionAction(TransitSectionAction.ENTRY,
                TransitSectionAction.SETSENSORACTIVE, delay, 0, 0, "", "IS1"));
        return ts;
    }

    @Test
    public void testDelayedActionRunsOnLayoutThread() {
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        List<Boolean> setOnLayoutThread = Collections.synchronizedList(new ArrayList<>());
        sensor.addPropertyChangeListener("KnownState",
                e -> setOnLayoutThread.add(ThreadingUtil.isLayoutThread()));

        newAction().addTransitSection(sectionWithDelayedSensorSet(20));

        JUnitUtil.waitFor(() -> sensor.getKnownState() == Sensor.ACTIVE, "sensor set by the action");
        Assertions.assertEquals(List.of(true), setOnLayoutThread,
                "the delayed action set the sensor on the layout thread");
    }

    @Test
    public void testActionClearedWhileWaitingToContinueDoesNotRun() throws InterruptedException {
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        AutoTrainAction action = newAction();

        // Hold the layout thread until the delay is over and the action has been cleared,
        // as when the train is terminated while the layout thread is busy.
        CountDownLatch layoutBusy = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ThreadingUtil.runOnLayoutEventually(() -> {
            layoutBusy.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Assertions.assertTrue(layoutBusy.await(10, TimeUnit.SECONDS));

        TransitSection ts = sectionWithDelayedSensorSet(20);
        action.addTransitSection(ts);
        Thread waiting = ts.getTransitSectionActionList().get(0).getWaitingThread();
        Assertions.assertNotNull(waiting, "the action waits on a thread of its own");
        JUnitUtil.waitFor(() -> !waiting.isAlive(), "the delay is over");
        action.clearRemainingActions();
        release.countDown();

        ThreadingUtil.runOnLayout(() -> { }); // let anything queued behind the hold run
        Assertions.assertNotEquals(Sensor.ACTIVE, sensor.getKnownState(),
                "an action cleared before it continued is not run");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(AutoTrainActionTest.class);

}
