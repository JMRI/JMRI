package jmri.jmrit.operations.routes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.table.TableCellEditor;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.SplitButtonColorChooserPanel;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of route locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2013
 */
public class RouteEditTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = ID_COLUMN + 1;
    private static final int TRAIN_DIRECTION_COLUMN = NAME_COLUMN + 1;
    private static final int MAXMOVES_COLUMN = TRAIN_DIRECTION_COLUMN + 1;
    private static final int RANDOM_CONTROL_COLUMN = MAXMOVES_COLUMN + 1;
    private static final int PICKUP_COLUMN = RANDOM_CONTROL_COLUMN + 1;
    private static final int DROP_COLUMN = PICKUP_COLUMN + 1;
    private static final int WAIT_COLUMN = DROP_COLUMN + 1;
    private static final int TIME_COLUMN = WAIT_COLUMN + 1;
    private static final int MAXLENGTH_COLUMN = TIME_COLUMN + 1;
    private static final int GRADE = MAXLENGTH_COLUMN + 1;
    private static final int TRAINICONX = GRADE + 1;
    private static final int TRAINICONY = TRAINICONX + 1;
    private static final int COMMENT_COLUMN = TRAINICONY + 1;
    private static final int UP_COLUMN = COMMENT_COLUMN + 1;
    private static final int DOWN_COLUMN = UP_COLUMN + 1;
    private static final int DELETE_COLUMN = DOWN_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

    private JTable _table;
    private Route _route;
    private RouteEditFrame _frame;
    List<RouteLocation> _routeList = new ArrayList<>();

    public RouteEditTableModel() {
        super();
    }

    public void setWait(boolean showWait) {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(WAIT_COLUMN), showWait);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), !showWait);
    }

    private void updateList() {
        if (_route == null) {
            return;
        }
        // first, remove listeners from the individual objects
        removePropertyChangeRouteLocations();
        _routeList = _route.getLocationsBySequenceList();
        // and add them back in
        for (RouteLocation rl : _routeList) {
            rl.addPropertyChangeListener(this);
        }
    }

    protected void initTable(RouteEditFrame frame, JTable table, Route route) {
        _frame = frame;
        _table = table;
        _route = route;
        if (_route != null) {
            _route.addPropertyChangeListener(this);
        }
        initTable(table);
    }

    private void initTable(JTable table) {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();
        // Install the button handlers
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(COMMENT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(COMMENT_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(UP_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(UP_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DOWN_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DOWN_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DELETE_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DELETE_COLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(150);
        table.getColumnModel().getColumn(TRAIN_DIRECTION_COLUMN).setPreferredWidth(95);
        table.getColumnModel().getColumn(MAXMOVES_COLUMN).setPreferredWidth(50);
        table.getColumnModel().getColumn(RANDOM_CONTROL_COLUMN).setPreferredWidth(65);
        table.getColumnModel().getColumn(PICKUP_COLUMN).setPreferredWidth(65);
        table.getColumnModel().getColumn(DROP_COLUMN).setPreferredWidth(65);
        table.getColumnModel().getColumn(WAIT_COLUMN).setPreferredWidth(65);
        table.getColumnModel().getColumn(TIME_COLUMN).setPreferredWidth(65);
        table.getColumnModel().getColumn(MAXLENGTH_COLUMN).setPreferredWidth(75);
        table.getColumnModel().getColumn(GRADE).setPreferredWidth(50);
        table.getColumnModel().getColumn(TRAINICONX).setPreferredWidth(35);
        table.getColumnModel().getColumn(TRAINICONY).setPreferredWidth(35);
        table.getColumnModel().getColumn(COMMENT_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(UP_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(DOWN_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(80);

        _frame.loadTableDetails(table);
        // does not use a table sorter
        table.setRowSorter(null);

        // turn off columns
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), false);

        updateList();
    }

    @Override
    public int getRowCount() {
        return _routeList.size();
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
                return Bundle.getMessage("Location");
            case TRAIN_DIRECTION_COLUMN:
                return Bundle.getMessage("TrainDirection");
            case MAXMOVES_COLUMN:
                return Bundle.getMessage("MaxMoves");
            case RANDOM_CONTROL_COLUMN:
                return Bundle.getMessage("Random");
            case PICKUP_COLUMN:
                return Bundle.getMessage("Pickups");
            case DROP_COLUMN:
                return Bundle.getMessage("Drops");
            case WAIT_COLUMN:
                return Bundle.getMessage("Wait");
            case TIME_COLUMN:
                return Bundle.getMessage("Time");
            case MAXLENGTH_COLUMN:
                return Bundle.getMessage("MaxLength");
            case GRADE:
                return Bundle.getMessage("Grade");
            case TRAINICONX:
                return Bundle.getMessage("X");
            case TRAINICONY:
                return Bundle.getMessage("Y");
            case COMMENT_COLUMN:
                return Bundle.getMessage("Comment");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
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
            case MAXMOVES_COLUMN:
            case WAIT_COLUMN:
            case MAXLENGTH_COLUMN:
            case GRADE:
            case TRAINICONX:
            case TRAINICONY:
                return String.class; 
            case TRAIN_DIRECTION_COLUMN:
            case RANDOM_CONTROL_COLUMN:
            case PICKUP_COLUMN:
            case DROP_COLUMN:
            case TIME_COLUMN:
                return JComboBox.class;
            case COMMENT_COLUMN:
            case UP_COLUMN:
            case DOWN_COLUMN:
            case DELETE_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case DELETE_COLUMN:
            case TRAIN_DIRECTION_COLUMN:
            case MAXMOVES_COLUMN:
            case RANDOM_CONTROL_COLUMN:
            case PICKUP_COLUMN:
            case DROP_COLUMN:
            case WAIT_COLUMN:
            case TIME_COLUMN:
            case MAXLENGTH_COLUMN:
            case GRADE:
            case TRAINICONX:
            case TRAINICONY:
            case COMMENT_COLUMN:
            case UP_COLUMN:
            case DOWN_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
            return "ERROR unknown " + row; // NOI18N
        }
        RouteLocation rl = _routeList.get(row);
        if (rl == null) {
            return "ERROR unknown route location " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return rl.getId();
            case NAME_COLUMN:
                return rl.getName();
            case TRAIN_DIRECTION_COLUMN: {
                JComboBox<String> cb = Setup.getTrainDirectionComboBox();
                cb.setSelectedItem(rl.getTrainDirectionString());
                return cb;
            }
            case MAXMOVES_COLUMN:
                return Integer.toString(rl.getMaxCarMoves());
            case RANDOM_CONTROL_COLUMN: {
                JComboBox<String> cb = getRandomControlComboBox();
                cb.setSelectedItem(rl.getRandomControl());
                return cb;
            }
            case PICKUP_COLUMN: {
                JComboBox<String> cb = getYesNoComboBox();
                cb.setSelectedItem(rl.isPickUpAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no"));
                return cb;
            }
            case DROP_COLUMN: {
                JComboBox<String> cb = getYesNoComboBox();
                cb.setSelectedItem(rl.isDropAllowed() ? Bundle.getMessage("yes") : Bundle.getMessage("no"));
                return cb;
            }
            case WAIT_COLUMN: {
                return Integer.toString(rl.getWait());
            }
            case TIME_COLUMN: {
                JComboBox<String> cb = getTimeComboBox();
                cb.setSelectedItem(rl.getDepartureTime());
                return cb;
            }
            case MAXLENGTH_COLUMN:
                return Integer.toString(rl.getMaxTrainLength());
            case GRADE:
                return Double.toString(rl.getGrade());
            case TRAINICONX:
                return Integer.toString(rl.getTrainIconX());
            case TRAINICONY:
                return Integer.toString(rl.getTrainIconY());
            case COMMENT_COLUMN: {
                if (rl.getComment().equals(RouteLocation.NONE)) {
                    return Bundle.getMessage("Add");
                } else {
                    return Bundle.getMessage("ButtonEdit");
                }
            }
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (value == null) {
            log.debug("Warning route table row {} still in edit", row);
            return;
        }
        RouteLocation rl = _routeList.get(row);
        if (rl == null) {
            log.error("ERROR unknown route location for row: {}", row); // NOI18N
        }
        switch (col) {
            case COMMENT_COLUMN:
                setComment(rl);
                break;
            case UP_COLUMN:
                moveUpRouteLocation(rl);
                break;
            case DOWN_COLUMN:
                moveDownRouteLocation(rl);
                break;
            case DELETE_COLUMN:
                deleteRouteLocation(rl);
                break;
            case TRAIN_DIRECTION_COLUMN:
                setTrainDirection(value, rl);
                break;
            case MAXMOVES_COLUMN:
                setMaxTrainMoves(value, rl);
                break;
            case RANDOM_CONTROL_COLUMN:
                setRandomControlValue(value, rl);
                break;
            case PICKUP_COLUMN:
                setPickup(value, rl);
                break;
            case DROP_COLUMN:
                setDrop(value, rl);
                break;
            case WAIT_COLUMN:
                setWait(value, rl);
                break;
            case TIME_COLUMN:
                setDepartureTime(value, rl);
                break;
            case MAXLENGTH_COLUMN:
                setMaxTrainLength(value, rl);
                break;
            case GRADE:
                setGrade(value, rl);
                break;
            case TRAINICONX:
                setTrainIconX(value, rl);
                break;
            case TRAINICONY:
                setTrainIconY(value, rl);
                break;
            default:
                break;
        }
    }

    private void moveUpRouteLocation(RouteLocation rl) {
        log.debug("move location up");
        _route.moveLocationUp(rl);
    }

    private void moveDownRouteLocation(RouteLocation rl) {
        log.debug("move location down");
        _route.moveLocationDown(rl);
    }

    private void deleteRouteLocation(RouteLocation rl) {
        log.debug("Delete location");
        _route.deleteLocation(rl);
    }

    private int _trainDirection = Setup.getDirectionInt(Setup.getTrainDirectionList().get(0));

    public int getLastTrainDirection() {
        return _trainDirection;
    }

    private void setTrainDirection(Object value, RouteLocation rl) {
        _trainDirection = Setup.getDirectionInt((String) ((JComboBox<?>) value).getSelectedItem());
        rl.setTrainDirection(_trainDirection);
        // update train icon
        rl.setTrainIconCoordinates();
    }

    private int _maxTrainMoves = Setup.getCarMoves();

    public int getLastMaxTrainMoves() {
        return _maxTrainMoves;
    }

    private void setMaxTrainMoves(Object value, RouteLocation rl) {
        int moves;
        try {
            moves = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Location moves must be a number");
            return;
        }
        if (moves <= 500) {
            rl.setMaxCarMoves(moves);
            _maxTrainMoves = moves;
        } else {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("MaximumLocationMoves"), Bundle
                    .getMessage("CanNotChangeMoves"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setRandomControlValue(Object value, RouteLocation rl) {
        rl.setRandomControl((String) ((JComboBox<?>) value).getSelectedItem());
    }

    private void setDrop(Object value, RouteLocation rl) {
        rl.setDropAllowed(
                ((String) ((JComboBox<?>) value).getSelectedItem()).equals(Bundle.getMessage("yes")));
    }

    private void setPickup(Object value, RouteLocation rl) {
        rl.setPickUpAllowed(
                ((String) ((JComboBox<?>) value).getSelectedItem()).equals(Bundle.getMessage("yes")));
    }

    private int _maxTrainLength = Setup.getMaxTrainLength();

    public int getLastMaxTrainLength() {
        return _maxTrainLength;
    }

    private void setWait(Object value, RouteLocation rl) {
        int wait;
        try {
            wait = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Location wait must be a number");
            JOptionPane.showMessageDialog(null, Bundle.getMessage("EnterWaitTimeMinutes"), Bundle
                    .getMessage("WaitTimeNotValid"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        rl.setWait(wait);
    }

    private void setDepartureTime(Object value, RouteLocation rl) {
        rl.setDepartureTime(((String) ((JComboBox<?>) value).getSelectedItem()));
    }

    private void setMaxTrainLength(Object value, RouteLocation rl) {
        int length;
        try {
            length = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Maximum departure length must be a number");
            return;
        }
        if (length < 0) {
            log.error("Maximum departure length must be a postive number");
            return;
        }
        if (length < 500 && Setup.getLengthUnit().equals(Setup.FEET) ||
                length < 160 && Setup.getLengthUnit().equals(Setup.METER)) {
            // warn that train length might be too short
            if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("LimitTrainLength"),
                    new Object[]{length, Setup.getLengthUnit().toLowerCase(), rl.getName()}),
                    Bundle
                            .getMessage("WarningTooShort"),
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        if (length > Setup.getMaxTrainLength()) {
            log.error("Maximum departure length can not exceed maximum train length");
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("DepartureLengthNotExceed"),
                    new Object[]{length, Setup.getMaxTrainLength()}), Bundle.getMessage("CanNotChangeMaxLength"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            rl.setMaxTrainLength(length);
            _maxTrainLength = length;
        }
    }

    private void setGrade(Object value, RouteLocation rl) {
        double grade;
        try {
            grade = Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.error("grade must be a number");
            return;
        }
        if (grade <= 6 && grade >= -6) {
            rl.setGrade(grade);
        } else {
            log.error("Maximum grade is 6 percent");
            JOptionPane.showMessageDialog(null, Bundle.getMessage("MaxGrade"), Bundle.getMessage("CanNotChangeGrade"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setTrainIconX(Object value, RouteLocation rl) {
        int x;
        try {
            x = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Train icon x coordinate must be a number");
            return;
        }
        rl.setTrainIconX(x);
    }

    private void setTrainIconY(Object value, RouteLocation rl) {
        int y;
        try {
            y = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Train icon y coordinate must be a number");
            return;
        }
        rl.setTrainIconY(y);
    }

    private void setComment(RouteLocation rl) {
        // Create comment panel
        final JDialog dialog = new JDialog();
        dialog.setLayout(new BorderLayout());
        dialog.setTitle(Bundle.getMessage("Comment") + " " + rl.getName());
        final JTextArea commentTextArea = new JTextArea(5, 100);
        JScrollPane commentScroller = new JScrollPane(commentTextArea);
        dialog.add(commentScroller, BorderLayout.CENTER);
        commentTextArea.setText(rl.getComment());

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        dialog.add(buttonPane, BorderLayout.SOUTH);
        
        // text color chooser
        JPanel pTextColor = new JPanel();
        pTextColor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TextColor")));
        JColorChooser commentColorChooser = new JColorChooser(rl.getCommentColor());
        AbstractColorChooserPanel commentColorPanels[] = {new SplitButtonColorChooserPanel()};
        commentColorChooser.setChooserPanels(commentColorPanels);
        commentColorChooser.setPreviewPanel(new JPanel());
        pTextColor.add(commentColorChooser);
        buttonPane.add(pTextColor);

        JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                rl.setComment(commentTextArea.getText());
                rl.setCommentColor(commentColorChooser.getColor());
                dialog.dispose();
                return;
            }
        });
        buttonPane.add(okayButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dialog.dispose();
                return;
            }
        });
        buttonPane.add(cancelButton);

        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
    }

    private JComboBox<String> getYesNoComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(Bundle.getMessage("yes"));
        cb.addItem(Bundle.getMessage("no"));
        return cb;
    }

    private JComboBox<String> getRandomControlComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(RouteLocation.DISABLED);
        // 10 to 100 by 10
        for (int i = 10; i < 101; i = i + 10) {
            cb.addItem(Integer.toString(i));
        }
        return cb;
    }

    private JComboBox<String> getTimeComboBox() {
        JComboBox<String> timeBox = new JComboBox<>();
        String hour;
        String minute;
        timeBox.addItem("");
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                hour = "0" + Integer.toString(i);
            } else {
                hour = Integer.toString(i);
            }
            for (int j = 0; j < 60; j += 1) {
                if (j < 10) {
                    minute = "0" + Integer.toString(j);
                } else {
                    minute = Integer.toString(j);
                }

                timeBox.addItem(hour + ":" + minute);
            }
        }
        return timeBox;
    }

    // this table listens for changes to a route and it's locations
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }

        if (e.getSource().getClass().equals(RouteLocation.class)) {
            RouteLocation rl = (RouteLocation) e.getSource();
            int row = _routeList.indexOf(rl);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update route table row: {} id: {}", row, rl.getId());
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private void removePropertyChangeRouteLocations() {
        for (RouteLocation rl : _routeList) {
            rl.removePropertyChangeListener(this);
        }
    }

    public void dispose() {
        removePropertyChangeRouteLocations();
        if (_route != null) {
            _route.removePropertyChangeListener(this);
        }
        _routeList.clear();
        fireTableDataChanged();
    }

    private final static Logger log = LoggerFactory.getLogger(RouteEditTableModel.class);
}
