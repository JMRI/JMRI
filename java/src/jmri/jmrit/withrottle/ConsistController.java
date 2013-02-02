package jmri.jmrit.withrottle;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.jmrit.consisttool.ConsistFile;
//import jmri.jmrix.nce.consist.NceConsistRoster;
//import jmri.jmrix.nce.consist.NceConsistRosterEntry;


/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */
public class ConsistController extends AbstractController implements ProgListener{
    
    private ConsistManager manager;
    private ConsistFile file;
    private boolean isConsistAllowed;

    public ConsistController(){
        //  writeFile needs to be separate method
        if (WiThrottleManager.withrottlePreferencesInstance().isUseWiFiConsist()){
            manager = new WiFiConsistManager();
            log.debug("Using WiFiConsisting");
        }else{
            manager = jmri.InstanceManager.consistManagerInstance();
            log.debug("Using JMRIConsisting");
        }
        
        if (manager == null){
            log.info("No consist manager instance.");
            isValid = false;
        }else {
            if (WiThrottleManager.withrottlePreferencesInstance().isUseWiFiConsist()) {
                file = new WiFiConsistFile(manager);
            } else {
                file = new ConsistFile();
                try {
                    file.ReadFile();
                } catch (Exception e) {
                    log.warn("error reading consist file: " + e);
                }
            }
            isValid = true;
        }
    }
/**
 * Allows device to decide how to handle consisting.
 * Just selection or selection and Make & Break.
 * .size() indicates how many consists are being sent so the device can wait before displaying them
 */
    public void sendConsistListType(){
        if (listeners == null) return;
        String message;
        
        int numConsists = manager.getConsistList().size();  //number of JMRI consists found
        if (log.isDebugEnabled()) {
        	log.debug(numConsists+" consists found.");
        }

        if (isConsistAllowed){  //  Allow Make & Break consists
            message = ("RCC"+numConsists);  //  Roster Consist Controller
        }else{  //  Just allow selection list
            message = ("RCL"+numConsists);  //  Roster Consist List
        }

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }
    }

    public void sendAllConsistData(){
        // Loop thru JMRI consists and send consist detail for each
    	for (DccLocoAddress conAddr : manager.getConsistList()){
            sendDataForConsist(manager.getConsist(conAddr));
        }
        
        // Loop through the NCE consists and send consist detail for each
    	/* dboudreau 2/26/2012 added consist manager for NCE
        NceConsistRoster r = NceConsistRoster.instance();
        List<NceConsistRosterEntry> l = r.matchingList(null, null, null, null, null, null, null, null, null, null); // take all
        int i=-1;
        for (i = 0; i<l.size(); i++) {
        	sendDataForNCEConsist(l.get(i));
        }
        */
    }
    
    //send consist detail record for a single NCE consist, e.g. RCD}|{127(S)}|{2591]\[2591(L)}|{true]\[6318(L)}|{true]\[2608(L)}|{false
    /* dboudreau 2/26/2012 added consist manager for NCE
    public void sendDataForNCEConsist(NceConsistRosterEntry con){
        if (listeners == null) return;
        StringBuilder list = new StringBuilder("RCD");  //  Roster Consist Data
        list.append("}|{");
        list.append(con.getConsistNumber());  //address
        list.append("(S)}|{");  //consist address is always short
        if (con.getId().length() > 0){  
            list.append(con.getId());   //id
        }
        
        //append entries for each loco (if set)
        list.append("]\\[");
        list.append(con.getLoco1DccAddress());  //loco address
        list.append(con.isLoco1LongAddress() ? "(L)" : "(S)");  //include length
        list.append("}|{");
        list.append((con.getLoco1Direction().equals("normal")));  //forward is true, reverse is false
        if (!con.getLoco2DccAddress().equals("")) {
        	list.append("]\\[");
                list.append(con.getLoco2DccAddress());
        	list.append(con.isLoco2LongAddress() ? "(L)" : "(S)");
        	list.append("}|{");
                list.append((con.getLoco2Direction().equals("normal")));
        }
        if (!con.getLoco3DccAddress().equals("")) {
        	list.append("]\\[");
                list.append(con.getLoco3DccAddress());
        	list.append(con.isLoco3LongAddress() ? "(L)" : "(S)");
        	list.append("}|{");
                list.append((con.getLoco3Direction().equals("normal")));
        }
        if (!con.getLoco4DccAddress().equals("")) {
        	list.append("]\\[");
                list.append(con.getLoco4DccAddress());
        	list.append(con.isLoco4LongAddress() ? "(L)" : "(S)");
        	list.append("}|{");
                list.append((con.getLoco4Direction().equals("normal")));
        }
        if (!con.getLoco5DccAddress().equals("")) {
        	list.append("]\\[");
                list.append(con.getLoco5DccAddress());
        	list.append(con.isLoco5LongAddress() ? "(L)" : "(S)");
        	list.append("}|{");
                list.append((con.getLoco5Direction().equals("normal")));
        }
        if (!con.getLoco6DccAddress().equals("")) {
        	list.append("]\\[");
                list.append(con.getLoco6DccAddress());
        	list.append(con.isLoco6LongAddress() ? "(L)" : "(S)");
        	list.append("}|{");
                list.append((con.getLoco6Direction().equals("normal")));
        }

        String message = list.toString();

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }
    }
    */
    
    public void sendDataForConsist(Consist con){
        if (listeners == null) return;
        StringBuilder list = new StringBuilder("RCD");  //  Roster Consist Data
        list.append("}|{");
        list.append(con.getConsistAddress());
        list.append("}|{");
        if (con.getConsistID().length() > 0){
            list.append(con.getConsistID());
        }
        
        for (DccLocoAddress loco : con.getConsistList()){
            list.append("]\\[");
            list.append(loco.toString());
            list.append("}|{");
            list.append(con.getLocoDirection(loco));
        }
        

        String message = list.toString();

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }
    }
    
    public void setIsConsistAllowed(boolean b){
        isConsistAllowed = b;
    }

    boolean verifyCreation() {
        return isValid;
    }

    /**
     * 
     * @param message string containing new consist information
     */
    void handleMessage(String message) {
        try{
            if (message.charAt(0) == 'P'){  //  Change consist 'P'ositions
                reorderConsist(message);
                
            }
            if (message.charAt(0) == 'R'){  //  'R'emove consist
                removeConsist(message);
                
            }
            if (message.charAt(0) == '+'){  //  Add loco to consist and/or set relative direction
                addLoco(message);
                
            }
            if (message.charAt(0) == '-'){  //  remove loco from consist
                removeLoco(message);
                
            }
            if (message.charAt(0) == 'F'){   //  program CV 21 & 22 'F'unctions
                setConsistCVs(message);
            }
        }catch (NullPointerException exb){
            log.warn("Message \""+message+"\" does not match a consist command.");
        }
    }
    
    /**
     * Change the sequence of locos in this consist. Reorders the consistList, instead of
     * setting the 'position' value. Lead and Trail are set on first and last locos by DccConsist.
     * @param message   RCP<;>consistAddress<:>leadLoco<;>nextLoco<;>...  ...<;>nextLoco<;>trailLoco
     */
    private void reorderConsist(String message){
        Consist consist;
        List<String> headerAndLocos = Arrays.asList(message.split("<:>"));
        
        if (headerAndLocos.size()<2){
            log.warn("reorderConsist missing data in message: " + message);
            return;
        }

        try{
            List<String> headerData = Arrays.asList(headerAndLocos.get(0).split("<;>"));
            //  
            consist = manager.getConsist(stringToDcc(headerData.get(1)));
            
            List<String> locoData = Arrays.asList(headerAndLocos.get(1).split("<;>"));
            /*
             * Reorder the consistList:
             * For each loco sent, remove it from the consistList
             * and reinsert it at the front of the list.
             */
            for (String loco : locoData){
                ArrayList<DccLocoAddress> conList = consist.getConsistList();
                int index = conList.indexOf(stringToDcc(loco));
                if (index != -1){
                    conList.add(conList.remove(index));
                }
                
            }
            
        }catch(NullPointerException e){
            log.warn("reorderConsist error for message: " + message);
            return;
        }
        
        writeFile();
                
    }
    
    
    /**
     * remove a consist by it's Dcc address. Wiil remove all locos in the process.
     * @param message RCR<;>consistAddress
     */
    private void removeConsist(String message){
        List<String> header = Arrays.asList(message.split("<;>"));
        Consist consist = null;
        try{
            consist = manager.getConsist(stringToDcc(header.get(1)));
            while (!consist.getConsistList().isEmpty()){
                DccLocoAddress loco = consist.getConsistList().get(0);
                if (log.isDebugEnabled()) log.debug("Remove loco: "+loco+", from consist: "+consist.getConsistAddress().toString());
                consist.remove(loco);
            }
        }catch(NullPointerException noCon){
            log.warn("Consist: "+header.get(1)+" not found. Cannot delete.");
            return;
        }
        
        try{
            manager.delConsist(stringToDcc(header.get(1)));
        }catch(NullPointerException noCon){
            log.warn("Consist: "+header.get(1)+" not found. Cannot delete.");
            return;
        }
        
        writeFile();
        
    }
    
    /**
     * Add a loco or change it's direction.
     * Creates a new consist if one does not already exist
     * @param message RC+<;>consistAddress<;>ID<:>locoAddress<;>directionNormal
     */
    private void addLoco(String message){
        Consist consist;
        
        List<String> headerAndLoco = Arrays.asList(message.split("<:>"));

        try{
            //  Break out header and either get existing consist or create new
            List<String> headerData = Arrays.asList(headerAndLoco.get(0).split("<;>"));

            consist = manager.getConsist(stringToDcc(headerData.get(1)));
            consist.setConsistID(headerData.get(2));
            
            List<String> locoData = Arrays.asList(headerAndLoco.get(1).split("<;>"));
            
            if (consist.isAddressAllowed(stringToDcc(locoData.get(0)))){
                consist.add(stringToDcc(locoData.get(0)), Boolean.valueOf(locoData.get(1)));
                if (log.isDebugEnabled()) log.debug("add loco: "+locoData.get(0)+", to consist: "+headerData.get(1));
            }
            
        }catch(NullPointerException e){
            log.warn("addLoco error for message: " + message);
            return;
        }
        
        writeFile();
    }
    
    /**
     * remove a loco if it exist in this consist.
     * @param message RC-<;>consistAddress<:>locoAddress
     */
    private void removeLoco(String message){
        Consist consist;
        
        List<String> headerAndLoco = Arrays.asList(message.split("<:>"));

        if (log.isDebugEnabled()) log.debug("remove loco string: "+message);
        
        try{
            List<String> headerData = Arrays.asList(headerAndLoco.get(0).split("<;>"));
            
            consist = manager.getConsist(stringToDcc(headerData.get(1)));
            
            List<String> locoData = Arrays.asList(headerAndLoco.get(1).split("<;>"));
            
            DccLocoAddress loco = stringToDcc(locoData.get(0));
            if (checkForBroadcastAddress(loco)) return;

            if (consist.contains(loco)){
                consist.remove(loco);
                if (log.isDebugEnabled()) log.debug("Remove loco: "+loco+", from consist: "+headerData.get(1));
            }
        }catch(NullPointerException e){
            log.warn("removeLoco error for message: " + message);
            return;
        }
        
        writeFile();
    }
    
    private void writeFile(){
        try{
            if (WiThrottleManager.withrottlePreferencesInstance().isUseWiFiConsist()) {
                file.WriteFile(manager.getConsistList(), WiFiConsistFile.getFileLocation()+"wifiConsist.xml");
            } else {
                file.WriteFile(manager.getConsistList());
            }
        }catch(IOException e){
            log.warn("Consist file could not be written!");
        }
    }
    
    /**
     * set CV 21&22 for consist functions
     * send each CV individually
     * @param message   RCF<;> locoAddress <:> CV# <;> value
     */
    private void setConsistCVs(String message){
        
        DccLocoAddress loco;
        
        List<String> headerAndCVs = Arrays.asList(message.split("<:>"));

        if (log.isDebugEnabled()) log.debug("setConsistCVs string: "+message);
        
        try{
            List<String> headerData = Arrays.asList(headerAndCVs.get(0).split("<;>"));
            
            loco = stringToDcc(headerData.get(1));
            if (checkForBroadcastAddress(loco)) return;
            
        }catch(NullPointerException e){
            log.warn("setConsistCVs error for message: " + message);

            return;
        }
        Programmer pom = jmri.InstanceManager.programmerManagerInstance()
                .getAddressedProgrammer(loco.isLongAddress(),loco.getNumber());

        // loco done, now get CVs

        for (int i = 1; i < headerAndCVs.size(); i++){
            List<String> CVData = Arrays.asList(headerAndCVs.get(i).split("<;>"));

            try{
                int CVNum = Integer.parseInt(CVData.get(0));
                int CVValue = Integer.parseInt(CVData.get(1));
                try{
                    pom.writeCV(CVNum,CVValue,this);
                }catch(ProgrammerException e){
                }
            }catch(NumberFormatException nfe){
                log.warn("Error in setting CVs: "+nfe);
            }
        }
        jmri.InstanceManager.programmerManagerInstance().releaseAddressedProgrammer(pom);
        
    }
    
    public void programmingOpReply(int value, int status) {
        
    }
    
    public DccLocoAddress stringToDcc(String s){
        int num = Integer.parseInt(s.substring(1));
        boolean isLong = (s.charAt(0) == 'L');
        return (new DccLocoAddress(num, isLong));
    }
    
    /**
     * Check to see if an address will try to broadcast (0) a programming message.
     * 
     * @param addr  The address to check
     * @return  true if address is no good, otherwise false
     */
    public boolean checkForBroadcastAddress(DccLocoAddress addr){
        if (addr.getNumber() < 1){
            log.warn("Trying to use broadcast address!");
            return true;
        }
        return false;
    }

    void register() {
        throw new UnsupportedOperationException("Not used.");
    }

    void deregister() {
        throw new UnsupportedOperationException("Not used.");
    }

    static Logger log = Logger.getLogger(ConsistController.class.getName());

}
