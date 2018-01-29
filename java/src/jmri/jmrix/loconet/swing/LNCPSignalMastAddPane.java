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
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class LNCPSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("LNCPMast");
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
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

        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("LNCPMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new LNCPSignalMastAddPane();
        }
    }
}
