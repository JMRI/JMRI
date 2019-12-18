package jmri.jmrit.operations.locations.schedules;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of schedules used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2011, 2013
 */
public class SchedulesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    ScheduleManager scheduleManager; // There is only one manager

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = ID_COLUMN + 1;
    private static final int SCHEDULE_STATUS_COLUMN = NAME_COLUMN + 1;
    private static final int SPUR_NUMBER_COLUMN = SCHEDULE_STATUS_COLUMN + 1;
    private static final int SPUR_COLUMN = SPUR_NUMBER_COLUMN + 1;
    private static final int STATUS_COLUMN = SPUR_COLUMN + 1;
    private static final int MODE_COLUMN = STATUS_COLUMN + 1;
    private static final int EDIT_COLUMN = MODE_COLUMN + 1;
    private static final int DELETE_COLUMN = EDIT_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

    public SchedulesTableModel() {
        super();
        scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        scheduleManager.addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;

    private int _sort = SORTBYNAME;

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        fireTableDataChanged();
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeSchedules();
        removePropertyChangeTracks();

        if (_sort == SORTBYID) {
            sysList = scheduleManager.getSchedulesByIdList();
        } else {
            sysList = scheduleManager.getSchedulesByNameList();
        }
        // and add them back in
        for (Schedule sch : sysList) {
            sch.addPropertyChangeListener(this);
        }
        addPropertyChangeTracks();
    }

    List<Schedule> sysList = null;
    JTable table;

    public void initTable(SchedulesTableFrame frame, JTable table) {
        this.table = table;
        // Install the button handlers
        TableColumnModel tcm = table.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DELETE_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DELETE_COLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(SCHEDULE_STATUS_COLUMN).setPreferredWidth(80);
        table.getColumnModel().getColumn(SPUR_NUMBER_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(SPUR_COLUMN).setPreferredWidth(350);
        table.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(150);
        table.getColumnModel().getColumn(MODE_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(EDIT_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(90);

        frame.loadTableDetails(table);
    }

    @Override
    public int getRowCount() {
        return sysList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHEST_COLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return Bundle.getMessage("Id");
            case NAME_COLUMN:
                return Bundle.getMessage("Name");
            case SCHEDULE_STATUS_COLUMN:
                return Bundle.getMessage("Status");
            case SPUR_NUMBER_COLUMN:
                return Bundle.getMessage("Number");
            case SPUR_COLUMN:
                return Bundle.getMessage("Spurs");
            case STATUS_COLUMN:
                return Bundle.getMessage("StatusSpur");
            case MODE_COLUMN:
                return Bundle.getMessage("ScheduleMode");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete"); // titles above all columns
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
            case NAME_COLUMN:
            case SCHEDULE_STATUS_COLUMN:
            case STATUS_COLUMN:
            case MODE_COLUMN:
                return String.class;
            case SPUR_COLUMN:
                return JComboBox.class;
            case SPUR_NUMBER_COLUMN:
                return Integer.class;
            case EDIT_COLUMN:
            case DELETE_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case EDIT_COLUMN:
            case DELETE_COLUMN:
            case SPUR_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
            return "ERROR row " + row; // NOI18N
        }
        Schedule schedule = sysList.get(row);
        if (schedule == null) {
            return "ERROR schedule unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return schedule.getId();
            case NAME_COLUMN:
                return schedule.getName();
            case SCHEDULE_STATUS_COLUMN:
                return getScheduleStatus(row);
            case SPUR_NUMBER_COLUMN:
                return scheduleManager.getSpursByScheduleComboBox(schedule).getItemCount();
            case SPUR_COLUMN: {
                return getComboBox(row, schedule);
            }
            case STATUS_COLUMN:
                return getSpurStatus(row);
            case MODE_COLUMN:
                return getSpurMode(row);
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case EDIT_COLUMN:
                editSchedule(row);
                break;
            case DELETE_COLUMN:
                deleteSchedule(row);
                break;
            case SPUR_COLUMN:
                selectJComboBox(value, row);
                break;
            default:
                break;
        }
    }

    ScheduleEditFrame sef = null;

    private void editSchedule(int row) {
        log.debug("Edit schedule");
        if (sef != null) {
            sef.dispose();
        }
        Schedule sch = sysList.get(row);
        LocationTrackPair ltp = getLocationTrackPair(row);
        if (ltp == null) {
            log.debug("Need location track pair");
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("AssignSchedule"),
                    new Object[]{sch.getName()}), MessageFormat.format(Bundle.getMessage("CanNotSchedule"),
                            new Object[]{Bundle.getMessage("ButtonEdit")}),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            sef = new ScheduleEditFrame(sch, ltp.getTrack());
        });
    }

    private void deleteSchedule(int row) {
        log.debug("Delete schedule");
        Schedule s = sysList.get(row);
        if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("DoYouWantToDeleteSchedule"),
                new Object[]{s.getName()}), Bundle.getMessage("DeleteSchedule?"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            scheduleManager.deregister(s);
            OperationsXml.save();
        }
    }

    protected Hashtable<Schedule, String> comboSelect = new Hashtable<Schedule, String>();

    private void selectJComboBox(Object value, int row) {
        Schedule schedule = sysList.get(row);
        JComboBox<?> box = (JComboBox<?>) value;
        if (box.getSelectedIndex() >= 0) {
            comboSelect.put(schedule, Integer.toString(box.getSelectedIndex()));
        }
        fireTableRowsUpdated(row, row);
    }

    private LocationTrackPair getLocationTrackPair(int row) {
        Schedule s = sysList.get(row);
        JComboBox<LocationTrackPair> box = scheduleManager.getSpursByScheduleComboBox(s);
        String index = comboSelect.get(sysList.get(row));
        LocationTrackPair ltp;
        if (index != null) {
            ltp = box.getItemAt(Integer.parseInt(index));
        } else {
            ltp = box.getItemAt(0);
        }
        return ltp;
    }

    private String getScheduleStatus(int row) {
        Schedule sch = sysList.get(row);
        JComboBox<?> box = scheduleManager.getSpursByScheduleComboBox(sch);
        for (int i = 0; i < box.getItemCount(); i++) {
            LocationTrackPair ltp = (LocationTrackPair) box.getItemAt(i);
            String status = ltp.getTrack().checkScheduleValid();
            if (!status.equals(Track.SCHEDULE_OKAY)) {
                return Bundle.getMessage("ErrorTitle");
            }
        }
        return Bundle.getMessage("ButtonOK");
    }

    private JComboBox<LocationTrackPair> getComboBox(int row, Schedule schedule) {
        JComboBox<LocationTrackPair> box = scheduleManager.getSpursByScheduleComboBox(schedule);
        String index = comboSelect.get(sysList.get(row));
        if (index != null && box.getItemCount() > Integer.parseInt(index)) {
            box.setSelectedIndex(Integer.parseInt(index));
        }
        box.addActionListener((ActionEvent e) -> {
            comboBoxActionPerformed(e);
        });
        return box;
    }

    protected void comboBoxActionPerformed(ActionEvent ae) {
        log.debug("combobox action");
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing(); // Allows the table contents to update
        }
    }

    private String getSpurStatus(int row) {
        LocationTrackPair ltp = getLocationTrackPair(row);
        if (ltp == null) {
            return "";
        }
        String status = ltp.getTrack().checkScheduleValid();
        if (!status.equals(Track.SCHEDULE_OKAY)) {
            return status;
        }
        return Bundle.getMessage("ButtonOK");
    }

    private String getSpurMode(int row) {
        LocationTrackPair ltp = getLocationTrackPair(row);
        if (ltp == null) {
            return "";
        }
        String mode = Bundle.getMessage("Sequential");
        if (ltp.getTrack().getScheduleMode() == Track.MATCH) {
            mode = Bundle.getMessage("Match");
        }
        return mode;
    }

    private void removePropertyChangeSchedules() {
        if (sysList != null) {
            for (Schedule sch : sysList) {
                sch.removePropertyChangeListener(this);
            }
        }
    }

    private void addPropertyChangeTracks() {
        // only spurs have schedules
        for (Track track : InstanceManager.getDefault(LocationManager.class).getTracks(Track.SPUR)) {
            track.addPropertyChangeListener(this);
        }
    }

    private void removePropertyChangeTracks() {
        for (Track track : InstanceManager.getDefault(LocationManager.class).getTracks(Track.SPUR)) {
            track.removePropertyChangeListener(this);
        }
    }

    public void dispose() {
        if (sef != null) {
            sef.dispose();
        }
        scheduleManager.removePropertyChangeListener(this);
        removePropertyChangeSchedules();
        removePropertyChangeTracks();
    }

    // check for change in number of schedules, or a change in a schedule
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        } else if (e.getSource().getClass().equals(Schedule.class)) {
            Schedule schedule = (Schedule) e.getSource();
            int row = sysList.indexOf(schedule);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update schedule table row: {} name: {}", row, schedule.getName());
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = (Track) e.getSource();
            Schedule schedule = track.getSchedule();
            int row = sysList.indexOf(schedule);
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            } else {
                fireTableDataChanged();
            }
        }

        if (e.getPropertyName().equals(Track.SCHEDULE_ID_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SchedulesTableModel.class);
}
