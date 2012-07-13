// ListedTableFrame.java

package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Vector;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;
import javax.swing.table.TableColumn;

import javax.swing.*;

/**
 * Provide access to the various tables via a 
 * listed pane.
 * Based upon the apps.gui3.TabbedPreferences.java by Bob Jacoben
 * <P>
 * @author	Kevin Dickerson   Copyright 2010
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class ListedTableFrame extends BeanTableFrame {
    
    
    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    ResourceBundle rbs = ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    ActionJList actionList;
    
    public boolean isMultipleInstances() { return true; }

    static ArrayList<tabbedTableItemList> tabbedTableItemListArray = new ArrayList<tabbedTableItemList>();
    ArrayList<tabbedTableItem> tabbedTableArray = new ArrayList<tabbedTableItem>();

    final UserPreferencesManager pref = InstanceManager.getDefault(UserPreferencesManager.class);
    JSplitPane cardHolder;
    JList list;
    JScrollPane listScroller;
    JPanel buttonpanel;
    JPanel detailpanel;
    static boolean init = false;
    tabbedTableItem itemBeingAdded = null;
    static int lastdivider;
    

    public ListedTableFrame(){
        this(rbean.getString("TitleListedTable"));
    }
    
    public ListedTableFrame(String s) {
        super(s);
        if (jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class)==null){
            //We add this to the instanceManager so that other components can add to the table
            jmri.InstanceManager.store(this, jmri.jmrit.beantable.ListedTableFrame.class);
        }
        if (!init){
            /*Add the default tables to the static list array, this should only be done
            once when first loaded*/
            addTable("jmri.jmrit.beantable.TurnoutTableTabAction", rbs.getString("MenuItemTurnoutTable"), false);
            addTable("jmri.jmrit.beantable.SensorTableTabAction", rbs.getString("MenuItemSensorTable"), false);
            addTable("jmri.jmrit.beantable.LightTableTabAction", rbs.getString("MenuItemLightTable"), false);
            addTable("jmri.jmrit.beantable.SignalHeadTableAction", rbs.getString("MenuItemSignalTable"), true);
            addTable("jmri.jmrit.beantable.SignalMastTableAction", rbs.getString("MenuItemSignalMastTable"), true);
            addTable("jmri.jmrit.beantable.SignalGroupTableAction", rbs.getString("MenuItemSignalGroupTable"), true);
            addTable("jmri.jmrit.beantable.SignalMastLogicTableAction",  rbs.getString("MenuItemSignalMastLogicTable"), true);
            addTable("jmri.jmrit.beantable.ReporterTableAction", rbs.getString("MenuItemReporterTable"), true);
            addTable("jmri.jmrit.beantable.MemoryTableAction", rbs.getString("MenuItemMemoryTable"), true);
            addTable("jmri.jmrit.beantable.RouteTableAction", rbs.getString("MenuItemRouteTable"), true);
            addTable("jmri.jmrit.beantable.LRouteTableAction", rbs.getString("MenuItemLRouteTable"), true);
            addTable("jmri.jmrit.beantable.LogixTableAction", rbs.getString("MenuItemLogixTable"), true);
            addTable("jmri.jmrit.beantable.BlockTableAction", rbs.getString("MenuItemBlockTable"), true);
            addTable("jmri.jmrit.beantable.SectionTableAction", rbs.getString("MenuItemSectionTable"), true);
            addTable("jmri.jmrit.beantable.TransitTableAction", rbs.getString("MenuItemTransitTable"), true);
            addTable("jmri.jmrit.beantable.AudioTableAction",  rbs.getString("MenuItemAudioTable"), false);
            addTable("jmri.jmrit.beantable.IdTagTableAction", rbs.getString("MenuItemIdTagTable"), true);
            init=true;
        }
    }
    
    public void initComponents(){
        actionList = new ActionJList(this);
        
        detailpanel = new JPanel();
        detailpanel.setLayout(new CardLayout());
        tabbedTableArray = new ArrayList<tabbedTableItem>(tabbedTableItemListArray.size());
        ArrayList<tabbedTableItemList> removeItem = new ArrayList<tabbedTableItemList>(5);
        for(int x=0; x<tabbedTableItemListArray.size(); x++){
            /* Here we add all the tables into the panel*/
            tabbedTableItemList item = tabbedTableItemListArray.get(x);
            try {
                tabbedTableItem itemModel = new tabbedTableItem(item.getClassAsString(), item.getItemString(), item.getStandardTableModel());
                itemBeingAdded = itemModel;
                detailpanel.add(itemModel.getPanel(), itemModel.getClassAsString());
                tabbedTableArray.add(itemModel);
                itemBeingAdded.getAAClass().addToFrame(this);
            } catch (Exception ex){
                detailpanel.add(errorPanel(item.getItemString()), item.getClassAsString());
                log.error("Error when adding " + item.getClassAsString() + " to display\n" + ex);
                ex.printStackTrace();
                removeItem.add(item);
            }
        }
        
        for(tabbedTableItemList dead : removeItem){
            tabbedTableItemListArray.remove(dead);
        }
        
        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addMouseListener(actionList);
        
        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BorderLayout(5,0));
        buttonpanel.setLayout(new BoxLayout(buttonpanel,BoxLayout.Y_AXIS));
        buttonpanel.add(listScroller);
        
        buildMenus(tabbedTableArray.get(0));
        setTitle(tabbedTableArray.get(0).getItemString());
        
        cardHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           buttonpanel, detailpanel);
        
        cardHolder.setDividerSize(8);
        if (lastdivider!=0){
            cardHolder.setDividerLocation(lastdivider);
        } else { //Else if no specific size has been given we set it to the lists preferred width
            cardHolder.setDividerLocation(listScroller.getPreferredSize().width);
        }
        cardHolder.addPropertyChangeListener(new PropertyChangeListener(){
                @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification="We only intend to use/save the last position of the Split frame")
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("dividerLocation")){
                    lastdivider = (Integer) e.getNewValue();
                }
            }
        });
        
        cardHolder.setOneTouchExpandable(true);
        getContentPane().add(cardHolder);
        pack();
        actionList.selectListItem(0);
    
    }
    
    JPanel errorPanel(String text){
        JPanel error = new JPanel();
        error.add(new JLabel(rbean.getString("ErrorAddingTable") + " " + text), BorderLayout.CENTER);
        return error;
    }
    
    /* Method allows for the table to goto a specific list item */
    
    public void gotoListItem(String selection){
        for(int x=0; x<tabbedTableArray.size(); x++){
            try {
                if(tabbedTableArray.get(x).getClassAsString().equals(selection)){
                    actionList.selectListItem(x);
                    return;
                }
            } catch (Exception ex){
                log.error("An error occured in the goto list for " + selection);
            }
        }
    }
    
    public void addTable(String aaClass, String choice, boolean stdModel){
        tabbedTableItemList itemBeingAdded = null;
        for(int x=0; x<tabbedTableItemListArray.size(); x++){
            if(tabbedTableItemListArray.get(x).getClassAsString().equals(aaClass)){
                log.info("Class " + aaClass + " is already added");
                itemBeingAdded=tabbedTableItemListArray.get(x);
                break;
            }
        }
        if (itemBeingAdded==null){
            itemBeingAdded = new tabbedTableItemList(aaClass, choice, stdModel);
            tabbedTableItemListArray.add(itemBeingAdded);
        }
    }
    
    public void dispose(){
        for(int x=0; x<tabbedTableArray.size(); x++){
            tabbedTableArray.get(x).dispose();
        }
        if (list.getListSelectionListeners().length>0){
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        super.dispose();
    }
    
    void buildMenus(final tabbedTableItem item){
        JMenuBar menuBar = new JMenuBar();
        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        
        JMenuItem newItem = new JMenuItem("New Window");
        fileMenu.add(newItem);
        newItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionList.openNewTableWindow(list.getSelectedIndex());
            }
        });

        fileMenu.add(new jmri.configurexml.SaveMenu());

        JMenuItem printItem = new JMenuItem(rb.getString("PrintTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                        MessageFormat footerFormat = new MessageFormat(getTitle()+" page {0,number}");
                        if (item.getStandardTableModel())
                            item.getDataTable().print(JTable.PrintMode.FIT_WIDTH , null, footerFormat);
                        else
                            item.getAAClass().print(JTable.PrintMode.FIT_WIDTH , null, footerFormat);
                    } catch (java.awt.print.PrinterException e1) {
                        log.warn("error printing: "+e1,e1);
                    } catch ( NullPointerException ex) {
                        log.error("Trying to print returned a NPE error");
                    }
                }
        });
        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        for(int i=0; i<tabbedTableItemListArray.size(); i++){
            final tabbedTableItemList itemList = tabbedTableItemListArray.get(i);
            JMenuItem viewItem = new JMenuItem(itemList.getItemString());
            viewMenu.add(viewItem);
            viewItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    gotoListItem(itemList.getClassAsString());
                }
            });
        }
        
        
        this.setJMenuBar(menuBar);
        try {
            item.getAAClass().setMenuBar(this);
            this.addHelpMenu(item.getAAClass().helpTarget(),true);
        } catch (Exception ex){
            log.error("Error when trying to set menu bar for " + item.getClassAsString()+"\n"+ex);
        }
        this.validate();
    }
    
    tabbedTableItem lastSelectedItem = null;
    
    /* This is a bit of a bodge to add the contents to the bottom box and keep
     * it backwardly compatable with the original views, if the original views
     * are depreciated then this can be re-written
     */
    //@TODO Sort out the procedure to add to bottom box
    protected void addToBottomBox(Component comp, String c) {
        for(int x=0; x<tabbedTableArray.size(); x++){
            if(tabbedTableArray.get(x).getClassAsString().equals(c)){
                tabbedTableArray.get(x).addToBottomBox(comp);
                return;
            }
        }
    }
    
    protected static ArrayList<String> getChoices() {
        ArrayList<String> choices = new ArrayList<String>();
        for(int x=0; x<tabbedTableItemListArray.size(); x++){
            choices.add(tabbedTableItemListArray.get(x).getItemString());
        }
        return choices;
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification="We only intend to use/save the last position of the Split frame")
    public void setDividerLocation(int loc){
        if (loc==0)
            return;
        cardHolder.setDividerLocation(loc);
        lastdivider=loc;
    }
     
    public int getDividerLocation(){
        return lastdivider;
    }
    
    static class tabbedTableItem {
        
        AbstractTableAction tableAction;
        String className;
        String itemText;
        BeanTableDataModel dataModel;
        JTable dataTable;
        JScrollPane dataScroll;
        Box bottomBox;
        int bottomBoxIndex;	// index to insert extra stuff
        static final int bottomStrutWidth = 20;
        
        boolean standardModel = true;
        
        final JPanel dataPanel = new JPanel();
        
        tabbedTableItem(String aaClass, String choice, boolean stdModel){
            className = aaClass;
            itemText = choice;
            standardModel=stdModel;
            
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            
            try{
                Class<?> cl = Class.forName(aaClass);
                java.lang.reflect.Constructor<?> co = cl.getConstructor(new Class[] {String.class});
                tableAction = (AbstractTableAction) co.newInstance(choice);
            } catch (ClassNotFoundException e1) {
                log.error("Not a valid class : " + aaClass);
                return;
            } catch (NoSuchMethodException e2) {
                log.error("Not such method : " + aaClass);
                return;
            } catch (InstantiationException e3) {
                log.error("Not a valid class : " + aaClass);
                return;
            } catch (ClassCastException e4){
                log.error("Not part of the abstractTableActions : " + aaClass);
                return;
            } catch (Exception e) {
                log.error("Exception " + e.toString());
                return;
            }
            
            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            dataPanel.setLayout(new BorderLayout());
            if (stdModel)
                createDataModel();
            else
                addPanelModel();
        }
        
        void createDataModel(){
            dataModel = tableAction.getTableDataModel();
            TableSorter sorter = new TableSorter(dataModel);
            dataTable = dataModel.makeJTable(sorter);
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

            dataPanel.add(dataScroll, BorderLayout.CENTER);
            
            dataPanel.add(bottomBox, BorderLayout.SOUTH);
            if(tableAction.includeAddButton()){
                JButton addButton = new JButton(rbean.getString("ButtonAdd"));
                addToBottomBox(addButton);
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        tableAction.addPressed(e);
                    }
                });
            }
            dataModel.loadTableColumnDetails(dataTable);
        }
        
        void addPanelModel(){
            try {
                dataPanel.add(tableAction.getPanel(), BorderLayout.CENTER);
                dataPanel.add(bottomBox, BorderLayout.SOUTH);
           } catch ( NullPointerException e) {
                log.error("An error occured while trying to create the table for " + itemText + " " + e.toString());
                e.printStackTrace();
           }
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
            if (dataModel != null){
                dataModel.saveTableColumnDetails(dataTable);
                dataModel.dispose();
            }
            if (tableAction!=null)
                tableAction.dispose();
            dataModel = null;
            dataTable = null;
            dataScroll = null;
        }
    }
    static class tabbedTableItemList {

        String className;
        String itemText;
        boolean standardModel = true;
        
        tabbedTableItemList(String aaClass, String choice, boolean stdModel){
            className = aaClass;
            itemText = choice;
            standardModel=stdModel;
        }

        boolean getStandardTableModel(){ return standardModel; }

        String getClassAsString(){
            return className;
        }

        String getItemString(){
            return itemText;
        }

    }
    /** ActionJList 
     * This deals with handling non-default mouse operations on the List panel
     * and allows for right click popups and double click to open new windows of
     * over the items we are hovering over.
     */
    class ActionJList extends MouseAdapter{

        JPopupMenu popUp;
        JMenuItem menuItem;

        protected BeanTableFrame frame;
        ActionJList(BeanTableFrame f){
            frame = f;
            popUp = new JPopupMenu();
            menuItem = new JMenuItem("Open in New Window");
            popUp.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openNewTableWindow(mouseItem);
                }
            });
            try {
                clickDelay = ((Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval")).intValue();
            } catch(Exception e){
                try {
                    clickDelay = ((Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt_multiclick_time")).intValue();
                } catch (Exception ex){
                    clickDelay = 500;
                    log.error("Unable to get the double click speed, Using JMRI default of half a second" + e.toString());
                }
            }
            currentItemSelected=0;
        }

        int clickDelay=500;
        int currentItemSelected=-1;

        public void mousePressed(MouseEvent e){
            if (e.isPopupTrigger()){
                showPopUp(e);
            }
        }
        public void mouseReleased(MouseEvent e){
            if (e.isPopupTrigger()){
                showPopUp(e);
            }
        }

        javax.swing.Timer clickTimer = null;

        //Records the item index that the mouse is currenlty over
        int mouseItem;

        void showPopUp(MouseEvent e){
            popUp.show(e.getComponent(), e.getX(), e.getY());
            mouseItem = list.locationToIndex(e.getPoint());
        }

        void setCurrentItem(int current){
            currentItemSelected = current;
        }

        public void mouseClicked(MouseEvent e){
            
            mouseItem = list.locationToIndex(e.getPoint());
            if (popUp.isVisible())
                return;
            if (e.isPopupTrigger()){
                showPopUp(e);
                return;
            }
            if (clickTimer==null){
                clickTimer=new Timer(clickDelay, new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    selectListItem(mouseItem);
                }
                });
                clickTimer.setRepeats(false);
            }
            if (e.getClickCount()==1){
                clickTimer.start();
            }
            else if(e.getClickCount() == 2){
                clickTimer.stop();
                openNewTableWindow(mouseItem);
                list.setSelectedIndex(currentItemSelected);
            }
        }

        void openNewTableWindow(int index){
            tabbedTableItem item = tabbedTableArray.get(index);
            class WindowMaker implements Runnable {
                tabbedTableItem item;
                WindowMaker(tabbedTableItem tItem){
                    item = tItem;
                }
                public void run() {
                    ListedTableAction tmp = new ListedTableAction(item.getItemString(), item.getClassAsString(),cardHolder.getDividerLocation());
                    tmp.actionPerformed();
                }
            }
            WindowMaker t = new WindowMaker(item);
            javax.swing.SwingUtilities.invokeLater(t);
        }

        void selectListItem(int index){
            currentItemSelected = index;
            tabbedTableItem item = tabbedTableArray.get(index);
            CardLayout cl = (CardLayout) (detailpanel.getLayout());
            cl.show(detailpanel, item.getClassAsString());
            frame.setTitle(item.getItemString());
            frame.generateWindowRef();
            try {
                item.getAAClass().setFrame(frame);
                buildMenus(item);
            } catch (Exception ex){
                log.error(ex);
            }
            list.ensureIndexIsVisible(index);
            list.setSelectedIndex(index);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ListedTableFrame.class.getName());
}

