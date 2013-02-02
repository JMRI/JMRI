package jmri.jmrit.withrottle;

import org.apache.log4j.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.RouteManager;
import jmri.TurnoutManager;
import jmri.util.JmriJFrame;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */
public class ControllerFilterFrame extends JmriJFrame implements TableModelListener{

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");
    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");
    private static String[] COLUMN_NAMES = {rbx.getString("ColumnLabelSystemName"),
                                            rbx.getString("ColumnLabelUserName"),
                                            rbx.getString("ColumnLabelInclude")};
    
    public ControllerFilterFrame(){
        super("Controls Filter", true, true);
    }

    public void initComponents() throws Exception {
        JTabbedPane tabbedPane = new JTabbedPane();
        if (InstanceManager.turnoutManagerInstance()!=null) {
            
            tabbedPane.addTab(rb.getString("LabelTurnout"), null, addTurnoutPanel(),rb.getString("ToolTipTurnoutTab"));
        }
        
        if (InstanceManager.routeManagerInstance()!=null) {
            
            tabbedPane.addTab(rb.getString("LabelRoute"), null, addRoutePanel(),rb.getString("ToolTipRouteTab"));
        }
        
        add(tabbedPane);

        pack();

        addHelpMenu("package.jmri.jmrit.withrottle.UserInterface", true);
    }

    private JPanel addTurnoutPanel(){
        JPanel tPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(rb.getString("LabelTurnoutTab"), SwingConstants.CENTER);
        tPanel.add(label, BorderLayout.NORTH);
        tPanel.add(addCancelSavePanel(), BorderLayout.WEST);

        final TurnoutFilterModel filterModel = new TurnoutFilterModel();
        JTable table = new JTable(filterModel);
        buildTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane,BorderLayout.CENTER);

        tPanel.add(getIncludeButtonsPanel(filterModel), BorderLayout.SOUTH);

