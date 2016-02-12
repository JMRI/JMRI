package jmri.jmrix.ecos.utilities;

import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RosterToEcos implements EcosListener {

    private EcosLocoAddress objEcosLoco = null;
    private EcosLocoAddressManager objEcosLocoManager;
    protected RosterEntry _re = null;
    private EcosPreferences ep;
    //private String _rosterid;
    EcosTrafficController tc;
    private boolean createloco;
    EcosSystemConnectionMemo adaptermemo;

    DecoderIndexFile decoderind = DecoderIndexFile.instance();

    public RosterToEcos() {
    }

    public void createEcosLoco(RosterEntry re, EcosSystemConnectionMemo memo) {
        if (createloco == true) {
            return;
        }
        createloco = true;
        adaptermemo = memo;
        tc = adaptermemo.getTrafficController();
        ep = adaptermemo.getPreferenceManager();
        _re = re;
        objEcosLocoManager = adaptermemo.getLocoAddressManager();

        String protocol = "";
        switch (re.getProtocol()) {
            case MOTOROLA:
                protocol = "MM28";
                break;
            case SELECTRIX:
                protocol = "SX28";
                break;
            case MFX:
                protocol = "MMFKT";
                break;
            default:
                protocol = "DCC128";
        }

        String message = "create(10, addr[" + _re.getDccAddress() + "], name[\"" + description() + "\"], protocol[" + protocol + "], append)";

        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);

    }

    private String description() {

        String result = "";
        String str = ep.getEcosLocoDescription();
        if ((str == null) || (str.equals(""))) {
            return _re.getId();
        }
        char comp = '%';
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == comp) {
                if (str.charAt(i + 1) == 'i') {
                    result = result + _re.getId();
                } else if (str.charAt(i + 1) == 'r') {
                    result = result + _re.getRoadName();
                } else if (str.charAt(i + 1) == 'n') {
                    result = result + _re.getRoadNumber();
                } else if (str.charAt(i + 1) == 'm') {
                    result = result + _re.getMfg();
                } else if (str.charAt(i + 1) == 'o') {
                    result = result + _re.getOwner();
                } else if (str.charAt(i + 1) == 'l') {
                    result = result + _re.getModel();
                } else if (str.charAt(i + 1) == 'c') {
                    result = result + _re.getComment();
                }
                i++;
            } else {
                result = result + str.charAt(i);
            }
        }
        return result;

    }

    //Need to deal with the loco not being created somehow.
    //If we get the error, then we could simply delete the loco from our loco list.
    public void reply(EcosReply m) {
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (m.getResultCode() == 0) {
            if (lines[0].startsWith("<REPLY create(10, addr")) {
                //System.out.println(msg);
                for (int i = 1; i < lines.length - 1; i++) {
                    if (lines[i].contains("10 id[")) {

                        start = lines[i].indexOf("[") + 1;
                        end = lines[i].indexOf("]");
                        String EcosAddr = lines[i].substring(start, end);
                        objEcosLoco = objEcosLocoManager.provideByEcosObject(EcosAddr);
                        objEcosLoco.setEcosTempEntry(false);
                        _re.putAttribute(ep.getRosterAttribute(), EcosAddr);
                        objEcosLoco.setRosterId(_re.getId());
                        objEcosLoco.setEcosDescription(description());
                        objEcosLoco.setLocoAddress(Integer.parseInt(_re.getDccAddress()));
                        switch (_re.getProtocol()) {
                            case MOTOROLA:
                                objEcosLoco.setProtocol("MM28");
                                break;
                            case SELECTRIX:
                                objEcosLoco.setProtocol("SX28");
                                break;
                            case MFX:
                                objEcosLoco.setProtocol("MMFKT");
                                break;
                            default:
                                objEcosLoco.setProtocol("DCC128");
                        }
                        _re.writeFile(null, null, null);
                        jmri.jmrit.roster.Roster.writeRosterFile();
                        objEcosLocoManager.register(objEcosLoco);
                        createloco = false;
                        dispose();
                    }
                }
            }
        }
    }

    public void message(EcosMessage m) {

    }

    void dispose() {
        objEcosLoco = null;
        objEcosLocoManager = null;
        _re = null;
        createloco = false;
    }

    private final static Logger log = LoggerFactory.getLogger(RosterToEcos.class.getName());
}
