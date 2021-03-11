package jmri.jmrit.beantable.light;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.implementation.DefaultLightControl;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightControl;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import static jmri.jmrit.beantable.LightTableAction.getDescriptionText;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Table model for Light Controls in the Add/Edit Light windows.
 * No direct access to this class is normally required, access via
 * LightControlPane.java
 * 
 * Code originally within LightTableAction.
 * 
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class LightControlTableModel extends javax.swing.table.AbstractTableModel {

    public static final int TYPE_COLUMN = 0;
    public static final int DESCRIPTION_COLUMN = 1;
    public static final int EDIT_COLUMN = 2;
    public static final int REMOVE_COLUMN = 3;
    
    private final LightControlPane lcp;
    private final ArrayList<LightControl> controlList;
    
    protected static final String sensorControl = Bundle.getMessage("LightSensorControl");
    protected static final String fastClockControl = Bundle.getMessage("LightFastClockControl");
    protected static final String turnoutStatusControl = Bundle.getMessage("LightTurnoutStatusControl");
    protected static final String timedOnControl = Bundle.getMessage("LightTimedOnControl");
    protected static final String twoSensorControl = Bundle.getMessage("LightTwoSensorControl");
    protected static final String noControl = Bundle.getMessage("LightNoControl");
    
    protected static final String[] controlTypes = new String[]{
        noControl,
        sensorControl,
        fastClockControl,
        turnoutStatusControl,
        timedOnControl,
        twoSensorControl };
    
    protected static final List<String> getControlTypeTips(){
        ArrayList<String> typeTooltips = new ArrayList<>();
        typeTooltips.add(null); // no Control Type selected
        typeTooltips.add(Bundle.getMessage("LightSensorControlTip"));
        typeTooltips.add(Bundle.getMessage("LightFastClockControlTip"));
        typeTooltips.add(Bundle.getMessage("LightTurnoutStatusControlTip",
                InstanceManager.turnoutManagerInstance().getClosedText(),
                InstanceManager.turnoutManagerInstance().getThrownText()));
        typeTooltips.add(Bundle.getMessage("LightTimedOnControlTip"));
        typeTooltips.add(Bundle.getMessage("LightTwoSensorControlTip"));
        return typeTooltips;
    }
    /**
     * Get text showing the type of Light Control.
     *
     * @param type the type of Light Control
     * @return name of type or the description for {@link jmri.Light#NO_CONTROL}
     *         if type is not recognized
     */
    public static String getControlTypeText(int type) {
        switch (type) {
            case Light.SENSOR_CONTROL:
                return sensorControl;
            case Light.FAST_CLOCK_CONTROL:
                return fastClockControl;
            case Light.TURNOUT_STATUS_CONTROL:
                return turnoutStatusControl;
            case Light.TIMED_ON_CONTROL:
                return timedOnControl;
            case Light.TWO_SENSOR_CONTROL:
                return twoSensorControl;
            case Light.NO_CONTROL:
            default:
                return noControl;
        }
    }

    public LightControlTableModel(LightControlPane pane) {
        super();
        controlList = new ArrayList<>();
        lcp = pane;
    }
    
    /**
     * Get the Current Light Control List for the Table.
     * @return unmodifiable List of Light Controls.
     */
    public List<LightControl> getControlList(){
        return java.util.Collections.unmodifiableList(controlList);
    }
    
    public void setTableToLight(Light light){
        // Get a fresh copy of the LightControl list
        controlList.clear();
        light.getLightControlList().forEach((lightControlList1) -> controlList.add(new DefaultLightControl(lightControlList1)));
        fireTableDataChanged();
    }
    
    public void addControl(LightControl lc){
        controlList.add(lc);
        fireTableDataChanged();
    }
    
    public void removeControl(LightControl lc){
        controlList.remove(lc);
        fireTableDataChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case EDIT_COLUMN:
            case REMOVE_COLUMN:
                return JButton.class;
            case TYPE_COLUMN:
            case DESCRIPTION_COLUMN:
            default:
                return String.class;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return REMOVE_COLUMN + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return controlList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int r, int c) {
        switch (c) {
            case EDIT_COLUMN:
            case REMOVE_COLUMN:
                return true;
            case TYPE_COLUMN:
            case DESCRIPTION_COLUMN:
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        if (col == TYPE_COLUMN) {
            return Bundle.getMessage("LightControlType");
        } else if (col == DESCRIPTION_COLUMN) {
            return Bundle.getMessage("LightControlDescription");
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int r, int c) {
        LightControl lc = controlList.get(r);
        switch (c) {
            case TYPE_COLUMN:
                return (getControlTypeText(lc.getControlType()));
            case DESCRIPTION_COLUMN:
                return (getDescriptionText(lc, lc.getControlType()));
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            case REMOVE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == EDIT_COLUMN) {
            // set up to edit. Use separate Runnable so window is created on top
            javax.swing.SwingUtilities.invokeLater(() -> {
                lcp.editControlAction(controlList.get(row));
            });
        }
        else if (col == REMOVE_COLUMN) {
            controlList.remove(row);
            fireTableDataChanged();
        }
    }
    
    protected void configureJTable(JTable table){
    
        table.setRowSelectionAllowed(false);
    
        TableColumnModel lightControlColumnModel = table.getColumnModel();
        TableColumn typeColumn = lightControlColumnModel.getColumn(LightControlTableModel.TYPE_COLUMN);
        typeColumn.setResizable(true);
        typeColumn.setMinWidth(130);
        typeColumn.setMaxWidth(170);
        TableColumn descriptionColumn = lightControlColumnModel.getColumn(
                LightControlTableModel.DESCRIPTION_COLUMN);
        descriptionColumn.setResizable(true);
        descriptionColumn.setMinWidth(270);
        descriptionColumn.setMaxWidth(380);

        table.setDefaultRenderer(JButton.class, new ButtonRenderer());
        table.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));

        JButton testButton = new JButton(Bundle.getMessage("ButtonDelete"));
        table.setRowHeight(testButton.getPreferredSize().height);
        TableColumn editColumn = lightControlColumnModel.getColumn(LightControlTableModel.EDIT_COLUMN);
        editColumn.setResizable(false);
        editColumn.setMinWidth(new JButton(Bundle.getMessage("ButtonEdit")).getPreferredSize().width);
        TableColumn removeColumn = lightControlColumnModel.getColumn(LightControlTableModel.REMOVE_COLUMN);
        removeColumn.setResizable(false);
        removeColumn.setMinWidth(testButton.getPreferredSize().width);
        
    }

    // private final static Logger log = LoggerFactory.getLogger(LightControlTableModel.class);

}
