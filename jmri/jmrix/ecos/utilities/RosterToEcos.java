package jmri.jmrix.ecos.utilities;

import java.util.List;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosTrafficController;

public class RosterToEcos implements EcosListener{

    private EcosLocoAddress objEcosLoco;
    private EcosLocoAddressManager objEcosLocoManager;
    private RosterEntry _re;
    private EcosPreferences ep;
    EcosTrafficController tc;
    
    DecoderIndexFile decoderind = DecoderIndexFile.instance();
    public RosterToEcos(){ }

    public void createEcosLoco(RosterEntry re) {
        tc = EcosTrafficController.instance();
        ep = EcosPreferences.instance();
        _re = re;
        objEcosLocoManager = (EcosLocoAddressManager)jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);
        //We go on a hunt to find an object with the dccaddress sent by our controller.
        
    
        objEcosLoco = objEcosLocoManager.provideByDccAddress(Integer.valueOf(re.getDccAddress()).intValue());
        List<DecoderFile> decoder = decoderind.matchingDecoderList(null, re.getDecoderFamily(), null, null, null, re.getDecoderModel());
        System.out.println(decoder);
    
        tc = EcosTrafficController.instance();
        //objEcosLoco.setDescription(description);
        //objEcosLoco.
        String message = "create(10, addr[" + re.getDccAddress() + "], name[\""+ description() +"\"], protocol["+ ep.getDefaultEcosProtocol()+"], append)";
        System.out.println(message);
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    
    }
    
    private String description(){
    
        String result ="";
        String str = ep.getEcosLocoDescription();
        if (str==null){
            return _re.getId();
        }
        char comp = '%';
        for(int i=0; i<str.length(); i++){
            if (str.charAt(i)==comp){
                if (str.charAt(i+1)=='i') result = result + _re.getId();
                else if (str.charAt(i+1)=='r') result = result + _re.getRoadName();
                else if (str.charAt(i+1)=='n') result = result + _re.getRoadNumber();
                else if (str.charAt(i+1)=='m') result = result + _re.getMfg();
                else if (str.charAt(i+1)=='o') result = result + _re.getOwner();
                else if (str.charAt(i+1)=='l') result = result + _re.getModel();
                else if (str.charAt(i+1)=='c') result = result + _re.getComment();
                i++;
            } else {
                result = result + str.charAt(i);
            }
        }
        System.out.println(result);
        return result;
    
    }
    
    //Need to deal with the loco not being created somehow.
    //If we get the error, then we could simply delete the loco from our loco list.
    public void reply(EcosReply m) {
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
            if (lines[0].startsWith("<REPLY create(10, addr")){
                for(int i =1; i<lines.length-1; i++) {
                    if(lines[i].contains("10 id[")){
                        start = lines[i].indexOf("[")+1;
                        end = lines[i].indexOf("]");
                        String EcosAddr = lines[i].substring(start, end);
                        objEcosLoco.setEcosObject(EcosAddr);
                        objEcosLocoManager.deregister(objEcosLoco);
                        objEcosLocoManager.register(objEcosLoco);
                        objEcosLoco.setEcosTempEntry(false);
                        _re.putAttribute("EcosObject", EcosAddr);
                        objEcosLoco.setRosterId(_re.getId());
                        objEcosLoco.setEcosDescription(description());
                    }
                }
            }
        }
    }
    
    public void message(EcosMessage m){
        
    }    
}