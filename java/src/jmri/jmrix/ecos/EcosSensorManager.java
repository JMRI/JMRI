// EcosSensorManager.java

package jmri.jmrix.ecos;

import java.util.Hashtable;
import jmri.Sensor;

/**
 * Implement sensor manager for Ecos systems.
 * The Manager handles all the state changes.
 * <P>
 * System names are "USnnn:yy", where nnn is the Ecos Object Number for a given
 * s88 Bus Module and yy is the port on that module.
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision$
 */
public class EcosSensorManager extends jmri.managers.AbstractSensorManager
                                implements EcosListener {

    public EcosSensorManager(EcosSystemConnectionMemo memo) {
        this.memo = memo;
        tc = memo.getTrafficController();
        // listen for sensor creation
        // connect to the TrafficManager
        tc.addEcosListener(this);
                
        // ask to be notified
        //Not sure if we need to worry about newly created sensors on the layout.
        /*EcosMessage m = new EcosMessage("request(26, view)");
        tc.sendEcosMessage(m, this);*/
        
        // get initial state
        EcosMessage m = new EcosMessage("queryObjects(26, ports)");
        tc.sendEcosMessage(m, this);
    }
    EcosSystemConnectionMemo memo;
    EcosTrafficController tc;
    //The hash table simply holds the object number against the EcosSensor ref.
    private Hashtable <Integer, EcosSensor> _tecos = new Hashtable<Integer, EcosSensor>();   // stores known Ecos Object ids to DCC
    private Hashtable <Integer, Integer> _sport = new Hashtable<Integer, Integer>();   // stores known Ecos Object ids to DCC
    
    public String getSystemPrefix() { return memo.getSystemPrefix(); }
    
    public Sensor createNewSensor(String systemName, String userName) {
        //int ports = Integer.valueOf(systemName.substring(2)).intValue();
        Sensor s = new EcosSensor(systemName, userName);
        //s.setUserName(userName);

        return s;
    }

    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // is this a list of sensors?
        EcosSensor es;
        if(m.getResultCode()==0){
            int ecosObjectId = m.getEcosObjectId();
            if((ecosObjectId!=26) && ((ecosObjectId<100) || (ecosObjectId>300))){
                log.debug("message receieved that is not within the valid Sensor object range");
                return;
            }
            if (m.isUnsolicited() || m.getReplyType().equals("get")){ //<Event Messages are unsolicited
                String[] lines = m.getContents();
                for(int i = 0; i<lines.length; i++){
                    int start = lines[i].indexOf("[")+1; 
                    int end = lines[i].indexOf("]");
                    if(lines[i].contains("state")){
                        start = start+2;
                        if (start>0 && end >0) {
                            String val = lines[i].substring(start, end);
                            int intState = Integer.valueOf(val,16).intValue();
                            decodeSensorState(ecosObjectId, intState);
                        }
                    } 
                    if(lines[i].contains("railcom")){
                        //int newstate = UNKNOWN;
                        if (start>0 && end >0) {
                            String val = lines[i].substring(start, lines[i].indexOf(",")).trim();
                            int j = Integer.valueOf(val).intValue();
                            j++;
                            StringBuilder sb = new StringBuilder();
                            sb.append(getSystemPrefix());
                            sb.append("R");
                            sb.append(ecosObjectId);
                            sb.append(":");
                            //Little work around to pad single digit address out.
                            if (j<10)
                                sb.append("0");
                            sb.append(j);
                            EcosReporter rp = (EcosReporter) memo.getReporterManager().provideReporter(sb.toString());
                            if(rp!=null) {
                                rp.decodeDetails(lines[i]);
                            }
                        }
                    
                    }
                }
                //With the sensor manager we don't keep an eye on the manager, which we proabaly need to do.
            } else {
                if(m.getReplyType().equals("queryObjects") && ecosObjectId==26){
                    String[] lines = m.getContents();
                    for (int i = 0; i<lines.length; i++) {
                        if (lines[i].contains("ports[")) { // skip odd lines
                            int start = 0;
                            int end = lines[i].indexOf(' ');
                            int object = Integer.parseInt(lines[i].substring(start, end));

                            if ( (100<=object) && (object<300)) { // only physical sensors
                                start = lines[i].indexOf('[')+1;
                                end = lines[i].indexOf(']');
                                int ports = Integer.parseInt(lines[i].substring(start, end));
                                log.debug("Found sensor object "+object+" ports "+ports);
                                
                                if ((ports == 8) || (ports == 16)){
                                    Sensor s;
                                    String sensorprefix = getSystemPrefix()+"S:"+object+":";
                                    _sport.put(object, ports);
                                    //ports 1, 5, 9 13 on a ECoS detector are railcom enabled., but value in messages is returned 0, 4, 8, 12
                                    for (int j=1; j<=ports; j++){
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(sensorprefix);
                                        //Little work around to pad single digit address out.
                                        if (j<10)
                                            sb.append("0");
                                        sb.append(j);
                                        s=getSensor(sb.toString());
                                        if(s==null){
                                            es = (EcosSensor)provideSensor(sb.toString());
                                            es.setObjectNumber(object);
                                            _tecos.put(object, es);
                                            if(object>=200 && (j==1 || j==5 || j==9 || j==13)){
                                                sb = new StringBuilder();
                                                sb.append(getSystemPrefix());
                                                sb.append("R");
                                                sb.append(object);
                                                sb.append(":");
                                                //Little work around to pad single digit address out.
                                                if (j<10)
                                                    sb.append("0");
                                                sb.append(j);
                                                EcosReporter rp = (EcosReporter) memo.getReporterManager().provideReporter(sb.toString());
                                                if(rp!=null) {
                                                    rp.setObjectPort(object, (j-1));
                                                    es.setReporter(rp);
                                                    EcosMessage em = new EcosMessage("get("+object+", railcom["+(j-1)+"])");
                                                    tc.sendEcosMessage(em, this);
                                                }
                                            }
                                        }
                                    }
                                    EcosMessage em = new EcosMessage("request("+object+", view)");
                                    tc.sendEcosMessage(em, this);
                                    
                                    em = new EcosMessage("get("+object+",state)");
                                    tc.sendEcosMessage(em, this);
                                } else {
                                    log.debug("Invalid number of ports returned for Module " + object);
                                }
                            } 
                        }
                    }
                }
            }
        }
    }
    

    public void message(EcosMessage m) {
        // messages are ignored
    }
    
    private void decodeSensorState(int object, int intState){
        EcosSensor es;
        int k = 1;
        int result;
        String sensorprefix = getSystemPrefix()+"S:"+object+":";
        for(int port =1; port<=_sport.get(object); port++){
            result = intState & k;
            //Little work around to pad single digit address out.
            StringBuilder sb = new StringBuilder();
            sb.append(sensorprefix);
            //Little work around to pad single digit address out.
            if (port<10)
                sb.append("0");
            sb.append(port);
            es=(EcosSensor)getSensor(sb.toString());
            if (result==0)
                es.setOwnState(Sensor.INACTIVE);
            else {
                es.setOwnState(Sensor.ACTIVE);
            }
            k=k*2;
        }
    }
    
    public void refreshItems(){
        /*ask to be notified about newly created sensors on the layout.
        Doing the request to view the list, will also kick off a request to 
        view each individual sensor*/
        EcosMessage m = new EcosMessage("queryObjects(26, ports)");
        tc.sendEcosMessage(m, this);
        
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosSensorManager.class.getName());
}

/* @(#)EcosSensorManager.java */
