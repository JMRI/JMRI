package jmri.jmrix.ecos.utilities;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;

public class RosterToEcos implements EcosListener {

    private EcosLocoAddress objEcosLoco = null;
    private EcosLocoAddressManager objEcosLocoManager;
    protected RosterEntry _re = null;
    private EcosPreferences ep;
    //private String _rosterid;
    EcosTrafficController tc;
    private boolean createloco;
    EcosSystemConnectionMemo adaptermemo;

    public RosterToEcos(EcosSystemConnectionMemo memo) {
        adaptermemo = memo;
        tc = adaptermemo.getTrafficController();
        ep = adaptermemo.getPreferenceManager();
        objEcosLocoManager = adaptermemo.getLocoAddressManager();
    }

    public void createEcosLoco(RosterEntry re) {
        if (createloco == true) {
            return;
        }
        createloco = true;
        _re = re;

        String protocol;
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
        if ((str == null) || (str.isEmpty())) {
            return _re.getId();
        }
        char comp = '%';
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == comp) {
                switch (str.charAt(i + 1)) {
                    case 'i':
                        result = result + _re.getId();
                        break;
                    case 'r':
                        result = result + _re.getRoadName();
                        break;
                    case 'n':
                        result = result + _re.getRoadNumber();
                        break;
                    case 'm':
                        result = result + _re.getMfg();
                        break;
                    case 'o':
                        result = result + _re.getOwner();
                        break;
                    case 'l':
                        result = result + _re.getModel();
                        break;
                    case 'c':
                        result = result + _re.getComment();
                        break;
                    default:
                        break;
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
    @Override
    public void reply(EcosReply m) {
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (m.getResultCode() == 0) {
            if (lines[0].startsWith("<REPLY create(10, addr")) {
                for (int i = 1; i < lines.length - 1; i++) {
                    if (lines[i].contains("10 id[")) {

                        start = lines[i].indexOf('[') + 1;
                        end = lines[i].indexOf(']');
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
                        _re.writeFile(null, null);
                        jmri.jmrit.roster.Roster.getDefault().writeRoster();
                        objEcosLocoManager.register(objEcosLoco);
                        createloco = false;
                        dispose();
                    }
                }
            }
        }
    }

    @Override
    public void message(EcosMessage m) {

    }

    void dispose() {
        objEcosLoco = null;
        objEcosLocoManager = null;
        _re = null;
        createloco = false;
    }

}
