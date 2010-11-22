// ListedTableFrame.java

package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.8 $
 */
public class ListedTableFrame extends BeanTableFrame {
    
    
    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    ResourceBundle rbs = ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public String getTitle() {
        if (list.getSelectedIndex()!=-1)
            return tabbedTableArray.get(list.getSelectedIndex()).getItemString(); 
        else
            return tabbedTableArray.get(0).getItemString(); 
    
    }
    public boolean isMultipleInstances() { return true; }  // only one of these!

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
    
    public ListedTableFrame() {
        super();

        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        
        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BorderLayout(5,0));
        buttonpanel.setLayout(new BoxLayout(buttonpanel,BoxLayout.Y_AXIS));
        
        buttonpanel.add(listScroller);
        detailpanel = new JPanel();
        detailpanel.setLayout(new CardLayout());
        if (!init){
            addTable("jmri.jmrit.beantable.TurnoutTableAction",  rbs.getString("MenuItemTurnoutTable"), true);
            addTable("jmri.jmrit.beantable.SensorTableAction", rbs.getString("MenuItemSensorTable"), true);
            addTable("jmri.jmrit.beantable.LightTableAction", rbs.getString("MenuItemLightTable"), true);
            addTable("jmri.jmrit.beantable.SignalHeadTableAction", rbs.getString("MenuItemSignalTable"), true);
            addTable("jmri.jmrit.beantable.SignalMastTableAction", rbs.getString("MenuItemSignalMastTable"), true);
            addTable("jmri.jmrit.beantable.SignalGroupTableAction", rbs.getString("MenuItemSignalGroupTable"), true);
            addTable("jmri.jmrit.beantable.ReporterTableAction", rbs.getString("MenuItemReporterTable"), true);
            addTable("jmri.jmrit.beantable.MemoryTableAction", rbs.getString("MenuItemMemoryTable"), true);
            addTable("jmri.jmrit.beantable.RouteTableAction", rbs.getString("MenuItemRouteTable"), true);
            addTable("jmri.jmrit.beantable.LRouteTableAction", rbs.getString("MenuItemLRouteTable"), true);
            addTable("jmri.jmrit.beantable.LogixTableAction", rbs.getString("MenuItemLogixTable"), true);
            addTable("jmri.jmrit.beantable.BlockTableAction", rbs.getString("MenuItemBlockTable"), true);
            addTable("jmri.jmrit.beantable.SectionTableAction", rbs.getString("MenuItemSectionTable"), true);
            addTable("jmri.jmrit.beantable.TransitTableAction", rbs.getString("MenuItemTransitTable"), true);
            addTable("jmri.jmrit.beantable.AudioTableAction",  rbs.getString("MenuItemAudioTable"), false);
            init=true;
        }
        tabbedTableArray = new ArrayList<tabbedTableItem>();
        for(int x=0; x<tabbedTableItemListArray.size(); x++){
            tabbedTableItemList item = tabbedTableItemListArray.get(x);
            tabbedTableItem itemModel = new tabbedTableItem(item.getClassAsString(), item.getItemString(), item.getStandardTableModel());
            itemBeingAdded = itemModel;
            tabbedTableArray.add(itemModel);
            detailpanel.add(itemModel.getPanel(), itemModel.getClassAsString());
            itemBeingAdded.getAAClass().addToFrame(this);
        }

        updateJList();
        buildMenus(tabbedTableArray.get(0));
        setTitle(tabbedTableArray.get(0).getItemString());

        cardHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           buttonpanel, detailpanel);
        cardHolder.setDividerSize(8);
        if (lastdivider!=0){
            cardHolder.setDividerLocation(lastdivider);
        }
        cardHolder.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("dividerLocation")){
                    lastdivider = (Integer) e.getNewValue();
                }
            }
        });

        cardHolder.setOneTouchExpandable(true);
        getContentPane().add(cardHolder);
        
        list.setSelectedIndex(0);
    }
    
    /* Method allows for the table to goto a specific list item */
    
    public void gotoListItem(String selection){
        for(int x=0; x<tabbedTableArray.size(); x++){
            if(tabbedTableArray.get(x).getClassAsString().equals(selection)){
                list.setSelectedIndex(x);
                selection(tabbedTableArray.get(x).getClassAsString());
                return;
            }
        }
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
    
    /*void updateJList(final JPanel detailPanel, JScrollPane listScroller, JPanel buttonpanel){
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
                selection(detailPanel, item.getClassAsString());
                Finalf.setTitle(item.getItemString());
                item.getAAClass().setFrame(Finalf);
                buildMenus(item);
            }
        });
        list.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                System.out.println(e.getPropertyName());
            }
        });
        buttonpanel.add(listScroller);    
    }*/

        void updateJList(){
        buttonpanel.remove(listScroller);
        if (list.getListSelectionListeners().length>0){
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        if (list.getMouseListeners().length>0){
            list.removeMouseListener(list.getMouseListeners()[0]);
        }
        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addMouseListener(new ActionJList(this));
        buttonpanel.add(listScroller);
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
    
    protected static ArrayList<String> getChoices() {
        ArrayList<String> choices = new ArrayList<String>();
        for(int x=0; x<tabbedTableItemListArray.size(); x++){
            choices.add(tabbedTableItemListArray.get(x).getItemString());
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
        
       boolean standardModel = true;
        
        final JPanel dataPanel = new JPanel();
        
        tabbedTableItem(String aaClass, String choice, boolean stdModel){
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
            standardModel=stdModel;
            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            //dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.setLayout(new BorderLayout());
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
            
            /*bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;*/
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
    class tabbedTableItemList {


        String className;
        String itemText;
        int bottomBoxIndex;	// index to insert extra stuff
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

    ActionJList actionList;

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
            clickDelay = ((Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval")).intValue();
            currentItemSelected=0;
        }

        int clickDelay=500;
        int currentItemSelected;
        int lastItemSelected;

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

        //MouseEvent mE;

        public void mouseClicked(MouseEvent e){
            //mE=e;
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
                    //int index = list.locationToIndex(mE.getPoint());
                    currentItemSelected = mouseItem;
                    //tabbedTableItem item = tabbedTableArray.get(mouseItem);
                    list.ensureIndexIsVisible(mouseItem);
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
            int x = frame.getX()+frame.getInsets().top;
            int y = frame.getY()+frame.getInsets().top;
            ListedTableAction tmp = new ListedTableAction(item.getItemString(), item.getClassAsString(), x, y);
            tmp.actionPerformed();
        }

        void selectListItem(int index){
            tabbedTableItem item = tabbedTableArray.get(index);
            selection(item.getClassAsString());
            frame.setTitle(item.getItemString());
            item.getAAClass().setFrame(frame);
            buildMenus(item);
            list.ensureIndexIsVisible(index);
            list.setSelectedIndex(index);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ListedTableFrame.class.getName());
}

