package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * LocoNet Monitor Frame displaying (and logging) LocoNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @deprecated 2.9.5
 */
@Deprecated
public class LocoMonFrame extends jmri.jmrix.AbstractMonFrame implements LocoNetListener {

    public LocoMonFrame(LnTrafficController tc) {
        super();
        this.tc = tc;
    }

    LnTrafficController tc;

    protected String title() {
        return "LocoNet Traffic";
    }

    public void dispose() {
        // disconnect from the LnTrafficController
        tc.removeLocoNetListener(~0, this);
        // and unwind swing
        super.dispose();
    }

    protected void init() {
        // connect to the LnTrafficController
        tc.addLocoNetListener(~0, this);
    }

    // provide customized help
    protected void addHelpMenu() {
        addHelpMenu("package.jmri.jmrix.loconet.locomon.LocoMonFrame", true);
    }

    public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
        // display the raw data if requested
        String raw = null;
        if (rawCheckBox.isSelected()) {
            raw = l.toString();
        }

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLine(llnmon.displayMessage(l), raw);
    }

    jmri.jmrix.loconet.locomon.Llnmon llnmon = new jmri.jmrix.loconet.locomon.Llnmon();
}
