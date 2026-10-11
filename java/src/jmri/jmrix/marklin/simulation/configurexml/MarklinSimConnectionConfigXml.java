package jmri.jmrix.marklin.simulation.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.marklin.simulation.MarklinSimConnectionConfig;
import jmri.jmrix.marklin.simulation.MarklinSimDriverAdapter;

/**
 * Handle XML persistence of layout connections by persisting the
 * NetworkDriverAdapter (and connections).
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public MarklinSimConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new MarklinSimDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((MarklinSimConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        register(new MarklinSimConnectionConfig(adapter));
    }

}
