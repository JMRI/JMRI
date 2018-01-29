package jmri.jmrix.openlcb;

import jmri.InstanceManager;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrix.SystemConnectionMemo;

import javax.annotation.Nonnull;
import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring OlcbSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class OlcbSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return "OlcbSignalMast";
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {

        /**
         * {@inheritDoc}
         * Requires a valid OpenLCB connection
         */
        @Override
        public boolean isAvailable() {
            for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
                if (memo instanceof jmri.jmrix.can.CanSystemConnectionMemo) {
                    return true;
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return "OlcbSignalMast";
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new OlcbSignalMastAddPane();
        }
    }

}
