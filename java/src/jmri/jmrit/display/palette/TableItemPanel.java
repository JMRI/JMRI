package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.NamedBean;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LightIcon;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for the various item types that come from tool Tables - e.g.
 * Sensors, Turnouts, etc.
 * 
* @author Pete Cressman Copyright (c) 2010, 2011
 */
public class TableItemPanel extends FamilyItemPanel implements ListSelectionListener {

    /**
     *
     */
    private static final long serialVersionUID = -72832594032854676L;

    int ROW_HEIGHT;

    protected JTable _table;
    protected PickListModel _model;

    JScrollPane _scrollPane;
    JDialog _addTableDialog;
    JTextField _sysNametext = new JTextField();
    JTextField _userNametext = new JTextField();
    JButton _addTableButton;

    /**
     * Constructor for all table types. When item is a bean, the itemType is the
     * name key for the item in jmri.NamedBeanBundle.properties
     */
    public TableItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, editor);
        _model = model;
    }

    /**
     * Init for creation insert table
     */
    public void init() {
        if (!_initialized) {
            super.init();
            add(initTablePanel(_model, _editor), 0);      // top of Panel    		
            _buttonPostion = 1;
        }
    }

    /**
     * Init for update of existing indicator turnout _bottom3Panel has "Update
     * Panel" button put into _bottom1Panel
     */
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        add(initTablePanel(_model, _editor), 0);
        _buttonPostion = 1;
    }

    /**
     * top Panel
     */
    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
        _table.getSelectionModel().addListSelectionListener(this);
        ROW_HEIGHT = _table.getRowHeight();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        java.awt.Dimension dim = _table.getPreferredSize();
        dim.height = ROW_HEIGHT * 12;
        _scrollPane.getViewport().setPreferredSize(dim);

        JPanel panel = new JPanel();
        _addTableButton = new JButton(Bundle.getMessage("CreateNewItem"));
        _addTableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                makeAddToTableWindow();
            }
        });
        _addTableButton.setToolTipText(Bundle.getMessage("ToolTipAddToTable"));
        panel.add(_addTableButton);
        JButton clearSelectionButton = new JButton(Bundle.getMessage("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                _table.clearSelection();
            }
        });
        clearSelectionButton.setToolTipText(Bundle.getMessage("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        _table.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        return topPanel;
    }

    protected void makeAddToTableWindow() {
        _addTableDialog = new JDialog(_paletteFrame, Bundle.getMessage("AddToTableTitle"), true);

        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) { cancelPressed(e); }
        };
        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addToTable();
            }
        };
        jmri.util.swing.JmriPanel addPanel = new jmri.jmrit.beantable.AddNewDevicePanel(
                _sysNametext, _userNametext, "addToTable", okListener, cancelListener);
        _addTableDialog.getContentPane().add(addPanel);
        _addTableDialog.pack();
        _addTableDialog.setSize(_paletteFrame.getSize().width - 20, _addTableDialog.getPreferredSize().height);
        _addTableDialog.setLocation(10, 35);
        _addTableDialog.setLocationRelativeTo(_paletteFrame);
        _addTableDialog.toFront();
        _addTableDialog.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    protected void addToTable() {
        String sysname = _sysNametext.getText();
        if (sysname != null && sysname.length() > 1) {
            String uname = _userNametext.getText();
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            try {
                jmri.NamedBean bean = _model.addBean(sysname, uname);
                if (bean != null) {
                    int setRow = _model.getIndexOf(bean);
                    if (log.isDebugEnabled()) {
                        log.debug("addToTable: row= " + setRow + ", bean= " + bean.getDisplayName());
                    }
                    _table.setRowSelectionInterval(setRow, setRow);
                    _scrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
                }
                _addTableDialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(_paletteFrame, ex.getMessage(),
                        Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        _sysNametext.setText("");
        _userNametext.setText("");
    }

    /**
     * Used by Panel Editor to make the final installation of the icon(s) into
     * the user's Panel.
     * <P>
     * Note! the selection is cleared. When two successive calls are made, the
     * 2nd will always return null, regardless of the 1st return.
     */
    public NamedBean getTableSelection() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            NamedBean b = _model.getBeanAt(row);
            _table.clearSelection();
            if (log.isDebugEnabled()) {
                log.debug("getTableSelection: row= " + row + ", bean= " + b.getDisplayName());
            }
            return b;
        } else if (log.isDebugEnabled()) {
            log.debug("getTableSelection: row= " + row);
        }
        return null;
    }

    public void setSelection(NamedBean bean) {
        int row = _model.getIndexOf(bean);
        log.debug("setSelection: NamedBean= " + bean + ", row= " + row);
        if (row >= 0) {
            _table.addRowSelectionInterval(row, row);
            _scrollPane.getVerticalScrollBar().setValue(row * ROW_HEIGHT);
        } else {
            valueChanged(null);
        }
    }

    /**
     * ListSelectionListener action
     */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null || _updateButton == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= " + row);
        }
        if (row >= 0) {
            _updateButton.setEnabled(true);
            _updateButton.setToolTipText(null);

        } else {
            _updateButton.setEnabled(false);
            _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        }
        hideIcons();
    }

    protected NamedBean getNamedBean() {
        if (_table == null) {
            return null;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) {
            log.debug("getNamedBean: from table \"" + _itemType + "\" at row " + row);
        }
        if (row < 0) {
            return null;
        }
        return _model.getBeanAt(row);
    }

    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map) {
        return new IconDragJLabel(flavor, map);
    }

    protected class IconDragJLabel extends DragJLabel {

        /**
         *
         */
        private static final long serialVersionUID = 2477024053040181591L;
        HashMap<String, NamedIcon> iconMap;

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2") // icon map is within package 
        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap == null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }

            if (_itemType.equals("Turnout")) {
                TurnoutIcon t = new TurnoutIcon(_editor);
                t.setTurnout(bean.getDisplayName());
                Iterator<Entry<String, NamedIcon>> iter = iconMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    t.setIcon(ent.getKey(), new NamedIcon(ent.getValue()));
                }
                t.setFamily(_family);
                t.setLevel(Editor.TURNOUTS);
                return t;
            } else if (_itemType.equals("Sensor")) {
                SensorIcon s = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                        "resources/icons/smallschematics/tracksegments/circuit-error.gif"), _editor);
                Iterator<Entry<String, NamedIcon>> iter = iconMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    s.setIcon(ent.getKey(), new NamedIcon(ent.getValue()));
                }
                s.setSensor(bean.getDisplayName());
                s.setFamily(_family);
                s.setLevel(Editor.SENSORS);
                return s;
            } else if (_itemType.equals("Light")) {
                LightIcon l = new LightIcon(_editor);
                l.setOffIcon(iconMap.get("LightStateOff"));
                l.setOnIcon(iconMap.get("LightStateOn"));
                l.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
                l.setUnknownIcon(iconMap.get("BeanStateUnknown"));
                l.setLight((jmri.Light) bean);
                l.setLevel(Editor.LIGHTS);
                return l;
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TableItemPanel.class.getName());
}
