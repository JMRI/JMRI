// EcosTurnoutManager.java

package jmri.jmrix.ecos;

import jmri.Turnout;

/**
 * Implement turnout manager for Ecos systems.
 * <P>
 * System names are "NTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision: 1.1 $
 */
public class EcosTurnoutManager extends jmri.AbstractTurnoutManager
                                implements EcosListener {

    public EcosTurnoutManager() {
        _instance = this;
        
        // listen for turnout creation
        // connect to the TrafficManager
        tc = EcosTrafficController.instance();
        tc.addEcosListener(this);
                
        // ask to be notified
        EcosMessage m = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(m, this);
        
        // get initial state
        m = new EcosMessage("queryObjects(11, addr)");
        tc.sendEcosMessage(m, this);

    }

    EcosTrafficController tc;
    
    public char systemLetter() { return 'U'; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new EcosTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // is this a list of turnouts?
        String msg = m.toString();
        if (msg.contains("<REPLY queryObjects(11, addr)>")) {
            // yes, make sure TOs exist
            String[] lines = msg.split("\n");
            log.debug("found "+(lines.length-2)+" turnout objects");
            for (int i = 1; i<lines.length-1; i++) {
                if (lines[i].contains("addr[")) { // skip odd lines
                    int start = 0;
                    int end = lines[i].indexOf(' ');
                    int object = Integer.parseInt(lines[i].substring(start, end));
    
                    start = lines[i].indexOf('[')+1;
                    end = lines[i].indexOf(']');
                    int addr = Integer.parseInt(lines[i].substring(start, end));
    
                    log.debug("Found turnout object "+object+" addr "+addr);
                    
                    if (addr != 0) {
                        Turnout t = getTurnout("UT"+addr);
                        if (t == null) {
                            EcosTurnout et = (EcosTurnout)provideTurnout("UT"+addr);
                            et.setObjectNumber(object);
                            
                            // listen for changes
                            EcosMessage em = new EcosMessage("request("+object+",view)");
                            tc.sendEcosMessage(em, null);
                            
                            // get initial state
                            em = new EcosMessage("get("+object+",state)");
                            tc.sendEcosMessage(em, null);
                            
                        }
                    }
                }
            }
        }

    }

    public void message(EcosMessage m) {
        // messages are ignored
    }

    static public EcosTurnoutManager instance() {
        if (_instance == null) _instance = new EcosTurnoutManager();
        return _instance;
    }
    static EcosTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EcosTurnoutManager.class.getName());
}

/* @(#)EcosTurnoutManager.java */
