package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.implementation.AbstractStringIO;

/**
 * Send a message to the LocoNet throttles.
 *
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class LnThrottleStringIO extends AbstractStringIO {

    private final LocoNetSystemConnectionMemo _scm;

    /**
     * Create a LnThrottleStringIO object
     *
     * @param scm connection memo
     */
    public LnThrottleStringIO(LocoNetSystemConnectionMemo scm) {
        super(scm.getSystemPrefix()+"CThrottles", null);
        this._scm = scm;
    }

    /** {@inheritDoc} */
    @Override
    protected void sendStringToLayout(String value) throws JmriException {
        _scm.getLnMessageManager().sendMessage(value);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximumLength() {
        return 8;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean cutLongStrings() {
        return true;
    }

}
