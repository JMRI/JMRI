package jmri.jmrix.marklin.mcan.protocol;

import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pure value object describing the sequence of CAN frames needed to discover
 * all modules of one or more CdB device classes.
 * <p>
 * Each {@link DiscoveryStep} carries a delay in milliseconds (relative to the
 * start of execution) and the frame to send at that point.  The plan has no
 * transport or threading dependency; the JMRI layer walks the step list and
 * dispatches each frame via whatever mechanism it uses (SwingWorker, Timer, etc.).
 * <p>
 * NEW-protocol classes: one broadcast ping at delay 0.<br>
 * OLD-protocol classes: 128 addressed pings, each staggered by
 * {@link #OLD_PROTOCOL_PING_INTERVAL_MS} ms.
 */
public final class CdBDiscoveryPlan {

    /** Delay between successive OLD-protocol addressed pings, in milliseconds. */
    public static final long OLD_PROTOCOL_PING_INTERVAL_MS = 5L;

    /**
     * Gap inserted between the last step of one class and the first step of the
     * next when building a multi-class plan via {@link #forClasses}.
     */
    public static final long CLASS_BOUNDARY_GAP_MS = 250L;

    private final List<DiscoveryStep> steps;

    private CdBDiscoveryPlan(List<DiscoveryStep> steps) {
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    /**
     * Returns an unmodifiable view of the ordered discovery steps.
     * Call {@link DiscoveryStep#frame()} to get a defensive copy of each frame.
     */
    public List<DiscoveryStep> steps() {
        return steps;
    }

    /**
     * Creates a plan for a single device class.
     *
     * @param deviceClass the class to discover
     * @return plan with steps starting at delay 0
     */
    public static CdBDiscoveryPlan forClass(CdBDeviceClass deviceClass) {
        Objects.requireNonNull(deviceClass, "deviceClass");
        return forClassAtOffset(deviceClass, 0L);
    }

    /**
     * Creates a plan covering all supplied device classes, with a
     * {@link #CLASS_BOUNDARY_GAP_MS} gap between each class boundary.
     *
     * @param classes ordered list of classes to discover; must not be null or empty
     * @return combined plan with monotonically increasing delays
     */
    public static CdBDiscoveryPlan forClasses(List<CdBDeviceClass> classes) {
        Objects.requireNonNull(classes, "classes");
        if (classes.isEmpty()) {
            throw new IllegalArgumentException("classes must not be empty");
        }
        List<DiscoveryStep> combined = new ArrayList<>();
        long offset = 0L;
        for (CdBDeviceClass cls : classes) {
            List<DiscoveryStep> classSteps = forClassAtOffset(cls, offset).steps();
            combined.addAll(classSteps);
            long lastDelay = classSteps.get(classSteps.size() - 1).delayMillis();
            offset = lastDelay + CLASS_BOUNDARY_GAP_MS;
        }
        return new CdBDiscoveryPlan(combined);
    }

    private static CdBDiscoveryPlan forClassAtOffset(CdBDeviceClass deviceClass, long offset) {
        List<DiscoveryStep> steps = new ArrayList<>();
        if (deviceClass.getProtocolGeneration() == CdBDeviceClass.ProtocolGeneration.NEW) {
            steps.add(new DiscoveryStep(offset, CdBFrameEncoder.buildPing(deviceClass.getClassId())));
        } else {
            int classId = deviceClass.getClassId();
            for (int addr = 0; addr < 128; addr++) {
                long delay = offset + addr * OLD_PROTOCOL_PING_INTERVAL_MS;
                steps.add(new DiscoveryStep(delay, CdBFrameEncoder.buildOldProtocolPing(classId, addr)));
            }
        }
        return new CdBDiscoveryPlan(steps);
    }

    /**
     * One step in a discovery plan: a delay and the frame to send at that delay.
     */
    public static final class DiscoveryStep {

        private final long  delayMillis;
        private final int[] frame;

        DiscoveryStep(long delayMillis, int[] frame) {
            this.delayMillis = delayMillis;
            this.frame       = frame.clone();
        }

        /** Delay in milliseconds from the start of plan execution. */
        public long delayMillis() { return delayMillis; }

        /** 13-byte CAN frame to send at this step (defensive copy). */
        public int[] frame() { return frame.clone(); }
    }
}
