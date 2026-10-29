package jmri.jmrix.marklin.mcan.protocol;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBDiscoveryPlan}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBDiscoveryPlanTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // --- forClass: NEW protocol ---

    @Test
    public void testNewProtocolPlanHasExactlyOneStep() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.WEICHEN_CHEF_KOMBI);
        assertEquals(1, plan.steps().size());
    }

    @Test
    public void testNewProtocolPlanStepHasDelayZero() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.WEICHEN_CHEF_KOMBI);
        assertEquals(0L, plan.steps().get(0).delayMillis());
    }

    @Test
    public void testNewProtocolPlanStepFrameClassifiesAsCdBConfig() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.WEICHEN_CHEF_KOMBI);
        int[] frame = plan.steps().get(0).frame();
        assertEquals(FrameProtocol.CDB_CONFIG,
                ProtocolClassifier.classify(frame).protocol());
    }

    @Test
    public void testNewProtocolPlanStepFrameHasDlcZero() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.WEICHEN_CHEF_KOMBI);
        int[] frame = plan.steps().get(0).frame();
        assertEquals(0, frame[4] & 0x0F);
    }

    // --- forClass: OLD protocol ---

    @Test
    public void testOldProtocolPlanHas128Steps() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.LAMPEN_CHEF);
        assertEquals(128, plan.steps().size());
    }

    @Test
    public void testOldProtocolPlanStepsHaveIncreasingDelays() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.LAMPEN_CHEF);
        List<CdBDiscoveryPlan.DiscoveryStep> steps = plan.steps();
        for (int i = 1; i < steps.size(); i++) {
            assertTrue(steps.get(i).delayMillis() > steps.get(i - 1).delayMillis(),
                    "Step " + i + " delay must exceed step " + (i - 1) + " delay");
        }
    }

    @Test
    public void testOldProtocolPlanFirstStepDelayIsZero() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.LAMPEN_CHEF);
        assertEquals(0L, plan.steps().get(0).delayMillis());
    }

    @Test
    public void testOldProtocolPlanFramesContainClassId() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.LAMPEN_CHEF);
        int classId = CdBDeviceClass.LAMPEN_CHEF.getClassId();
        for (CdBDiscoveryPlan.DiscoveryStep step : plan.steps()) {
            assertEquals(classId, step.frame()[2] & 0xFF,
                    "OLD-protocol frame byte[2] must carry classId");
            assertEquals(0, step.frame()[4] & 0x0F,
                    "OLD-protocol frame DLC must be 0");
        }
    }

    // --- DiscoveryStep defensive copy ---

    @Test
    public void testStepFrameIsDefensiveCopy() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(CdBDeviceClass.WEICHEN_CHEF_KOMBI);
        int[] frame1 = plan.steps().get(0).frame();
        int[] frame2 = plan.steps().get(0).frame();
        assertNotSame(frame1, frame2);
    }

    // --- forClasses ---

    @Test
    public void testForClassesCombinesTwoNewProtocolClasses() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClasses(
                Arrays.asList(CdBDeviceClass.WEICHEN_CHEF_KOMBI, CdBDeviceClass.WEICHEN_CHEF_MAGNET));
        // 1 step per NEW-protocol class = 2 total
        assertEquals(2, plan.steps().size());
    }

    @Test
    public void testForClassesSecondClassStartsAfterClassBoundaryGap() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClasses(
                Arrays.asList(CdBDeviceClass.WEICHEN_CHEF_KOMBI, CdBDeviceClass.WEICHEN_CHEF_MAGNET));
        long firstDelay  = plan.steps().get(0).delayMillis();
        long secondDelay = plan.steps().get(1).delayMillis();
        assertTrue(secondDelay > firstDelay + CdBDiscoveryPlan.CLASS_BOUNDARY_GAP_MS - 1,
                "Second class must start at least CLASS_BOUNDARY_GAP_MS after first");
    }

    @Test
    public void testForClassesRejectsNullList() {
        assertThrows(NullPointerException.class, () -> CdBDiscoveryPlan.forClasses(null));
    }

    @Test
    public void testForClassesRejectsEmptyList() {
        assertThrows(IllegalArgumentException.class,
                () -> CdBDiscoveryPlan.forClasses(List.of()));
    }
}
