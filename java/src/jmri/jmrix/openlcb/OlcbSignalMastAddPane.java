package jmri.jmrix.openlcb;

import jmri.InstanceManager;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrix.SystemConnectionMemo;

import javax.annotation.Nonnull;
import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring OlcbSignalMast objects
 * <P>
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
@ServiceProvider(service = SignalMastAddPane.class)
public class OlcbSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return "OlcbSignalMast";
    }

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

}
