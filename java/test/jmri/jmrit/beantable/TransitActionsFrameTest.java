package jmri.jmrit.beantable;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.SensorManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for TransitActionsFrame.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class TransitActionsFrameTest {

    private TransitActionsFrame frame;
    private final List<Transit> edited = new ArrayList<>();

    private static Transit transit(String name, String section, TransitSectionAction... actions) {
        Transit t = InstanceManager.getDefault(TransitManager.class).createNewTransit(name);
        Section s = InstanceManager.getDefault(SectionManager.class).createNewSection(section);
        TransitSection ts = new TransitSection(s, 1, Section.FORWARD);
        for (TransitSectionAction a : actions) {
            ts.addAction(a);
        }
        t.addTransitSection(ts);
        return t;
    }

    private static TransitSectionAction sensorAction(String sensor) {
        return new TransitSectionAction(TransitSectionAction.ENTRY, TransitSectionAction.SETSENSORACTIVE,
                0, 0, 0, "", sensor);
    }

    private void open() {
        ThreadingUtil.runOnGUI(() -> {
            frame = new TransitActionsFrame(edited::add);
            frame.initComponents();
        });
    }

    @Test
    public void testListsEveryTransitsActions() {
        open();
        assertEquals(3, frame.getTable().getRowCount());
        assertEquals("3 actions in 2 Transits; 1 with problems.", frame.summaryText());
        String text = frame.visibleText();
        assertTrue(text.startsWith("Transit\tSeq\tSection\tWhen\tWhat\tProblems\n"), text);
        assertTrue(text.contains("East\t1\tSecE\tOn Entry to this Section\tSet Sensor \"Gone\" to ACTIVE\t"
                + "Sensor \"Gone\" not found\n"), text);
        assertTrue(text.contains("West\t1\tSecW\tOn Entry to this Section\tPause for \"2\" fast minutes\t\n"), text);
    }

    @Test
    public void testChooseOneTransit() {
        open();
        ThreadingUtil.runOnGUI(() -> frame.setTransit("West"));
        assertEquals(2, frame.getTable().getRowCount());
        assertEquals("2 actions in 1 Transits; 0 with problems.", frame.summaryText());
        ThreadingUtil.runOnGUI(() -> frame.setTransit(null));
        assertEquals(3, frame.getTable().getRowCount());
    }

    @Test
    public void testFindAndProblemsOnly() {
        open();
        ThreadingUtil.runOnGUI(() -> frame.getFindField().setText("is1"));
        assertEquals(1, frame.getTable().getRowCount(), "find ignores case");
        assertTrue(frame.visibleText().contains("Set Sensor \"IS1\" to ACTIVE"));
        ThreadingUtil.runOnGUI(() -> {
            frame.getFindField().setText("");
            frame.getProblemsOnly().doClick();
        });
        assertEquals(1, frame.getTable().getRowCount());
        assertTrue(frame.visibleText().contains("Sensor \"Gone\" not found"));
    }

    @Test
    public void testRefreshShowsNewActions() {
        open();
        Transit t = InstanceManager.getDefault(TransitManager.class).getTransit("East");
        t.getTransitSectionList().get(0).addAction(sensorAction("IS1"));
        assertEquals(3, frame.getTable().getRowCount());
        ThreadingUtil.runOnGUI(() -> frame.refresh());
        assertEquals(4, frame.getTable().getRowCount());
    }

    @Test
    public void testEditOpensTheRowsTransit() {
        open();
        ThreadingUtil.runOnGUI(() -> frame.setTransit("East"));
        ThreadingUtil.runOnGUI(() -> frame.editRow(frame.getTable().convertRowIndexToModel(0)));
        assertEquals(List.of(InstanceManager.getDefault(TransitManager.class).getTransit("East")), edited);
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initInternalSensorManager();
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        transit("West", "SecW",
                new TransitSectionAction(TransitSectionAction.ENTRY, TransitSectionAction.PAUSE, 0, 2, 0, "", ""),
                sensorAction("IS1"));
        transit("East", "SecE", sensorAction("Gone"));
    }

    @AfterEach
    public void tearDown() {
        if (frame != null) {
            JUnitUtil.dispose(frame);
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
