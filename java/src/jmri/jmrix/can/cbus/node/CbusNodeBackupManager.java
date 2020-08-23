package jmri.jmrix.can.cbus.node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
import jmri.util.ThreadingUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;


/**
 * Class to work with CbusNode xml files
 * Loosely based on
 * Load and store the timetable data file: TimeTableData.xml
 * @author Dave Sand Copyright (C) 2018
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupManager {
    
    public final SimpleDateFormat xmlDateStyle = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private int _nodeNum = 0;
    private final CbusBasicNodeWithManagers _node;
    private ArrayList<CbusNodeFromBackup> _backupInfos;
    private final CbusPreferences preferences;
    private boolean backupInit; // node details loaded from file
    private boolean backupStarted; // auto startup backup started
    
    /**
     * Create a new CbusNodeBackupManager
     * @param node the CbusNode which the xml is associated with
     */
    public CbusNodeBackupManager(CbusBasicNodeWithManagers node) {
        _nodeNum = node.getNodeNumber();
        _node = node;
        _backupInfos = new ArrayList<>();
        preferences = jmri.InstanceManager.getNullableDefault(CbusPreferences.class);
        backupInit = false;
        backupStarted = false;
        // doLoad();
        
    }
    
    /**
     * Get a list of all of the backups currently in the xml file
     * @return may be zero length if no backups
     */
    public ArrayList<CbusNodeFromBackup> getBackups() {
        return _backupInfos;
    }
    
    public int getNumCompleteBackups() {
        int i=0;
        for (int j = 0; j <_backupInfos.size() ; j++) {
            if (_backupInfos.get(j).getBackupResult() == BackupType.COMPLETE ){
                i++;
            }
        }
        return i;
    }
    
    /**
     * Get the time of first full backup for the Node.
     *
     * @return value else null if unknown
     */
    @CheckForNull
    public java.util.Date getFirstBackupTime() {
        for (int j = _backupInfos.size()-1; j >-1 ; j--) {
            if ( _backupInfos.get(j).getBackupResult() == BackupType.COMPLETE ){
                return _backupInfos.get(j).getBackupTimeStamp();
            }
        }
        return null;
    }
    
    /**
     * Get the time of last full backup for the Node.
     *
     * @return value else null if unknown
     */
    @CheckForNull
    public java.util.Date getLastBackupTime() {
        for (int j = 0; j <_backupInfos.size(); j++) {
            if ( _backupInfos.get(j).getBackupResult() == BackupType.COMPLETE ){
                return _backupInfos.get(j).getBackupTimeStamp();
            }
        }
        return null;
    }
    
    /**
     * Get number of backups in arraylist that are complete, do no have a comment
     * and could potentially be deleted.
     */
    private int numAutoBackups(){
        int i=0;
        for (int j = _backupInfos.size()-1; j >-1 ; j--) {
            if (_backupInfos.get(j).getBackupComment().isEmpty()
                && _backupInfos.get(j).getBackupResult() == BackupType.COMPLETE ){
                i++;
            }
        }
        return i;
    }
    
    /**
     * Delete older backups depending on user pref. number
     */
    private void trimBackups(){
        
        if (preferences==null) {
            return;
        }
        
        // note size-2 means we never delete the oldest one
        for (int i = _backupInfos.size()-2; i >-1 ; i--) {
            if ( numAutoBackups()<=preferences.getMinimumNumBackupsToKeep()){
                return;
            }
            if ( _backupInfos.get(i).getBackupComment().isEmpty()){
                _backupInfos.remove(i);
            }
        }
    }
    
    /**
     * Full XML load.
     * Searches for XML file for the node and reads info
     * Sets internal flag so can only be triggered once
     */
    public final void doLoad(){
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        
        if (!( _node instanceof CbusNode)){
            return;
        }
        
        if (backupInit) {
            return;
        }
        
        backupInit = true;
        
        ThreadingUtil.runOnLayout( () -> {
            
            File file = x.getFile(_node.getNodeNumber(),true);

            if (file == null) {
                log.debug("No backup file to load");
                return;
            }
            boolean _sortOnLoad = true;
            // Find root
            Element root;

            try {
                root = x.rootFromFile(file);
                if (root == null) {
                    log.info("File could not be read");
                    return;
                }

                Element details;

                // UserName
                details = root.getChild("UserName");  // NOI18N
                if (details != null && (!details.getValue().isEmpty())) {
                    ((CbusNode) _node).setUserName(details.getValue());
                }

                // Module Type Name
                details = root.getChild("ModuleTypeName");  // NOI18N
                if (details != null && (!details.getValue().isEmpty())) {
                   ((CbusNode) _node).setNodeNameFromName(details.getValue());
                }

                // user Comments Freetext
                details = root.getChild("FreeText");  // NOI18N
                if (details != null && (!details.getValue().isEmpty())) {
                    ((CbusNode) _node).setUserComment(
                        details.getValue().replaceAll("\\\\n",System.getProperty("line.separator")));
                }

                Element BackupStatus = root.getChild("Backups");  // NOI18N
                if (BackupStatus == null) {
                    log.warn("Unable to find a Previous Layout Backup Entry");
                }
                else {
                    for (Element info : BackupStatus.getChildren("BackupInfo")) {  // NOI18N
                        boolean _backupInfoError = false;
                        CbusNodeFromBackup nodeBackup = new CbusNodeFromBackup(null,_nodeNum);

                        if ( info.getAttributeValue("dateTimeStamp") !=null ) { // NOI18N
                            try {
                                Date newDate = xmlDateStyle.parse(info.getAttributeValue("dateTimeStamp")); // NOI18N
                                nodeBackup.setBackupTimeStamp( newDate ); // temp
                            } catch (java.text.ParseException e) { 
                                log.error("Unable to parse date {}",info.getAttributeValue("dateTimeStamp")); // NOI18N
                                _sortOnLoad = false;
                                _backupInfoError = true;
                            }
                        } else {
                            log.error("NO datetimestamp in a backup log entry");
                            _sortOnLoad = false;
                            _backupInfoError = true;
                        }
                        if ( info.getAttributeValue("result") !=null &&  // NOI18N
                            CbusNodeConstants.lookupByName(info.getAttributeValue("result"))!=null ) { // NOI18N
                            nodeBackup.setBackupResult(CbusNodeConstants.lookupByName(info.getAttributeValue("result")) );
                        } else {
                            log.error("NO result in a backup log entry");
                            _backupInfoError = true;
                            nodeBackup.setBackupResult(BackupType.INCOMPLETE);
                        }
                        Element params = info.getChild("Parameters");  // NOI18N
                        if ( params !=null && !params.getValue().isEmpty()) {
                            nodeBackup.getNodeParamManager().setParameters(StringUtil.intBytesWithTotalFromNonSpacedHexString(params.getValue(),true));
                        } else {
                            _backupInfoError = true;
                        }
                        Element nvs = info.getChild("NodeVariables");  // NOI18N
                        if ( nvs !=null ) {
                            nodeBackup.getNodeNvManager().setNVs(StringUtil.intBytesWithTotalFromNonSpacedHexString(nvs.getValue(),true));
                        }
                        Element comment = info.getChild("Comment");  // NOI18N
                        if ( comment !=null ) {
                            nodeBackup.setBackupComment(comment.getValue());
                        }
                        Element events = info.getChild("NodeEvents");  // NOI18N
                        if ( events !=null ) {
                            for (Element xmlEvent : events.getChildren("NodeEvent")) {  // NOI18N
                                if ( xmlEvent.getAttributeValue("NodeNum") !=null &&  // NOI18N
                                    xmlEvent.getAttributeValue("EventNum") !=null &&  // NOI18N
                                    xmlEvent.getAttributeValue("EvVars") !=null ) {  // NOI18N

                                    // check event variable length matches expected length in parameters
                                    if (nodeBackup.getNodeParamManager().getParameter(5)!=(xmlEvent.getAttributeValue("EvVars").length()/2)) {
                                        log.error("Incorrect Event Variable Length in Backup");
                                        _backupInfoError = true;
                                    }
                                    try {
                                        nodeBackup.addBupEvent(
                                            Integer.parseInt(xmlEvent.getAttributeValue("NodeNum")),
                                            Integer.parseInt(xmlEvent.getAttributeValue("EventNum")),
                                            xmlEvent.getAttributeValue("EvVars"));
                                    } catch (java.lang.NumberFormatException ex) {
                                        log.error("Incorrect Node / Event Number in Backup");
                                        _backupInfoError = true;
                                    }
                                }
                                else {
                                    log.error("Node / Event Number Missing in Backup");
                                    _backupInfoError = true;
                                }
                            }
                        }


                        if (_backupInfoError && nodeBackup.getBackupResult()==BackupType.COMPLETE ){
                            nodeBackup.setBackupResult(BackupType.INCOMPLETE);
                        }

                        _backupInfos.add(nodeBackup);
                    }
                }



            } catch (JDOMException ex) {
                log.error("File invalid: {}", ex, ex);  // NOI18N
                return;
            } catch (IOException ex) {
                // file might not yet exist as 1st time Node on Network
                log.debug("Possible Error reading file: ", ex);  // NOI18N
                return;
            }
            // make sure ArrayList is most recent at start array index 0, oldest at end

            if (_sortOnLoad) {
                java.util.Collections.sort(_backupInfos, java.util.Collections.reverseOrder());
            }
            _node.notifyPropertyChangeListener("BACKUPS", null, null);


        });
        
        
    }
    
    /**
     * Save the xml to user profile
     * trims backup list as per user pref.
     * @param createNew if true, creates a new backup then saves, false just saves
     * @param seenErrors if true sets backup completed with errors, else logs as backup complete
     * @return true if all OK, else false if error occurred
     */
    public boolean doStore( boolean createNew, boolean seenErrors) {
        
        if (!( _node instanceof CbusNode)){
            return false;
        }
        
        setBackupStarted(true);
      
        Date thisBackupDate = new Date();
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        File file = x.getFile(_node.getNodeNumber(),true);
        
        if (file == null) {
            log.error("Unable to get backup file prior to save");  // NOI18N
            return false;
        }
        
        doRotate();
        
        if ( createNew ) {
            _backupInfos.add(0,new CbusNodeFromBackup((CbusNode) _node,thisBackupDate));
            if (seenErrors){
                _backupInfos.get(0).setBackupResult(BackupType.COMPLETEDWITHERROR);
            }
        }
        
        // now we trim the number of backups in the list
        trimBackups();
        
        // Create root element
        Element root = new Element("CbusNode");  // NOI18N
        root.setAttribute("noNamespaceSchemaLocation", // NOI18N
            "https://raw.githubusercontent.com/MERG-DEV/JMRI/master/schema/MergCBUSNodeBackup.xsd",  // NOI18N
            org.jdom2.Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
        root.setAttribute("NodeNum", ""+_node.getNodeNumber() );  // NOI18N
        
        if (!((CbusNode) _node).getUserName().isEmpty()) {
            root.addContent(new Element("UserName").addContent(((CbusNode) _node).getUserName() )); // NOI18N
        }
        if (!((CbusNode) _node).getNodeNameFromName().isEmpty()) {
            root.addContent(new Element("ModuleTypeName").addContent(((CbusNode) _node).getNodeNameFromName() )); // NOI18N
        }
        if (!((CbusNode) _node).getUserComment().isEmpty()) {
            root.addContent(new Element("FreeText").addContent( // NOI18N
                ((CbusNode) _node).getUserComment().replaceAll("\r\n|\n|\r", "\\\\n")));
        }
        
        Document doc = new Document(root);
        Element values = new Element("Backups");
        root.addContent(values);  // NOI18N
        _backupInfos.stream().map((node) -> {
            Element e = new Element("BackupInfo");  // NOI18N
            e.setAttribute("dateTimeStamp",xmlDateStyle.format( node.getBackupTimeStamp() ));  // NOI18N
            e.setAttribute("result","" + node.getBackupResult());  // NOI18N
            if (!node.getBackupComment().isEmpty()) {
                e.addContent(new Element("Comment").addContent("" + node.getBackupComment() ));  // NOI18N
            }
            if (!node.getNodeParamManager().getParameterHexString().isEmpty()) {
                e.addContent(new Element("Parameters").addContent("" + node.getNodeParamManager().getParameterHexString() ));  // NOI18N
            }
            if (!node.getNodeNvManager().getNvHexString().isEmpty()) {
                e.addContent(new Element("NodeVariables").addContent("" + node.getNodeNvManager().getNvHexString() ));  // NOI18N
            }
            if (node.getNodeEventManager().getTotalNodeEvents()>0) {
                // log.info("events on backup node");
                Element bupev = new Element("NodeEvents"); // NOI18N
                
                ArrayList<CbusNodeEvent> _tmpArr = node.getNodeEventManager().getEventArray();
                if ( _tmpArr!=null ) {
                    _tmpArr.forEach((bupndev) -> {
                        Element ndev = new Element("NodeEvent"); // NOI18N
                        ndev.setAttribute("NodeNum","" + bupndev.getNn());  // NOI18N
                        ndev.setAttribute("EventNum","" + bupndev.getEn());  // NOI18N
                        ndev.setAttribute("EvVars","" + bupndev.getHexEvVarString());  // NOI18N
                        bupev.addContent(ndev);
                    });
                    e.addContent(bupev);
                }
            }
            return e;
        }).forEachOrdered((e) -> {
            values.addContent(e);
        });
        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: ", ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("IO Exception when writing: ", ex);  // NOI18N
            return false;
        }

        log.debug("...done");  // NOI18N
        _node.notifyPropertyChangeListener("BACKUPS", null, null);
        return true;
    }
    
    /**
     * Add an xml entry advising Node Not on Network
     */
    protected void nodeNotOnNetwork(){
        if (_node instanceof CbusNode) { 
            CbusNodeFromBackup newBup = new CbusNodeFromBackup((CbusNode)_node,new Date());
            newBup.setBackupResult(BackupType.NOTONNETWORK);
            _backupInfos.add(0,newBup);
            doStore(false, false);
        }
    }
    
    /**
     * Add an xml entry advising Node in SLiM Mode
     */
    protected void nodeInSLiM(){
        if (_node instanceof CbusNode) { 
            CbusNodeFromBackup newBup = new CbusNodeFromBackup((CbusNode)_node,new Date());
            newBup.setBackupResult(BackupType.SLIM);
            _backupInfos.add(0,newBup);
            doStore(false, false);
        }
    }

    /**
     * Remove Node XML File
     * @param rotate if true, creates and rotates .bup files before delete, false just deletes the core node file
     * @return true on success, else false
     */
    protected boolean removeNode( boolean rotate){
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        if (rotate) {
            doRotate();
        }
        if ( !x.deleteFile(_node.getNodeNumber())){
            log.error("Unable to delete node xml file");
            return false;
        }
        return true;
    }
    
    private void doRotate(){
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        File file = x.getFile(_node.getNodeNumber(),false);
        if (file == null){
            return;
        }
        try {
            Element roots = x.rootFromFile(file);
            log.debug("File exists {}",roots);
            FileUtil.rotate(file, 5, "bup");  // NOI18N
        } catch (IOException ex) {
            // the file might not exist
            log.debug("Backup Rotate failed {}",file);  // NOI18N
        } catch (JDOMException | NullPointerException ex) {
            // file might not exist
            log.debug("File invalid: {}", ex);  // NOI18N
        }
    }
    
    /**
     * Get the XML File Location
     * @return Location of the file, creating new if required
     */
    protected File getFileLocation() {
        return new CbusNodeBackupFile().getFile(_node.getNodeNumber(),true);
    }
    
    /**
     * Reset the backup array for testing
     */
    protected void resetBupArray() {
        _backupInfos = new ArrayList<>();
        backupInit = false;
    }
    
    /**
     * Get the current backup status for the Node.
     *
     * @return ENUM from CbusNodeConstants, e.g. BackupType.OUTSTANDING or BackupType.COMPLETE
     */
    @Nonnull
    public BackupType getSessionBackupStatus() {
        if (backupStarted && getBackups().size() >0 ) {
            return getBackups().get(0).getBackupResult();
        } else {
            return BackupType.OUTSTANDING;
        }
    }

    /**
     * Set internal flag for backup started.
     * Triggered within the backup script which is called from various places
     * @param started true if started
     */
    protected void setBackupStarted( boolean started) {
        backupStarted = started;
    }
    
    protected boolean getBackupStarted(){
        return backupStarted;
    }
    
    protected void setNodeInSlim() {
        log.info("Node {} in SLiM mode",_node);
        if (getBackupStarted()) { // 1st time in this session
            doLoad();
            nodeInSLiM();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeBackupManager.class);
}
