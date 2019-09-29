package jmri.jmrix.can.cbus.node;

import static java.util.Comparator.comparing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JTextField;
import jmri.jmrit.XmlFile;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
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
public class CbusNodeXml {
    
    public SimpleDateFormat xmlDateStyle = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private int _nodeNum = 0;
    private CbusNode _node;
    private ArrayList<CbusNodeFromBackup> _backupInfos;
    private CbusPreferences preferences;
    
    /**
     * Create a new CbusNodeXml
     * Searches for xml file for the node and reads info
     * @param node the CbusNode which the xml is associated with
     */
    public CbusNodeXml(CbusNode node) {
        _nodeNum = node.getNodeNumber();
        _node = node;
        _backupInfos = new ArrayList<CbusNodeFromBackup>();
        preferences = jmri.InstanceManager.getNullableDefault(CbusPreferences.class);
        doLoad();
        
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
        return;
    }
    
    /**
     * Full xml load
     */
    protected void doLoad(){
        CbusNodeBackupFile x = new CbusNodeBackupFile();
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
                _node.setUserName(details.getValue());
            }
            
            // Module Type Name
            details = root.getChild("ModuleTypeName");  // NOI18N
            if (details != null && (!details.getValue().isEmpty())) {
                _node.setNodeNameFromName(details.getValue());
            }
            
