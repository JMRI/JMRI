package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.*;
import javax.swing.*;

public class RemoveObjectFromEcos implements EcosListener{
    
    public RemoveObjectFromEcos(){}
    
    private String _ecosObject;
    private int ecosretry;
    
    private EcosTrafficController tc;
    //Need to deal with the fact this method has a contructor name.
    public void removeObjectFromEcos(String ecosObject){
        _ecosObject = ecosObject;
        tc = EcosTrafficController.instance();
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
            System.out.println(ecosretry);
            log.info("We have no control over the ecos object " + _ecosObject + "Retry Counter = "+ ecosretry);
            retryControl();
        }
    }
    
    private void retryControl(){
            if (ecosretry <3){
                //It might be worth adding in a sleep/pause of discription between retries.
                ecosretry++;
                tc = EcosTrafficController.instance();

                String message = "request("+ _ecosObject +", control)";
                EcosMessage ms = new EcosMessage(message);
                tc.sendEcosMessage(ms, this);
                log.error("We have no control over the ecos object " + _ecosObject + " Retrying Attempt " + ecosretry);


            }
            else if(ecosretry==3){
                ecosretry++;
                String objectType = "Object";
                int objectNo = Integer.parseInt(_ecosObject);
                if ( (1000<=objectNo) && (objectNo<2000))
                    objectType = "Loco";
                
                int val = javax.swing.JOptionPane.showConfirmDialog(null,"Unable to gain control of the " + objectType + "\n Another operator may have control of the " + objectType + " \n To enable the " + objectType + " to be deleted from the Ecos \n Do you want to attempt a forced take over?","No Control", JOptionPane.YES_NO_OPTION,javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (val==0)
                {
                    tc = EcosTrafficController.instance();
                    String message = "request("+_ecosObject+", control, force)";
                    EcosMessage ms = new EcosMessage(message);
                    tc.sendEcosMessage(ms, this);
                }
                log.error("We have no control over the ecos object " + _ecosObject + "Trying a forced control");
            }
            else{
                javax.swing.JOptionPane.showMessageDialog(null,"Unable to delete the loco from the Ecos" + "\n" + "Please delete it manually","No Control",javax.swing.JOptionPane.WARNING_MESSAGE);
                ecosretry=0;
            }
    }
    
    private void deleteObject(){
        tc = EcosTrafficController.instance();
        EcosMessage m;
        String message = "delete(" +_ecosObject+")";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }
    
    public void message(EcosMessage m) {
        System.out.println("Ecos message - "+ m);
        // messages are ignored
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RemoveObjectFromEcos.class.getName());
}