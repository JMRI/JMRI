package jmri.jmrix.openlcb.swing.lccpro;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public static final int BACKUPCOL = 7;
    public static final int DESCRIPTIONCOL = 8;
    public static final int NUMCOL = DESCRIPTIONCOL + 1;

    CanSystemConnectionMemo memo;
    MimicNodeStore nodestore;
    private final Set<MimicNodeStore.NodeMemo> listenedNodeMemos = new HashSet<>();
    private final AtomicBoolean tableUpdatePending = new AtomicBoolean(false);
    
    public LccProTableModel(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.nodestore = memo.get(MimicNodeStore.class);
        log.trace("Found nodestore {}", nodestore);
        nodestore.addPropertyChangeListener(this);
        nodestore.refresh();
    }


    @Override
    public void propertyChange(PropertyChangeEvent e) {
        log.trace("received {}", e);
        if (e.getSource() == nodestore) {
            if (MimicNodeStore.CLEAR_ALL_NODES.equals(e.getPropertyName())) {
                clearNodeMemoListeners();
                requestTableUpdate();
            } else if (MimicNodeStore.ADD_PROP_NODE.equals(e.getPropertyName())
                    && e.getNewValue() instanceof MimicNodeStore.NodeMemo) {
                MimicNodeStore.NodeMemo nodeMemo = (MimicNodeStore.NodeMemo) e.getNewValue();
                synchronized (listenedNodeMemos) {
                    if (listenedNodeMemos.add(nodeMemo)) {
                        nodeMemo.addPropertyChangeListener(this);
                    }
                }
                requestTableUpdate();
            }
            return;
        }

        if (e.getSource() instanceof MimicNodeStore.NodeMemo) {
            // The node collection can change while the event is queued on the EDT,
            // so a row-specific update may no longer refer to a valid sorter row.
            requestTableUpdate();
        }
    }

    /**
     * Ask for the whole table to be redrawn on the GUI (Swing) thread.
     * <p>
     * The events come from the thread handling OpenLCB messages, which must
     * not wait for Swing, so the update is queued rather than run and waited
     * for. While one update is queued, further requests are covered by it: a
     * burst of node replies, as at start-up, gives one redraw, not one each.
     */
    private void requestTableUpdate() {
        if (tableUpdatePending.compareAndSet(false, true)) {
            jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
                // cleared before the redraw, so a change arriving during it
                // queues another update rather than being lost
                tableUpdatePending.set(false);
                fireTableDataChanged();
            });
        }
    }

    private void clearNodeMemoListeners() {
        synchronized (listenedNodeMemos) {
            for (MimicNodeStore.NodeMemo nodeMemo : listenedNodeMemos) {
                nodeMemo.removePropertyChangeListener(this);
            }
            listenedNodeMemos.clear();
        }
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
            case BACKUPCOL:
                return Bundle.getMessage("FieldBackup");
            case DESCRIPTIONCOL:
                return Bundle.getMessage("FieldDescription");
            default:
                return "<unexpected column number>";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case CONFIGURECOL:
            case UPGRADECOL:
            case BACKUPCOL:
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
     * @return true if cell is editable 
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case CONFIGURECOL:
            case UPGRADECOL:
            case BACKUPCOL:
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
        
        var memoArray = nodestore.getNodeMemos().toArray(new MimicNodeStore.NodeMemo[0]);
        if (row >= memoArray.length) return "";  // sometimes happens at startup
        var nodememo = memoArray[row];
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
                if (pip.hasProtocol(ProtocolIdentification.Protocol.FirmwareUpgrade)) {
                    return Bundle.getMessage("FieldUpgrade");
                } else {
                    return null;
                }
            case BACKUPCOL:
                if (pip.hasProtocol(ProtocolIdentification.Protocol.ConfigurationDescription)) {
                    return Bundle.getMessage("FieldBackup");
                } else {
                    return null;
                }
            case DESCRIPTIONCOL:
                return snip.getUserDesc();
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
                    actions.openCdiWindow(node, description);
                    // We want the table to retain focus while the CDI loads
                    // This also removes the selection from this cell, so that cmd-`
                    // no longer repeats the action of pressing the button
                    forceFocus();
                }
                break;
            case UPGRADECOL:
                if (pip.hasProtocol(ProtocolIdentification.Protocol.FirmwareUpgrade)) {
                    var node = nodememo.getNodeID();
                    var action = new jmri.jmrix.openlcb.swing.downloader.LoaderPane.Default(node);
                    action.actionPerformed(null);
                }
                break;
            case BACKUPCOL:
                if (pip.hasProtocol(ProtocolIdentification.Protocol.ConfigurationDescription)) {
                    var snip = nodememo.getSimpleNodeIdent();
                    String name = (snip != null) ? snip.getUserName() : "<unknown>";
                    var backuper = new jmri.jmrix.openlcb.swing.lccpro.NodeBackupAction();
                    backuper.doBackup(nodememo, memo, name);
                    // We want the table to retain focus while the CDI loads
                    // This also removes the selection from this cell, so that cmd-`
                    // no longer repeats the action of pressing the button
                    forceFocus();
                }
                break;
            default:
                break;
        }
    }
    
    // to be overridden at construction time with e.g.
    //      frame.toFront();
    //      frame.requestFocus();
    //
    public void forceFocus() {
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
        nodestore.removePropertyChangeListener(this);
        clearNodeMemoListeners();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LccProTableModel.class);
}
