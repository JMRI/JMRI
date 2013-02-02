package jmri.jmrix.ecos.swing.locodatabase;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import jmri.NamedBean;
import jmri.Manager;
import java.awt.Component;
import java.util.List;
import java.util.Hashtable;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;

import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;

public class EcosLocoTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
   public EcosLocoTableAction(String s) {
        super(s);
    }
    public EcosLocoTableAction() { this("Ecos Loco Table");}
    
    public EcosLocoTableAction(String s, EcosSystemConnectionMemo memo) { 
        this(s);
        setAdapterMemo(memo);
        includeAddButton = false;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableSorter sorter = new TableSorter(m);
    	JTable dataTable = m.makeJTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());
        // create the frame
        f = new jmri.jmrit.beantable.BeanTableFrame(m, helpTarget(), dataTable){

        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    protected EcosSystemConnectionMemo adaptermemo;
    protected EcosLocoAddressManager locoManager;
    public void setManager(Manager man) { 
        locoManager = (EcosLocoAddressManager) man;
    }
    protected String rosterAttribute;
    public void setAdapterMemo(EcosSystemConnectionMemo memo) {
        adaptermemo = memo;
        locoManager = adaptermemo.getLocoAddressManager();
        rosterAttribute = adaptermemo.getPreferenceManager().getRosterAttribute();
    }
    
    protected EcosLocoAddress getByEcosObject(String object) {return locoManager.getByEcosObject(object);}
    
    List<String> ecosObjectIdList = null;
    JTable table;
    
    static public final int ADDTOROSTERCOL = 5;
    static public final int SPEEDDIR = 6;
    static public final int STOP = 7;
    
    protected void createModel() {
        m = new BeanTableDataModel() {
        
            //We have to set a manager first off, but this gets replaced.
            protected EcosLocoAddressManager getManager() { return locoManager;}
            protected String getRosterAttribute() { return rosterAttribute; }
            
            public String getValue(String s) {
                return "Set";
            }
            protected String getMasterClassName() { return getClassName(); }
            public void clickOn(jmri.NamedBean t) { }
            
            @Override
            protected synchronized void updateNameList() {
                // first, remove listeners from the individual objects
                if (ecosObjectIdList != null) {
                    for (int i = 0; i< ecosObjectIdList.size(); i++) {
                        // if object has been deleted, it's not here; ignore it
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                        if (b!=null) b.removePropertyChangeListener(this);
                    }
                }
                ecosObjectIdList = getManager().getEcosObjectList();
                // and add them back in
                for (int i = 0; i< ecosObjectIdList.size(); i++)
                    getByEcosObject(ecosObjectIdList.get(i)).addPropertyChangeListener(this);
            }
            
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateNameList();
                if (e.getPropertyName().equals("length")) {
                    // a new jmri.jmrix.ecos.EcosLocoAddressManager is available in the manager
                    updateNameList();
                    fireTableDataChanged();
                } else if (matchPropertyName(e)) {
                    // a value changed.  Find it, to avoid complete redraw
                    String object = ((jmri.jmrix.ecos.EcosLocoAddress)e.getSource()).getEcosObject();
                    // since we can add columns, the entire row is marked as updated
                    int row = ecosObjectIdList.indexOf(object);
                    fireTableRowsUpdated(row, row);
                }
            }
            @Override
            public int getColumnCount(){ 
                return STOP+1;
            }
            
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col==COMMENTCOL) {
                    RosterEntry re = null;
                    String ecosObjectNo = ecosObjectIdList.get(row);
                    if (value==null){
                        return;
                    } else {
                        if(value instanceof RosterEntry){
                            re = (RosterEntry)value;
                            if((re.getAttribute(getRosterAttribute())!=null && !re.getAttribute(getRosterAttribute()).equals(""))){
                                JOptionPane.showMessageDialog(f, ecosObjectNo +" This roster entry already has an ECOS loco assigned to it ");
                                log.error(ecosObjectNo +" This roster entry already has an ECOS loco assigned to it ");
                                return;
                            }
                            String oldRoster = getByEcosObject(ecosObjectNo).getRosterId();
                            RosterEntry oldre =null;
                            if(oldRoster!=null){
                                oldre = Roster.instance().getEntryForId(oldRoster);
                                if(oldre!=null)
                                    oldre.deleteAttribute(getRosterAttribute());
                            }
                            re.putAttribute(getRosterAttribute(), ecosObjectNo);
                            getByEcosObject(ecosObjectNo).setRosterId(re.getId());
                            re.updateFile();
                        } else if (value instanceof String){
                            List<RosterEntry> r = Roster.instance().getEntriesWithAttributeKeyValue(getRosterAttribute(), ecosObjectNo);
                            if(r.size()!=0){
                                r.get(0).deleteAttribute(getRosterAttribute());
                                getByEcosObject(ecosObjectNo).setRosterId(null);
                                r.get(0).updateFile();
                            }
                        
                        }
                    }
                    Roster.writeRosterFile();
                }  else if (col==ADDTOROSTERCOL) {
                    addToRoster(row, col);
                } else if (col==STOP){
                    stopLoco(row, col);
                }
            }
            
            public JTable makeJTable(TableSorter srtr) {
                JTable table = new JTable(srtr)  {
                    
                    public TableCellRenderer getCellRenderer(int row, int column) {
                        if (column == COMMENTCOL) {
                            return getRenderer(row);
                        } else
                            return super.getCellRenderer(row, column);
                    }
                    public TableCellEditor getCellEditor(int row, int column) {
                        if (column == COMMENTCOL) {
                            return getEditor(row);
                        } else
                            return super.getCellEditor(row, column);
                    }
                    
                    TableCellRenderer getRenderer(int row) {
                        TableCellRenderer retval = rendererMap.get(ecosObjectIdList.get(row));
                        if (retval == null) {
                            jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                            RosterEntry re = null;
                            if (b!=null){
                                re = Roster.instance().getEntryForId(b.getRosterId());
                            }
                            retval = new RosterBoxRenderer(re);
                            rendererMap.put(ecosObjectIdList.get(row), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<Object, TableCellRenderer>();
                    
                    TableCellEditor getEditor(int row) {
                        TableCellEditor retval = editorMap.get(ecosObjectIdList.get(row));
                        if (retval == null) {
                            jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                            RosterEntry re = null;
                            if (b!=null){
                                re = Roster.instance().getEntryForId(b.getRosterId());
                            }
                            GlobalRosterEntryComboBox cb = new GlobalRosterEntryComboBox();
                            cb.setNonSelectedItem(" ");
                            if(re==null){
                                cb.setSelectedIndex(0);
                            } else {
                                cb.setSelectedItem(re);
                            }
                            // create a new one with right aspects
                            retval = new RosterComboBoxEditor(cb);
                            editorMap.put(ecosObjectIdList.get(row), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellEditor> editorMap = new Hashtable<Object, TableCellEditor>();
                };
                table.getTableHeader().setReorderingAllowed(true);
                table.setColumnModel(new XTableColumnModel());
                table.createDefaultColumnsFromModel();
                
                addMouseListenerToHeader(table);
                return table;
            }
            /**
             * Is this property event announcing a change this table should display?
             * <P>
             * Note that events will come both from the jmri.jmrix.ecos.EcosLocoAddressManagers and also from the manager
             */
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if(!showLocoMonitor && (e.getPropertyName().equals("Speed") || e.getPropertyName().equals("Direction"))){
                    return false;
                }
                return true;
            }
            
            @Override
            public String getColumnName(int col) {
                 switch (col) {
                case SYSNAMECOL: return "ECoS Object Id";
                case USERNAMECOL: return "ECoS Descritpion";
                case VALUECOL: return "ECoS Address";
                case COMMENTCOL: return "JMRI Roster Id";
                case DELETECOL: return "ECOS Protocol";
                case ADDTOROSTERCOL: return "Add to Roster";
                case SPEEDDIR: return "Speed Direction";
                case STOP: return "Stop";
                default: return "unknown";
                }
            }
            
            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                case SYSNAMECOL:
                case USERNAMECOL:
                case VALUECOL:
                case DELETECOL:
                case SPEEDDIR:
                    return String.class;
                case ADDTOROSTERCOL:
                case STOP:
                    return JButton.class;
                case COMMENTCOL:
                  return JComboBox.class;
                default:
                    return null;
                }
            }
            
            @Override
            public boolean isCellEditable(int row, int col) {
                switch (col) {
                case COMMENTCOL:
                    return true;
                case ADDTOROSTERCOL:
                    jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                    if(b.getRosterId()==null || b.getRosterId().equals(""))
                        return true;
                    else
                        return false;
                case STOP:
                    return true;
                default:
                    return false;
                }
            }
            
            @Override
            public int getPreferredWidth(int col) {
                switch (col) {
                case SYSNAMECOL:
                    return new JTextField(5).getPreferredSize().width;
                case COMMENTCOL:
                    return new JTextField(20).getPreferredSize().width;
                case USERNAMECOL:
                    return new JTextField(20).getPreferredSize().width;
                case ADDTOROSTERCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(12).getPreferredSize().width;
                case STOP: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(6).getPreferredSize().width;
                case VALUECOL: 
                    return new JTextField(5).getPreferredSize().width;
                case SPEEDDIR: 
                    return new JTextField(10).getPreferredSize().width;
                default:
                    //log.warn("Unexpected column in getPreferredWidth: "+col);
                    return new JTextField(8).getPreferredSize().width;
                }
            }
    
            public void configureTable(JTable tbl) {
                table = tbl;
                setColumnToHoldButton(table, ADDTOROSTERCOL, 
                        new JButton("Add to Roster"));
                setColumnToHoldButton(table,STOP,stopButton());
                super.configureTable(table);
                XTableColumnModel columnModel = (XTableColumnModel)table.getColumnModel();
                TableColumn column  = columnModel.getColumnByModelIndex(SPEEDDIR);
                columnModel.setColumnVisible(column, false);
                column  = columnModel.getColumnByModelIndex(STOP);
                columnModel.setColumnVisible(column, false);
            }
            public NamedBean getBySystemName(String name) { return null;}
            public NamedBean getByUserName(String name) { return null;}
            
            synchronized public void dispose() {
                showLocoMonitor=false;
                getManager().removePropertyChangeListener(this);
                if (ecosObjectIdList != null) {
                    for (int i = 0; i< ecosObjectIdList.size(); i++) {
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                        if (b!=null) b.removePropertyChangeListener(this);
                    }
                }
            }
            
            public int getRowCount() {
                return ecosObjectIdList.size();
            }
            
            @Override
            public Object getValueAt(int row, int col) {
                if (row >= ecosObjectIdList.size()){
                    log.debug("row is greater than list size");
                    return null;
                    
                }
                jmri.jmrix.ecos.EcosLocoAddress b;
                switch (col) {
                case SYSNAMECOL:
                    return ecosObjectIdList.get(row);
                case USERNAMECOL:  // return user name
                    // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? b.getEcosDescription() : null;
                case VALUECOL:  //
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? b.getEcosLocoAddress() : null;
                case COMMENTCOL:
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    RosterEntry re = null;
                    if (b!=null){
                        re = Roster.instance().getEntryForId(b.getRosterId());
                    }
                    GlobalRosterEntryComboBox cb = (GlobalRosterEntryComboBox) table.getCellRenderer(row, col);
                    if(re==null){
                        cb.setSelectedIndex(0);
                    } else{
                        cb.setSelectedItem(re);
                    }
                    return re;
                case DELETECOL:
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? b.getProtocol() : null;
                case ADDTOROSTERCOL:  //
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    if(b.getRosterId()==null || b.getRosterId().equals(""))
                        return "Add To Roster";
                    else
                        return " ";
                case STOP:
                    return "Stop";
                case SPEEDDIR:
                    b = getByEcosObject(ecosObjectIdList.get(row));
                    return (b!=null) ? (b.getDirectionAsString() + " : " + b.getSpeed()) : null;
                    
                default:
                    //log.error("internal state inconsistent with table requst for "+row+" "+col);
                    return null;
                }
            }
            
            protected String getBeanType(){
                return "Ecos Loco";
            }
            
            @Override
            protected void showPopup(MouseEvent e){
            
            }
        };
    }
    
    boolean showLocoMonitor = false;
    void showMonitorChanged() {
        showLocoMonitor = showMonitorLoco.isSelected();
        XTableColumnModel columnModel = (XTableColumnModel)table.getColumnModel();
        TableColumn column  = columnModel.getColumnByModelIndex(SPEEDDIR);
        columnModel.setColumnVisible(column, showLocoMonitor);
        column  = columnModel.getColumnByModelIndex(STOP);
        columnModel.setColumnVisible(column, showLocoMonitor);
    }
    
    JCheckBox showMonitorLoco = new JCheckBox("Monitor Loco Speed");
    
    /**
     * Create a JButton to edit a turnout operation. 
     * @return	the JButton
     */
    protected JButton stopButton() {
        JButton stopButton = new JButton("STOP");
        return(stopButton);
    }
    
    void stopLoco(int row, int col){
    
        String objectNumber = ecosObjectIdList.get(row);
        EcosMessage m;
        //We will repeat this three times to make sure it gets through.
        for (int x = 0 ;x<3 ; x++){
            m = new EcosMessage("request("+objectNumber+", control, force)");
            adaptermemo.getTrafficController().sendEcosMessage(m, null);
            m = new EcosMessage("set("+objectNumber+", stop)");
            adaptermemo.getTrafficController().sendEcosMessage(m, null);
            m = new EcosMessage("release("+objectNumber+", control)");
            adaptermemo.getTrafficController().sendEcosMessage(m, null);
        }
    }
    
    public void addToPanel(EcosLocoTableTabAction f) {
        f.addToBottomBox(showMonitorLoco, adaptermemo.getUserName());
        showMonitorLoco.setToolTipText("Show extra columns for configuring turnout feedback?");
        showMonitorLoco.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showMonitorChanged();
                }
            });
    }    
    void addToRoster(int row, int col){
        if (getByEcosObject(ecosObjectIdList.get(row)).getRosterId()==null){
            EcosLocoToRoster addLoco = new EcosLocoToRoster(adaptermemo);
            getByEcosObject(ecosObjectIdList.get(row)).allowAddToRoster();
            addLoco.addToQueue(getByEcosObject(ecosObjectIdList.get(row)));
            addLoco.processQueue();
            m.fireTableRowsUpdated(row, row);
        }
    }

    protected void setTitle() {
        if(adaptermemo!=null){
            f.setTitle(adaptermemo.getUserName() + " Loco Table");
        }
        f.setTitle("Ecos Loco Table");
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrix.ecos.ecosLocoTable";
    }
    
    static class RosterBoxRenderer extends GlobalRosterEntryComboBox implements TableCellRenderer {
        public RosterBoxRenderer(RosterEntry re) {
            super();
            setNonSelectedItem(" ");
            if(re==null){
                setSelectedIndex(0);
            } else {
                setSelectedItem(re);
            }
        }
    
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            if(value == null)
                setSelectedIndex(0);
            else
                setSelectedItem(value);
            return this;
        }
    }
    
    static class RosterComboBoxEditor extends DefaultCellEditor {
        public RosterComboBoxEditor(GlobalRosterEntryComboBox cb) {
            super(cb);
        }
    }
    
    protected void addPressed(ActionEvent e){ }
    
    protected String getClassName() { return EcosLocoTableAction.class.getName(); }
    
    static final Logger log = Logger.getLogger(EcosLocoTableAction.class.getName());
}
