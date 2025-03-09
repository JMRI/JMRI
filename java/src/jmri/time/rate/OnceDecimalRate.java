package jmri.time.rate;

import jmri.time.Rate;

/**
 * A rate of integer values.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class OnceDecimalRate implements Rate {

    protected int _rateTimesTen;  // Rate * 10

    public OnceDecimalRate(int rateTimesTen) {
        this._rateTimesTen = rateTimesTen;
    }

    /** {@inheritDoc} */
    @Override
    public double getRate() {
        return _rateTimesTen / 10.0;
    }

    /** {@inheritDoc} */
    @Override
    public String getRateString() {
        return String.format("%1.1f:1", _rateTimesTen/10.0);
    }

}
