package jmri.jmrit.beantable;

import jmri.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import jmri.util.ConnectionNameFromSystemName;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import jmri.util.com.sun.TableSorter;

public class SensorTableTabAction extends AbstractTableAction {

    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    
    JPanel dataPanel;
    JTabbedPane sensorTabs;
    
    public SensorTableTabAction(String s){
        super(s);
    }
    
    public SensorTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected void createModel(){
        dataPanel = new JPanel();
        sensorTabs = new JTabbedPane();
        dataPanel.setLayout(new BorderLayout());
        if (InstanceManager.sensorManagerInstance().getClass().getName().contains("ProxySensorManager")){
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) InstanceManager.sensorManagerInstance();
            List<jmri.Manager> managerList = proxy.getManagerList();
            tabbedTableArray.add(new tabbedTableItem("All", "All", true, InstanceManager.sensorManagerInstance()));
            for(int x = 0; x<managerList.size(); x++){
                if (managerList.get(x) instanceof SensorManager){
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    boolean addToList = true;
                    for(int i = 0; i<tabbedTableArray.size(); i++){
                        if(tabbedTableArray.get(i).getClassAsString().equals(manuName))
                            addToList = false;
                    }
                    if(addToList){
                        tabbedTableItem itemModel = new tabbedTableItem(manuName, manuName, true, (SensorManager) managerList.get(x));
                        tabbedTableArray.add(itemModel);
                    }
                }
            }
        } else {
            String manuName = ConnectionNameFromSystemName.getConnectionName(InstanceManager.sensorManagerInstance().getSystemPrefix());
            tabbedTableArray.add(new tabbedTableItem(manuName, manuName, true, (InstanceManager.sensorManagerInstance())));
        }
        //tabbedTableArray.get(0).getAAClass().addToFrame(f);
        for(int x = 0; x<tabbedTableArray.size(); x++){
            SensorTableAction table = (SensorTableAction) tabbedTableArray.get(x).getAAClass();
            table.addToPanel(this);
            sensorTabs.addTab(tabbedTableArray.get(x).getClassAsString(),null, tabbedTableArray.get(x).getPanel(),null);
        }
        sensorTabs.addChangeListener(new ChangeListener() { 
            public void stateChanged(ChangeEvent evt) { 
                setMenuBar(f);
        }
    });
        dataPanel.add(sensorTabs, BorderLayout.CENTER);
    }
    
    @Override
    public JPanel getPanel(){
        createModel();
        return dataPanel;
    }
       
    ArrayList<tabbedTableItem> tabbedTableArray = new ArrayList<tabbedTableItem>();
    
    protected JTable makeJTable(TableSorter sorter) {
        return new JTable(sorter)  {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                boolean res = super.editCellAt(row, column, e);
                java.awt.Component c = this.getEditorComponent();
                if (c instanceof javax.swing.JTextField) {
                    ( (JTextField) c).selectAll();
                }            
                return res;
            }
        };
    }
    
    protected void setTitle() {
        //atf.setTitle("multiple sensors");
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
    
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }
    
    public void addToFrame(BeanTableFrame f){
        tabbedTableArray.get(sensorTabs.getSelectedIndex()).getAAClass().addToFrame(f);
    }
    
    public void setMenuBar(BeanTableFrame f){
        tabbedTableArray.get(sensorTabs.getSelectedIndex()).getAAClass().setMenuBar(f);
    }
    
    public void addToBottomBox(JComponent c, String str){
        for(int x = 0; x<tabbedTableArray.size(); x++){
            if (tabbedTableArray.get(x).getClassAsString().equals(str))
                tabbedTableArray.get(x).addToBottomBox(c);
        }
    }

    public void print(javax.swing.JTable.PrintMode mode, java.text.MessageFormat headerFormat, java.text.MessageFormat footerFormat){
        try {
            tabbedTableArray.get(sensorTabs.getSelectedIndex()).getDataTable().print(mode, headerFormat, footerFormat);
        } catch (java.awt.print.PrinterException e1) {
            log.warn("error printing: "+e1,e1);
        } catch ( NullPointerException ex) {
            log.error("Trying to print returned a NPE error");
        }
    }
    
    class tabbedTableItem {
        
        SensorTableAction tableAction;
        String className;
        String itemText;
        BeanTableDataModel dataModel;
        JTable dataTable;
        JScrollPane dataScroll;
        Box bottomBox;
        Boolean AddToFrameRan = false;
        SensorManager sensorManager;
        int bottomBoxIndex;	// index to insert extra stuff
        static final int bottomStrutWidth = 20;
        
        boolean standardModel = true;
        
        final JPanel dataPanel = new JPanel();
        
        tabbedTableItem(String aaClass, String choice, boolean stdModel, SensorManager manager){
            try{
                Class<?> cl = Class.forName("jmri.jmrit.beantable.SensorTableAction");
                java.lang.reflect.Constructor<?> co = cl.getConstructor(new Class[] {String.class});
                tableAction = (SensorTableAction) co.newInstance(choice);
            } catch (ClassNotFoundException e1) {
                log.error("Not a valid class");
                return;
            } catch (NoSuchMethodException e2) {
                log.error("Not such method");
                return;
            } catch (InstantiationException e3) {
                log.error("Not a valid class");
                return;
            } catch (ClassCastException e4){
                log.error("Not part of the abstractTableActions");
                return;
            } catch (Exception e) {
                log.error("Exception " + e.toString());
                return;
            }
            
            className = aaClass;
            itemText = choice;
            standardModel=stdModel;
            sensorManager = manager;
            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            dataPanel.setLayout(new BorderLayout());
            if (stdModel)
                createDataModel();
            else
                addPanelModel();
        }
        
        void createDataModel(){
            if (sensorManager!=null)
                tableAction.setManager(sensorManager);
            dataModel = tableAction.getTableDataModel();
            TableSorter sorter = new TableSorter(dataModel);
            dataTable = makeJTable(sorter);
            sorter.setTableHeader(dataTable.getTableHeader());
            dataScroll	= new JScrollPane(dataTable);
            
            try {
                TableSorter tmodel = ((TableSorter)dataTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(BeanTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);
            } catch (java.lang.ClassCastException e) {}  // happens if not sortable table
            
            dataModel.configureTable(dataTable);
            
            java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
            // width is right, but if table is empty, it's not high
            // enough to reserve much space.
            dataTableSize.height = Math.max(dataTableSize.height, 400);
            dataScroll.getViewport().setPreferredSize(dataTableSize);
            
            // set preferred scrolling options
            dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            
            //dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.add(dataScroll, BorderLayout.CENTER);
            
            dataPanel.add(bottomBox, BorderLayout.SOUTH);
            JButton addButton = new JButton(rbean.getString("ButtonAdd"));
            addToBottomBox(addButton);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableAction.addPressed(e);
                }
            });       
        }
        
        void addPanelModel(){
            dataPanel.add(tableAction.getPanel(), BorderLayout.CENTER);
            dataPanel.add(bottomBox, BorderLayout.SOUTH);
        }
        
        boolean getStandardTableModel(){ return standardModel; }

        String getClassAsString(){
            return className;
        }
        
        String getItemString(){
            return itemText;
        }
        
        AbstractTableAction getAAClass(){
            return tableAction;
        }
        
        JPanel getPanel() {
            return dataPanel;
        }
        
        boolean getAdditionsToFrameDone() { return AddToFrameRan; }
        
        void setAddToFrameRan() { AddToFrameRan=true; }
        
        JTable getDataTable(){ 
            return dataTable;
        }
        
        protected void addToBottomBox(JComponent comp) {
            bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
            ++bottomBoxIndex;
            bottomBox.add(comp, bottomBoxIndex);
            ++bottomBoxIndex;
        }
        
        void dispose(){
            if (dataModel != null)
                dataModel.dispose();
            dataModel = null;
            dataTable = null;
            dataScroll = null;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableTabAction.class.getName());
}