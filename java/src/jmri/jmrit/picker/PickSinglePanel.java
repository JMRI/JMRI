package jmri.jmrit.picker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container with a single PickList table
 * <p>
 * See also PickTabbedPanel for multiple panes with multiple tables
 *
 * @author Bob Jacobsen  Copyright (c) 2017
 * @author Pete Cressman Copyright (c) 2010
 */
public class PickSinglePanel extends JPanel implements ListSelectionListener, ChangeListener {

    private int ROW_HEIGHT;

    PickListModel _model;
    JTabbedPane _tabPane;

    JPanel _addPanel;
    JPanel _cantAddPanel;
    JTextField _sysNametext;
    JTextField _userNametext;
    jmri.jmrit.picker.PickFrame _pickTables; // Opened from LogixTableAction

    public PickSinglePanel(PickListModel model) {
        _tabPane = new JTabbedPane();
        _model = model;
        JTable table = _model.makePickTable();
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(5, 5));
        p.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        ROW_HEIGHT = table.getRowHeight();

        setLayout(new BorderLayout(5, 5));
        add(p, BorderLayout.CENTER);
        add(makeAddToTablePanel(), BorderLayout.SOUTH);
        _tabPane.addChangeListener(this);
    }

    private JPanel makeAddToTablePanel() {
        _sysNametext = new JTextField();
        _userNametext = new JTextField();

        ActionListener cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                //do nothing as Cancel button is hidden on Pick Lists
            }
        };

        ActionListener okListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                addToTable();
            }
        };
        _addPanel = new jmri.jmrit.beantable.AddNewDevicePanel(
                _sysNametext, _userNametext, "addToTable", okListener, cancelListener); // No I18N
        // hide Cancel button as not handled bij Picker Panel

        _cantAddPanel = new JPanel();
        _cantAddPanel.setLayout(new BorderLayout(5, 5));
        _cantAddPanel.add(new JLabel(Bundle.getMessage("CantAddNew"), SwingConstants.CENTER), BorderLayout.NORTH);
        _cantAddPanel.add(new JLabel(Bundle.getMessage("OpenToAdd"), SwingConstants.CENTER), BorderLayout.SOUTH);
        JPanel p = new JPanel();
        p.add(_addPanel);
        p.add(_cantAddPanel);
        stateChanged(null);
        int width = Math.max(100, this.getPreferredSize().width);
        _sysNametext.setPreferredSize(new java.awt.Dimension(width, _sysNametext.getPreferredSize().height));
        return p;
    }

    void addToTable() {
        String sysname = _sysNametext.getText();
        if (sysname != null && sysname.length() > 1) {
            String uname = _userNametext.getText();
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            jmri.NamedBean bean = _model.addBean(sysname, uname);
            if (bean != null) {
                int setRow = _model.getIndexOf(bean);
                _model.getTable().setRowSelectionInterval(setRow, setRow);
                JPanel p = (JPanel) _tabPane.getSelectedComponent();
                ((JScrollPane) p.getComponent(1)).getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
                _sysNametext.setText("");
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (_model.canAddBean()) {
            _cantAddPanel.setVisible(false);
            _addPanel.setVisible(true);
        } else {
            _addPanel.setVisible(false);
            _cantAddPanel.setVisible(true);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("ListSelectionEvent from " + e.getSource().getClass().getName()
                    + " idx= " + e.getFirstIndex());
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PickSinglePanel.class.getName());
}
