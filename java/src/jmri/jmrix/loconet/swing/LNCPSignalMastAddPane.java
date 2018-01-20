package jmri.jmrix.loconet.swing;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.jmrix.loconet.SlotManager;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;

import javax.annotation.Nonnull;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring LNCPSignalMast objects
 * <P>
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
@ServiceProvider(service = SignalMastAddPane.class)
public class LNCPSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("LNCPMast");
    }

    /**
     * {@inheritDoc}
     * Requires a valid LocoNet connection
     */
    @Override
    public boolean isAvailable() {
        for (CommandStation c : InstanceManager.getList(CommandStation.class)) {
            if (c instanceof SlotManager) {
                return true;
            }
        }
        return false;
    }

}
