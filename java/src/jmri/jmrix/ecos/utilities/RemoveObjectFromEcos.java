package jmri.jmrix.ecos.utilities;

import org.apache.log4j.Logger;
import jmri.jmrix.ecos.*;

public class RemoveObjectFromEcos implements EcosListener{
    
    public RemoveObjectFromEcos(){}
    
    private String _ecosObject;
    private int ecosretry;
    
    private EcosTrafficController tc;
    //Need to deal with the fact this method has a contructor name.
    public void removeObjectFromEcos(String ecosObject, EcosTrafficController etc){
        tc = etc;
        _ecosObject = ecosObject;
        log.debug("Call to delete Object " + ecosObject + " from the Ecos");
        tc = etc;
        String message = "request("+ _ecosObject +", control, view)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }
    
    public void reply(EcosReply m){
    
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
            if (lines[0].startsWith("<REPLY request("+_ecosObject+",")){
                deleteObject();
            } else if (lines[0].startsWith("<EVENT "+ _ecosObject +">")){
                if (msg.contains("CONTROL_LOST")){
                    retryControl();
                    log.debug("We have no control over the ecos object");
                }
            }
        } else if (lines[lines.length-1].equals("<END 25 (NERROR_NOCONTROL)>")){
            /**
            * This section deals with no longer having control over the ecos loco object.
            * we try three times to request control, on the fourth attempt we try a forced
            * control, if that fails we inform the user and reset the counter to zero.
            */
            log.info("We have no control over the ecos object " + _ecosObject + "Retry Counter = "+ ecosretry);
            retryControl();
        }
    }
    
    private void retryControl(){
            if (ecosretry <3){
                //It might be worth adding in a sleep/pause of description between retries.
                ecosretry++;

                String message = "request("+ _ecosObject +", control)";
                EcosMessage ms = new EcosMessage(message);
                tc.sendEcosMessage(ms, this);
                log.error("We have no control over the ecos object " + _ecosObject + " Retrying Attempt " + ecosretry);
            }
            //We do not want to do a force control over an object when we are trying to delete it, bad things might happen on the layout if we do this!
            else{
                javax.swing.JOptionPane.showMessageDialog(null,"Unable to delete the loco from the Ecos" + "\n" + "Please delete it manually","No Control",javax.swing.JOptionPane.WARNING_MESSAGE);
                ecosretry=0;
            }
    }
    
    private void deleteObject(){
        EcosMessage m;
        String message = "delete(" +_ecosObject+")";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }
    
    public void message(EcosMessage m) {
        // messages are ignored
    }
    
    static Logger log = Logger.getLogger(RemoveObjectFromEcos.class.getName());
}