            // user Comments Freetext
            details = root.getChild("FreeText");  // NOI18N
            if (details != null && (!details.getValue().isEmpty())) {
                _node.setUserComment(
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
                        nodeBackup.setParameters(StringUtil.intBytesWithTotalFromNonSpacedHexString(params.getValue(),true));
                    } else {
                        _backupInfoError = true;
                    }
                    Element nvs = info.getChild("NodeVariables");  // NOI18N
                    if ( nvs !=null ) {
                        nodeBackup.setNVs(StringUtil.intBytesWithTotalFromNonSpacedHexString(nvs.getValue(),true));
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
                                if (nodeBackup.getParameter(5)!=(xmlEvent.getAttributeValue("EvVars").length()/2)) {
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
            log.error("File invalid: " + ex);  // NOI18N
            return;
        } catch (IOException ex) {
            // file might not yet exist as 1st time Node on Network
            log.debug("Possible Error reading file: {}", ex);  // NOI18N
            return;
        }
        // make sure ArrayList is most recent at start array index 0, oldest at end
        
        if (_sortOnLoad) {
            java.util.Collections.sort(_backupInfos, java.util.Collections.reverseOrder());
        }
        _node.notifyNodeBackupTable();
        
    }
    
    /**
     * Save the xml to user profile
     * trims backup list as per user pref.
     * @param createNew if true, creates a new backup then saves, false just saves
     * @param seenErrors if true sets backup completed with errors, else logs as backup complete
     * @return true if all ok, else false if error ocurred
     */
    public boolean doStore( boolean createNew, boolean seenErrors) {
        
        _node.setBackupStarted(true);
      
        Date thisBackupDate = new Date();
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        File file = x.getFile(_node.getNodeNumber(),true);
        
        if (file == null) {
            log.error("Unable to get backup file prior to save");  // NOI18N
            return false;
        }
        
        doRotate();
        
        if ( createNew ) {
            _backupInfos.add(0,new CbusNodeFromBackup(_node,thisBackupDate));
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
        
        if (!_node.getUserName().isEmpty()) {
            root.addContent(new Element("UserName").addContent(_node.getUserName() )); // NOI18N
        }
        if (!_node.getNodeNameFromName().isEmpty()) {
            root.addContent(new Element("ModuleTypeName").addContent(_node.getNodeNameFromName() )); // NOI18N
        }
        if (!_node.getUserComment().isEmpty()) {
            root.addContent(new Element("FreeText").addContent( // NOI18N
                _node.getUserComment().replaceAll("\r\n|\n|\r", "\\\\n")));
        }
        
        Document doc = new Document(root);
        Element values;
        
        root.addContent(values = new Element("Backups"));  // NOI18N
        for (CbusNodeFromBackup node : _backupInfos ) {
            Element e = new Element("BackupInfo");  // NOI18N
            e.setAttribute("dateTimeStamp",xmlDateStyle.format( node.getBackupTimeStamp() ));  // NOI18N
            e.setAttribute("result","" + node.getBackupResult());  // NOI18N
            if (!node.getBackupComment().isEmpty()) {
                e.addContent(new Element("Comment").addContent("" + node.getBackupComment() ));  // NOI18N
            }
            if (!node.getParameterHexString().isEmpty()) {
                e.addContent(new Element("Parameters").addContent("" + node.getParameterHexString() ));  // NOI18N
            }
            if (!node.getNvHexString().isEmpty()) {
                e.addContent(new Element("NodeVariables").addContent("" + node.getNvHexString() ));  // NOI18N
            }
            
            if (node.getTotalNodeEvents()>0) {
                // log.info("events on backup node");
                Element bupev = new Element("NodeEvents"); // NOI18N
                node.getEventArray().forEach((bupndev) -> {
                    Element ndev = new Element("NodeEvent"); // NOI18N
                    ndev.setAttribute("NodeNum","" + bupndev.getNn());  // NOI18N
                    ndev.setAttribute("EventNum","" + bupndev.getEn());  // NOI18N
                    ndev.setAttribute("EvVars","" + bupndev.getHexEvVarString());  // NOI18N
                    bupev.addContent(ndev);
                });
                e.addContent(bupev);
            }
            values.addContent(e);
        }
        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: " + ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("IO Exception when writing: " + ex);  // NOI18N
            return false;
        }

        log.debug("...done");  // NOI18N
        _node.notifyNodeBackupTable();
        return true;
    }
    
    /**
     * Add an xml entry advising Node Not on Network
     */
    protected void nodeNotOnNetwork(){
        CbusNodeFromBackup newBup = new CbusNodeFromBackup(_node,new Date());
        newBup.setBackupResult(BackupType.NOTONNETWORK);
        _backupInfos.add(0,newBup);
        doStore(false, false);
    }
    
    /**
     * Add an xml entry advising Node in SLiM Mode
     */
    protected void nodeInSLiM(){
        CbusNodeFromBackup newBup = new CbusNodeFromBackup(_node,new Date());
        newBup.setBackupResult(BackupType.SLIM);
        _backupInfos.add(0,newBup);
        doStore(false, false);
    }

    /**
     * Remove Node XML File
     * @param rotate if true, creates and rotates .bup files before delete, false just deletes the core node file
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
        try {
            Element roots = x.rootFromFile(file);
            log.debug("File exists {}",roots);
            FileUtil.rotate(file, 5, "bup");  // NOI18N
        } catch (IOException ex) {
            // the file might not exist
            log.debug("Backup Rotate failed {}",file);  // NOI18N
        } catch (JDOMException ex) {
            // file might not exist
            log.debug("File invalid: {}", ex);  // NOI18N
        } catch (NullPointerException ex){
            // file might not exist
            log.debug("File invalid: {}", ex);  // NOI18N
        }
    }
    
    /**
     * Get the xml File Location
     * @return Location of the file, creating new if required
     */
    protected File getFileLocation() {
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        return x.getFile(_node.getNodeNumber(),true);
    }
    
    /**
     * Reset the backup array for testing
     */
    protected void resetBupArray() {
        _backupInfos = new ArrayList<CbusNodeFromBackup>();
    }

    public static class CbusNodeBackupFile extends XmlFile {
        
        public String getDefaultFileName(int nodeNum) {
            return getFileLocation() + getFileName(nodeNum);
        }

        public File getFile(int nodeNum, boolean store) {
            // Verify that cbus/node/ directory exists
            FileUtil.createDirectory(getFileLocation());
            
            File file = findFile(getDefaultFileName(nodeNum));
            if (file == null && store) {
                log.debug("create new file");  // NOI18N
                file = new File(getDefaultFileName(nodeNum));
            }
            return file;
        }

        public String getFileName(int nodeNum) {
            return nodeNum + ".xml";  // NOI18N
        }

        /**
         * Path to location of files.
         *
         * @return path to location
         */
        public static String getFileLocation() {
            return FileUtil.getUserFilesPath() 
            + "cbus" + File.separator + "nodes" + File.separator;  // NOI18N
        }
        
        public boolean deleteFile(int nodeNum) {
            return getFile(nodeNum,false).delete();
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeXml.class);
}
