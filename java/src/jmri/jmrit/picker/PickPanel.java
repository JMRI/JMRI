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
 * Tabbed Container for holding pick list tables
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class PickPanel extends JPanel implements ListSelectionListener, ChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -5093844168716608126L;

    private int ROW_HEIGHT;

    PickListModel[] _models;
    JTabbedPane _tabPane;

    JPanel _addPanel;
    JPanel _cantAddPanel;
    JTextField _sysNametext;
    JTextField _userNametext;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2")
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
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                addToTable();
            }
        };
        _addPanel = new jmri.jmrit.beantable.AddNewDevicePanel(
                _sysNametext, _userNametext, "addToTable", listener);
        _cantAddPanel = new JPanel();
        _cantAddPanel.setLayout(new BorderLayout(5, 5));
        _cantAddPanel.add(new JLabel("Cannot add new items to this pick panel", SwingConstants.CENTER), BorderLayout.NORTH);
        _cantAddPanel.add(new JLabel("Open another tool to add an item.", SwingConstants.CENTER), BorderLayout.SOUTH);
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
            PickListModel model = _models[_tabPane.getSelectedIndex()];
            String uname = _userNametext.getText();
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            jmri.NamedBean bean = model.addBean(sysname, uname);
            if (bean != null) {
                int setRow = model.getIndexOf(bean);
                model.getTable().setRowSelectionInterval(setRow, setRow);
                JPanel p = (JPanel) _tabPane.getSelectedComponent();
                ((JScrollPane) p.getComponent(1)).getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
                _sysNametext.setText("");
            }
        }
    }

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

    public void valueChanged(ListSelectionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("ListSelectionEvent from " + e.getSource().getClass().getName()
                    + " idx= " + e.getFirstIndex());
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PickPanel.class.getName());
}
