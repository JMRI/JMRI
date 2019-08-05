package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;
    
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to represent a node imported from FCU file or CbusNodeXml.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeFromBackup extends CbusNode implements Comparable<CbusNodeFromBackup> {
    
    private Date _timeStamp;
    private String _backupComment;
    private BackupType _backupType;
    
    /**
     * Create a new CbusNodeFrommBackup by connection type and Node Number
     * 
     * @param connmemo CAN Connection
     * @param nodenumber Node Number between 1 and 65535
     */  
    public CbusNodeFromBackup ( CanSystemConnectionMemo connmemo, int nodenumber ){
        super( connmemo, nodenumber );  
        _backupComment = "";
    }
    
    /**
     * Create a new CbusNodeFrommBackup from an existing Node
     * 
     * @param node The Node to make a copy of
     * @param timeStamp to set the Backup TimeStamp
     */  
    public CbusNodeFromBackup ( CbusNode node, Date timeStamp) {
        super( null, node.getNodeNumber() ); 
        _backupComment = "";
        setBackupResult(BackupType.INCOMPLETE);
        _timeStamp = timeStamp;
        if (node.getParameters()!=null) {
            setParameters(node.getParameters());
        } else {
            setBackupResult(BackupType.COMPLETEDWITHERROR);
        }
        if (node.getNvArray()!=null) {
            setNVs(node.getNvArray());
        } else {
            setBackupResult(BackupType.COMPLETEDWITHERROR);
        }
        // copy events
        if (node.getEventArray()!=null) {
            node.getEventArray().forEach((ndEv) -> {
                addNewEvent(new CbusNodeEvent( ndEv ));
            });
        } else {
            setBackupResult(BackupType.COMPLETEDWITHERROR);
        }
        if (getBackupResult() == BackupType.INCOMPLETE) {
            setBackupResult(BackupType.COMPLETE);
        }
    }
    
    /**
     * Set the Backup DateTime
     * @param thisDate Timestamp
     */     
    protected void setBackupTimeStamp( Date thisDate){
        _timeStamp = thisDate;
    }
    
    /**
     * Get the Backup DateTime
     * @return DateTime in format
     */  
    public Date getBackupTimeStamp(){
        return _timeStamp;
    }

    /**
     * Set the Backup Result
     * @param type Backup Type Enum
     */  
    protected void setBackupResult(BackupType type) {
        _backupType = type;
    }

    /**
     * Get the Backup Result
     * @return enum
     */  
    public BackupType getBackupResult() {
        return _backupType;
    }
    
    /**
     * Set the backup comment
     * @param backupComment  text representation of the single backup state
     */  
    public void setBackupComment(String backupComment) {
        _backupComment = backupComment;
    }
    
    /**
     * Get the Backup Comment
     * eg. Completed No Issues, 9 NVs, 12 Events with 4 EVs
     * 
     * @return index number, -1 if unset
     */  
    public String getBackupComment() {
        return _backupComment;
    }
    
    /**
     * Add an event to the Node in backup format
     * 
     * @param nn Event Node Number
     * @param en Event Event Number
     * @param evVars Event Variable Hex String eg. "0102DC3AFF"
     */
    public void addBupEvent(int nn, int en, String evVars){
        CbusNodeEvent buildEv = new CbusNodeEvent( nn, en , getNodeNumber(), evVars);
        addNewEvent(buildEv);
    }
    
    /**
     * Get a String comparison with another CbusNodeFromBackup
     * 
     * @param toTest The CbusNodeFromBackup to test against
     * @return eg. "Parameters Changed"
     */
    public String compareWithString( CbusNodeFromBackup toTest) {
        
        if (toTest==null){
            return ("");
        }
        
        if (equals(toTest)) {
            return Bundle.getMessage("NoChanges");
        }
        
        StringBuilder text = new StringBuilder();
        
        if (!(getParameterHexString().equals(toTest.getParameterHexString()))){
            text.append("Parameters Changed"+" ");
        }
        
        if (!(getNvHexString().equals(toTest.getNvHexString()))){
            text.append("NV's Changed"+" ");
        }
        
        if ( getTotalNodeEvents() != toTest.getTotalNodeEvents() ) {
            text.append("Number Events Changed"+" ");
        } else if (getEventArrayHash()!=toTest.getEventArrayHash()){
            text.append("Events Changed"+" ");
        }
        
        if (text.toString().isEmpty()) {
            text.append(Bundle.getMessage("NoChanges"));
        }
        
        return text.toString();
        
    }
    
    /** 
     * {@inheritDoc} 
     * Compares to the Time Date Stamp of the Backup
     */
    @Override
    public int compareTo(CbusNodeFromBackup o) {
        return this.getBackupTimeStamp().compareTo(o.getBackupTimeStamp());
    }
    
    /** 
     * {@inheritDoc} 
     * <p>
     * Used for highlighting changes to Node Backups,
     * so the Date Time Stamp does NOT need to be equal.
     * checking for Node Number, Parameters, NVs, Events.
     * Events can be in any order, are sorted mid comparison.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CbusNodeFromBackup)) {
            return false;
        }
        CbusNodeFromBackup t = (CbusNodeFromBackup) obj;
        if (this.getNodeNumber() != t.getNodeNumber()) {
            return false;
        }
        if (!(this.getParameterHexString().equals(t.getParameterHexString()))){
            return false;
        }
        if (!(this.getNvHexString().equals(t.getNvHexString()))){
            return false;
        }
        if ( this.getTotalNodeEvents() != t.getTotalNodeEvents() ) {
            return false;
        }
        if ( this.getEventArray() !=null && t.getEventArray() !=null) {
            java.util.Collections.sort(this.getEventArray());
            java.util.Collections.sort(t.getEventArray());
        }
        if (this.getEventArrayHash()!=t.getEventArrayHash()){
            return false;
        }
        return true;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override public int hashCode() {
        return Objects.hash(getNodeNumber(),getParameterHexString(),getNvHexString(),getEventArrayHash());
    }
    
    /** 
     * Get a Hashcode for the Event Array
     * @return 0 if event array null
     */
    public int getEventArrayHash(){
        if ( getEventArray() == null ) {
            return 0;
        } else {
            return getEventArray().hashCode();
        }
    }
    
    /**
     * toString reports the Node Number Name and backup timestamp
     * @return string eg "1234 UserName Backup Sun Jul 07 22:41:22".
     * {@inheritDoc} 
     */
    @Override
    public String toString(){
        return getNodeNumberName()+ " Backup " + getBackupTimeStamp();
    }
    
    /** 
     * {@inheritDoc}
     * Ignores outgoing CAN Frames
     */
    @Override
    public void message(CanMessage m) {
    }
    
    /** 
     * {@inheritDoc}
     * Ignores incoming CAN Frames
     */
    @Override
    public void reply(CanReply m) {
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose(){
    }
    
    // private static final Logger log = LoggerFactory.getLogger(CbusNodeFromBackup.class);
    
}
