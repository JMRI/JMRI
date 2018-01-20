package jmri.jmrit.beantable.signalmast;

import javax.annotation.Nonnull;
import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring SignalHeadSignalMast objects
 * <P>
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
@ServiceProvider(service = SignalMastAddPane.class)
public class SignalHeadSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("HeadCtlMast");
    }

}
