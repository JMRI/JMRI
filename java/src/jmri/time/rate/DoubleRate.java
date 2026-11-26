package jmri.time.rate;

import jmri.time.Rate;

/**
 * A rate of integer values.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class DoubleRate implements Rate {

    protected double _rate;

    public DoubleRate(double rate) {
        this._rate = rate;
    }

    /** {@inheritDoc} */
    @Override
    public double getRate() {
        return _rate;
    }

    /** {@inheritDoc} */
    @Override
    public String getRateString() {
        return String.format("%1.2f:1", _rate);
    }

}
