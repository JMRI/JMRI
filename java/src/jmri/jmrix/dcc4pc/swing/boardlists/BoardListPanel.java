package jmri.jmrix.dcc4pc.swing.boardlists;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.swing.Dcc4PcPanelInterface;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Signal Mast Add / Edit Panel
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @version $Revision: 19647 $
 */
public class BoardListPanel extends jmri.jmrix.dcc4pc.swing.Dcc4PcPanel implements PropertyChangeListener, Dcc4PcPanelInterface {

    /**
     *
     */
    private static final long serialVersionUID = -8283443350770492724L;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.dcc4pc.swing.boardlists.BoardListBundle");

    jmri.jmrix.dcc4pc.Dcc4PcSensorManager senMan;
    ReaderBoardModel _BoardModel;

    JScrollPane _BoardScrollPane;

    public BoardListPanel() {
        super();
    }

    public void initComponents(Dcc4PcSystemConnectionMemo memo) {
        super.initComponents(memo);
        senMan = jmri.InstanceManager.getDefault(jmri.jmrix.dcc4pc.Dcc4PcSensorManager.class);
        if (senMan != null) {
            _boardListCount = senMan.getBoards();
        }

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();
        header.add(sourcePanel);
        add(header, BorderLayout.NORTH);

        _BoardModel = new ReaderBoardModel();
        JTable boardTable = jmri.util.JTableUtil.sortableDataModel(_BoardModel);

        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) boardTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(ReaderBoardModel.ADDRESS_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model

        boardTable.setRowSelectionAllowed(false);
        boardTable.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 120));
        _BoardModel.configureTable(boardTable);
        _BoardScrollPane = new JScrollPane(boardTable);
        _BoardModel.fireTableDataChanged();
        add(_BoardScrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();

        add(footer, BorderLayout.SOUTH);
    }

    public void initComponents() throws Exception {
    }

    JLabel sourceLabel = new JLabel();

    public void propertyChange(java.beans.PropertyChangeEvent e) {

    }

    private List<Integer> _boardListCount;

    public class ReaderBoardModel extends AbstractTableModel implements PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = -3477572258901807384L;

        ReaderBoardModel() {
            super();
            if (senMan != null) {
                senMan.addPropertyChangeListener(this);
            }
        }

        /*void updateSignalMastLogic(SignalMastLogic senManOld, SignalMastLogic senManNew){
         if(senManOld!=null)
         senManOld.removePropertyChangeListener(this);
         if(senManNew!=null)
         senManNew.addPropertyChangeListener(this);
         fireTableDataChanged();
         }*/
        @Override
        public Class<?> getColumnClass(int c) {
            /*if (c ==ENCODING_COLUMN)
             return Boolean.class;
             if (c ==VERSION_COLUMN)
             return Boolean.class;
             if(c==DESCRIPTION_COLUMN)
             return JButton.class;*/
            if (c == EDIT_COLUMN) {
                return JButton.class;
            }
            return String.class;
        }

        public void configureTable(JTable table) {
            // allow reordering of the columns
            table.getTableHeader().setReorderingAllowed(true);

            // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // resize columns as requested
            for (int i = 0; i < table.getColumnCount(); i++) {
                int width = getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            table.sizeColumnsToFit(-1);

            configEditColumn(table);

        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case ADDRESS_COLUMN:
                case VERSION_COLUMN:
                case INPUTS_COLUMN:
                    return new JTextField(5).getPreferredSize().width;
                case ENCODING_COLUMN:
                    return new JTextField(22).getPreferredSize().width;
                case DESCRIPTION_COLUMN: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                case EDIT_COLUMN: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: " + col);
                    return new JTextField(8).getPreferredSize().width;
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == INPUTS_COLUMN) {
                return rb.getString("ColumnInput");
            }
            if (col == ADDRESS_COLUMN) {
                return rb.getString("ColumnAddress");
            }
            if (col == ENCODING_COLUMN) {
                return rb.getString("ColumnEncoding");
            }
            if (col == VERSION_COLUMN) {
                return rb.getString("ColumnVersion");
            }
            if (col == DESCRIPTION_COLUMN) {
                return rb.getString("ColumnDescription");
            }
            if (col == EDIT_COLUMN) {
                return rb.getString(""); //no title above Edit buttons
            }
            return "";
        }

        public void dispose() {
            if (senMan != null) {
                senMan.removePropertyChangeListener(this);
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                _boardListCount = senMan.getBoards();
                int length = (Integer) e.getNewValue();
                if (length == 0) {
                    senMan.removePropertyChangeListener(this);
                    senMan = null;
                }
                fireTableDataChanged();
            } /*else if (e.getPropertyName().equals("updatedDestination")) {
             // a new NamedBean is available in the manager
             _boardListCount = senMan.getBoards();
             fireTableDataChanged();
             } else if((e.getPropertyName().equals("state")) || (e.getPropertyName().equals("Enabled"))) {
             fireTableDataChanged();
             fireTableRowsUpdated(0, _boardListCount.size());
             }*/

        }

        protected void configEditColumn(JTable table) {
            // have the delete column hold a button
            /*AbstractTableAction.rb.getString("EditDelete")*/
            /*setColumnToHoldButton(table, DESCRIPTION_COLUMN, 
             new JButton(rb.getString("ButtonEdit")));*/
            setColumnToHoldButton(table, EDIT_COLUMN,
                    new JButton(rb.getString("ButtonEdit")));
        }

        protected void setColumnToHoldButton(JTable table, int column, JButton sample) {
            //TableColumnModel tcm = table.getColumnModel();
            // install a button renderer & editor
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            table.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            table.setDefaultEditor(JButton.class, buttonEditor);
            // ensure the table rows, columns have enough room for buttons
            table.setRowHeight(sample.getPreferredSize().height);
            table.getColumnModel().getColumn(column)
                    .setPreferredWidth((sample.getPreferredSize().width) + 4);
        }

        public int getColumnCount() {
            return 6;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == EDIT_COLUMN) {
                return true;
            }
            return false;
        }

        protected void editAddress(int r) {
            int boardAddress = _boardListCount.get(r);
            JPanel newAddressPanel = new JPanel();
            JTextField newAddressField = new JTextField(10);
            //newAddressPanel.add(new JLabel(rb.getString("SensorInactiveTimer")));
            newAddressPanel.add(newAddressField);

            int retval = JOptionPane.showOptionDialog(null,
                    rb.getString("ChangeAddress"), rb.getString("ChangeAddressTitle"),
                    0, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{"Cancel", "OK", newAddressPanel}, null);
            if (retval != 1) {
                return;
            }
            //Need some validation!
            senMan.changeBoardAddress(boardAddress, Integer.valueOf(newAddressField.getText()));
        }

       //
        //protected void deletePair(int r){
            /*jmri.InstanceManager.signalMastLogicManagerInstance().removeSignalMastLogic(senMan, _boardListCount.get(r));*/
        //}
        public static final int ADDRESS_COLUMN = 0;
        public static final int INPUTS_COLUMN = 1;
        public static final int ENCODING_COLUMN = 2;
        public static final int VERSION_COLUMN = 3;
        public static final int DESCRIPTION_COLUMN = 4;
        public static final int EDIT_COLUMN = 5;

        public void setSetToState(String x) {
        }

        public int getRowCount() {
            if (_boardListCount == null) {
                return 0;
            }
            return _boardListCount.size();
        }

        public Object getValueAt(int r, int c) {
            if (senMan == null) {
                return null;
            }
            // some error checking
            if (r >= _boardListCount.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INPUTS_COLUMN:
                    return senMan.getBoardInputs(_boardListCount.get(r)) + 1;
                case ADDRESS_COLUMN:  // slot number
                    return _boardListCount.get(r);
                case ENCODING_COLUMN:
                    return senMan.getBoardEncodingAsString(_boardListCount.get(r));
                case VERSION_COLUMN:
                    return senMan.getBoardVersion(_boardListCount.get(r));
                case DESCRIPTION_COLUMN:
                    return senMan.getBoardDescription(_boardListCount.get(r));
                case EDIT_COLUMN:
                    return rb.getString("ButtonEdit");
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == EDIT_COLUMN) {
                editAddress(r);
            }
            /*else if (c==EDIT_COLUMN)
             deletePair(r);
             else if (c==VERSION_COLUMN){
             boolean b = ((Boolean)type).booleanValue();
             if(b)
             senMan.setEnabled(_boardListCount.get(r));
             else
             senMan.setDisabled(_boardListCount.get(r));
                
             }*/
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.dcc4pc.swing.Dcc4PcNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = -6085890861522428975L;

        public Default() {
            super("Dcc4PC Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    BoardListPanel.class.getName(),
                    jmri.InstanceManager.getDefault(Dcc4PcSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BoardListPanel.class.getName());

}
