package jmri.time.rate;

import jmri.time.RateSetter;

/**
 * A changeable rate of double values.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class ChangeableDoubleRate extends DoubleRate implements RateSetter {

    public ChangeableDoubleRate(double rate) {
        super(rate);
    }

    /** {@inheritDoc} */
    @Override
    public void setRateValue(double rate) {
        this._rate = rate;
    }

}
