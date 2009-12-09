// EcosTurnoutManager.java

package jmri.jmrix.ecos;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
//import jmri.Turnout;
import jmri.Turnout;

/**
 * Implement turnout manager for Ecos systems.
 * <P>
 * System names are "UTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision: 1.8 $
 */
public class EcosTurnoutManager extends jmri.managers.AbstractTurnoutManager
                                implements EcosListener {

    public EcosTurnoutManager() {
        _instance = this;
        
        // listen for turnout creation
        // connect to the TrafficManager
        tc = EcosTrafficController.instance();
        tc.addEcosListener(this);
                
        // ask to be notified
        //Not sure if we need to worry about newly created turnouts on the layout.
        EcosMessage m = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(m, this);
        
        // get initial state
        m = new EcosMessage("queryObjects(11, addrext)");
        tc.sendEcosMessage(m, this);
        
    }

    EcosTrafficController tc;
    
    //The hash table simply holds the object number against the EcosTurnout ref.
    protected static Hashtable <Integer, EcosTurnout> _tecos = new Hashtable<Integer, EcosTurnout>();   // stores known Ecos Object ids to DCC
    
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
        EcosTurnout et;
        int startobj;
        int endobj;

        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (msg.contains("<REPLY queryObjects(11, addr)>")) {
            // yes, make sure TOs exist
            
            log.debug("found "+(lines.length-2)+" turnout objects");
            for (int i = 1; i<lines.length-1; i++) {
                if (lines[i].contains("addr[")) { // skip odd lines
                    int start = 0;
                    int end = lines[i].indexOf(' ');
                    int object = Integer.parseInt(lines[i].substring(start, end));

                    if ( (20000<=object) && (object<30000)) { // only physical turnouts
                        start = lines[i].indexOf('[')+1;
                        end = lines[i].indexOf(']');
                        int addr = Integer.parseInt(lines[i].substring(start, end));
                        log.debug("Found turnout object "+object+" addr "+addr);
                        
                        if ( addr > 0 ) {
                            Turnout t = getTurnout("UT"+addr);
                            if (t == null) {
                                et = (EcosTurnout)provideTurnout("UT"+addr);
                                et.setObjectNumber(object);
                                _tecos.put(object, et);
                                
                                // listen for changes
                                EcosMessage em = new EcosMessage("request("+object+",view)");
                                tc.sendEcosMessage(em, null);
                                
                                // get initial state
                                em = new EcosMessage("get("+object+",state)");
                                tc.sendEcosMessage(em, null);
                                
                            }
                        }
                    } else if (( 30000<=object) && (object<40000)){  //This is a ecos route
                        start = lines[i].indexOf('[')+1;
                        end = lines[i].indexOf(']');
                        //int addr = Integer.parseInt(lines[i].substring(start, end));

                        log.debug("Found route object " + object);

                        Turnout t = getTurnout("UT"+object);
                        if (t==null) {
                                et = (EcosTurnout)provideTurnout("UT"+object);
                                et.setObjectNumber(object);
                                _tecos.put(object, et);

                                // get initial state
                                EcosMessage em = new EcosMessage("get("+object+",state)");
                                tc.sendEcosMessage(em, null);

                                // listen for changes
                                //em = new EcosMessage("request("+object+",view)");
                                //tc.sendEcosMessage(em, null);

                                // get the name from the ecos to set as Username
                                em = new EcosMessage("get("+object+", name1, name2, name3)");
                                tc.sendEcosMessage(em, null);
                        }
                    }
                }
            }
        } else if (lines[0].contains("<REPLY get(") ){
            /*
            Potentially we could have received a message that is for a Loco or sensor
            rather than for a turnout or route
            We therefore need to extract the object number to check.
             */
            int start;
            int end;
            startobj=lines[0].indexOf("(")+1;
            endobj=(lines[0].substring(startobj)).indexOf(",")+startobj;
            //The first part of the messages is always the object id.
            int object = Integer.parseInt(lines[0].substring(startobj, endobj));
            if ((20000<=object) && (object<40000)){
                et = _tecos.get(object);
                if(lines[0].contains("state")){
                    //As this is in response to a change in state we shall forward
                    //it straight on to the ecos turnout to deal with.
                    et.reply(m);
                    //As the event will come from one object, we shall check to see if it is an extended address,
                    // if it is we also forward the message onto the slaved address.
                    if(et.getExtended()!=0){
                        //System.out.println("sending to slave");
                        EcosTurnout etx = (EcosTurnout)provideTurnout(et.getSlaveAddress());
                        etx.reply(m);
                    }

                } else if (lines[0].contains("symbol")){
                //Extract symbol number and set on turnout.
                    start = lines[1].indexOf('[')+1;
                    end = lines[1].indexOf(']');
                    //System.out.println(lines[1]);
                    int symbol = Integer.parseInt(lines[1].substring(start, end));
                    et.setExtended(symbol);
                    et.setTurnoutOperation(jmri.TurnoutOperationManager.getInstance().getOperation("NoFeedback"));
                    if((symbol==2)||(symbol==4)){
                        
                        EcosTurnout etx = (EcosTurnout)provideTurnout(et.getSlaveAddress());
                        etx.setExtended(symbol);
                        etx.setTurnoutOperation(jmri.TurnoutOperationManager.getInstance().getOperation("NoFeedback"));
                        switch(symbol) {
                            case 2 : et.setComment("Three Way Point with " + et.getSlaveAddress());
                                    break;
                            case 4 : et.setComment("Double Slip with " + et.getSlaveAddress());
                                    break;
                        }
                    }
                    // get initial state
                    EcosMessage em = new EcosMessage("get("+object+",state)");
                    tc.sendEcosMessage(em, null);
                
                } else {
                    String name = "";
                    for(int i = 1; i<lines.length-1;i++){
                        if (lines[i].contains("name")){
                            start=lines[i].indexOf("[")+2;
                            end=lines[i].indexOf("]")-1;
                            name = name + " " + lines[i].substring(start, end);
                            et.setUserName(name);
                        }
                    }
                }
            }
        } else if (lines[0].contains("<EVENT ")){
            //So long as the event information is for a turnout we will determine
            //which turnout it is for and let that deal with the message.
            startobj=lines[0].indexOf(" ")+1;
            endobj=(lines[0].substring(startobj)).indexOf(">")+startobj;
            //The first part of the messages is always the object id.
            int object = Integer.parseInt(lines[0].substring(startobj, endobj));
            if ((20000<=object) && (object<40000)){
                et = _tecos.get(object);
                et.reply(m);
                //As the event will come from one object, we shall check to see if it is an extended address,
                // if it is we also forward the message onto the slaved address.
                if(et.getExtended()!=0){
                    EcosTurnout etx = (EcosTurnout)provideTurnout(et.getSlaveAddress());
                    etx.reply(m);
                }
            }

        } else if (lines[0].contains("<REPLY queryObjects(11, addrext)>")){
            for (int i = 1; i<lines.length-1; i++) {
                if (lines[i].contains("addrext[")) { // skip odd lines
                    int start = 0;
                    int end = lines[i].indexOf(' ');
                    int object = Integer.parseInt(lines[i].substring(start, end));
                    if ( (20000<=object) && (object<30000)) {
                        start = lines[i].indexOf('[')+1;
                        end = lines[i].indexOf(']');
                        String turnoutadd=stripChar(lines[i].substring(start, end));
                        //System.out.println(turnoutadd);
                        String[] straddr = turnoutadd.split(",");
                        log.debug("Number of Address for this device is " + straddr.length);
                        if(straddr.length<=2){
                            if (straddr.length==2) {
                                if (!straddr[0].equals(straddr[1])) log.debug("Addresses are not the same, we shall use the first address listed.");
                            }
                            int addr=Integer.parseInt(straddr[0]);
                            if ( addr > 0 ) {
                                Turnout t = getTurnout("UT"+addr);
                                if (t == null) {
                                    et = (EcosTurnout)provideTurnout("UT"+addr);
                                    et.setObjectNumber(object);
                                    _tecos.put(object, et);
                                    
                                    // listen for changes
                                    EcosMessage em = new EcosMessage("request("+object+",view)");
                                    tc.sendEcosMessage(em, null);
                                    
                                    // get initial state
                                    em = new EcosMessage("get("+object+",state)");
                                    tc.sendEcosMessage(em, null);
                                }
                            }
                            
                        }else if (straddr.length ==4){
                            log.debug("We have a two address object.");
                            //The first two addresses should be the same
                            if(!straddr[0].equals(straddr[1])) log.debug("First Pair of Addresses are not the same, we shall use the first address");
                            if(!straddr[2].equals(straddr[3])) log.debug("Second Pair of Addresses are not the same, we shall use the first address");
                            int addr=Integer.parseInt(straddr[0]);
                            int addr2=Integer.parseInt(straddr[2]);
                            if ( addr > 0 ) {
                                //addr = straddr[0];
                                Turnout t = getTurnout("UT"+addr);
                                if (t == null) {
                                    et = (EcosTurnout)provideTurnout("UT"+addr);
                                    et.setObjectNumber(object);
                                    et.setSlaveAddress(addr2);
                                    _tecos.put(object, et);
                                    
                                    //Get the type of accessory...
                                    EcosMessage em = new EcosMessage("get("+object+",symbol)");
                                    tc.sendEcosMessage(em, this);
                                    
                                    // listen for changes
                                    em = new EcosMessage("request("+object+",view)");
                                    tc.sendEcosMessage(em, this);
                                    
                                }
                            }
                            
                            if (addr2 > 0){
                                Turnout t = getTurnout("UT"+addr2);
                                if (t == null) {
                                    et = (EcosTurnout)provideTurnout("UT"+addr2);
                                    et.setMasterObjectNumber(false);
                                    et.setObjectNumber(object);
                                    et.setComment("Extended address linked with turnout " + systemLetter()+"T"+straddr[0]);
                                }
                            }
                        }
                        
                    } else if (( 30000<=object) && (object<40000)){  //This is a ecos route
                        start = lines[i].indexOf('[')+1;
                        end = lines[i].indexOf(']');
                        //int addr = Integer.parseInt(lines[i].substring(start, end));

                        log.debug("Found route object " + object);

                        Turnout t = getTurnout("UT"+object);
                        if (t==null) {
                                et = (EcosTurnout)provideTurnout("UT"+object);
                                et.setObjectNumber(object);
                                _tecos.put(object, et);

                                // get initial state
                                EcosMessage em = new EcosMessage("get("+object+",state)");
                                tc.sendEcosMessage(em, null);
                                //Need to do some more work on routes on the ecos.

                                // listen for changes
                               // em = new EcosMessage("request("+object+",view)");
                               // tc.sendEcosMessage(em, null);

                                // get the name from the ecos to set as Username
                                em = new EcosMessage("get("+object+", name1, name2, name3)");
                                tc.sendEcosMessage(em, null);
                        }
                    }
                }
            }
        }
    }
    
    public String stripChar(String s) {  
        String allowed =
          ",0123456789";
        String result = "";
        for ( int i = 0; i < s.length(); i++ ) {
            if ( allowed.indexOf(s.charAt(i)) >= 0 )
               result += s.charAt(i);
            }
        return result;
    }

    public void message(EcosMessage m) {
        // messages are ignored
    }

    public void dispose(){
        //List<String> list = getEcosObjectList();
        Enumeration<Integer> en = _tecos.keys();
        EcosMessage em;
        while (en.hasMoreElements()) {
            int ecosObject = en.nextElement();
            em = new EcosMessage("release("+ecosObject+",view)");
            tc.sendEcosMessage(em, this);
        }
        
        if (jmri.InstanceManager.configureManagerInstance()!= null)
            jmri.InstanceManager.configureManagerInstance().deregister(this);
        _tecos.clear();
    }
    
    public List<String> getEcosObjectList() {
        String[] arr = new String[_tecos.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<Integer> en = _tecos.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = ""+en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    static public EcosTurnoutManager instance() {
        if (_instance == null) _instance = new EcosTurnoutManager();
        return _instance;
    }
    static EcosTurnoutManager _instance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosTurnoutManager.class.getName());
}

/* @(#)EcosTurnoutManager.java */
