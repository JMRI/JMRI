package jmri.jmrit.picker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
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
 * Tabbed Container for holding pick list tables
 * <p>
 * Should perhaps be called PickTabbedPanel to distinguish from PickSinglePanel
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class PickPanel extends JPanel implements ListSelectionListener, ChangeListener {

    private int ROW_HEIGHT;

    PickListModel[] _models;
    JTabbedPane _tabPane;

    JPanel _addPanel;
    JPanel _cantAddPanel;
    JTextField _sysNametext;
    JTextField _userNametext;
    jmri.jmrit.picker.PickFrame _pickTables; // Opened from LogixTableAction

    public PickPanel(PickListModel[] models) {
        _tabPane = new JTabbedPane();
        _models = new PickListModel[models.length];
        System.arraycopy(models, 0, _models, 0, models.length);
        for (int i = 0; i < models.length; i++) {
            JTable table = models[i].makePickTable();
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout(5, 5));
            p.add(new JLabel(models[i].getName(), SwingConstants.CENTER), BorderLayout.NORTH);
            p.add(new JScrollPane(table), BorderLayout.CENTER);
            _tabPane.add(p, models[i].getName());
            ROW_HEIGHT = table.getRowHeight();
        }
        setLayout(new BorderLayout(5, 5));
        add(_tabPane, BorderLayout.CENTER);
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

    @SuppressWarnings("unchecked") // PickList is a parameterized class, but we don't use that here
    void addToTable() {
        String sysname = _sysNametext.getText();
        if (sysname != null && sysname.length() > 1) {
            PickListModel model = _models[_tabPane.getSelectedIndex()];
            String uname = _userNametext.getText();
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            jmri.NamedBean bean = null;
            try {
                bean = model.addBean(sysname, uname);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("PickAddFailed", ex.getMessage()),  // NOI18N
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            }
            if (bean != null) {
                int setRow = model.getIndexOf(bean);
                model.getTable().setRowSelectionInterval(setRow, setRow);
                JPanel p = (JPanel) _tabPane.getSelectedComponent();
                ((JScrollPane) p.getComponent(1)).getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
                _sysNametext.setText("");
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        PickListModel model = _models[_tabPane.getSelectedIndex()];
        if (model.canAddBean()) {
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
    private final static Logger log = LoggerFactory.getLogger(PickPanel.class);
}
