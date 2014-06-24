// TamsSensorManager.java

package jmri.jmrix.tams;

import java.util.Hashtable;
import jmri.JmriException;
import jmri.Sensor;

/**
 * Implement sensor manager for Tams systems.
 * The Manager handles all the state changes.
 * Requires v1.4.7 of TAMS software to work correctly
 * <P>
 * System names are "USnnn:yy", where nnn is the Tams Object Number for a given
 * s88 Bus Module and yy is the port on that module.
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 20820 $
 */
public class TamsSensorManager extends jmri.managers.AbstractSensorManager
                                implements TamsListener {

    public TamsSensorManager(TamsSystemConnectionMemo memo) {
        this.memo = memo;
        tc = memo.getTrafficController();
        //Send a message to tell the s88 to auto reset.
        startPolling();
    }
    
    TamsSystemConnectionMemo memo;
    TamsTrafficController tc;
    //The hash table simply holds the object number against the TamsSensor ref.
    private Hashtable <Integer, Hashtable<Integer, TamsSensor>> _ttams = new Hashtable<Integer, Hashtable<Integer, TamsSensor>>();   // stores known Tams Obj
    
    public String getSystemPrefix() { return memo.getSystemPrefix(); }
    
    public Sensor createNewSensor(String systemName, String userName) {
        TamsSensor s = new TamsSensor(systemName, userName);
        if(systemName.contains(":")){
            int board = 0;
            int channel = 0;
            
            String curAddress = systemName.substring(getSystemPrefix().length()+1, systemName.length());
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                if(!_ttams.containsKey(board)){
                    _ttams.put(board, new Hashtable<Integer, TamsSensor>());
                    if(_ttams.size()==1){
                        synchronized(pollHandler) {
                            pollHandler.notify();
                        }
                    }
                }
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
            Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
            try {
                channel = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
                if(!sensorList.containsKey(channel)){
                    sensorList.put(channel, s);
                }
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
        }
        
        return s;
    }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        if(!curAddress.contains(":")){
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Hardware Address passed should be past in the form 'Module:port'");
        }

        //Address format passed is in the form of board:channel or T:turnout address
        int seperator = curAddress.indexOf(":");
        try {
            board = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
        } catch (NumberFormatException ex) { 
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Module Address passed should be a number");
        }
        try {
            port = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
        } catch (NumberFormatException ex) { 
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Port Address passed should be a number");
        }
        
        if(port==0 || port>16){
            log.error("Port number must be between 1 and 16");
            throw new JmriException("Port number must be between 1 and 16");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPrefix());
        sb.append("S");
        sb.append(board);
        sb.append(":");
        //Little work around to pad single digit address out.
        padPortNumber(port, sb);
        return sb.toString();
    }
    
    int board = 0;
    int port = 0;
    
    public String getNextValidAddress(String curAddress, String prefix){

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
            showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false);
            return null;
        }
        
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if(s!=null){
            port++;
            while(port<17){
                try{
                    tmpSName = createSystemName(board+":"+port, prefix);
                } catch (Exception e){
                    log.error("Error creating system name for " + board + ":" + port);
                }
                s = getBySystemName(tmpSName);
                if(s==null){
                    StringBuilder sb = new StringBuilder();
                    sb.append(board);
                    sb.append(":");
                    //Little work around to pad single digit address out.
                    padPortNumber(port, sb);
                    return sb.toString();
                }
                port++;
            }
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(board);
            sb.append(":");
            //Little work around to pad single digit address out.
            padPortNumber(port, sb);
            return sb.toString();
        }
        
    }
    
    void padPortNumber (int portNo, StringBuilder sb){
        if (portNo<10){
            sb.append("0");
        }
        sb.append(portNo);
    }

    // to listen for status changes from Tams system
    public void reply(TamsReply r) {
        decodeSensorState(r);
    }
    
    Thread pollThread;
    boolean stopPolling = true;
    
    protected Runnable pollHandler ;
    
    protected void startPolling(){
        stopPolling = false;
        log.debug("Completed build of active readers " + _ttams.size());
        //if (_ttams.size()>0) {
            if( pollHandler == null )
              pollHandler = new PollHandler(this);
            Thread pollThread = new Thread(pollHandler, "TAMS Sensor Poll handler");
            //log.debug("Poll Handler thread starts at priority "+xmtpriority);
            pollThread.setDaemon(true);
            pollThread.setPriority(Thread.MAX_PRIORITY-1);
            pollThread.start();
            //pollHandler.notify();
        /*} else {
            log.debug("No active boards found");
        }*/
    }
    
    class PollHandler implements Runnable{
        TamsSensorManager sm = null;
        PollHandler(TamsSensorManager m){
            sm = m;
        }
        public void run() {
            while(true){
                new jmri.util.WaitHandler(this);
                TamsMessage m = new TamsMessage(new byte[] {(byte)0x78,(byte)0x53,(byte)0x52,(byte)0x31});
                tc.sendTamsMessage(m, null);
                m = new TamsMessage(new byte[] {(byte)0x78,(byte)0x53,(byte)0x52,(byte)0x30});
                tc.sendTamsMessage(m, null);
                try{
                    synchronized(this) {
                        wait(200); //Wait for 200ms between xSR0 and 0x99.
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
                m = new TamsMessage(new byte[] {(byte)0x99});
                tc.sendTamsMessage(m, sm);
            }
        }
    }
    
    public void handleTimeout(TamsMessage m){
        if(log.isDebugEnabled())
            log.debug("timeout recieved to our last message " + m.toString());

        if(!stopPolling){
            synchronized(pollHandler) {
                pollHandler.notify();
            }
            if(log.isDebugEnabled())
                log.debug("time out to sensor status request");
        }
    }
    

    public void message(TamsMessage m) {
        // messages are ignored
    }
    
    
    private void decodeSensorState(TamsReply r){
        String sensorprefix = getSystemPrefix()+"S"+board+":";
        //First byte represents board 1, ports 1 to 8, second byte represents ports 9 to 16.
        for(int board: _ttams.keySet()){
            Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
            int startElement = (board*2)-2;
            int i = (r.getElement(startElement)&0xff)<<8;
            i = i + (r.getElement(startElement+1)&0xff);
            int mask = 32768;
            for(int port = 1; port<=16; port++){
                int result = i & mask;
                TamsSensor ms= sensorList.get(port);
                if(ms==null){
                    StringBuilder sb = new StringBuilder();
                    sb.append(sensorprefix);
                    //Little work around to pad single digit address out.
                    padPortNumber(port, sb);
                    ms = (TamsSensor)provideSensor(sb.toString());
                }
                if(ms!=null){
                    if (result==0)
                        ms.setOwnState(Sensor.INACTIVE);
                    else {
                        ms.setOwnState(Sensor.ACTIVE);
                    }
                }
                mask = mask/2;
            }
        }
        if(!stopPolling){
            synchronized(pollHandler) {
                pollHandler.notify();
            }
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsSensorManager.class.getName());
}

/* @(#)TamsSensorManager.java */
