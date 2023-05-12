package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;

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
        if (node.getNodeParamManager().getParameters()!=null) {
            super.getNodeParamManager().setParameters(node.getNodeParamManager().getParameters());
        } else {
            setBackupResult(BackupType.COMPLETEDWITHERROR);
        }
        if (node.getNodeNvManager().getNvArray()!=null) {
            super.getNodeNvManager().setNVs(node.getNodeNvManager().getNvArray());
        } else {
            setBackupResult(BackupType.COMPLETEDWITHERROR);
        }
        // copy events
        ArrayList<CbusNodeEvent> _tmpArr = node.getNodeEventManager().getEventArray();
        if (_tmpArr !=null) {
            _tmpArr.forEach((ndEv) -> {
                getNodeEventManager().addNewEvent(new CbusNodeEvent( ndEv ));
            });
        } else {
            setBackupResult(BackupType.COMPLETEDWITHERROR);
        }
        if (getBackupResult() == BackupType.INCOMPLETE) {
            setBackupResult(BackupType.COMPLETE);
        }
    }
    
    /**
     * Ignores incoming and outgoing CAN Frames
     * {@inheritDoc}
     */
    @Override
    public CbusNodeCanListener getNewCanListener(){
        return new DoNothingCanListener();
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
    protected final void setBackupResult(BackupType type) {
        _backupType = type;
    }

    /**
     * Get the Backup Result
     * @return enum
     */  
    public final BackupType getBackupResult() {
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
        getNodeEventManager().addNewEvent(buildEv);
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
        
        if (!(getNodeParamManager().getParameterHexString().equals(toTest.getNodeParamManager().getParameterHexString()))){
            text.append("Parameters Changed"+" ");
        }
        
        if (!(getNodeNvManager().getNvHexString().equals(toTest.getNodeNvManager().getNvHexString()))){
            text.append("NV's Changed"+" ");
        }
        
        if ( getNodeEventManager().getTotalNodeEvents() != toTest.getNodeEventManager().getTotalNodeEvents() ) {
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
        if (!(this.getNodeParamManager().getParameterHexString().equals(t.getNodeParamManager().getParameterHexString()))){
            return false;
        }
        if (!(this.getNodeNvManager().getNvHexString().equals(t.getNodeNvManager().getNvHexString()))){
            return false;
        }
        if ( this.getNodeEventManager().getTotalNodeEvents() != t.getNodeEventManager().getTotalNodeEvents() ) {
            return false;
        }
        
        ArrayList<CbusNodeEvent> thisEvs = this.getNodeEventManager().getEventArray();
        ArrayList<CbusNodeEvent> otherEvs = t.getNodeEventManager().getEventArray();
        
        if ( thisEvs !=null && otherEvs !=null) {
            java.util.Collections.sort(thisEvs);
            java.util.Collections.sort(otherEvs);
        }
        return this.getEventArrayHash() == t.getEventArrayHash();
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override public int hashCode() {
        return Objects.hash(getNodeNumber(),getNodeParamManager().getParameterHexString(),
            getNodeNvManager().getNvHexString(),getEventArrayHash());
    }
    
    /** 
     * Get a Hashcode for the Event Array
     * @return 0 if event array null
     */
    public int getEventArrayHash(){
        ArrayList<CbusNodeEvent> _tmpArr = getNodeEventManager().getEventArray();
        if ( _tmpArr == null ) {
            return 0;
        } else {
            return _tmpArr.hashCode();
        }
    }
    
    /**
     * toString reports the Node Number Name and backup timestamp
     * @return string eg "1234 UserName Backup Sun Jul 07 22:41:22".
     * {@inheritDoc} 
     */
    @Override
    public String toString(){
        return super.toString()+ " Backup " + getBackupTimeStamp();
    }
    
    /**
     * Ignores Incoming and Outgoing CAN Frames.
     */
    protected static class DoNothingCanListener extends CbusNodeCanListener {

        public DoNothingCanListener(){
            super(null,null);
        }
        
        /**
         * Ignores outgoing CAN Frames.
         * {@inheritDoc}
         */
        @Override
        public void message(CanMessage m) {}

        /**
         * Ignores incoming CAN Frames.
         * {@inheritDoc}
         */
        @Override
        public void reply(CanReply m) {}
    
}
    
    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeFromBackup.class);
    
}
