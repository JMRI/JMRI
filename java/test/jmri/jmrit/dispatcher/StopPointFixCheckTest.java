package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.SensorManager;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The set-up check of Sections' stop points.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class StopPointFixCheckTest {

    private Block block(String name, float length, boolean detected) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        b.setLength(length);
        if (detected) {
            b.setSensor(InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name).getSystemName());
        }
        return b;
    }

    private Section section(String name, Block... blocks) {
        Section s = InstanceManager.getDefault(SectionManager.class).createNewSection(name);
        for (Block b : blocks) {
            s.addBlock(b);
        }
        return s;
    }

    private static List<Finding> check(Section s, int direction) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            List<Finding> found = new ArrayList<>();
            StopPointFixCheck.check(s, direction, found);
            return found;
        });
    }

    @Test
    public void testStopPointNearAFix() {
        // a 1000 mm platform road, with a 300 mm stop Block at its far end
        Section s = section("Platform 1", block("Road", 1000, true), block("Stop", 300, true));
        s.setStopPointMm(Section.FORWARD, 1050); // 50 mm into the stop Block
        assertTrue(check(s, Section.FORWARD).isEmpty(), "50 mm past the start of a detected Block");
        assertTrue(check(s, Section.REVERSE).isEmpty(), "no reverse stop point");
    }

    @Test
    public void testStopPointFarFromAFix() {
        Block road = block("Road", 1000, true);
        Section s = section("Platform 1", road, block("Dark", 300, false));
        s.setStopPointMm(Section.FORWARD, 900);
        List<Finding> found = check(s, Section.FORWARD);
        assertEquals(1, found.size());
        assertEquals(Kind.STOP_POINT_FAR_FROM_FIX, found.get(0).getKind());
        assertSame(s, found.get(0).getSubject());
        assertEquals(List.of(road), found.get(0).getRelated());
        assertEquals("Section \"Platform 1\"'s forward stop point is 900 mm past the last place before it where a"
                + " train is seen, the start of Block \"Road\". A train stopping there by distance is placed only by"
                + " reckoning its speed over that distance, so it may stop about 18 mm out. A detector boundary or a"
                + " stopping sensor within 100 mm before the stop point would place it to a few mm.",
                found.get(0).getMessage());

        // the dark Block beyond doesn't help a stop point before it
        s.setStopPointMm(Section.FORWARD, 1100);
        assertTrue(check(s, Section.FORWARD).get(0).getMessage().contains("is 1100 mm past"));
    }

    @Test
    public void testGhostBlockIsNotAFix() {
        // a ghost Block's sensor is set by Dispatcher, not by a detector
        Block road = block("Road", 1000, true);
        Block ghost = block("Ghost", 300, true);
        ghost.setIsGhost(true);
        Section s = section("Platform 1", road, ghost);
        s.setStopPointMm(Section.FORWARD, 1050);
        List<Finding> found = check(s, Section.FORWARD);
        assertEquals(1, found.size(), "50 mm into a ghost Block is 1050 mm past the start of Road");
        assertEquals(List.of(road), found.get(0).getRelated());
    }

    @Test
    public void testStoppingSensorIsTheFix() {
        Section s = section("Platform 1", block("Road", 1000, true), block("Dark", 300, false));
        s.setStopPointMm(Section.FORWARD, 900);
        s.setForwardStoppingSensorName(InstanceManager.getDefault(SensorManager.class)
                .provideSensor("ISStop").getSystemName());
        assertTrue(check(s, Section.FORWARD).isEmpty());
    }

    @Test
    public void testReverseFromTheOtherEnd() {
        // reverse trains enter at the stop Block's end: 50 mm from there is in the stop Block
        Section s = section("Platform 1", block("Road", 1000, true), block("Stop", 300, true));
        s.setStopPointMm(Section.REVERSE, 50);
        assertTrue(check(s, Section.REVERSE).isEmpty());
        s.setStopPointMm(Section.REVERSE, 700);
        List<Finding> found = check(s, Section.REVERSE);
        assertEquals(1, found.size());
        assertTrue(found.get(0).getMessage().startsWith(
                "Section \"Platform 1\"'s reverse stop point is 400 mm past the last place before it where a train is"
                + " seen, the start of Block \"Road\"."), found.get(0).getMessage());
    }

    @Test
    public void testNoFixInTheSection() {
        Section s = section("Loop", block("Dark", 800, false), block("Road", 1000, true));
        s.setStopPointMm(Section.FORWARD, 500);
        List<Finding> found = check(s, Section.FORWARD);
        assertEquals(1, found.size());
        assertTrue(found.get(0).getMessage().startsWith(
                "Section \"Loop\"'s forward stop point, 500 mm into it, has no place before it"),
                found.get(0).getMessage());
    }

    @Test
    public void testBlockWithNoLengthBeforeTheStopPoint() {
        Section s = section("Platform 1", block("Road", 0, true), block("Dark", 300, false));
        s.setStopPointMm(Section.FORWARD, 900);
        assertTrue(check(s, Section.FORWARD).isEmpty(), "can't be placed by distance at all");
    }

    /**
     * How far is too far is a scale distance: 100 mm in HO, twice that in O
     * (1:43.5), and about 55 mm in N (1:160).
     */
    @Test
    public void testNearEnoughDependsOnTheLayoutScale() {
        jmri.implementation.SignalSpeedMap map = InstanceManager.getDefault(jmri.implementation.SignalSpeedMap.class);
        Section s = section("Platform 1", block("Road", 1000, true), block("Dark", 300, false));
        s.setStopPointMm(Section.FORWARD, 150);
        assertEquals(100, StopPointFixCheck.nearEnoughMm(), 0.5, "HO, the default");
        assertEquals(1, check(s, Section.FORWARD).size(), "150 mm is too far in HO");

        map.setLayoutScale(43.5f);
        assertEquals(200, StopPointFixCheck.nearEnoughMm(), 0.5);
        assertTrue(check(s, Section.FORWARD).isEmpty(), "150 mm is near enough in O");
        s.setStopPointMm(Section.FORWARD, 250);
        assertTrue(check(s, Section.FORWARD).get(0).getMessage().endsWith(
                "within 200 mm before the stop point would place it to a few mm."),
                check(s, Section.FORWARD).get(0).getMessage());

        map.setLayoutScale(160f);
        assertEquals(54.4, StopPointFixCheck.nearEnoughMm(), 0.1);
        s.setStopPointMm(Section.FORWARD, 60);
        assertEquals(1, check(s, Section.FORWARD).size(), "60 mm is too far in N");
    }

    @Test
    public void testListedInSetUpChecks() {
        Section s = section("Platform 1", block("Road", 1000, true), block("Dark", 300, false));
        s.setStopPointMm(Section.FORWARD, 900);
        List<Finding> all = ThreadingUtil.runOnGUIwithReturn(jmri.jmrit.trainrecord.TrainTrackingChecks::run);
        assertTrue(all.stream().anyMatch(f -> f.getKind() == Kind.STOP_POINT_FAR_FROM_FIX), all.toString());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initSectionManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
