// AddSensorPanel.java

package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.GridLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.JOptionPane;
import java.util.ResourceBundle;
import java.util.Enumeration;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.SignalMast;
import jmri.NamedBean;
import jmri.Block;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.util.com.sun.TableSorter;
import jmri.InstanceManager;
import jmri.JmriException;

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
    ActionListener selectPanelListener;
    ActionListener addButton;
    ArrayList<LayoutEditor> panels;
    
    protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.EntryExitBundle");
    

    public AddEntryExitPairPanel(LayoutEditor panel) {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(3,2));
        //JPanel p;
        //p = new JPanel();
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        top.add(new JLabel(rb.getString("SelectPanel")));
        top.add(selectPanel);
        selectPanel.removeActionListener(selectPanelListener);
        selectPanel.removeAllItems();
        panels = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        for (int i = 0; i<panels.size(); i++){
            selectPanel.addItem(panels.get(i).getLayoutName());
            /*if(panels.get(i).getLayoutName().equals(panel.getLayoutName()))
                selectPanel.setSelect*/
        }
        if(panel!=null)
            selectPanel.setSelectedItem(panel.getLayoutName());
        selectPanel.addActionListener(selectPanelListener);
        JButton ok;
        //add(p);

        //p = new JPanel();
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        top.add(new JLabel(rb.getString("FromLocation")));
        top.add(fromPoint);
        //add(p);
        selectPanelListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    beanComboBox(fromPoint, null);
                    beanComboBox(toPoint, null);
                }
            };
        selectPointsFromPanel();
        beanComboBox(fromPoint, null);
        beanComboBox(toPoint, null);
        selectPanel.addActionListener(selectPanelListener);
        //p = new JPanel();
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        top.add(new JLabel(rb.getString("ToLocation")));
        top.add(toPoint);
        add(top);

        JPanel p=new JPanel();
        
        p.add(ok = new JButton(rb.getString("Add")));
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
        nxDataScroll	= new JScrollPane(nxDataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        configDeleteColumn(nxDataTable);
        
        add(nxDataScroll);
    }

    LayoutEditor panel;
    private void addButton(){
        ValidPoints from = getValidPointFromCombo(fromPoint);
        ValidPoints to = getValidPointFromCombo(toPoint);
        if(from==null || to==null)
            return;
        
        EntryExitPairs entryexit = EntryExitPairs.instance();
        entryexit.addNXDestination(from.getPoint(), to.getPoint(), panel);
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
        panel1.add(sourceLabel);
        entryExitFrame.add(panel1);
        entryExitFrame.pack();
        entryExitFrame.setVisible(true);
        int retval = JOptionPane.showOptionDialog(null, rb.getString("AutoGenEntryExitMessage"), rb.getString("AutoGenEntryExitTitle"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (retval == 0) {
            try {
                //InstanceManager.signalMastLogicManagerInstance().addPropertyChangeListener(this);
                EntryExitPairs entryexit = EntryExitPairs.instance();
                entryexit.addPropertyChangeListener(propertyNXListener);
                entryexit.automaticallyDiscoverEntryExitPairs(panels.get(selectPanel.getSelectedIndex()));
                //InstanceManager.signalMastLogicManagerInstance().automaticallyDiscoverEntryExitPairs();
                    
            } catch (jmri.JmriException e){
                EntryExitPairs.instance().removePropertyChangeListener(propertyNXListener);
                //InstanceManager.signalMastLogicManagerInstance().removePropertyChangeListener(this);
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

    public void itemStateChanged(ItemEvent e){
        fromButtonPressed();
    }

    ArrayList<ValidPoints> validPoints = new ArrayList<ValidPoints>();

    private void fromButtonPressed(){

    }

    private void selectPointsFromPanel(){
        if(selectPanel.getSelectedIndex()==-1)
            return;
        panel= panels.get(selectPanel.getSelectedIndex());
        ArrayList<PositionablePoint> pp = panel.pointList;
        fromPoint.removeAllItems();
        toPoint.removeAllItems();
        for(int i = 0; i<panel.pointList.size(); i++){
            String description = "";
            NamedBean source = null;
            /* Initial release to deal with sensors only
            if ((pp.get(i).getWestBoundSignalMast()!=null) && (!pp.get(i).getWestBoundSignalMast().equals(""))){
                description = pp.get(i).getWestBoundSignalMast();
                source = InstanceManager.signalMastManagerInstance().getSignalMast(pp.get(i).getWestBoundSignalMast());
                
            } 
            else if((pp.get(i).getWestBoundSignal()!=null) && (!pp.get(i).getWestBoundSignal().equals(""))){
                description = pp.get(i).getWestBoundSignal();
                source = InstanceManager.signalHeadManagerInstance().getSignalHead(pp.get(i).getWestBoundSignal());
            } else*/ if ((pp.get(i).getWestBoundSensor()!=null) && (!pp.get(i).getWestBoundSensor().equals(""))){
                description = pp.get(i).getWestBoundSensor();
                source = InstanceManager.sensorManagerInstance().getSensor(pp.get(i).getWestBoundSensor());
            }

            if(source!=null){
                //description = getPointAsString(pp.get(i),false);
                validPoints.add(new ValidPoints(source, description));
                
                fromPoint.addItem(description);
                toPoint.addItem(description);
            }
            source=null;
            description = "";
            /* Initial release to deal with sensors only
            if ((pp.get(i).getEastBoundSignalMast()!=null)&& (!pp.get(i).getEastBoundSignalMast().equals(""))){
                description = pp.get(i).getEastBoundSignalMast();
                source = InstanceManager.signalMastManagerInstance().getSignalMast(pp.get(i).getEastBoundSignalMast());
            }
            else if((pp.get(i).getEastBoundSignal()!=null) && (!pp.get(i).getEastBoundSignal().equals(""))){
                description = pp.get(i).getEastBoundSignal();
                source = InstanceManager.signalHeadManagerInstance().getSignalHead(pp.get(i).getEastBoundSignal());

            } else*/ if ((pp.get(i).getEastBoundSensor()!=null) && (!pp.get(i).getEastBoundSensor().equals(""))){
                description = pp.get(i).getEastBoundSensor();
                source = InstanceManager.sensorManagerInstance().getSensor(pp.get(i).getEastBoundSensor());
            }
            if(source!=null){
                //description = getPointAsString(pp.get(i),true);
                validPoints.add(new ValidPoints(source, description));
                fromPoint.addItem(description);
                toPoint.addItem(description);
            }
        }
        
        ArrayList<LayoutTurnout> tt = panel.turnoutList;
        
        for(int i = 0; i<tt.size(); i++){
            LayoutTurnout t = tt.get(i);
            String description = "";
            NamedBean source = null;
            
            if((t.getSignalAMast()!=null) && (!t.getSignalAMast().equals(""))){
                description = t.getSignalAMast();
                source = InstanceManager.signalMastManagerInstance().getSignalMast(description);
            } else if((t.getSensorA()!=null) && (!t.getSensorA().equals(""))){
                description = t.getSensorA();
                source = InstanceManager.sensorManagerInstance().getSensor(description);
            }
            
            
            if(source!=null){
                validPoints.add(new ValidPoints(source, description));
                fromPoint.addItem(description);
                toPoint.addItem(description);
            }
            source=null;
            description = "";

            if((t.getSignalBMast()!=null) && (!t.getSignalBMast().equals(""))){
                description = t.getSignalBMast();
                source = InstanceManager.signalMastManagerInstance().getSignalMast(description);
            } else if((t.getSensorB()!=null) && (!t.getSensorB().equals(""))){
                description = t.getSensorB();
                source = InstanceManager.sensorManagerInstance().getSensor(description);
            }
            
            if(source!=null){
                validPoints.add(new ValidPoints(source, description));
                fromPoint.addItem(description);
                toPoint.addItem(description);
            }
            source=null;
            description = "";
            
            if((t.getSignalCMast()!=null) && (!t.getSignalCMast().equals(""))){
                description = t.getSignalCMast();
                source = InstanceManager.signalMastManagerInstance().getSignalMast(description);
            } else if((t.getSensorC()!=null) && (!t.getSensorC().equals(""))){
                description = t.getSensorC();
                source = InstanceManager.sensorManagerInstance().getSensor(description);
            }
            
            if(source!=null){
                validPoints.add(new ValidPoints(source, description));
                fromPoint.addItem(description);
                toPoint.addItem(description);
            }
            source=null;
            description = "";
            
            if((t.getSignalDMast()!=null) && (!t.getSignalDMast().equals(""))){
                description = t.getSignalDMast();
                source = InstanceManager.signalMastManagerInstance().getSignalMast(description);
            } else if((t.getSensorD()!=null) && (!t.getSensorD().equals(""))){
                description = t.getSensorD();
                source = InstanceManager.sensorManagerInstance().getSensor(description);
            }
            
            if(source!=null){
                validPoints.add(new ValidPoints(source, description));
                fromPoint.addItem(description);
                toPoint.addItem(description);
            }
            source=null;
            description = "";
        }

    }

    TableSorter         nxSorter;
    JTable			    nxDataTable;
    JScrollPane 		nxDataScroll;

    TableModel nxModel;
    
    EntryExitPairs nxPairs = EntryExitPairs.instance();
    
    /*void signalMastCombo(JComboBox box, SignalMast select){
        jmri.SignalMastManager smm = jmri.InstanceManager.signalMastManagerInstance();
        box.removeAllItems();
        List<String> nameList = smm.getSystemNameList();
        String[] displayList = new String[nameList.size()];
        for(int i = 0; i<nameList.size(); i++){
            NamedBean sm = smm.getBySystemName(nameList.get(i));
            displayList[i] = sm.getDisplayName();
            validPoints.add(new ValidPoints(sm, sm.getDisplayName()));
        }
        java.util.Arrays.sort(displayList);
        for(int i = 0; i<displayList.length; i++){
            box.addItem(displayList[i]);
            if ((select!=null) && (displayList[i].equals(select.getDisplayName()))){
                box.setSelectedIndex(i);
            }
    }*/
    
    void beanComboBox(JComboBox box, NamedBean select){
        String[] displayList = new String[validPoints.size()];
        for (int i =0 ; i<validPoints.size(); i++){
            displayList[i] = validPoints.get(i).getDescription();
        }
    
        box.removeAllItems();

        java.util.Arrays.sort(displayList);
        for(int i = 0; i<displayList.length; i++){
            box.addItem(displayList[i]);
            if ((select!=null) && (displayList[i].equals(select.getDisplayName()))){
                box.setSelectedIndex(i);
            }
        }
    
    }

    String getPointAsString(PositionablePoint p, Boolean direction){
        String description = "";
        if(direction) {
            if ((p.getEastBoundSignalMast()!=null) && (!p.getEastBoundSignalMast().equals(""))){
                description = p.getEastBoundSignalMast();
                if ((p.getEastBoundSensor()!=null) && (!p.getEastBoundSensor().equals("")))
                    description = description + " (" + p.getEastBoundSensor() + ")";
            }
            else if((p.getEastBoundSignal()!=null) && (!p.getEastBoundSignal().equals(""))){
                description = p.getEastBoundSignal();
                if ((p.getEastBoundSensor()!=null) && (!p.getEastBoundSensor().equals("")))
                    description = description + " (" + p.getEastBoundSensor() + ")";
            } else if ((p.getEastBoundSensor()!=null) && (!p.getEastBoundSensor().equals(""))){
                description = p.getEastBoundSensor();
            }
        } else {
            if ((p.getWestBoundSignalMast()!=null) && (!p.getWestBoundSignalMast().equals(""))){
                description = p.getWestBoundSignalMast();
                if ((p.getWestBoundSensor()!=null) && (!p.getWestBoundSensor().equals("")))
                    description = description + " (" + p.getWestBoundSensor() + ")";
            }
            else if((p.getWestBoundSignal()!=null) && (!p.getWestBoundSignal().equals(""))){
                description = p.getWestBoundSignal();
                if ((p.getWestBoundSensor()!=null) && (!p.getWestBoundSensor().equals("")))
                    description = description + " (" + p.getWestBoundSensor() + ")";
            } else if ((p.getWestBoundSensor()!=null) && (!p.getWestBoundSensor().equals(""))){
                description = p.getWestBoundSensor();
            }
        }
        return (description);
    }

    static final int FROMPOINTCOL= 0;
    static final int TOPOINTCOL = 1;
    static final int ACTIVECOL = 2;
    static final int DELETECOL = 4;
    static final int BOTHWAYCOL = 3;

    static final int NUMCOL = 4+1;
    //Need to add a property change listener to catch when paths go active.
    class TableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener{
        //needs a method to for when panel changes
        //need a method to delete an item
        //Possibly also to set a route.
        //Add a propertychange listener to hear when the route goes active.
        TableModel(LayoutEditor panel){
            this.panel=panel;
            nxPairs.addPropertyChangeListener(this);
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        }

        LayoutEditor panel;

        ArrayList<Object> source=null;
        ArrayList<Object> dest=null;

        void updateNameList(){
            if(source!=null){
                for (int i=0; i<source.size();i++){
                    //source.get(i).removePropertyChangeListener(this, source.get(i), panel);
                }
            }
            if (dest!=null){
                for (int i=0; i<source.size();i++){
                   //source.get(i).removePropertyChangeListener(this);
                }            
            }
            
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        
        }

        public int getRowCount() {
            if (panel!=null)
                return nxPairs.getNxPairNumbers(panel);
            return 0;
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
                case DELETECOL:  //
                    return jmri.jmrit.beantable.AbstractTableAction.rb.getString("ButtonDelete");
            }
            return "";
        }
        
        public void setValueAt(Object value, int row, int col) {
            if (col==DELETECOL) {
                // button fired, delete Bean
                deleteEntryExit(row, col);
            }
            if (col==BOTHWAYCOL){
                boolean b = !((Boolean)value).booleanValue();
                nxPairs.setUniDirection(source.get(row), panel,dest.get(row), b);
            }
        
        }
        
        protected void deleteEntryExit(int row, int col) {
            NamedBean nbSource = ((NamedBean)source.get(row));
            NamedBean nbDest = (NamedBean)dest.get(row);
            nxPairs.deleteNxPair(nbSource, nbDest, panel);
        }

        String isPairActive(int row){
            // isPathActive
            if(nxPairs.isPathActive(source.get(row), dest.get(row), panel))
                return ("*");
            return ("");
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
            case FROMPOINTCOL:         return rb.getString("ColumnFrom");
            case TOPOINTCOL:    return rb.getString("ColumnTo");
            case ACTIVECOL:    return rb.getString("ColumnActive");
            case DELETECOL:    return "";
            case BOTHWAYCOL:    return rb.getString("ColumnBoth");
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
                return JButton.class;
            case BOTHWAYCOL:
                return Boolean.class;
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
                    return true;
                case DELETECOL:
                    return true;
                default:
                    return false;
            }
        }

        public int getColumnCount( ){
            return NUMCOL;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent e) {
           // updateNameList();
            if (e.getPropertyName().equals("length")) {
                updateNameList();
                //log.debug("Table changed length to "+signalMastLogicList.size());
                fireTableDataChanged();
            }/* else if (e.getSource() instanceof SignalMastLogic) {
                SignalMastLogic logic = (SignalMastLogic)e.getSource();
                if (matchPropertyName(e)){
                    for (int i = 0; i< signalMastLogicList.size(); i++) {
                        Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(i);
                        Enumeration<SignalMastLogic> en = b.keys();
                        while (en.hasMoreElements()) {
                            SignalMastLogic sm = en.nextElement();
                            if (sm==logic){
                                fireTableRowsUpdated(i, i);
                            }
                        }
                    }
                }
            } else if (e.getSource() instanceof jmri.SignalMast){
                jmri.SignalMast sigMast = (jmri.SignalMast)e.getSource();
                for (int i = 0; i<signalMastLogicList.size(); i++){
                    Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(i);
                    Enumeration<SignalMastLogic> en = b.keys();
                    while (en.hasMoreElements()) {
                        SignalMastLogic sm = en.nextElement();
                        //SignalMast dest = b.get(sm);
                        if(sm.getSourceMast()==sigMast)
                            fireTableRowsUpdated(i, i);
                    }
                }
            }*/
        }
    }
    
        protected void configDeleteColumn(JTable table) {
            // have the delete column hold a button
            setColumnToHoldButton(table, DELETECOL, 
                    new JButton(rb.getString("ButtonDelete")));
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

    class ValidPoints{

        NamedBean bean;
        boolean direction;
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
    
    /*protected PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent e) {
            if(e.getPropertyName().equals("topology")){
                //boolean newValue = new Boolean.parseBoolean(String.valueOf(e.getNewValue()));
                boolean newValue = (Boolean) e.getNewValue();
                if(newValue){
                //    for(int i = 0; i <signalMastLogic.size(); i++){
               //         signalMastLogic.get(i).setupLayoutEditorDetails();
               //     }
                    if(runWhenStablised){
                        try {
                           EntryExitPairs.instance().automaticallyDiscoverEntryExitPairs(toUseWhenStable);
                        } catch (JmriException je){
                            //Considered normal if routing not enabled
                        }
                    }
                }
            }
        }
    };*/
    
    protected PropertyChangeListener propertyNXListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("autoGenerateComplete")){
                if (entryExitFrame!=null)
                    entryExitFrame.setVisible(false);
                EntryExitPairs.instance().removePropertyChangeListener(propertyNXListener);
                JOptionPane.showMessageDialog(null, "Generation of Entry Exit Pairs Completed");
            }
        }
    };
    
    boolean runWhenStablised = false;
    LayoutEditor toUseWhenStable;
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddEntryExitPairPanel.class.getName());
}


/* @(#)AddNewHardwareDevicePanel.java */
