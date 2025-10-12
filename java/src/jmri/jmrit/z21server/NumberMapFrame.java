package jmri.jmrit.z21server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.util.JmriJFrame;

/**
 * This class handles the turnout number mapping window.
 * It contains multiple tabs, one for each supported component type (turnout, light, etc...)
 * 
 * Each tab display all components of that type with a column containing a Z21 turnout number,
 * which can be edited by the user.
 * 
 * @author Eckart Meyer Copyright (C) 2025
 * 
 * Inspired from jmri.jmrit.withrottle.ControllerFilterFrame.
 */
public class NumberMapFrame extends JmriJFrame implements TableModelListener {

    private static final String[] COLUMN_NAMES = {
        Bundle.getMessage("ColumnSystemName"), //from jmri.jmrit.Bundle
        Bundle.getMessage("ColumnUserName"), //from jmri.jmrit.Bundle
        Bundle.getMessage("ColumnTurnoutNumber")};

    private final List<JTable> tablelList = new ArrayList<>(); //one JTable for each component type

/**
 * Constructor.
 * Set the windows title.
 */
    public NumberMapFrame() {
        super(Bundle.getMessage("TitleNumberMapFrame"), true, true);
    }

/**
 * Build the frame.
 * Add a tab for each JMRI component types (turnout, light, etc.)
 */
    @Override
    public void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // NOTE: This list should match the classes used in TurnoutNumberMapHandler.java
        addTab(Turnout.class, TurnoutManager.class, "Turnouts", "ToolTipTurnoutTab", "LabelTurnoutTab", tabbedPane);
        addTab(Route.class, RouteManager.class, "Routes", "ToolTipRouteTab", "LabelRouteTab", tabbedPane);
        addTab(Light.class, LightManager.class, "Lights", "ToolTipLightTab", "LabelLightTab", tabbedPane);
        addTab(SignalMast.class, SignalMastManager.class, "SignalMasts", "ToolTipSignalMastTab", "LabelSignalMastTab", tabbedPane);
        addTab(SignalHead.class, SignalHeadManager.class, "SignalHeads", "ToolTipSignalHeadTab", "LabelSignalHeadTab", tabbedPane);
        addTab(Sensor.class, SensorManager.class, "Sensors", "ToolTipSensorTab", "LabelSensorTab", tabbedPane);

        add(tabbedPane);

        pack();

        addHelpMenu("package.jmri.jmrit.z21server.z21server", true);
    }
    
/**
 * Build a tab for a given component type.
 * 
 * @param <T>
 * @param type - component type such as Turnout.class, Light.class
 * @param mgrType - component manager type such as TurnoutManager.class, LightManager.class
 * @param tabName - name on the tab
 * @param tabToolTip - tool tip for the tab
 * @param tabLabel - text displayed as the description of the table in the tab
 * @param tabbedPane - the pane to which to add the tab
 */
    @SuppressWarnings("unchecked")
    private <T extends NamedBean> void addTab(@Nonnull Class<T> type, @Nonnull Class<?> mgrType, String tabName, String tabToolTip, String tabLabel, JTabbedPane tabbedPane ) {
        
        
        Manager<T> mgr = (Manager<T>)InstanceManager.getNullableDefault(mgrType);
        if (mgr == null) {
            return;
        }
        
        JPanel tPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(Bundle.getMessage(tabLabel), SwingConstants.CENTER);
        tPanel.add(label, BorderLayout.NORTH);
        tPanel.add(addCancelSavePanel(), BorderLayout.WEST);

        JLabel messageField = new JLabel();
        final MapTableModel<T, Manager<T>> mapTableModel = new MapTableModel<>(mgrType, messageField);
        JTable table = new JTable(mapTableModel);
        tablelList.add(table);
        mapTableModel.setTable(table);
        
        buildTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        tPanel.add(scrollPane, BorderLayout.CENTER);

        tPanel.add(addButtonsPanel(messageField, mapTableModel), BorderLayout.SOUTH);

        tabbedPane.addTab(Bundle.getMessage(tabName), null, tPanel, Bundle.getMessage(tabToolTip));
    }

