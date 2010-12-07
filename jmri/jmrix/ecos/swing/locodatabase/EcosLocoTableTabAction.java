package jmri.jmrix.ecos.swing.locodatabase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import jmri.jmrit.beantable.*;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

import jmri.util.com.sun.TableSorter;

public class EcosLocoTableTabAction extends AbstractTableAction {

    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    
    JPanel dataPanel;
    JTabbedPane ecosLocoTabs;
    
    public EcosLocoTableTabAction(String s){
        super(s);
    }
    
    public EcosLocoTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected void createModel(){
        dataPanel = new JPanel();
        ecosLocoTabs = new JTabbedPane();
        dataPanel.setLayout(new BorderLayout());
        
        java.util.List<Object> list = jmri.InstanceManager.getList(jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
        if (list != null) {
            for (Object memo : list) {
                EcosSystemConnectionMemo eMemo = (EcosSystemConnectionMemo) memo;
                //We only want to add connections that have an active loco address manager
                if (eMemo.getLocoAddressManager()!=null){
                    tabbedTableItem itemModel = new tabbedTableItem(eMemo.getUserName(), eMemo.getUserName(), true, eMemo);
                    tabbedTableArray.add(itemModel);
                }
            }
        }
        if (tabbedTableArray.size()==1){
            EcosLocoTableAction table = (EcosLocoTableAction) tabbedTableArray.get(0).getAAClass();
            table.addToPanel(this);
            dataPanel.add(tabbedTableArray.get(0).getPanel(), BorderLayout.CENTER);
        } else {
            for(int x = 0; x<tabbedTableArray.size(); x++){
                EcosLocoTableAction table = (EcosLocoTableAction) tabbedTableArray.get(x).getAAClass();
                table.addToPanel(this);
                ecosLocoTabs.addTab(tabbedTableArray.get(x).getClassAsString(),null, tabbedTableArray.get(x).getPanel(),null);
            }
            ecosLocoTabs.addChangeListener(new ChangeListener() { 
                public void stateChanged(ChangeEvent evt) { 
                    setMenuBar(f);
                }
            }); 
            dataPanel.add(ecosLocoTabs, BorderLayout.CENTER);
        }
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
        //atf.setTitle("multiple turnouts");
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.EcosLocoTable";
    }
    
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }
    
    public void setMenuBar(BeanTableFrame f){
        int x = 0;
        if (tabbedTableArray.size()==0)
                return;
        if (tabbedTableArray.size()!=1)
            x = ecosLocoTabs.getSelectedIndex();
        if (x==-1) x=0;
        if (tabbedTableArray.get(x).getAAClass()!=null)
            tabbedTableArray.get(x).getAAClass().setMenuBar(f);
    }
    
    public void addToBottomBox(JComponent c, String str){
        for(int x = 0; x<tabbedTableArray.size(); x++){
            if (tabbedTableArray.get(x).getClassAsString().equals(str))
                tabbedTableArray.get(x).addToBottomBox(c);
        }
    }
    
    public void print(javax.swing.JTable.PrintMode mode, java.text.MessageFormat headerFormat, java.text.MessageFormat footerFormat){
        try {
            tabbedTableArray.get(ecosLocoTabs.getSelectedIndex()).getDataTable().print(mode, headerFormat, footerFormat);
        } catch (java.awt.print.PrinterException e1) {
            log.warn("error printing: "+e1,e1);
        } catch ( NullPointerException ex) {
            log.error("Trying to print returned a NPE error");
        }
    }
    
    class tabbedTableItem {
        
        EcosLocoTableAction tableAction;
        String className;
        String itemText;
        BeanTableDataModel dataModel;
        JTable dataTable;
        JScrollPane dataScroll;
        Box bottomBox;
        Boolean AddToFrameRan = false;
        EcosSystemConnectionMemo adaptermemo;
        int bottomBoxIndex;	// index to insert extra stuff
        static final int bottomStrutWidth = 20;
        
        boolean standardModel = true;
        
        final JPanel dataPanelCont = new JPanel();
        
        tabbedTableItem(String aaClass, String choice, boolean stdModel, EcosSystemConnectionMemo memo){
            try{
                Class<?> cl = Class.forName("jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableAction");
                java.lang.reflect.Constructor<?> co = cl.getConstructor(new Class[] {String.class, EcosSystemConnectionMemo.class});
                tableAction = (EcosLocoTableAction) co.newInstance(choice, memo);
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
                log.error("This Exception " + e.toString());
                return;
            }
            
            className = aaClass;
            itemText = choice;
            standardModel=stdModel;
            adaptermemo = memo;
            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            dataPanelCont.setLayout(new BorderLayout());
            if (stdModel)
                createDataModel();
            else
                addPanelModel();
        }
        
        void createDataModel(){
            dataModel = tableAction.getTableDataModel();
            TableSorter sorter = new TableSorter(dataModel);
            dataTable = makeJTable(sorter);
            sorter.setTableHeader(dataTable.getTableHeader());
            dataScroll	= new JScrollPane(dataTable);
            try {
                TableSorter tmodel = ((TableSorter)dataTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(BeanTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);
            } catch (java.lang.ClassCastException e) { log.error(e.toString());}  // happens if not sortable table
            
            dataModel.configureTable(dataTable);
            java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
            // width is right, but if table is empty, it's not high
            // enough to reserve much space.
            dataTableSize.height = Math.max(dataTableSize.height, 400);
            dataScroll.getViewport().setPreferredSize(dataTableSize);
            
            // set preferred scrolling options
            dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            
            dataPanelCont.add(dataScroll, BorderLayout.CENTER);
            
            dataPanelCont.add(bottomBox, BorderLayout.SOUTH);
        }
        
        void addPanelModel(){
            dataPanelCont.add(tableAction.getPanel(), BorderLayout.CENTER);
            dataPanelCont.add(bottomBox, BorderLayout.SOUTH);
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
            //if the adapter isn't enabled then we just put a blank panel in.
            if (!adaptermemo.getDisabled())
                return dataPanelCont;
            else {
                JPanel noData = new JPanel();
                noData.add(new JLabel("No Active Connection for "+ adaptermemo.getUserName()));
                return noData;
            }
                
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
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoTableTabAction.class.getName());
}