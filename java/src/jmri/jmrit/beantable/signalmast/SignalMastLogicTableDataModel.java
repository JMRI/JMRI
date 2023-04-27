package jmri.jmrit.beantable.signalmast;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableColumn;

import jmri.*;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.SignalMastLogicTableAction;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.XTableColumnModel;

/**
 * Create a SignalMastLogic Table Data Model.
 * Code originally from SignalMastLogicTableAction
 * @author Kevin Dickerson Copyright(C) 2011
 * @author Steve Young (C) 2023
 */
public class SignalMastLogicTableDataModel extends BeanTableDataModel<SignalMastLogic>{
    
    static public final int SOURCECOL = 0;
    static public final int SOURCEAPPCOL = 1;
    static public final int DESTCOL = 2;
    static public final int DESTAPPCOL = 3;
    static public final int COMCOL = 4;
    static public final int DELCOL = 5;
    static public final int ENABLECOL = 6;
    static public final int EDITLOGICCOL = 7;
    static public final int MAXSPEEDCOL = 8;
    static public final int COLUMNCOUNT = 9;

    
    private boolean suppressUpdate = false; // does not update table model changelistener during auto create pairs

    public SignalMastLogicTableDataModel(){
        super();
        SignalMastLogicTableDataModel.this.updateNameList();
    }

    //We have to set a manager first off, but this gets replaced.
    @Override
    protected SignalMastLogicManager getManager() {
        return InstanceManager.getDefault(SignalMastLogicManager.class);
    }

    @Override
    public String getValue(String s) {
        return "Set";
    }

    @Override
    protected String getMasterClassName() {
        return SignalMastLogicTableAction.class.getName();
    }

    @Override
    public void clickOn(SignalMastLogic t) {
    }

    private ArrayList<Hashtable<SignalMastLogic, SignalMast>> signalMastLogicList = null;
    
    @Nonnull
    private List<Hashtable<SignalMastLogic, SignalMast>> getSMLList(){
        if ( signalMastLogicList == null) {
            signalMastLogicList = new ArrayList<>();
        }
        return signalMastLogicList;
    }
    
    @Override
    protected synchronized void updateNameList() {
        // first, remove listeners from the individual objects

        for (int i = 0; i < getSMLList().size(); i++) {
            // if object has been deleted, it's not here; ignore it
            Hashtable<SignalMastLogic, SignalMast> b = getSMLList().get(i);
            Enumeration<SignalMastLogic> en = b.keys();
            while (en.hasMoreElements()) {
                SignalMastLogic sm = en.nextElement();
                SignalMast dest = b.get(sm);
                sm.removePropertyChangeListener(this);
                sm.getSourceMast().removePropertyChangeListener(this);
                dest.removePropertyChangeListener(this);
            }
        }
        List<SignalMastLogic> source = getManager().getSignalMastLogicList();
        signalMastLogicList.clear();
        for (int i = 0; i < source.size(); i++) {
            List<SignalMast> destList = source.get(i).getDestinationList();
            source.get(i).addPropertyChangeListener(this);
            source.get(i).getSourceMast().addPropertyChangeListener(this);
            for (int j = 0; j < destList.size(); j++) {
                Hashtable<SignalMastLogic, SignalMast> hash = new Hashtable<>(1);
                hash.put(source.get(i), destList.get(j));
                destList.get(j).addPropertyChangeListener(this);
                getSMLList().add(hash);
            }
        }
    }

    public void setSuppressUpdate(boolean newVal) {
        suppressUpdate = newVal;
    }

