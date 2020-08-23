package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import javax.annotation.CheckForNull;
import javax.swing.JTextField;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeFromBackup;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table model for Backup Files.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener  {

    private CbusNode _nodeOfInterest;

    public static final int DATE_COLUMN = 0;
    public static final int STATUS_COLUMN = 1;
    public static final int BYTES_COLUMN = 2;
    public static final int COMMENT_COLUMN = 3;
    public static final int DESCRIPTION_COLUMN = 4;

    /**
     * Create a new CbusNode Backup Table Model.
     * @param nodeOfInterest Node containing the backups.
     */
    public CbusNodeBackupTableModel(CbusNode nodeOfInterest) {
        super();
        _nodeOfInterest = nodeOfInterest;
    }
    
    public void setNode(CbusNode newNode){
        _nodeOfInterest = newNode;
        fireTableDataChanged();
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        if (ev.getPropertyName().equals("BACKUPS")) {
            jmri.util.ThreadingUtil.runOnGUIEventually( ()->{
                fireTableDataChanged();
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case DATE_COLUMN:
                return Date.class;
            case STATUS_COLUMN:
                return BackupType.class;
            case BYTES_COLUMN:
                return Integer.class;
            case COMMENT_COLUMN:
            case DESCRIPTION_COLUMN:
            default:
                return String.class;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return DESCRIPTION_COLUMN + 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        if ( _nodeOfInterest==null ){
            return 0;
        }
        return (_nodeOfInterest.getNodeBackupManager().getBackups().size());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(int r, int c) {
        return c == COMMENT_COLUMN;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case DATE_COLUMN:
                return ("Date / Time");
            case STATUS_COLUMN:
                return ("Backup Integrity");
            case BYTES_COLUMN:
                return Bundle.getMessage("TotalBytes");
            case COMMENT_COLUMN:
                return Bundle.getMessage("ColumnComment");
            default:
                return "";
        }
    }

    public static int getPreferredWidth(int col) {
        switch (col) {
            case DATE_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            case STATUS_COLUMN:
                return new JTextField(60).getPreferredSize().width;
            case BYTES_COLUMN:
                return new JTextField(8).getPreferredSize().width;
            case COMMENT_COLUMN:
                return new JTextField(80).getPreferredSize().width;
            case DESCRIPTION_COLUMN:
                return new JTextField(70).getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return new JTextField(8).getPreferredSize().width;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int r, int c) {
        if (r > _nodeOfInterest.getNodeBackupManager().getBackups().size()) {
            c = -1;
        }
        CbusNodeFromBackup lc = _nodeOfInterest.getNodeBackupManager().getBackups().get(r);
        switch (c) {
            case DATE_COLUMN:
                return ((lc.getBackupTimeStamp()));
            case STATUS_COLUMN:
                return (lc.getBackupResult());
            case BYTES_COLUMN:
                return lc.getNodeStats().totalNodeFileBytes();
            case COMMENT_COLUMN:
                return (lc.getBackupComment());
            case DESCRIPTION_COLUMN:
                return getDescription(r, lc);
            default:
                return null;
        }
    }
    
    /**
     * Get a description of the backup.
     * 1st backup on file, not on network, else comparison with previous backup.
     * @param r index of position in main array.
     * @param lc the single backup.
     * @return String with backup description.
     */
    private String getDescription(int r, CbusNodeFromBackup lc) {
        if ( r == _nodeOfInterest.getNodeBackupManager().getBackups().size()-1 ){
            return ("First Backup on File");
        }
        if (lc.getBackupResult() == BackupType.NOTONNETWORK) {
            return BackupType.NOTONNETWORK.toString();
        }
        return lc.compareWithString(getPreviousBackup(r+1));
    }
    
    /** 
     * Get the previous actual backup to this one in array order, else null
     */
    @CheckForNull
    private CbusNodeFromBackup getPreviousBackup(int arrayIndex){
        for (int i = arrayIndex; i < _nodeOfInterest.getNodeBackupManager().getBackups().size()-1; i++) {
            if (_nodeOfInterest.getNodeBackupManager().getBackups().get(i).getBackupResult() != BackupType.NOTONNETWORK) {
                return _nodeOfInterest.getNodeBackupManager().getBackups().get(i);
            }
        }
        return null;
    }

    /**
     * If Backup Comment changes, update backup and save XML
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == COMMENT_COLUMN) {
            _nodeOfInterest.getNodeBackupManager().getBackups().get(row).setBackupComment(String.valueOf(value));
            if(!_nodeOfInterest.getNodeBackupManager().doStore(false, _nodeOfInterest.getNodeStats().hasLoadErrors())){
                log.error("Unable to save Backup User Comment to File");
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeBackupTableModel.class);
    
}
