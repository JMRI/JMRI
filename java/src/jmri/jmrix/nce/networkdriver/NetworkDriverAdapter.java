package jmri.jmrix.nce.networkdriver;

import jmri.jmrix.nce.NceNetworkPortController;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Implements SerialPortAdapter for the NCE system network connection.
 * <p>
 * This connects an NCE command station via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003
 */
public class NetworkDriverAdapter extends NceNetworkPortController {

    public NetworkDriverAdapter() {
        super(new NceSystemConnectionMemo());
        option2Name = "Eprom";
        // the default is 2006 or later
        options.put(option2Name, new Option("Command Station EPROM", new String[]{"2006 or later", "2004 or earlier"}));
        setManufacturer(jmri.jmrix.nce.NceConnectionTypeList.NCE);
    }

    /**
     * set up all of the other objects to operate with an NCE command station
     * connected to this port
     */
    @Override
    public void configure() {
        NceTrafficController tc = new NceTrafficController();
        this.getSystemConnectionMemo().setNceTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());

        // set the command options, Note that the NetworkDriver uses
        // the second option for EPROM revision
        if (getOptionState(option2Name).equals(getOptionChoices(option2Name)[0])) {
            // setting binary mode
            this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2006);
            this.getSystemConnectionMemo().setNceCmdGroups(~NceTrafficController.CMDS_USB);
        } else {
            this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2004);
            this.getSystemConnectionMemo().setNceCmdGroups(~NceTrafficController.CMDS_USB);
        }

        tc.connectPort(this);

        this.getSystemConnectionMemo().configureManagers();
    }

}
