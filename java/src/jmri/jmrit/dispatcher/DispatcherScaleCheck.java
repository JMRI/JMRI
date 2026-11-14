package jmri.jmrit.dispatcher;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.Scale;
import jmri.ScaleManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.trainrecord.SetUpCheck;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;

import org.openide.util.lookup.ServiceProvider;

/**
 * Checks that Dispatcher's layout scale, set in its Options, is the layout
 * scale set in the Warrant preferences. Speed profiles give scale speeds at
 * the Warrant preferences' scale, and train tracking works out wheel
 * overhangs from it, while Dispatcher works out scale lengths and speeds at
 * its own: if the two disagree, so do they, and nothing says so.
 * <p>
 * Only while Dispatcher is open, since its scale is read from its options
 * when it starts.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@ServiceProvider(service = SetUpCheck.class)
public class DispatcherScaleCheck implements SetUpCheck {

    /**
     * How far apart, as a part of the Warrant preferences' scale, the two
     * scales may be and still be taken as the same: the Warrant preferences
     * hold the scale as a number typed in, as 87.1 for HO, and Dispatcher
     * as a named scale, so they need not match to the last digit.
     */
    static final double TOLERANCE = 0.005;

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Finding> check() {
        if (!InstanceManager.containsDefault(DispatcherFrame.class)) {
            return List.of();
        }
        Scale scale = InstanceManager.getDefault(DispatcherFrame.class).getScale();
        float warrants = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        if (scale == null || !(warrants > 0)) {
            return List.of();
        }
        double ratio = scale.getScaleRatio();
        if (same(ratio, warrants)) {
            return List.of();
        }
        Scale named = namedScale(warrants);
        return List.of(new Finding(Kind.SETTINGS_DISAGREE, Bundle.getMessage("TitleDispatcher"), List.of(),
                named == null
                        ? Bundle.getMessage("CheckScalesDisagree", scale.getUserName(), ratio, warrants)
                        : Bundle.getMessage("CheckScalesDisagreeNamed", scale.getUserName(), ratio,
                                named.getUserName(), warrants)));
    }

    private static boolean same(double ratio, float warrants) {
        return Math.abs(ratio - warrants) <= TOLERANCE * warrants;
    }

    /**
     * The named scale the Warrant preferences' scale is, as Dispatcher's is
     * one of these.
     *
     * @param warrants the Warrant preferences' scale
     * @return the nearest of JMRI's scales that is the same within
     *         {@link #TOLERANCE}, or null if none is
     */
    @CheckForNull
    static Scale namedScale(float warrants) {
        Scale best = null;
        for (Scale s : ScaleManager.getScales()) {
            if (same(s.getScaleRatio(), warrants)
                    && (best == null || Math.abs(s.getScaleRatio() - warrants) < Math.abs(best.getScaleRatio() - warrants))) {
                best = s;
            }
        }
        return best;
    }
}
