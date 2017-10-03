package jmri.jmrit.picker;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import jmri.*;

/**
 * Container with a single PickList table
 * <p>
 * See also PickTabbedPanel for multiple panes with multiple tables
 *
 * @author Bob Jacobsen  Copyright (c) 2017
 * @author Pete Cressman Copyright (c) 2010
 */
public class PickSinglePanel<T extends NamedBean> extends JPanel {

    private int ROW_HEIGHT;

    PickListModel _model;

    JPanel _addPanel;
    JPanel _cantAddPanel;
    JTextField _sysNametext;
    JTextField _userNametext;
    JTable _table;
    JScrollPane _scroll;

    public PickSinglePanel(PickListModel model) {
        _model = model;
        _table = _model.makePickTable();
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setCellSelectionEnabled(true);
        _table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = getTable().getSelectedRow();
                int col = getTable().getSelectedColumn(); // might be -1 if just inserted
                if (col != 1) return;
                if (row >= 0) {
                    String username = (String) _model.getTable().getValueAt(row, 1);
                    if (username != null) return;
                }
                // have to set selection to col 0
                _model.getTable().setColumnSelectionInterval(0,0);
            }
        });
        
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(5, 5));
        p.add(new JLabel(_model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(_scroll = new JScrollPane(_table), BorderLayout.CENTER);
        ROW_HEIGHT = _table.getRowHeight();

        setLayout(new BorderLayout(5, 5));
        add(p, BorderLayout.CENTER);
        add(makeAddToTablePanel(), BorderLayout.SOUTH);
    }

    public NamedBeanHandle<T> getSelectedBeanHandle() {
        int row = getTable().getSelectedRow();
        int col = getTable().getSelectedColumn(); // might be -1 if just inserted
        System.out.println(" r c "+row+" "+col);
        
        // are we sure this is always col 0 for sysname and col 1 for user name?
        String sysname = _model.getTable().getValueAt(row, 0).toString();
        String username = (String) _model.getTable().getValueAt(row, 1);
        
        String beanName = sysname;
        if (col == 1 && username != null) beanName = username;
        jmri.NamedBean bean = _model.addBean(sysname, username);
        return InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(beanName, (T)bean);
    }
    
    public JTable getTable() { return _table; }
    
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
        int width = Math.max(100, this.getPreferredSize().width);
        _sysNametext.setPreferredSize(new java.awt.Dimension(width, _sysNametext.getPreferredSize().height));
        return p;
    }

    void addToTable() {
        String sysname = _model.getManager().normalizeSystemName(_sysNametext.getText());
        
        if (sysname != null && sysname.length() > 1) {
            String uname = NamedBean.normalizeUserName(_userNametext.getText());
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            jmri.NamedBean bean = _model.addBean(sysname, uname);
            if (bean != null) {
                int setRow = _model.getIndexOf(bean);
                _model.getTable().setRowSelectionInterval(setRow, setRow);
                _scroll.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
                _sysNametext.setText("");
            }
        }
    }

    // initialize logging
    // private final static Logger log = LoggerFactory.getLogger(PickSinglePanel.class);
}
