package jmri.jmrit.withrottle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.InstanceManager;
import jmri.RouteManager;
import jmri.TurnoutManager;
import jmri.util.JmriJFrame;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.6 $
 */
public class ControllerFilterFrame extends JmriJFrame implements TableModelListener{

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");
    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");
    private static String[] COLUMN_NAMES = {rbx.getString("ColumnLabelSystemName"),
                                            rbx.getString("ColumnLabelUserName"),
                                            rbx.getString("ColumnLabelInclude")};
    
    public ControllerFilterFrame(){
        super("Controls Filter");
    }

    public void initComponents() throws Exception {
        JTabbedPane tabbedPane = new JTabbedPane();
        if (InstanceManager.turnoutManagerInstance()!=null) {
            
            tabbedPane.addTab(rb.getString("LabelTurnout"), null, addTurnoutPanel(),"Limit the turnouts controllable by WiFi devices.");
        }
        
        if (InstanceManager.routeManagerInstance()!=null) {
            
            tabbedPane.addTab(rb.getString("LabelRoute"), null, addRoutePanel(),"Limit the routes controllable by WiFi devices.");
        }
        
        add(tabbedPane);

        pack();

        addHelpMenu("package.jmri.jmrit.withrottle.UserInterface", true);
    }

    private JPanel addTurnoutPanel(){
        JPanel tPanel = new JPanel();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Please select "));
        p.add(new JLabel("Turnouts to "));
        p.add(new JLabel("be controlled "));
        p.add(new JLabel("by WiFi devices."));
        p.add(new JLabel("\n"));
        p.add(new JLabel("Save changes "));
        p.add(new JLabel("to panel file."));
        JButton saveButton = new JButton(rb.getString("ButtonSave"));
        saveButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                storeValues();
                dispose();
            }
        });
        p.add(saveButton);
        tPanel.add(p);
        
        JTable table = new JTable(new TurnoutFilterModel());
        buildTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane,BorderLayout.CENTER);

        return tPanel;
    }
    
    private JPanel addRoutePanel(){
        JPanel tPanel = new JPanel();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Please select "));
        p.add(new JLabel("Routes to "));
        p.add(new JLabel("be controlled "));
        p.add(new JLabel("by WiFi devices."));
        p.add(new JLabel("\n"));
        p.add(new JLabel("Save changes "));
        p.add(new JLabel("to panel file."));
        JButton saveButton = new JButton(rb.getString("ButtonSave"));
        saveButton.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                storeValues();
                dispose();
            }
        });
        p.add(saveButton);
        tPanel.add(p);

        JTable table = new JTable(new RouteFilterModel());
        buildTable(table);
        

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane,BorderLayout.CENTER);

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

    protected void storeValues() {
        new jmri.configurexml.StoreXmlUserAction().actionPerformed(null);
    }

    public void tableChanged(TableModelEvent e) {
        log.debug("Set mod flag true for: "+getTitle());
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
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControllerFilterFrame.class.getName());

}
