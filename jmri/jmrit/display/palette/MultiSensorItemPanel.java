package jmri.jmrit.display.palette;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.Transferable; 

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;

import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.MultiSensorIcon;

public class MultiSensorItemPanel extends TableItemPanel {

    JPanel _multiSensorPanel;
    MultiSensorSelectionModel _selectionModel;
    boolean _upDown = false;

    public MultiSensorItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame, itemType, model, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragSelection"));
    }

    protected void initTablePanel(PickListModel model, Editor editor) {
        super.initTablePanel(model, editor);

        TableColumn column = new TableColumn(PickListModel.POSITION_COL);
        column.setHeaderValue("Position");
        _table.addColumn(column);
        _selectionModel = new MultiSensorSelectionModel(model);
        _table.setSelectionModel(_selectionModel);
        _table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setTransferHandler(new MultiSensorDnD(editor));
    }

    protected void initIconFamiliesPanel() {
        super.initIconFamiliesPanel();
        makeMultiSensorPanel(_family);
        _iconFamilyPanel.add(_multiSensorPanel);
    }

    private void makeMultiSensorPanel(String family) {
        _multiSensorPanel = new JPanel();
        _multiSensorPanel.setLayout(new BoxLayout(_multiSensorPanel, BoxLayout.Y_AXIS));
        if (family==null) {
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        Hashtable<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, family);
        JPanel panel1 = new JPanel();
        _selectionModel.setPositionRange(map.size()-3);
        panel1.setToolTipText(ItemPalette.rbp.getString("ToolTipSetIconSensor"));
        JButton clearSelectionButton = new JButton(ItemPalette.rbp.getString("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _selectionModel.clearSelection();
                    _selectionModel.setPositionRange(ItemPalette.getIconMap(_itemType, _family).size()-3);
                }
        });
        clearSelectionButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
        panel1.add(clearSelectionButton);
        _multiSensorPanel.add(panel1);
        JPanel panel2 = new JPanel();
        ButtonGroup group2 = new ButtonGroup();
        JRadioButton button = new JRadioButton(ItemPalette.rbp.getString("LeftRight"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _upDown = false;
                }
            });
        group2.add(button);
        panel2.add(button);
        button.setSelected(true);
        button = new JRadioButton(ItemPalette.rbp.getString("UpDown"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _upDown = true;
                }
            });
        group2.add(button);
        panel2.add(button);
        _multiSensorPanel.add(panel2);
        _multiSensorPanel.repaint();
    }

    protected void setFamily(String family) {
        super.setFamily(family);
        if (_multiSensorPanel!=null) {
            _iconFamilyPanel.remove(_multiSensorPanel);
        }
        makeMultiSensorPanel(family);
        _iconFamilyPanel.add(_multiSensorPanel);
        _iconFamilyPanel.repaint();
        _paletteFrame.updateFamiliesPanel(_itemType);
    }

    static final String[] POSITION = {"first", "second", "third", "fourth", "fifth",
                                         "sixth", "seventh", "eighth", "nineth", "tenth" };

    protected class MultiSensorSelectionModel extends DefaultListSelectionModel {

        ArrayList<NamedBean> _selections;
        int[] _postions;
        int _nextPosition;
        PickListModel _tableModel;

        MultiSensorSelectionModel(PickListModel tableModel) {
            super();
            _tableModel = tableModel;
            setPositionRange(0);
        }

        protected ArrayList<NamedBean> getSelections() {
            return _selections;
        }

        protected int[] getPositions() {
            return _postions;
        }

        protected int getNextPosition() {
            return _nextPosition;
        }

        protected void setPositionRange(int size) {
            if (log.isDebugEnabled()) log.debug("setPositionRange: size= "+size);
            if (size>POSITION.length) {
                size = POSITION.length;
            }
            _postions = new int[size];
            for (int i=0; i<size; i++) {
                _postions[i] = -1;
            }
            _selections = new ArrayList<NamedBean>(size);
            _nextPosition = 0;
        }

        /*************** DefaultListSelectionModel overrides ********************/

        public boolean isSelectedIndex(int index) {
            for (int i=0; i<_postions.length; i++) {
                if (_postions[i] == index) {
                    if (log.isDebugEnabled()) log.debug("isSelectedIndex("+index+") returned true");
                    return true;
                }
            }
            if (log.isDebugEnabled()) log.debug("isSelectedIndex("+index+") returned false");
            return false;
        }

        public void clearSelection() {
            if (log.isDebugEnabled()) log.debug("clearSelection()");
            for (int i=0; i<_postions.length; i++) {
                if (_postions[i] >= 0) {
                    _tableModel.setValueAt(null, _postions[i], PickListModel.POSITION_COL);
                    super.setSelectionInterval(_postions[i], _postions[i]);
                    super.clearSelection();
                    _postions[i] = -1;
                }
            }
            _selections = new ArrayList<NamedBean>(_postions.length);
            _nextPosition = 0;
        }

        public void addSelectionInterval(int index0, int index1) {
            if (log.isDebugEnabled()) log.debug("addSelectionInterval("+index0+", "+index1+") - stubbed");
//            super.addSelectionInterval(index0, index1);
        }

        public void setSelectionInterval(int row, int index1) {
            if (_nextPosition>=_postions.length) {
                return;
            }
            if (log.isDebugEnabled()) log.debug("setSelectionInterval("+row+", "+index1+")");
            NamedBean bean = _tableModel.getBeanAt(row);
            String position = (String)_tableModel.getValueAt(row, PickListModel.POSITION_COL);
            if (position!=null && position.length()>0) {
                JOptionPane.showMessageDialog(_paletteFrame,
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicatePosition"), 
                            new Object[]{bean.getDisplayName(), position}),
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            } else {
                _tableModel.setValueAt(ItemPalette.rbp.getString(POSITION[_nextPosition]), row, PickListModel.POSITION_COL);
                _selections.add(_nextPosition, bean);
                _postions[_nextPosition] = row;
                _nextPosition++;
                super.setSelectionInterval(row, row);
            }
        }
    }
 
    protected class MultiSensorDnD extends DnDTableItemHandler {

        MultiSensorDnD(Editor editor) {
            super(editor);
        }

        public Transferable createTransferable(JComponent c) {
            if (c instanceof JTable) {
                return createPositionableDnD((JTable)c);
            }
            return null;
        }

        public Transferable createPositionableDnD(JTable table) {
            _selectionModel.getPositions();
            Hashtable <String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            MultiSensorIcon ms = new MultiSensorIcon(_editor);
            ms.setInactiveIcon(iconMap.get("SensorStateInactive"));
            ms.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
            ms.setUnknownIcon(iconMap.get("BeanStateUnknown"));
            ArrayList<NamedBean> selections = _selectionModel.getSelections();
            for (int i=0; i<selections.size(); i++) {
                ms.addEntry(selections.get(i).getDisplayName(), iconMap.get(POSITION[i]));
            }
            _selectionModel.clearSelection();
            ms.setUpDown(_upDown);
            ms.setDisplayLevel(Editor.SENSORS);
            return new PositionableDnD(ms, ms.getNameString());
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorItemPanel.class.getName());
}
