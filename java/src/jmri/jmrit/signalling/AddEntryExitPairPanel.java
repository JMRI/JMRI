// AddSensorPanel.java

package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.ResourceBundle;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.NamedBean;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.com.sun.TableSorter;
import jmri.InstanceManager;

/**
 * JPanel to create a new JMRI devices
 * HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.2 $
 */

public class AddEntryExitPairPanel extends jmri.util.swing.JmriPanel{

    JComboBox selectPanel = new JComboBox();
    JComboBox fromPoint = new JComboBox();
    JComboBox toPoint = new JComboBox();
    
    String [] clearOptions = {"Prompt User", "Clear Route", "Cancel Route"};
    JComboBox clearEntry = new JComboBox(clearOptions);
    String [] interlockTypes = {"Set Turnouts Only", "Set Turnouts and SignalMasts", "Full Interlock"};
    JComboBox typeBox = new JComboBox(interlockTypes);
    
    ArrayList<LayoutEditor> panels;
    
     EntryExitPairs nxPairs = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);
    
    protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.EntryExitBundle");
    

    public AddEntryExitPairPanel(LayoutEditor panel) {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(6,2));

        top.add(new JLabel(rb.getString("SelectPanel")));
        top.add(selectPanel);
        selectPanel.removeAllItems();
        panels = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        for (int i = 0; i<panels.size(); i++){
            selectPanel.addItem(panels.get(i).getLayoutName());
        }
        if(panel!=null)
            selectPanel.setSelectedItem(panel.getLayoutName());
        

        top.add(new JLabel(rb.getString("FromLocation")));
        top.add(fromPoint);
        ActionListener selectPanelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectPointsFromPanel();
                nxModel.setPanel(panels.get(selectPanel.getSelectedIndex())); 
            }
        };
        selectPointsFromPanel();
        selectPanel.addActionListener(selectPanelListener);

        top.add(new JLabel(rb.getString("ToLocation")));
        top.add(toPoint);
        top.add(new JLabel("NX Type"));
        top.add(typeBox);
        add(top);

        clearEntry.setSelectedIndex(nxPairs.getClearDownOption());
        ActionListener clearEntryListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nxPairs.setClearDownOption(clearEntry.getSelectedIndex());
            }
        };
        
        top.add(new JLabel(""));
        top.add(new JLabel(""));
        clearEntry.addActionListener(clearEntryListener);
        clearEntry.setToolTipText("set the action for when the NX buttons are reselected");
        top.add(new JLabel("ReSelection Action"));
        top.add(clearEntry);
        add(top);
        
        JPanel p=new JPanel();
        JButton ok = new JButton(rb.getString("Add"));
        p.add(ok);
        ok.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent e){
                addButton();
            }
        });
        
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton auto;
        p.add(auto = new JButton(rb.getString("AutoGenerate")));
        auto.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent e){
                autoDiscovery();
            }
        });
        p.add(auto);
        add(p);
        nxModel = new TableModel(panel);
        nxSorter = new TableSorter(nxModel);
    	nxDataTable = new JTable(nxSorter);
        nxSorter.setTableHeader(nxDataTable.getTableHeader());
        nxDataScroll = new JScrollPane(nxDataTable);
        nxModel.configureTable(nxDataTable);
        java.awt.Dimension dataTableSize = nxDataTable.getPreferredSize();
        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        nxDataScroll.getViewport().setPreferredSize(dataTableSize);
        add(nxDataScroll);
    }

    LayoutEditor panel;
    private void addButton(){
        ValidPoints from = getValidPointFromCombo(fromPoint);
        ValidPoints to = getValidPointFromCombo(toPoint);
        if(from==null || to==null)
            return;
        
        nxPairs.addNXDestination(from.getPoint(), to.getPoint(), panel);
        nxPairs.setEntryExitType(from.getPoint(), panel, to.getPoint(), typeBox.getSelectedIndex());
    }
    
    jmri.util.JmriJFrame entryExitFrame = null;
    JLabel sourceLabel = new JLabel();
    
    private void autoDiscovery(){
        if (!InstanceManager.layoutBlockManagerInstance().isAdvancedRoutingEnabled()){
            int response = JOptionPane.showConfirmDialog(null, rb.getString("EnableLayoutBlockRouting"));
            if (response == 0){
                InstanceManager.layoutBlockManagerInstance().enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, rb.getString("LayoutBlockRoutingEnabled"));
            }
        }
        entryExitFrame = new jmri.util.JmriJFrame("Discover Entry Exit Pairs", false, false);
        entryExitFrame.setPreferredSize(null);
        JPanel panel1 = new JPanel();
        sourceLabel = new JLabel("Discovering Entry Exit Pairs");
        /*ImageIcon i;
        i = new ImageIcon("resources"+File.separator+"icons"+File.separator+"misc" + File.separator+ "gui3" + File.separator+"process-working.gif");
        JLabel label = new JLabel(); 
        label.setIcon(i); 
        panel1.add(label);*/
        panel1.add(sourceLabel);

        entryExitFrame.add(panel1);
        entryExitFrame.pack();
        entryExitFrame.setVisible(true);
        int retval = JOptionPane.showOptionDialog(null, rb.getString("AutoGenEntryExitMessage"), rb.getString("AutoGenEntryExitTitle"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (retval == 0) {
            final PropertyChangeListener propertyNXListener = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("autoGenerateComplete")){
                        if (entryExitFrame!=null){
                            entryExitFrame.setVisible(false);
                            entryExitFrame.dispose();
                        }
                        nxPairs.removePropertyChangeListener(this);
                        JOptionPane.showMessageDialog(null, "Generation of Entry Exit Pairs Completed");
                    }
                }
            };
            try {
                nxPairs.addPropertyChangeListener(propertyNXListener);
                nxPairs.automaticallyDiscoverEntryExitPairs(panels.get(selectPanel.getSelectedIndex()), typeBox.getSelectedIndex());
            } catch (jmri.JmriException e){
                log.info("Exception here");
                nxPairs.removePropertyChangeListener(propertyNXListener);
                JOptionPane.showMessageDialog(null, e.toString());
                entryExitFrame.setVisible(false);
            }
        } else {
            entryExitFrame.setVisible(false);
        }
    }
    
    ValidPoints getValidPointFromCombo(JComboBox box){
        String item = (String)box.getSelectedItem();
        for(int i = 0; i<validPoints.size(); i++){
            if(validPoints.get(i).getDescription().equals(item))
                return validPoints.get(i);
        }
        return null;
    }

    ArrayList<ValidPoints> validPoints = new ArrayList<ValidPoints>();
    
    private void selectPointsFromPanel(){
        if(selectPanel.getSelectedIndex()==-1)
            return;
        if(panel == panels.get(selectPanel.getSelectedIndex())){
            return;
        }
        panel= panels.get(selectPanel.getSelectedIndex());
        fromPoint.removeAllItems();
        toPoint.removeAllItems();
        for(PositionablePoint pp: panel.pointList){
            addPointToCombo(pp.getWestBoundSignalMast(), pp.getWestBoundSensor());
            addPointToCombo(pp.getEastBoundSignalMast(), pp.getEastBoundSensor());
        }
        
        for(LayoutTurnout t: panel.turnoutList){
            addPointToCombo(t.getSignalAMast(), t.getSensorA());
            addPointToCombo(t.getSignalBMast(), t.getSensorB());
            addPointToCombo(t.getSignalCMast(), t.getSensorC());
            addPointToCombo(t.getSignalDMast(), t.getSensorD());
        }
        
        for(LevelXing xing: panel.xingList){
            addPointToCombo(xing.getSignalAMastName(), xing.getSensorAName());
            addPointToCombo(xing.getSignalBMastName(), xing.getSensorBName());
            addPointToCombo(xing.getSignalCMastName(), xing.getSensorCName());
            addPointToCombo(xing.getSignalDMastName(), xing.getSensorDName());
        }
        for(LayoutSlip slip: panel.slipList){
            addPointToCombo(slip.getSignalAMast(), slip.getSensorA());
            addPointToCombo(slip.getSignalBMast(), slip.getSensorB());
            addPointToCombo(slip.getSignalCMast(), slip.getSensorC());
            addPointToCombo(slip.getSignalDMast(), slip.getSensorD());
        }
    }
    
    void addPointToCombo(String signalMastName, String sensorName){
        NamedBean source=null;
        if(sensorName!=null && !sensorName.isEmpty()){
            String description = sensorName;
            source = InstanceManager.sensorManagerInstance().getSensor(sensorName);
            if(signalMastName!=null && !signalMastName.isEmpty()){
                description = sensorName + " (" + signalMastName + ")";
            }
            validPoints.add(new ValidPoints(source, description));
            fromPoint.addItem(description);
            toPoint.addItem(description);
        }
    }
    
    TableSorter         nxSorter;
    JTable			    nxDataTable;
    JScrollPane 		nxDataScroll;

    TableModel nxModel;
    
    static final int FROMPOINTCOL= 0;
    static final int TOPOINTCOL = 1;
    static final int ACTIVECOL = 2;
    static final int CLEARCOL = 3;
    static final int BOTHWAYCOL = 4;
    static final int DELETECOL = 5;
    static final int TYPECOL = 6;
    static final int ENABLEDCOL = 7;

    static final int NUMCOL = ENABLEDCOL+1;
    //Need to add a property change listener to catch when paths go active.
    class TableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener{
        //needs a method to for when panel changes
        //need a method to delete an item
        //Possibly also to set a route.
        //Add a propertychange listener to hear when the route goes active.
        TableModel(LayoutEditor panel){
            setPanel(panel);
            nxPairs.addPropertyChangeListener(this);
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        }
        
        void setPanel(LayoutEditor panel){
            if(this.panel==panel)
                return;
            this.panel = panel;
            rowCount = nxPairs.getNxPairNumbers(panel);
            updateNameList();
            fireTableDataChanged();
        }
        
        LayoutEditor panel;

        ArrayList<Object> source=null;
        ArrayList<Object> dest=null;

        void updateNameList(){
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        }
        
        int rowCount = 0;
        public int getRowCount() {
            return rowCount;
        }
        
        public void configureTable(JTable table) {
            // allow reordering of the columns
            table.getTableHeader().setReorderingAllowed(true);

            // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // resize columns as requested
            for (int i=0; i<table.getColumnCount(); i++) {
                int width = getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            table.sizeColumnsToFit(-1);

            configDeleteColumn(table);
            
        }

        public Object getValueAt(int row, int col) {
            // get roster entry for row
            if (panel == null){
                log.debug("no panel selected!");
                return "Error";
            }
            switch (col) {
                case FROMPOINTCOL: return nxPairs.getPointAsString((NamedBean)source.get(row), panel);
                case TOPOINTCOL:    return nxPairs.getPointAsString((NamedBean)dest.get(row), panel);
                case ACTIVECOL:    return isPairActive(row);
                case BOTHWAYCOL:    return !nxPairs.isUniDirection(source.get(row), panel, dest.get(row));
                case ENABLEDCOL:    return !nxPairs.isEnabled(source.get(row), panel, dest.get(row));
                case CLEARCOL:  return jmri.jmrit.beantable.AbstractTableAction.rb.getString("ButtonClear");
                case DELETECOL:  //
                    return jmri.jmrit.beantable.AbstractTableAction.rb.getString("ButtonDelete");
                case TYPECOL:
                    return NXTYPE_NAMES[nxPairs.getEntryExitType(source.get(row), panel, dest.get(row))];
                default: return "";
            }
        }
        
        public void setValueAt(Object value, int row, int col) {
            if (col==DELETECOL) {
                // button fired, delete Bean
                deleteEntryExit(row, col);
            }
            if (col == CLEARCOL){
                nxPairs.cancelInterlock(source.get(row), panel,dest.get(row));
            }
            if (col==BOTHWAYCOL){
                boolean b = !((Boolean)value).booleanValue();
                nxPairs.setUniDirection(source.get(row), panel,dest.get(row), b);
            }            
            if (col==ENABLEDCOL){
                boolean b = !((Boolean)value).booleanValue();
                nxPairs.setEnabled(source.get(row), panel,dest.get(row), b);
            }
            if(col==TYPECOL){
                String val = (String) value;
                if(val.equals("Turnout"))
                    nxPairs.setEntryExitType(source.get(row), panel,dest.get(row), 0x00);
                else if(val.equals("SignalMast"))
                    nxPairs.setEntryExitType(source.get(row), panel,dest.get(row), 0x01);
                else if(val.equals("Full InterLock"))
                    nxPairs.setEntryExitType(source.get(row), panel,dest.get(row), 0x02);
            }
        }
        
        public int getPreferredWidth(int col) {
            switch (col) {
                case FROMPOINTCOL:
                case TOPOINTCOL:    return new JTextField(15).getPreferredSize().width;
                case ACTIVECOL:    
                case BOTHWAYCOL:    
                case ENABLEDCOL:    return new JTextField(5).getPreferredSize().width;
                case CLEARCOL:
                case DELETECOL:  //
                    return new JTextField(22).getPreferredSize().width;
                case TYPECOL:
                    return new JTextField(10).getPreferredSize().width;
            default:
                log.warn("Unexpected column in getPreferredWidth: "+col);
                return new JTextField(8).getPreferredSize().width;
            }
        }
        
        protected void deleteEntryExit(int row, int col) {
            NamedBean nbSource = ((NamedBean)source.get(row));
            NamedBean nbDest = (NamedBean)dest.get(row);
            nxPairs.deleteNxPair(nbSource, nbDest, panel);
        }

        String isPairActive(int row){
            if(nxPairs.isPathActive(source.get(row), dest.get(row), panel))
                return ("yes");
            return ("");
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
            case FROMPOINTCOL:         return rb.getString("ColumnFrom");
            case TOPOINTCOL:    return rb.getString("ColumnTo");
            case ACTIVECOL:    return rb.getString("ColumnActive");
            case DELETECOL:    return "";
            case CLEARCOL:    return "";
            case BOTHWAYCOL:    return rb.getString("ColumnBoth");
            case TYPECOL:    return "NX Type";
            case ENABLEDCOL:    return "Disabled";
            default:            return "<UNKNOWN>";
            }
        }
        
        public Class<?> getColumnClass(int col) {
            switch (col) {
            case FROMPOINTCOL:
            case TOPOINTCOL:
            case ACTIVECOL:
                return String.class;
            case DELETECOL:
            case CLEARCOL:
                return JButton.class;
            case BOTHWAYCOL:
            case ENABLEDCOL:
                return Boolean.class;
            case TYPECOL:
                return String.class;
            default:
                return null;
            }
        }
        
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case BOTHWAYCOL:
                    Object obj = nxPairs.getEndPointLocation((NamedBean)dest.get(row), panel);
                    if(obj instanceof PositionablePoint){
                        PositionablePoint point = (PositionablePoint)obj;
                        if(point.getType()==PositionablePoint.END_BUMPER){
                            JOptionPane.showMessageDialog(null, rb.getString("EndBumperPoint"));
                            return false;
                        }
                    }
                    if(!nxPairs.canBeBiDirectional(source.get(row), panel, dest.get(row))){
                        JOptionPane.showMessageDialog(null, rb.getString("BothWayTurnoutOnly"));
                        return false;
                    }
                    /*if(nxPairs.getEntryExitType(source.get(row), panel, dest.get(row))!=0x00){
                        JOptionPane.showMessageDialog(null, rb.getString("BothWayTurnoutOnly"));
                        return false;
                    }*/
                    return true;
                case DELETECOL:
                case CLEARCOL:
                case ENABLEDCOL:
                case TYPECOL:
                    return true;
                default:
                    return false;
            }
        }

        public int getColumnCount( ){
            return NUMCOL;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length") || e.getPropertyName().equals("active")) {
                rowCount = nxPairs.getNxPairNumbers(panel);
                updateNameList();
                fireTableDataChanged();
            }
        }
    }
    
        String [] NXTYPE_NAMES = { "Turnout", "SignalMast", "Full InterLock" };
        protected void configDeleteColumn(JTable table) {
            // have the delete column hold a button
            setColumnToHoldButton(table, DELETECOL, 
                    new JButton(rb.getString("ButtonDelete")));
                    
            setColumnToHoldButton(table, CLEARCOL, 
                    new JButton(rb.getString("ButtonClear")));
                    
            JComboBox typeCombo = new JComboBox(NXTYPE_NAMES);
        
            TableColumn col = table.getColumnModel().getColumn(TYPECOL);
            col.setCellEditor(new DefaultCellEditor(typeCombo));
        }
        
        
        
        /**
         * Service method to setup a column so that it will hold a
         * button for it's values
         * @param table
         * @param column
         * @param sample Typical button, used for size
         */
        protected void setColumnToHoldButton(JTable table, int column, JButton sample) {
            //TableColumnModel tcm = table.getColumnModel();
            // install a button renderer & editor
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            table.setDefaultRenderer(JButton.class,buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            table.setDefaultEditor(JButton.class,buttonEditor);
            // ensure the table rows, columns have enough room for buttons
            table.setRowHeight(sample.getPreferredSize().height);
            table.getColumnModel().getColumn(column)
                .setPreferredWidth((sample.getPreferredSize().width)+4);
        }

    static class ValidPoints{

        NamedBean bean;
        String description;

        ValidPoints(NamedBean bean, String description){
            this.bean = bean;
            this.description=description;
        }

        NamedBean getPoint(){
            return bean;
        }

        String getDescription(){
            return description;
        }
    }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddEntryExitPairPanel.class.getName());
}


/* @(#)AddNewHardwareDevicePanel.java */
