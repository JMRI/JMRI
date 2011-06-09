package jmri.jmrit.signalling;


import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import javax.swing.table.*;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.JmriJFrame;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.util.ResourceBundle;

/**
 * Frame for Signal Mast Add / Edit Panel
 * @author	Kevin Dickerson   Copyright (C) 2011
 * @version $Revision: 1.4 $
*/

public class SignallingSourcePanel extends jmri.util.swing.JmriPanel implements PropertyChangeListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    SignalMastLogic sml;
    SignalMast sourceMast;
    JLabel fixedSourceMastLabel = new JLabel();
    
    JButton discoverPairs = new JButton(rb.getString("ButtonDiscover"));
    
    SignalMastAppearanceModel _AppearanceModel;
    JScrollPane _SignalAppearanceScrollPane;
    
    public SignallingSourcePanel(final SignalMast sourceMast){
        super();
        sml = jmri.InstanceManager.signalMastLogicManagerInstance().getSignalMastLogic(sourceMast);
        this.sourceMast = sourceMast;
        fixedSourceMastLabel = new JLabel(sourceMast.getDisplayName());
        if (sml!=null){
            _signalMastList = sml.getDestinationList();
        }
        
        jmri.InstanceManager.layoutBlockManagerInstance().addPropertyChangeListener(this);
        
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());
        
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        
        JPanel sourcePanel = new JPanel();
        sourcePanel.add(fixedSourceMastLabel);
        header.add(sourcePanel);
        containerPanel.add(header, BorderLayout.NORTH);

        
        JPanel p3xsi = new JPanel();
        JPanel p3xsiSpace = new JPanel();
        p3xsiSpace.setLayout(new BoxLayout(p3xsiSpace, BoxLayout.Y_AXIS));
        p3xsiSpace.add(new JLabel(" "));
        p3xsi.add(p3xsiSpace);
        
        _AppearanceModel = new SignalMastAppearanceModel();
        JTable SignalAppearanceTable = jmri.util.JTableUtil.sortableDataModel(_AppearanceModel);

        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)SignalAppearanceTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(SignalMastAppearanceModel.SYSNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {}  // if not a sortable table model
        
        
        SignalAppearanceTable.setRowSelectionAllowed(false);
        SignalAppearanceTable.setPreferredScrollableViewportSize(new java.awt.Dimension(630,120));
        _AppearanceModel.configureTable(SignalAppearanceTable);
        _SignalAppearanceScrollPane = new JScrollPane(SignalAppearanceTable);
        p3xsi.add(_SignalAppearanceScrollPane,BorderLayout.CENTER);
       // p3.add(p3xsi);
        p3xsi.setVisible(true);
        _AppearanceModel.fireTableDataChanged();
        containerPanel.add(p3xsi, BorderLayout.CENTER);
        
        JPanel footer = new JPanel();
        
        footer.add(discoverPairs);
        discoverPairs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                discoverPressed(e);
            }
        });
        
        JButton addLogic = new JButton(rb.getString("AddLogic"));
        footer.add(addLogic);
        addLogic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sigLog.setMast(sourceMast, null);
                sigLog.actionPerformed(null);
            }
        });
        
        if(!jmri.InstanceManager.layoutBlockManagerInstance().isAdvancedRoutingEnabled())
            discoverPairs.setEnabled(false);
        containerPanel.add(footer, BorderLayout.SOUTH);
        add(containerPanel);
    }
    
    JmriJFrame signalMastLogicFrame = null;
    JLabel sourceLabel = new JLabel();
    
    void discoverPressed(ActionEvent e){
        signalMastLogicFrame = new JmriJFrame("Discover Signal Mast Pairs");
        signalMastLogicFrame.setPreferredSize(null);
        Container theContentPane = signalMastLogicFrame.getContentPane();
        theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
        sourceLabel = new JLabel("Discovering Signalmasts");
        signalMastLogicFrame.pack();
        signalMastLogicFrame.setVisible(true);
        
        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        jmri.InstanceManager.signalMastLogicManagerInstance().addPropertyChangeListener(this);
        for(int i = 0; i<layout.size(); i++){
            try{
                jmri.InstanceManager.signalMastLogicManagerInstance().discoverSignallingDest(sourceMast, layout.get(i));
            } catch (jmri.JmriException ex) {
                signalMastLogicFrame.setVisible(false);
                JOptionPane.showMessageDialog(null, ex.toString());
            }
        }
        jmri.InstanceManager.signalMastLogicManagerInstance().removePropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("autoSignalMastGenerateComplete")){
            signalMastLogicFrame.setVisible(false);
            //signalMastLogicFrame = null;
            JOptionPane.showMessageDialog(null, "Generation of Signalling Pairs Completed");
            if(sml==null){
                updateDetails();
            }
        }
        if(e.getPropertyName().equals("advancedRoutingEnabled")){
            boolean newValue = (Boolean) e.getNewValue();
            discoverPairs.setEnabled(newValue);
        }
    }
    
    private ArrayList <SignalMast> _signalMastList;
    
    private void updateDetails(){
        sml = jmri.InstanceManager.signalMastLogicManagerInstance().getSignalMastLogic(sourceMast);
        if (sml!=null){
            _signalMastList = sml.getDestinationList();
            _AppearanceModel.fireTableDataChanged();
        }
    }
    
    public class SignalMastAppearanceModel extends AbstractTableModel implements PropertyChangeListener
    {
        SignalMastAppearanceModel(){
            super();
            if(sml!=null)
                sml.addPropertyChangeListener(this);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c ==ACTIVE_COLUMN)
                return Boolean.class;
            if (c ==ENABLE_COLUMN)
                return Boolean.class;
            if(c==EDIT_COLUMN)
                return JButton.class;
            return String.class;
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

            //configValueColumn(table);
            configEditColumn(table);
            
        }
        
        public int getPreferredWidth(int col) {
            switch (col) {
            case SYSNAME_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            case ENABLE_COLUMN:
            case ACTIVE_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case USERNAME_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            case EDIT_COLUMN: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                return new JTextField(22).getPreferredSize().width;
            default:
                log.warn("Unexpected column in getPreferredWidth: "+col);
                return new JTextField(8).getPreferredSize().width;
            }
        }
        
        @Override
        public String getColumnName(int col) {
            if (col==USERNAME_COLUMN) return rb.getString("ColumnUserName");
            if (col==SYSNAME_COLUMN) return rb.getString("ColumnSystemName");
            if (col==ACTIVE_COLUMN) return rb.getString("ColumnActive");
            if (col==ENABLE_COLUMN) return rb.getString("ColumnEnabled");
            if (col==EDIT_COLUMN) return rb.getString("ColumnEdit");
            return "";
        }
        
        public void dispose(){
           // jmri.InstanceManager.signalMastManagerInstance().removePropertyChangeListener(this);
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                _signalMastList = sml.getDestinationList();
                fireTableDataChanged();
            } else {
                fireTableRowsUpdated(0, _signalMastList.size());
            }
        }
        
        
        protected void configEditColumn(JTable table) {
            // have the delete column hold a button
            /*AbstractTableAction.rb.getString("EditDelete")*/
            setColumnToHoldButton(table, EDIT_COLUMN, 
                    new JButton("EDIT"));
        }
        
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

        public int getColumnCount () {return 5;}

        @Override
        public boolean isCellEditable(int r,int c) {
            if (c==EDIT_COLUMN)
                return true;
            return ( (c==USERNAME_COLUMN) );
        }
        
        protected void editPair(int r){
            sigLog.setMast(sourceMast, _signalMastList.get(r));
            sigLog.actionPerformed(null);        
        }
        
        public static final int SYSNAME_COLUMN = 0;
        public static final int USERNAME_COLUMN = 1;
        public static final int ACTIVE_COLUMN = 2;
        public static final int ENABLE_COLUMN = 3;
        public static final int EDIT_COLUMN = 4;
        
        public void setSetToState(String x){}
        
        public int getRowCount () {
            if (_signalMastList==null)
                return 0;
            return _signalMastList.size();
        }

        public Object getValueAt (int r,int c) {
            // some error checking
            if (r >= _signalMastList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case USERNAME_COLUMN:
                    return _signalMastList.get(r).getUserName();
                case SYSNAME_COLUMN:  // slot number
                    return _signalMastList.get(r).getSystemName();
//                    return appearList.get(r).getAppearance();
                case ACTIVE_COLUMN:
                    return sml.isActive(_signalMastList.get(r));
                 case ENABLE_COLUMN:
                    return sml.isEnabled(_signalMastList.get(r));
                case EDIT_COLUMN:
                    return "EDIT";
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type,int r,int c) {
            if (c==EDIT_COLUMN)
                editPair(r);
        }
    }
    
    jmri.jmrit.signalling.SignallingAction sigLog = new jmri.jmrit.signalling.SignallingAction();
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignallingSourcePanel.class.getName());

}