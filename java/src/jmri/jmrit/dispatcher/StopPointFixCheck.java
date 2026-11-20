package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.jmrit.trainrecord.SetUpCheck;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;

import org.openide.util.lookup.ServiceProvider;

/**
 * Checks each Section's stop points: a train that stops by distance at a stop
 * point is placed there by reckoning from the last place before it where the
 * train was seen, a boundary into a Block with a detector. Reckoning is only
 * as good as the train's speed is repeatable, a few parts in a hundred, so
 * the further that place is from the stop point, the further off the stop can
 * be. A stop point with a stopping sensor for its direction is not checked:
 * the sensor is where the train stops.
 * <p>
 * How far is too far is a scale distance, {@value #NEAR_ENOUGH_SCALE_MM}
 * scale millimetres, worked out for the layout's scale (Warrants' layout
 * scale, as Dispatcher's set-up check keeps it): 100 mm in HO, about 200 mm
 * in O and 55 mm in N, since a stop a few millimetres out matters as much in
 * one scale as the same scale distance does in another. It doesn't depend on
 * the train: it is how far a train's position is reckoned, not how far it
 * takes to stop.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = SetUpCheck.class)
public class StopPointFixCheck implements SetUpCheck {

    /**
     * How far, in scale millimetres, the last place a train is seen may be
     * before a stop point: 8.7 m, which is 100 mm in HO (1:87).
     */
    public static final float NEAR_ENOUGH_SCALE_MM = 8700.0f;

    /**
     * How far, in millimetres on the layout, the last place a train is seen
     * may be before a stop point, for the layout's scale.
     *
     * @return {@link #NEAR_ENOUGH_SCALE_MM} in the layout's scale
     */
    public static float nearEnoughMm() {
        float scale = InstanceManager.getDefault(jmri.implementation.SignalSpeedMap.class).getLayoutScale();
        return NEAR_ENOUGH_SCALE_MM / (scale > 0 ? scale : 87.1f);
    }

    /** The part of the distance reckoned a stop may be out by: 2%. */
    static final float SPEED_SPREAD = 0.02f;

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Finding> check() {
        List<Finding> found = new ArrayList<>();
        for (Section s : InstanceManager.getDefault(SectionManager.class).getNamedBeanSet()) {
            check(s, Section.FORWARD, found);
            check(s, Section.REVERSE, found);
        }
        return found;
    }

    /**
     * Check one stop point.
     *
     * @param s         the Section
     * @param direction {@link Section#FORWARD} or {@link Section#REVERSE}
     * @param found     where to add a finding
     */
    static void check(@Nonnull Section s, int direction, @Nonnull List<Finding> found) {
        float stopMm = s.getStopPointMm(direction);
        if (!(stopMm > 0)) {
            return; // no stop point
        }
        if ((direction == Section.FORWARD ? s.getForwardStoppingSensor() : s.getReverseStoppingSensor()) != null) {
            return; // the train stops at the sensor
        }
        List<Block> blocks = new ArrayList<>(s.getBlockList());
        if (direction == Section.REVERSE) {
            Collections.reverse(blocks); // in the order a train running in reverse enters them
        }
        float into = 0; // how far the start of each Block is from where trains enter the Section
        Block seenAt = null;
        float seenMm = 0;
        for (Block b : blocks) {
            if (into > stopMm) {
                break;
            }
            if (!(b.getLengthMm() > 0)) {
                return; // nothing can be placed by distance: the Section's own validation says so
            }
            if (b.getSensor() != null) {
                seenAt = b;
                seenMm = into;
            }
            into += b.getLengthMm();
        }
        String dir = Bundle.getMessage(direction == Section.FORWARD ? "CheckStopPointForward" : "CheckStopPointReverse");
        if (seenAt == null) {
            found.add(new Finding(Kind.STOP_POINT_FAR_FROM_FIX, s, List.of(),
                    Bundle.getMessage("CheckStopPointNoFix", s.getDisplayName(), dir, mm(stopMm))));
            return;
        }
        float distance = stopMm - seenMm;
        float nearEnough = nearEnoughMm();
        if (distance > nearEnough) {
            found.add(new Finding(Kind.STOP_POINT_FAR_FROM_FIX, s, List.<NamedBean>of(seenAt),
                    Bundle.getMessage("CheckStopPointFarFromFix", s.getDisplayName(), dir, mm(distance),
                            seenAt.getDisplayName(), mm(distance * SPEED_SPREAD), mm(nearEnough))));
        }
    }

    /**
     * A length for a message, in whole millimetres, without the grouping a
     * number argument would get.
     *
     * @param mm a length in mm
     * @return it, rounded, as text
     */
    private static String mm(float mm) {
        return Long.toString(Math.round(mm));
    }
}
