package jmri.jmrix.openlcb;

import jmri.JmriException;
import jmri.implementation.AbstractStringIO;
import jmri.jmrix.can.CanSystemConnectionMemo;


/**
 * Send a message to the OpenLCB/LCC network
 *
 * @author Bob Jacobsen   Copyright (C) 2024
 */
public class OlcbStringIO extends AbstractStringIO {

    private final CanSystemConnectionMemo _scm;

    /**
     * Create a LnThrottleStringIO object
     *
     * @param scm connection memo
     */
    public OlcbStringIO(CanSystemConnectionMemo scm, String sName) {
        super(sName, null);
        this._scm = scm;
    }

    /** {@inheritDoc} */
    @Override
    protected void sendStringToLayout(String value) throws JmriException {
        // Only sets the known string and fires listeners.
        setString(value);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximumLength() {
        return 242; // Event With Payload limit
    }

    /** {@inheritDoc} */
    @Override
    protected boolean cutLongStrings() {
        return true;
    }

}
