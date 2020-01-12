package jmri.jmrix.rps.rpsmon;

import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.ReadingListener;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Frame displaying (and logging) RPS messages.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class RpsMonFrame extends jmri.jmrix.AbstractMonFrame
        implements ReadingListener, MeasurementListener {

    RpsSystemConnectionMemo memo = null;

    public RpsMonFrame(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;
        Distributor.instance().addReadingListener(this);
        Distributor.instance().addMeasurementListener(this);
    }

    @Override
    protected String title() {
        return "RPS Monitor";
    }

    @Override
    public void dispose() {
        // remove from notification
        Distributor.instance().removeReadingListener(this);
        Distributor.instance().removeMeasurementListener(this);
        // and unwind swing
        super.dispose();
    }

    @Override
    protected void init() {
    }

    @Override
    public void notify(Reading r) {
        String raw = "";
        if (r.getRawData() != null) {
            raw = r.getRawData().toString();
        }
        nextLine(r.toString() + "\n", raw);
    }

    @Override
    public void notify(Measurement m) {
        String raw = "";
        if (m.getReading() != null) {
            raw = m.getReading().toString();
        }
        nextLine(m.toString() + "\n", raw);
    }

}
