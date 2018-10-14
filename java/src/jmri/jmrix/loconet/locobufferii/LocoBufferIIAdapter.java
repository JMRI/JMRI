package jmri.jmrix.loconet.locobufferii;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

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
     * Get an array of valid baud rates. This is modified to have different
     * comments. Because the speeds are the same as the parent class (19200 and
     * 57600), we don't override validBaudNumber().
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"19,200 baud (Sw1 off, Sw3 off)",
            "57,600 baud (Sw1 on, Sw3 off)"}; // TODO I18N
    }

    public String option1Name() {
        return Bundle.getMessage("XconnectionUsesLabel", "LocoBuffer-II");
    }

}