/**
 * Build a table. Identical for all types.
 * 
 * @param table - given table object
 */
    private void buildTable(JTable table) {
        table.getModel().addTableModelListener(this);

        //table.setRowSelectionAllowed(false);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(580, 240));

        //table.getTableHeader().setBackground(Color.lightGray);
        //table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.gray);
        //table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn tNumber = columnModel.getColumn(MapTableModel.TNUMCOL);
        tNumber.setResizable(false);
        tNumber.setMinWidth(60);
        tNumber.setMaxWidth(200);

        TableColumn sName = columnModel.getColumn(MapTableModel.SNAMECOL);
        sName.setResizable(true);
        sName.setMinWidth(80);
        sName.setPreferredWidth(80);
        sName.setMaxWidth(340);

        TableColumn uName = columnModel.getColumn(MapTableModel.UNAMECOL);
        uName.setResizable(true);
        uName.setMinWidth(180);
        uName.setPreferredWidth(300);
        uName.setMaxWidth(440);
    }

/**
 * Construct a pane with some elements to be placed under the table.
 * 
 * @param messageField - message field for error messages as JLabel
 * @param fm - the table model to be used with action events when a button is pressed.
 * @return a new panel containing the new elements.
 */
    private JPanel addButtonsPanel(JLabel messageField, final MapTableModel<?,?> fm) {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(Box.createHorizontalGlue());

        pane.add(messageField);
        pane.add(Box.createHorizontalStrut(10));
        JButton removeAllButton = new JButton(Bundle.getMessage("ButtonRemoveAll"));
        removeAllButton.addActionListener((ActionEvent event) -> {
            fm.removeAllMapNumbers();
        });
        pane.add(removeAllButton);

        return pane;
    }

/**
 * Construct a panel containing a Cancel button and a Save button.
 * 
 * @return a new panel containing the buttons.
 */
    private JPanel addCancelSavePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalGlue());

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.setAlignmentX(CENTER_ALIGNMENT);
        cancelButton.setToolTipText(Bundle.getMessage("ToolTipCancel"));
        cancelButton.addActionListener((ActionEvent event) -> {
            dispose();
        });
        p.add(cancelButton);

        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        saveButton.setAlignmentX(CENTER_ALIGNMENT);
        saveButton.setToolTipText(Bundle.getMessage("ToolTipSave"));
        saveButton.addActionListener((ActionEvent event) -> {
            storeValues();
            dispose();
        });
        p.add(saveButton);

        return p;
    }

/**
 * Store the full XML file to disk.
 */
    @Override
    protected void storeValues() {
        new jmri.configurexml.StoreXmlUserAction().actionPerformed(null);
    }

/**
 * Event handler for table changes. Set the frame modified.
 * @param e - table model event
 */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Set mod flag true for: {}", getTitle());
        }
        this.setModifiedFlag(true);
    }
    
/**
 * Called when the window closes.
 * Shut down all table model instances to free resources.
 */
    @Override
    public void dispose() {
        log.trace("dispose - remove table models and its listeners from ");
        for (JTable t : tablelList) {
            MapTableModel<?,?> model = (MapTableModel<?,?>)t.getModel();
            model.dispose();
        }
        tablelList.clear();
        super.dispose();
    }
    
