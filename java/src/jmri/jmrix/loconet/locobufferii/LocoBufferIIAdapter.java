package jmri.jmrix.loconet.locobufferii;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

import java.util.Arrays;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it refers to the
 * switch settings on the new LocoBuffer-II.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class LocoBufferIIAdapter extends LocoBufferAdapter {

    public LocoBufferIIAdapter(LocoNetSystemConnectionMemo adapterMemo) {
        super(adapterMemo);
    }

    public LocoBufferIIAdapter() {
        this(new LocoNetSystemConnectionMemo());
    }

    /**
     * {@inheritDoc}
     * This is modified to have different comments. Because the speeds are the same as
     * the parent class (19200 and 57600), we don't override validBaudNumbers().
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud19200LBII"), Bundle.getMessage("Baud57600LBII")};
    }

    /**
     * Get a String that says what Option 1 represents.
     */
    public String option1Name() {
        return Bundle.getMessage("XconnectionUsesLabel", "LocoBuffer-II");
    }

}
