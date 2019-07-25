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
 * Loosely based on
 * Load and store the timetable data file: TimeTableData.xml
 * @author Dave Sand Copyright (C) 2018
 */
public class CbusNodeXml {
    
    public SimpleDateFormat xmlDateStyle = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private int _nodeNum = 0;
    private CbusNode _node;
    private ArrayList<CbusNodeFromBackup> _backupInfos;
    private CbusPreferences preferences;
    
    public CbusNodeXml(CbusNode node) {
        _nodeNum = node.getNodeNumber();
        _node = node;
        _backupInfos = new ArrayList<CbusNodeFromBackup>();
        preferences = jmri.InstanceManager.getNullableDefault(CbusPreferences.class);
        doBasicLoad();
        
    }
    
    public ArrayList<CbusNodeFromBackup> getBackups() {
        return _backupInfos;
    }
    
    // number of backups in arraylist that do no have a comment
    // 
    private int numAutoBackups(){
        int i=0;
        for (int j = _backupInfos.size()-1; j >0 ; j--) {
            if (_backupInfos.get(i).getBackupComment().isEmpty()
                && _backupInfos.get(i).getBackupResult() == BackupType.COMPLETE ){
                i++;
            }
        }
        return i;
    }
    
    private void trimBackups(){
        
        if (preferences==null) {
            return;
        }
        
        // note size-2 means we never delete the oldest one
        for (int i = _backupInfos.size()-2; i >0 ; i--) {
            if ( numAutoBackups()<preferences.getMinimumNumBackupsToKeep()){
                return;
            }
            if ( _backupInfos.get(i).getBackupComment().isEmpty()){
                _backupInfos.remove(i);
            }
        }
        return;
    }
    
