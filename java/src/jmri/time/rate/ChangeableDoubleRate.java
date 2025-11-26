package jmri.time.rate;

import jmri.time.RateSetter;

/**
 * A rate of integer values.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class ChangeableDoubleRate extends DoubleRate implements RateSetter {

    public ChangeableDoubleRate(double rate) {
        super(rate);
    }

    /** {@inheritDoc} */
    @Override
    public void setRate(double rate) throws UnsupportedOperationException {
        this._rate = (int) Math.round(rate);
    }

}
