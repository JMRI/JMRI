package jmri.jmrit.simulation.link;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * An interface (gateway) as a link sees it: a name, and the stages it puts in
 * each direction, made fresh each time it is chosen so no state is shared.
 * Each system offers its own list; {@link #NONE} is always first.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class LinkPreset {

    /** No link: every message passes at once, as with no link at all. */
    public static final LinkPreset NONE = new LinkPreset(Bundle.getMessage("PresetNone"), List::of, List::of);

    private final String name;
    private final Supplier<List<Stage>> towardsLayout;
    private final Supplier<List<Stage>> towardsJmri;

    /**
     * @param name          the name shown in the simulator window
     * @param towardsLayout makes the stages from JMRI to the medium, in order
     * @param towardsJmri   makes the stages from the medium to JMRI, in order
     */
    public LinkPreset(@Nonnull String name, @Nonnull Supplier<List<Stage>> towardsLayout,
            @Nonnull Supplier<List<Stage>> towardsJmri) {
        this.name = name;
        this.towardsLayout = towardsLayout;
        this.towardsJmri = towardsJmri;
    }

    /**
     * @return the name shown in the simulator window
     */
    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    List<Stage> makeTowardsLayout() {
        return towardsLayout.get();
    }

    @Nonnull
    List<Stage> makeTowardsJmri() {
        return towardsJmri.get();
    }

    @Override
    public String toString() {
        return name;
    }
}
