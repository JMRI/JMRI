// ListedTableFrame.java

package jmri.jmrit.beantable;

import jmri.UserPreferencesManager;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.awt.*;

import javax.swing.*;

import jmri.InstanceManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Vector;

import jmri.util.com.sun.TableSorter;


/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.3 $
 */
public class ListedTableFrame extends BeanTableFrame {
    
    
    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    ResourceBundle rbs = ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public String getTitle() { 
        return tabbedTableArray.get(list.getSelectedIndex()).getItemString(); 
    
    }
    public boolean isMultipleInstances() { return true; }  // only one of these!

    static ArrayList<tabbedTableItem> tabbedTableArray = new ArrayList<tabbedTableItem>();

    JPanel detailpanel = new JPanel();

    final UserPreferencesManager pref = InstanceManager.getDefault(UserPreferencesManager.class);
    JPanel cardHolder;
    JList list;
    JScrollPane listScroller;
    JPanel buttonpanel;
    
    public ListedTableFrame() {
        super();
        
        cardHolder = new JPanel();
        pref.disallowSave();
        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));
        
        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel,BoxLayout.Y_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));
        buttonpanel.add(listScroller);

        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));
        
        addTable("jmri.jmrit.beantable.TurnoutTableAction",  rbs.getString("MenuItemTurnoutTable"));
        addTable("jmri.jmrit.beantable.SensorTableAction", rbs.getString("MenuItemSensorTable"));
        addTable("jmri.jmrit.beantable.LightTableAction", rbs.getString("MenuItemLightTable"));
        addTable("jmri.jmrit.beantable.SignalHeadTableAction", rbs.getString("MenuItemSignalTable"));
        addTable("jmri.jmrit.beantable.SignalMastTableAction", rbs.getString("MenuItemSignalMastTable"));
        addTable("jmri.jmrit.beantable.SignalGroupTableAction", rbs.getString("MenuItemSignalGroupTable"));
        addTable("jmri.jmrit.beantable.ReporterTableAction", rbs.getString("MenuItemReporterTable"));
        addTable("jmri.jmrit.beantable.MemoryTableAction", rbs.getString("MenuItemMemoryTable"));
        addTable("jmri.jmrit.beantable.RouteTableAction", rbs.getString("MenuItemRouteTable"));
        addTable("jmri.jmrit.beantable.LRouteTableAction", rbs.getString("MenuItemLRouteTable"));
        addTable("jmri.jmrit.beantable.LogixTableAction", rbs.getString("MenuItemLogixTable"));
        addTable("jmri.jmrit.beantable.BlockTableAction", rbs.getString("MenuItemBlockTable"));
        addTable("jmri.jmrit.beantable.SectionTableAction", rbs.getString("MenuItemSectionTable"));
        addTable("jmri.jmrit.beantable.TransitTableAction", rbs.getString("MenuItemTransitTable"));

        cardHolder.setLayout(new BoxLayout(cardHolder, BoxLayout.X_AXIS));
        
        buildMenus(tabbedTableArray.get(0));
        setTitle(tabbedTableArray.get(0).getItemString());
        
        cardHolder.add(buttonpanel);
        cardHolder.add(new JSeparator(JSeparator.VERTICAL));
        cardHolder.add(detailpanel);
        getContentPane().add(cardHolder);
        
        list.setSelectedIndex(0);

    }
    
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
    
    void selection(String View){
        CardLayout cl = (CardLayout) (detailpanel.getLayout());
        cl.show(detailpanel, View);
    }
    
    tabbedTableItem itemBeingAdded;
    
    public void addTable(String aaClass, String choice){
        itemBeingAdded =null;
        for(int x=0; x<tabbedTableArray.size(); x++){
            if(tabbedTableArray.get(x).getClassAsString().equals(aaClass)){
                log.info("Class " + aaClass + " is already added");
                itemBeingAdded=tabbedTableArray.get(x);
                break;
            }
        }
        
        if (itemBeingAdded==null){
            itemBeingAdded = new tabbedTableItem(aaClass, choice);
            itemBeingAdded.getAAClass().addToFrame(this);
            tabbedTableArray.add(itemBeingAdded);
        }
        updateJList();
        detailpanel.add(itemBeingAdded.getPanel(), aaClass);
    
    }
    
    void updateJList(){
        buttonpanel.remove(listScroller);
        if (list.getListSelectionListeners().length>0){
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));
        
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        final BeanTableFrame Finalf = this;
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                tabbedTableItem item = tabbedTableArray.get(list.getSelectedIndex());
                selection(item.getClassAsString());
                //item.getAAClass().setMenuBar(Finalf);
                Finalf.setTitle(item.getItemString());
                buildMenus(item);
            }
        });
        buttonpanel.add(listScroller);    
    }
    
    public void dispose(){
        for(int x=0; x<tabbedTableArray.size(); x++){
            tabbedTableArray.get(x).dispose();
        }
        super.dispose();
    }
    
    void buildMenus(final tabbedTableItem item){
        JMenuBar menuBar = new JMenuBar();
        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.SaveMenu());
        
        JMenuItem printItem = new JMenuItem(rb.getString("PrintTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                        MessageFormat footerFormat = new MessageFormat(getTitle()+" page {0,number}");
                        item.getDataTable().print(JTable.PrintMode.FIT_WIDTH , null, footerFormat);
                    } catch (java.awt.print.PrinterException e1) {
                        log.warn("error printing: "+e1,e1);
                    }
                }
        });

        this.setJMenuBar(menuBar);
        item.getAAClass().setMenuBar(this);
        this.addHelpMenu(item.getAAClass().helpTarget(),true);
        this.validate();
    }
    
    tabbedTableItem lastSelectedItem = null;
    
    protected void addToBottomBox(Component comp) {
        if (lastSelectedItem!=null && itemBeingAdded!=lastSelectedItem){
            lastSelectedItem.setAddToFrameRan();
        }
        lastSelectedItem=itemBeingAdded;
        if (itemBeingAdded.getAdditionsToFrameDone())
            return;
        itemBeingAdded.addToBottomBox(comp);
    }
    
    protected ArrayList<String> getChoices() {
        ArrayList<String> choices = new ArrayList<String>();
        for(int x=0; x<tabbedTableArray.size(); x++){
            choices.add(tabbedTableArray.get(x).getItemString());
        }
        return choices;
    }
    
    class tabbedTableItem {
        
        AbstractTableAction tableAction;
        String className;
        String itemText;
        BeanTableDataModel dataModel;
        JTable dataTable;
        JScrollPane dataScroll;
        Box bottomBox;
        Boolean AddToFrameRan = false;
        int bottomBoxIndex;	// index to insert extra stuff
        static final int bottomStrutWidth = 20;
        
        final JPanel dataPanel = new JPanel();
        
        tabbedTableItem(String aaClass, String choice){
            try{
                Class<?> cl = Class.forName(aaClass);
                java.lang.reflect.Constructor<?> co = cl.getConstructor(new Class[] {String.class});
                tableAction = (AbstractTableAction) co.newInstance(choice);
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
            
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.add(dataScroll);
            
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            dataPanel.add(bottomBox);
            JButton addButton = new JButton(rbean.getString("ButtonAdd"));
            addToBottomBox(addButton);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableAction.addPressed(e);
                }
            });
        }
        
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
        
        protected void addToBottomBox(Component comp) {
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
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ListedTableFrame.class.getName());
}