/**
 * Internal TableModel class.
 * There will be one instance for each component type (turnout, light, etc.)
 * The Table Model is identical for all types, but uses different classes.
 * 
 * @param <E> - type of component, e.g. Turnout, Light
 * @param <M> - type of component manager, e.g. TurnoutManager, LightManager.
 */
    private static class MapTableModel<E extends NamedBean, M extends Manager<E>> extends AbstractTableModel implements PropertyChangeListener {
        
        private final Manager<E> mgr;
        private final JLabel messageField;
        private JTable table;
        private String lastInvalid = null; //to prevent endless loop

/**
 * Constructor.
 * Get and save manager object
 * Fill list with system names
 * 
 * @param mgrType - component manager class, e.g. TurnoutManager.class
 * @param messageField - JLabel field to write messages to
 */
        @SuppressWarnings("unchecked")
        MapTableModel(Class<?> mgrType, JLabel messageField) {
            mgr = (Manager<E>)InstanceManager.getDefault(mgrType);
            sysNameList = new java.util.ArrayList<>(mgr.getNamedBeanSet().size());
            mgr.getNamedBeanSet().forEach(bean -> {
                sysNameList.add(bean.getSystemName());
            });
            mgr.addPropertyChangeListener(this);
            this.messageField = messageField;
        }
        
/**
 * Set the corresponding JTable object, so we can reset the tables field values.
 * 
 * @param table table
 */
        public void setTable(JTable table) {
            this.table = table; //used to rollback invalid map values
        }
        
/**
 * The model takes the value and fills the table field.
 * 
 * @param r - table row
 * @param c - table column
 * @return the value to display in the table field
 */
        @Override
        public Object getValueAt(int r, int c) {

            // some error checking
            if (r >= sysNameList.size()) {
                log.debug("row is greater than list size");
                return null;
            }
            E t = mgr.getBySystemName(sysNameList.get(r));
            switch (c) {
                case TNUMCOL:
                    if (t != null) {
                        Object o = t.getProperty(TurnoutNumberMapHandler.beanProperty);
                        if (o != null) {
                            return o.toString();
                        }
                    }
                    return null;
                case SNAMECOL:
                    return sysNameList.get(r);
                case UNAMECOL:
                    return t != null ? t.getUserName() : null;
                default:
                    return null;
            }
        }

/**
 * The model calls this function with the new value set by the user.
 * Only the mapping value (this is the Z21 Turnout Number) can be edited.
 * The new value is validated. If it is valid, it will be written as a
 * property of the bean.
 * If it is invalid, the previous value will be set. If that also fails,
 * The field contents and the bean property will be removed.
 * 
 * @param type - the value to set. Always as String here.
 * @param r - table row
 * @param c - table column
 */
        @Override
        public void setValueAt(Object type, int r, int c) {
            log.trace("field modified {}: row: {}, col: {}", type, r, c);
            E t = mgr.getBySystemName(sysNameList.get(r));
            if (t != null) {
                switch (c) {
                    case TNUMCOL:
                        if (type == null  ||  type.toString().isEmpty()) {
                            t.removeProperty(TurnoutNumberMapHandler.beanProperty);
                            lastInvalid = null;
                            messageField.setText(null);
                        }
                        else {
                            log.trace("old value: {}, new value {}" , getValueAt(r, c), type);
                            if (Pattern.matches("^(#.*|(\\d+))$", type.toString())) {
                                t.setProperty(TurnoutNumberMapHandler.beanProperty, type);
                                lastInvalid = null;
                                messageField.setText(null);
                            }
                            else {
                                log.warn("Invalid value: '{}'", type);
                                if (lastInvalid.equals(getValueAt(r, c))) {
                                    t.removeProperty(TurnoutNumberMapHandler.beanProperty); //remove value on double failure
                                    lastInvalid = null;
                                }
                                else {
                                    table.setValueAt(getValueAt(r, c), r, c); //rollback to old value
                                    lastInvalid = type.toString(); //prevent double failure - would result in endless loop - just in case...
                                    messageField.setText(Bundle.getMessage("MessageInvalidValue", type.toString()));
                                }
                            }

                        }
                        if (!isDirty) {
                            this.fireTableChanged(new TableModelEvent(this));
                            isDirty = true;
                        }
                        TurnoutNumberMapHandler.getInstance().propertyChange(new PropertyChangeEvent(this, "NumberMapChanged", null, t));
                        break;
                    default:
                        log.warn("Unhandled col: {}", c);
                        break;
                }
            }
        }

/**
 * Remove all our bean properties. Then the table will be redrawn.
 */
        public void removeAllMapNumbers() {
            for (String sysName : sysNameList) {
                E t = mgr.getBySystemName(sysName);
                if (t != null) {
                    t.removeProperty(TurnoutNumberMapHandler.beanProperty);
                }
            }
            messageField.setText(null);
            fireTableDataChanged();
        }



        List<String> sysNameList = null;
        boolean isDirty;

/**
 * Return the field class of each column. All types will be returned as String.class
 * 
 * @param c - column
 * @return field class
 */
        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

/**
 * Called if the manager instance has changes.
 * Always rebuild the table contents.
 * 
 * @param e - the change event
 */
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            log.trace("property changed: {}", e.getPropertyName());
            fireTableDataChanged();
        }

/**
 * Before the model is deleted, remove the model instance from the manager instance property change listener list.
 */
        public void dispose() {
            log.trace("dispose MapTableModel - remove listeners from {}", mgr.getClass().getName());
            mgr.removePropertyChangeListener(this);
        }

/**
 * Get the column names displayed in the header line.
 * 
 * @param c - column
 * @return header text for the column
 */
        @Override
        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

/**
 * We have three columns
 * 
 * @return number of columns
 */
        @Override
        public int getColumnCount() {
            return 3;
        }

/**
 * Get the current row count - that is also the length of out system name list.
 * 
 * @return current row count
 */
        @Override
        public int getRowCount() {
            return sysNameList.size();
        }

/**
 * Only the map value column is editable
 * 
 * @param r - row (not used)
 * @param c - column
 * @return true for the map value column, false for all others
 */
        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == TNUMCOL);
        }

        public static final int SNAMECOL = 0;
        public static final int UNAMECOL = 1;
        public static final int TNUMCOL = 2;
    }
    
    private final static Logger log = LoggerFactory.getLogger(NumberMapFrame.class);

}
