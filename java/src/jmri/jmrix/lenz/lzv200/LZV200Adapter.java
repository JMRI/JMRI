package jmri.jmrix.lenz.lzv200;

import jmri.jmrix.lenz.liusb.LIUSBAdapter;

/**
 * Provide access to XpressNet via the built in connection
 * on the LZV200.   This connection is a copy of an LIUSB with
 * an  FTDI Virtual Com Port.
 *
 * @author Paul Bender Copyright (C) 2005-2010,2019
 */
public class LZV200Adapter extends jmri.jmrix.lenz.liusb.LIUSBAdapter {

    public LZV200Adapter() {
        super();
        //option1Name = "FlowControl"; // NOI18N
        //options.put(option1Name, new Option(Bundle.getMessage("XconnectionUsesLabel", Bundle.getMessage("IFTypeLZV200")), validOption1));
        //this.manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;
    }

}