    private void doBasicLoad(){
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        File file = x.getFile(true);
        
        // Find root
        Element root;
        
        if (file == null) {
            log.error("84: Unable to load backup file");  // NOI18N
            return;
        }
        
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
                
                    CbusNodeFromBackup newinfo = new CbusNodeFromBackup(null,_nodeNum);
                    
                    if ( info.getAttributeValue("dateTimeStamp") !=null ) { // NOI18N
                        try {
                            Date newDate = xmlDateStyle.parse(info.getAttributeValue("dateTimeStamp")); // NOI18N
                            newinfo.setBackupTimeStamp( newDate ); // temp
                        } catch (java.text.ParseException e) { 
                            log.warn("Unable to parse date {}",info.getAttributeValue("dateTimeStamp")); // NOI18N
                        }
                    } else {
                        log.error("NO datetimestamp in a backup log entry");
                    }
                    if ( info.getAttributeValue("result") !=null &&  // NOI18N
                        CbusNodeConstants.lookupByName(info.getAttributeValue("result"))!=null ) { // NOI18N
                        newinfo.setBackupResult(CbusNodeConstants.lookupByName(info.getAttributeValue("result")) );
                    } else {
                        log.warn("NO result in a backup log entry");
                        newinfo.setBackupResult(BackupType.INCOMPLETE);
                    }
                    Element params = info.getChild("Parameters");  // NOI18N
                    if ( params !=null ) {
                        newinfo.setParameters(StringUtil.intBytesWithTotalFromNonSpacedHexString(params.getValue(),true));
                    }
                    Element nvs = info.getChild("NodeVariables");  // NOI18N
                    if ( nvs !=null ) {
                        newinfo.setNVs(StringUtil.intBytesWithTotalFromNonSpacedHexString(nvs.getValue(),true));
                    }
                    Element comment = info.getChild("Comment");  // NOI18N
                    if ( comment !=null ) {
                        newinfo.setBackupComment(comment.getValue());
                    }
                    Element events = info.getChild("NodeEvents");  // NOI18N
                    if ( events !=null ) {
                        for (Element xmlEvent : events.getChildren("NodeEvent")) {  // NOI18N
                            if ( xmlEvent.getAttributeValue("NodeNum") !=null &&  // NOI18N
                                xmlEvent.getAttributeValue("EventNum") !=null &&  // NOI18N
                                xmlEvent.getAttributeValue("EvVars") !=null ) {  // NOI18N
                                
                                // check event variable length matches expected length in parameters
                                if (newinfo.getParameter(5)!=(xmlEvent.getAttributeValue("EvVars").length()/2) &&
                                    ( newinfo.getBackupResult()!=BackupType.COMPLETEDWITHERROR &&
                                        newinfo.getBackupResult()!=BackupType.INCOMPLETE  )) {
                                        newinfo.setBackupResult(BackupType.COMPLETEDWITHERROR);
                                    }
                                
                                newinfo.addBupEvent(
                                    Integer.parseInt(xmlEvent.getAttributeValue("NodeNum")),
                                    Integer.parseInt(xmlEvent.getAttributeValue("EventNum")),
                                    xmlEvent.getAttributeValue("EvVars"));
                            }
                        }
                    }
                    _backupInfos.add(newinfo);
                }
            }
            
        } catch (JDOMException ex) {
            log.error("File invalid: " + ex);  // NOI18N
            return;
        } catch (IOException ex) {
            // file might not yet exist as 1st time Node on Network
            log.debug("Possible Error reading file: " + ex);  // NOI18N
            return;
        }
        // make sure ArrayList ismost recent at start, oldest at end
        java.util.Collections.sort(_backupInfos, java.util.Collections.reverseOrder());
        _node.notifyNodeBackupTable();
        
    }
    
    public boolean doStore( boolean createNew) {
        
        _node.setBackupStarted();
      
        Date thisBackupDate = new Date();
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        File file = x.getFile(true);
        
        if (file == null) {
            log.error("202: Unable to load backup file");  // NOI18N
            return false;
        }
        
        try {
            Element roots = x.rootFromFile(file);
            log.debug("File exists {}",roots);
            FileUtil.rotate(file, 4, "bup");  // NOI18N
        } catch (IOException ex) {
            // the file might not exist
            log.debug("Backup Rotate failed {}",file);  // NOI18N
        } catch (JDOMException ex) {
            // file might not exist
            log.debug("File invalid: " + ex);  // NOI18N
        }
        
        if ( createNew ) {

            _backupInfos.add(0,new CbusNodeFromBackup(_node,thisBackupDate));
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
        for (CbusNodeFromBackup info : _backupInfos ) {
            Element e = new Element("BackupInfo");  // NOI18N
            e.setAttribute("dateTimeStamp",xmlDateStyle.format( info.getBackupTimeStamp() ));  // NOI18N
            e.setAttribute("result","" + info.getBackupResult());  // NOI18N
            if (!info.getBackupComment().isEmpty()) {
                e.addContent(new Element("Comment").addContent("" + info.getBackupComment() ));  // NOI18N
            }
            if (!info.getParameterHexString().isEmpty()) {
                e.addContent(new Element("Parameters").addContent("" + info.getParameterHexString() ));  // NOI18N
            }
            if (!info.getNvHexString().isEmpty()) {
                e.addContent(new Element("NodeVariables").addContent("" + info.getNvHexString() ));  // NOI18N
            }
            
            if (info.getTotalNodeEvents()>0) {
                // log.info("events on backup node");
                Element bupev = new Element("NodeEvents"); // NOI18N
                info.getEventArray().forEach((bupndev) -> {
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
    
    protected void nodeNotOnNetwork(){
        
        CbusNodeFromBackup newBup = new CbusNodeFromBackup(_node,new Date());
        newBup.setBackupResult(BackupType.NOTONNETWORK);
        _backupInfos.add(0,newBup);
        doStore(false);
        
    }

    // takes a backup then
    // removes main node xml file
    protected boolean removeNode( boolean rotate){
        CbusNodeBackupFile x = new CbusNodeBackupFile();
        File file = x.getFile(false);
        
        if (rotate) {
            try {
                Element roots = x.rootFromFile(file);
                log.debug("File exists {}",roots);
                FileUtil.rotate(file, 5, "bup");  // NOI18N
            } catch (IOException ex) {
                // the file might not exist
                log.debug("Backup Rotate failed {}",file);  // NOI18N
            } catch (JDOMException ex) {
                // file might not exist
                log.debug("File invalid: " + ex);  // NOI18N
            }
        }
        
        if ( !x.deleteFile()){
            log.error("Unable to delete node xml file");
            return false;
        }
        return true;
    }

    public class CbusNodeBackupFile extends XmlFile {
        private String fileLocation = FileUtil.getUserFilesPath() + "cbus/nodes/";  // NOI18N
        private String baseFileName = ""+_node.getNodeNumber()+".xml";  // NOI18N

        public String getDefaultFileName() {
            return getFileLocation() + getFileName();
        }

        public File getFile(boolean store) {
            // Verify that cbus:node directory exists
            File chkdir = new File(getFileLocation());
            if (!chkdir.exists()) {
                if (!chkdir.mkdir()) {
                    log.error("Create directory:cbus/node/ failed");  // NOI18N
                    return null;
                }
            }
            
            File file = findFile(getDefaultFileName());
            if (file == null && store) {
                log.debug("create new file");  // NOI18N
                file = new File(getDefaultFileName());
            }
            return file;
        }

        public String getFileName() {
            if(baseFileName == null) {
               baseFileName = "" + _nodeNum + ".xml";  // NOI18N
            }
            return baseFileName;
        }

        /**
         * Absolute path to location of TimeTable files.
         *
         * @return path to location
         */
        public String getFileLocation() {
            if(fileLocation==null){
               fileLocation = FileUtil.getUserFilesPath() + "cbus/nodes/";  // NOI18N
            }
            return fileLocation;
        }
        
        public boolean deleteFile() {
            return getFile(false).delete();
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeXml.class);
}