        return tPanel;
    }
    
    private JPanel addRoutePanel(){
        JPanel tPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(rb.getString("LabelRouteTab"), SwingConstants.CENTER);
        tPanel.add(label, BorderLayout.NORTH);
        tPanel.add(addCancelSavePanel(), BorderLayout.WEST);

        final RouteFilterModel filterModel = new RouteFilterModel();
        JTable table = new JTable(filterModel);
        buildTable(table);
        

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane,BorderLayout.CENTER);

        tPanel.add(getIncludeButtonsPanel(filterModel), BorderLayout.SOUTH);

        return tPanel;
    }

    private void buildTable(JTable table){
        table.getModel().addTableModelListener(this);

        table.setRowSelectionAllowed(false);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(580,240));

        //table.getTableHeader().setBackground(Color.lightGray);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.gray);
        table.setRowHeight(30);

        TableColumnModel columnModel = table.getColumnModel();

        TableColumn include = columnModel.getColumn(AbstractFilterModel.INCLUDECOL);
        include.setResizable(false);
        include.setMinWidth(60);
        include.setMaxWidth(70);

        TableColumn sName = columnModel.getColumn(AbstractFilterModel.SNAMECOL);
        sName.setResizable(true);
        sName.setMinWidth(80);
        sName.setPreferredWidth(80);
        sName.setMaxWidth(340);

        TableColumn uName = columnModel.getColumn(AbstractFilterModel.UNAMECOL);
        uName.setResizable(true);
        uName.setMinWidth(180);
        uName.setPreferredWidth(300);
        uName.setMaxWidth(440);
    }

    private JPanel getIncludeButtonsPanel(final AbstractFilterModel fm){
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(Box.createHorizontalGlue());

        JButton selectAllButton = new JButton(rb.getString("ButtonSelectAll"));
        selectAllButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                fm.setIncludeColToValue(true);
            }
        });
        pane.add(selectAllButton);

        JButton deselectAllButton = new JButton(rb.getString("ButtonDeselectAll"));
        deselectAllButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                fm.setIncludeColToValue(false);
            }
        });
        pane.add(deselectAllButton);

        JButton selectUserNamedButton = new JButton(rb.getString("ButtonSelectByUserName"));
        selectUserNamedButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                fm.SetIncludeToUserNamed();
            }
        });
        pane.add(selectUserNamedButton);

        return pane;
    }

    private JPanel addCancelSavePanel(){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalGlue());

        JButton cancelButton = new JButton(rb.getString("ButtonCancel"));
        cancelButton.setAlignmentX(CENTER_ALIGNMENT);
        cancelButton.setToolTipText(rb.getString("ToolTipCancel"));
        cancelButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                dispose();
            }
        });
        p.add(cancelButton);

        JButton saveButton = new JButton(rb.getString("ButtonSave"));
        saveButton.setAlignmentX(CENTER_ALIGNMENT);
        saveButton.setToolTipText(rb.getString("ToolTipSave"));
        saveButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                storeValues();
                dispose();
            }
        });
        p.add(saveButton);

        return p;
    }

    protected void storeValues() {
        new jmri.configurexml.StoreXmlUserAction().actionPerformed(null);
    }

    public void tableChanged(TableModelEvent e) {
        if (log.isDebugEnabled()) log.debug("Set mod flag true for: "+getTitle());
        this.setModifiedFlag(true);
    }

    
    
    public abstract class AbstractFilterModel extends AbstractTableModel implements PropertyChangeListener{
        
        List<String> sysNameList= null;
        boolean isDirty;

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDECOL) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                fireTableDataChanged();
            }
        }
        
        public void dispose() {
            InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
            InstanceManager.routeManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

        public int getColumnCount () {
            return 3;
        }

        public int getRowCount () {
            return sysNameList.size();
        }
        
        public boolean isCellEditable(int r,int c) {
            return (c==INCLUDECOL);
        }

        abstract void setIncludeColToValue(boolean value);

        abstract void SetIncludeToUserNamed();
        
        public static final int SNAMECOL = 0;
        public static final int UNAMECOL = 1;
        public static final int INCLUDECOL = 2;
    }

    class TurnoutFilterModel extends AbstractFilterModel{

        TurnoutManager mgr = InstanceManager.turnoutManagerInstance();
        
        TurnoutFilterModel() {
            
            sysNameList = mgr.getSystemNameList();
            mgr.addPropertyChangeListener(this);
        }

        public Object getValueAt (int r,int c) {
            
            // some error checking
            if (r >= sysNameList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case INCLUDECOL:
                    Object o = mgr.getBySystemName(sysNameList.get(r)).getProperty("WifiControllable");
                    if ((o != null) && (o.toString().equalsIgnoreCase("false"))){
                        return Boolean.valueOf(false);
                    }
                    return Boolean.valueOf(true);
                case SNAMECOL:
                    return sysNameList.get(r);
                case UNAMECOL:
                    return mgr.getBySystemName(sysNameList.get(r)).getUserName();
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object type,int r,int c) {
            
            switch (c) {
                case INCLUDECOL:
                    mgr.getBySystemName(sysNameList.get(r)).setProperty("WifiControllable", ((Boolean)type).booleanValue());
                    if (!isDirty){
                        this.fireTableChanged(new TableModelEvent(this));
                        isDirty = true;
                    }
                    break;
            }
        }

        public void setIncludeColToValue(boolean value){
            for (String sysName : sysNameList){
                mgr.getBySystemName(sysName).setProperty("WifiControllable", value);
            }
            fireTableDataChanged();
        }

        public void SetIncludeToUserNamed(){
            for (String sysName : sysNameList){
                NamedBean bean = mgr.getBySystemName(sysName);
                if ((bean.getUserName() != null) && (bean.getUserName().length() > 0)){
                    bean.setProperty("WifiControllable", true);
                }else {
                    bean.setProperty("WifiControllable", false);
                }
            }
            fireTableDataChanged();
        }
    }

    class RouteFilterModel extends AbstractFilterModel{

        RouteManager mgr = InstanceManager.routeManagerInstance();

        RouteFilterModel() {

            sysNameList = mgr.getSystemNameList();
            mgr.addPropertyChangeListener(this);
        }

        public Object getValueAt (int r,int c) {

            // some error checking
            if (r >= sysNameList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case INCLUDECOL:
                    Object o = mgr.getBySystemName(sysNameList.get(r)).getProperty("WifiControllable");
                    if ((o != null) && (o.toString().equalsIgnoreCase("false"))){
                        return Boolean.valueOf(false);
                    }
                    return Boolean.valueOf(true);
                case SNAMECOL:
                    return sysNameList.get(r);
                case UNAMECOL:
                    return mgr.getBySystemName(sysNameList.get(r)).getUserName();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {

            switch (c) {
                case INCLUDECOL:
                    mgr.getBySystemName(sysNameList.get(r)).setProperty("WifiControllable", ((Boolean)type).booleanValue());
                    if (!isDirty){
                        this.fireTableChanged(new TableModelEvent(this));
                        isDirty = true;
                    }
                    break;
            }
        }

        public void setIncludeColToValue(boolean value){
            for (String sysName : sysNameList){
                mgr.getBySystemName(sysName).setProperty("WifiControllable", value);
            }
            fireTableDataChanged();
        }

        public void SetIncludeToUserNamed(){
            for (String sysName : sysNameList){
                NamedBean bean = mgr.getBySystemName(sysName);
                if (bean.getUserName().length() > 0){
                    bean.setProperty("WifiControllable", true);
                }else {
                    bean.setProperty("WifiControllable", false);
                }
            }
            fireTableDataChanged();
        }
    }

    static Logger log = Logger.getLogger(ControllerFilterFrame.class.getName());

}
