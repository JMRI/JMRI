package jmri.jmrix.openlcb.swing.lccpro;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.*;

/**
 * Table data model for display of LCC node values.
 * <p>
 * Any desired ordering, etc, is handled outside this class.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2024
 * @since 5.11.1
 */
public class LccProTableModel extends DefaultTableModel implements PropertyChangeListener {

    static final int NAMECOL = 0;
    public static final int IDCOL = 1;
    static final int MFGCOL = 2;
    static final int MODELCOL = 3;
    static final int SVERSIONCOL = 4;
    public static final int CONFIGURECOL = 5;
    public static final int UPGRADECOL = 6;
    public static final int NUMCOL = UPGRADECOL + 1;

    CanSystemConnectionMemo memo;
    MimicNodeStore nodestore;
    
    public LccProTableModel(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.nodestore = memo.get(MimicNodeStore.class);
        log.trace("Found nodestore {}", nodestore);
        nodestore.addPropertyChangeListener(this);
        nodestore.refresh();
    }


    @Override
    public void propertyChange(PropertyChangeEvent e) {
        // set this up to fireTableDataChanged() when a new node appears
        log.trace("received {}", e);
        // SNIP might take a bit, so fire slightly later
        jmri.util.ThreadingUtil.runOnGUIDelayed( ()->{ 
            fireTableDataChanged(); 
        }, 250);
        jmri.util.ThreadingUtil.runOnGUIDelayed( ()->{ 
            fireTableDataChanged(); 
        }, 1000);
    }

    @Override
    public int getRowCount() {
        if (nodestore == null) {
            log.trace("Did not expect null nodestore, except in initialization before ctor runs");
            return 1;
        }
        if (nodestore.getNodeMemos() == null) {
            log.debug("Did not expect missing node memos");
            return 0;
        }
        return nodestore.getNodeMemos().size();
    }

    @Override
    public int getColumnCount() {
        return NUMCOL;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAMECOL:
                return Bundle.getMessage("FieldName");
            case IDCOL:
                return Bundle.getMessage("FieldID");
            case MFGCOL:
                return Bundle.getMessage("FieldMfg");
            case MODELCOL:
                return Bundle.getMessage("FieldModel");
            case SVERSIONCOL:
                return Bundle.getMessage("FieldSVersion");
            case CONFIGURECOL:
                return Bundle.getMessage("FieldConfig");
            case UPGRADECOL:
                return Bundle.getMessage("FieldUpgrade");
            default:
                return "<unexpected column number>";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case CONFIGURECOL:
            case UPGRADECOL:
                return JButton.class;
            default:
                return String.class;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the table can be set to be non-editable when constructed, in
     * which case this always returns false.
     *
     * @return true if cell is editable in roster entry model and table allows
     *         editing
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case CONFIGURECOL:
            case UPGRADECOL:
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Provides an empty string for a column if the model returns null for that
     * value.
     */
    @Override
    public Object getValueAt(int row, int col) {
        log.trace("getValue({}, {})", row, col);
        MimicNodeStore.NodeMemo nodememo = nodestore.getNodeMemos().toArray(new MimicNodeStore.NodeMemo[0])[row];
        if (nodememo == null) return "<invalid node memo>";
        var snip = nodememo.getSimpleNodeIdent();
        if (snip == null) return "<snip info not yet availble>";
        var pip = nodememo.getProtocolIdentification();

        switch (col) {
            case NAMECOL:
                return snip.getUserName();
            case IDCOL:
                return nodememo.getNodeID().toString();
            case MFGCOL:
                return snip.getMfgName();
            case MODELCOL:
                return snip.getModelName();
            case SVERSIONCOL:
                return snip.getSoftwareVersion();
            case CONFIGURECOL:
                if (pip.hasProtocol(ProtocolIdentification.Protocol.ConfigurationDescription)) {
                    return Bundle.getMessage("FieldConfig");
                } else {
                    return null;
                }
            case UPGRADECOL:
                return Bundle.getMessage("FieldUpgrade");
            default:
                return "<unexpected column number>";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        log.trace("getValue({}, {})", row, col);
        MimicNodeStore.NodeMemo nodememo = nodestore.getNodeMemos().toArray(new MimicNodeStore.NodeMemo[0])[row];
        if (nodememo == null) {
            log.error("Button pushed but no corresponding node for row {}", row);
            return;
        }
        var pip = nodememo.getProtocolIdentification();

        switch (col) {
            case CONFIGURECOL:
                if (pip.hasProtocol(ProtocolIdentification.Protocol.ConfigurationDescription)) {
                    var actions = new jmri.jmrix.openlcb.swing.ClientActions(memo.get(org.openlcb.OlcbInterface.class), memo);
                    var node = nodememo.getNodeID();
                    var description = jmri.jmrix.openlcb.swing.networktree.NetworkTreePane.augmentedNodeName(nodememo);
                    actions.openCdiWindow(node, description.toString());
                }
                break;
            case UPGRADECOL:
                log.info("Upgrade button pressed for {}, not functioning yet", nodememo.getNodeID().toString());
                break;
            default:
                // TODO - fire the buttons
                break;
        }
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new JLabel(getColumnName(column))
            .getPreferredSize().width + 15);  // leave room for sorter arrow
        for (int row = 0; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class)) {
                retval = Math.max(retval, new JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            } else if (getColumnClass(column).equals(Integer.class)) {
                retval = Math.max(retval, new JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            } else if (getColumnClass(column).equals(ImageIcon.class)) {
                retval = Math.max(retval, new JLabel((Icon) getValueAt(row, column)).getPreferredSize().width);
            }
        }
        return retval + 5;
    }


    // drop listeners
    public void dispose() {
        // TODO - drop the listener for node changes
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LccProTableModel.class);

}
