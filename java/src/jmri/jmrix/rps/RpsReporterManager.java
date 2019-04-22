package jmri.jmrix.rps;

import jmri.Reporter;
import jmri.managers.AbstractReporterManager;

/**
 * RPS implementation of a ReporterManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2019
 * @since 2.3.1
 */
public class RpsReporterManager extends AbstractReporterManager {

    private RpsSystemConnectionMemo memo = null;
    protected String prefix = "R";

    public RpsReporterManager(RpsSystemConnectionMemo memo) {
        super();
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Create a new reporter if all checks are passed.
     * System name is normalized to ensure uniqueness.
     */
    @Override
    protected Reporter createNewReporter(String systemName, String userName) {
        RpsReporter r = new RpsReporter(systemName, userName, prefix);
        Distributor.instance().addMeasurementListener(r);
        return r;
    }

    /**
     * Static function returning the RpsReporterManager instance to use.
     *
     * @return The registered RpsReporterManager instance for general use.
     * @deprecated since 4.15.6
     */
    @Deprecated
    static public RpsReporterManager instance() {
        return null;
    }

}
