package jmri.jmrit.withrottle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.jmrit.consisttool.ConsistFile;


/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.2 $
 */
public class ConsistController extends AbstractController{
    
    private ConsistManager manager;
    private ConsistFile file;
    private boolean isConsistAllowed;

    public ConsistController(){
        manager = jmri.InstanceManager.consistManagerInstance();
        if (manager == null){
            log.info("No consist manager instance.");
            isValid = false;
        }else {
            file = new ConsistFile();
            try{
               file.ReadFile();
            }
            catch(Exception e){
               log.warn("error reading consist file: " +e);
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
        
        if (isConsistAllowed){  //  Allow Make & Break consists
            message = ("RCC"+manager.getConsistList().size());  //  Roster Consist Controller
        }else{  //  Just allow selection list
            message = ("RCL"+manager.getConsistList().size());  //  Roster Consist List
        }

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }
    }

    public void sendAllConsistData(){
        for (DccLocoAddress conAddr : manager.getConsistList()){
            sendDataForConsist(manager.getConsist(conAddr));
        }
        
    }
    
    public void sendDataForConsist(Consist con){
        if (listeners == null) return;
        StringBuilder list = new StringBuilder("RCD");  //  Roster Consist Data
        list.append("}|{" + con.getConsistAddress());
        list.append("}|{");
        if (con.getConsistID().length() > 0){
            list.append(con.getConsistID());
        }
        
        for (DccLocoAddress loco : con.getConsistList()){
            list.append("]\\[" + loco.toString());
            list.append("}|{" + con.getLocoDirection(loco));
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
        
        try{
            file.WriteFile(manager.getConsistList());
        }catch(IOException e){
            log.warn("Consist file could not be written!");
        }
                
    }
    
    
    /**
     * remove a consist by it's Dcc address. Wiil remove all locos in the process.
     * @param message RCR<;>consistAddress
     */
    private void removeConsist(String message){
        List<String> header = Arrays.asList(message.split("<;>"));
        try{
            manager.delConsist(stringToDcc(header.get(1)));
        }catch(NullPointerException noCon){
            log.warn("Consist: "+header.get(1)+" not found. Cannot delete.");
            return;
        }
        
        try{
            file.WriteFile(manager.getConsistList());
        }catch(IOException e){
            log.warn("Consist file could not be written!");
        }
        
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
        
        try{
            file.WriteFile(manager.getConsistList());
        }catch(IOException e){
            log.warn("Consist file could not be written!");
        }
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
            
            if (consist.contains(stringToDcc(locoData.get(0)))){
                consist.remove(stringToDcc(locoData.get(0)));
                if (log.isDebugEnabled()) log.debug("Remove loco: "+locoData.get(0)+", from consist: "+headerData.get(1));
            }
        }catch(NullPointerException e){
            log.warn("removeLoco error for message: " + message);
            return;
        }
        
        try{
            file.WriteFile(manager.getConsistList());
        }catch(IOException e){
            log.warn("Consist file could not be written!");
        }
    }
    
    public DccLocoAddress stringToDcc(String s){
        int num = Integer.parseInt(s.substring(1));
        boolean isLong = (s.charAt(0) == 'L');
        return (new DccLocoAddress(num, isLong));
    }

    void register() {
        throw new UnsupportedOperationException("Not used.");
    }

    void deregister() {
        throw new UnsupportedOperationException("Not used.");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConsistController.class.getName());

}