    //Will need to redo this so that we work out the row number from looking in the signalmastlogiclist.
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (suppressUpdate) {
            return;
        }
        // updateNameList();
        if (e.getPropertyName().equals("length") || e.getPropertyName().equals("updatedDestination") || e.getPropertyName().equals("updatedSource")) {
            updateNameList();
            log.debug("Table changed length to {}", getSMLList().size());
            fireTableDataChanged();
        } else if (e.getSource() instanceof SignalMastLogic) {
            SignalMastLogic logic = (SignalMastLogic) e.getSource();
            if (matchPropertyName(e)) {
                for (int i = 0; i < getSMLList().size(); i++) {
                    Hashtable<SignalMastLogic, SignalMast> b = getSMLList().get(i);
                    Enumeration<SignalMastLogic> en = b.keys();
                    while (en.hasMoreElements()) {
                        SignalMastLogic sm = en.nextElement();
                        if (sm == logic) {
                            fireTableRowsUpdated(i, i);
                        }
                    }
                }
            }
        } else if (e.getSource() instanceof SignalMast) {
            SignalMast sigMast = (SignalMast) e.getSource();
            for (int i = 0; i < getSMLList().size(); i++) {
                Hashtable<SignalMastLogic, SignalMast> b = getSMLList().get(i);
                Enumeration<SignalMastLogic> en = b.keys();
                while (en.hasMoreElements()) {
                    SignalMastLogic sm = en.nextElement();
                    //SignalMast dest = b.get(sm);
                    if (sm.getSourceMast() == sigMast) {
                        fireTableRowsUpdated(i, i);
                    }
                }
            }
        }
    }

    /**
     * Is this property event announcing a change this table should
     * display?
     * <p>
     * Note that events will come both from the NamedBeans and also from
     * the manager
     */
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return ((e.getPropertyName().contains("Comment")) || (e.getPropertyName().contains("Enable")));
    }

    @Override
    public int getColumnCount() {
        return COLUMNCOUNT;
    }

    @Override
    public int getRowCount() {
        return getSMLList().size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= getSMLList().size()) {
            log.debug("row index is greater than signalMastLogicList size");
            return null;
        }
        SignalMastLogic b = getLogicFromRow(row);
        if (b==null){
            return null;
        }
        SignalMast destMast;
        switch (col) {
            case SOURCECOL:
                return b.getSourceMast().getDisplayName();
            case DESTCOL:  // return user name
                // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                destMast = getDestMastFromRow(row);
                return ( destMast != null ? destMast.getDisplayName() : null);
            case SOURCEAPPCOL:  //
                return b.getSourceMast().getAspect();
            case DESTAPPCOL:  //
                destMast = getDestMastFromRow(row);
                return ( destMast != null ? destMast.getAspect() : null);
            case COMCOL:
                return b.getComment(getDestMastFromRow(row));
            case DELCOL:
                return Bundle.getMessage("ButtonDelete");
            case EDITLOGICCOL:
                return Bundle.getMessage("ButtonEdit");
            case ENABLECOL:
                return b.isEnabled(getDestMastFromRow(row));
            case MAXSPEEDCOL:
                return b.getMaximumSpeed(getDestMastFromRow(row));
            default:
                return super.getValueAt(row, col);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        SignalMastLogic rowLogic = getLogicFromRow(row);
        if ( rowLogic == null ){
            return;
        }
        switch (col) {
            case COMCOL:
                rowLogic.setComment((String) value, getDestMastFromRow(row));
                break;
            case EDITLOGICCOL:
                SwingUtilities.invokeLater(() -> {
                    editLogic(row);
                });
                break;
            case DELCOL:
                // button fired, delete Bean
                deleteLogic(row);
                break;
            case ENABLECOL:
                SignalMast destMast = getDestMastFromRow(row);
                if (destMast==null){
                    break;
                }
                if ((Boolean) value) {
                    rowLogic.setEnabled(destMast);
                } else {
                    rowLogic.setDisabled(destMast);
                }
                break;
            default:
                super.setValueAt(value, row, col);
        }
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case SOURCECOL:
                return Bundle.getMessage("Source");
            case DESTCOL:
                return Bundle.getMessage("Destination");
            case SOURCEAPPCOL:
                return Bundle.getMessage("LabelAspectType");
            case DESTAPPCOL:
                return Bundle.getMessage("LabelAspectType");
            case COMCOL:
                return Bundle.getMessage("Comment");
            case DELCOL:
                return ""; // override default, no title for Delete column
            case EDITLOGICCOL:
                return ""; // override default, no title for Edit column
            case ENABLECOL:
                return Bundle.getMessage("ColumnHeadEnabled");
            case MAXSPEEDCOL:
                return Bundle.getMessage("LabelMaxSpeed");
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SOURCECOL:
            case DESTCOL:
            case SOURCEAPPCOL:
            case COMCOL:
            case DESTAPPCOL:
                return String.class;
            case ENABLECOL:
                return Boolean.class;
            case EDITLOGICCOL:
            case DELCOL:
                return JButton.class;
            case MAXSPEEDCOL:
                return Float.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case COMCOL:
            case EDITLOGICCOL:
            case DELCOL:
            case ENABLECOL:
                return true;
            default:
                return false;
        }
    }

    final jmri.jmrit.signalling.SignallingAction sigLog = new jmri.jmrit.signalling.SignallingAction();
    
    void editLogic(int row) {
        SignalMastLogic sml = getLogicFromRow(row);
        if ( sml != null ) {
            
            sigLog.setMast(sml.getSourceMast(), getDestMastFromRow(row));
            sigLog.actionPerformed(null);
        }
    }

    void deleteLogic(int row) {
        //This needs to be looked at
        SignalMastLogic sml = getLogicFromRow(row);
        SignalMast destMast = getDestMastFromRow(row);
        if ( sml != null && destMast !=null ) {
            InstanceManager.getDefault(SignalMastLogicManager.class).removeSignalMastLogic(sml, destMast);
        }
    }

    @CheckForNull
    public SignalMast getDestMastFromRow(int row) {
        // if object has been deleted, it's not here; ignore it
        Hashtable<SignalMastLogic, SignalMast> b = getSMLList().get(row);
        Enumeration<SignalMastLogic> en = b.keys();
        while (en.hasMoreElements()) {
            return b.get(en.nextElement());
        }
        return null;
    }

    @CheckForNull
    public SignalMastLogic getLogicFromRow(int row) {
        Hashtable<SignalMastLogic, SignalMast> b = getSMLList().get(row);
        Enumeration<SignalMastLogic> en = b.keys();
        while (en.hasMoreElements()) {
            return en.nextElement();
        }
        return null;
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case SOURCECOL:
            case DESTCOL:
            case DESTAPPCOL:
            case SOURCEAPPCOL:
            case MAXSPEEDCOL:
                return new JTextField(10).getPreferredSize().width;
            case COMCOL:
                return 75;
            case EDITLOGICCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                return new JTextField(6).getPreferredSize().width;
            case DELCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
            case ENABLECOL:
                return new JTextField(5).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    @Override
    public void configureTable(JTable table) {
        setColumnToHoldButton(table, EDITLOGICCOL,
                new JButton(Bundle.getMessage("ButtonEdit")));
        table.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);

        // configValueColumn(table);
        configDeleteColumn(table);
    }

    @Override
    public SignalMastLogic getBySystemName(@Nonnull String name) {
        return null;
    }

    @Override
    public SignalMastLogic getByUserName(@Nonnull String name) {
        return null;
    }

    @Override
    synchronized public void dispose() {

        getManager().removePropertyChangeListener(this);
        
            for (int i = 0; i < getSMLList().size(); i++) {
                SignalMastLogic b = getLogicFromRow(i);
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }

        super.dispose();
    }

    @Override
    protected void configDeleteColumn(JTable table) {
        // have the delete column hold a button
        setColumnToHoldButton(table, DELCOL,
                new JButton(Bundle.getMessage("ButtonDelete")));
    }

    @Override
    protected String getBeanType() {
        return "Signal Mast Logic";
    }

    @Override
    protected void showPopup(JmriMouseEvent e) {
    }

    @Override
    protected void setColumnIdentities(JTable table) {
        super.setColumnIdentities(table);
        Enumeration<TableColumn> columns;
        if (table.getColumnModel() instanceof XTableColumnModel) {
            columns = ((XTableColumnModel) table.getColumnModel()).getColumns(false);
        } else {
            columns = table.getColumnModel().getColumns();
        }
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            switch (column.getModelIndex()) {
                case SOURCEAPPCOL:
                    column.setIdentifier("SrcAspect");
                    break;
                case DESTAPPCOL:
                    column.setIdentifier("DstAspect");
                    break;
                case DELCOL:
                    column.setIdentifier("Delete");
                    break;
                case EDITLOGICCOL:
                    column.setIdentifier("Edit");
                    break;
                default:
                // use existing value
            }
        }
    }

    @Override
    public String getCellToolTip(JTable table, int row, int col) {

        SignalMastLogic sml = getLogicFromRow(row);
        if ( sml == null ) {
            return null;
        }

        String tip = null;
        SignalMast dest;
        switch (col) {
            case SOURCECOL:
                tip = formatToolTip(sml.getSourceMast().getComment());
                break;
            case DESTCOL:
                dest = this.getDestMastFromRow(row);
                if ( dest != null) {
                    tip = formatToolTip(dest.getComment());
                }
                break;
            case COMCOL:
                dest = this.getDestMastFromRow(row);
                if ( dest != null) {
                    tip = formatToolTip(sml.getComment(dest));
                }
                break;
            default:
                break;
        }
        return tip;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastLogicTableDataModel.class);
}
