package jmri.time.rate;

import jmri.time.Rate;

/**
 * A rate of integer values.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class IntegerRate implements Rate {

    protected int _rate;

    public IntegerRate(int rate) {
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
        return String.format("%d:1", _rate);
    }

}
