package jmri.jmrit.picker;


import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent; 

public class PickPanel extends JPanel implements ListSelectionListener {

    static final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    private static int ROW_HEIGHT;

    JTable[]    _tables;
    JTabbedPane _tabPane;

    JButton         _addTableButton;
    JTextField      _sysNametext;
    JTextField      _userNametext;

    public PickPanel(PickListModel[] models) {
        _tabPane = new JTabbedPane();
        _tables =new JTable[models.length];
        for (int i=0; i<models.length; i++) {
            _tables[i] = models[i].makePickTable();
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout(5,5));
            p.add(new JLabel(models[i].getName(), SwingConstants.CENTER), BorderLayout.NORTH);
            p.add(new JScrollPane(_tables[i]), BorderLayout.CENTER);
            _tabPane.add(p, models[i].getName());
        }
        setLayout(new BorderLayout(5,5));
        add(_tabPane, BorderLayout.CENTER);
        add(makeAddToTablePanel(), BorderLayout.SOUTH);
    }

    private JTable makeTable(PickListModel model) {
        model.init();
        JTable table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setDragEnabled(true);
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn sNameColumnT = columnModel.getColumn(PickListModel.SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(50);
        sNameColumnT.setMaxWidth(200);

        TableColumn uNameColumnT = columnModel.getColumn(PickListModel.UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(100);
        uNameColumnT.setMaxWidth(300);

        ROW_HEIGHT = table.getRowHeight();
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(200,7*ROW_HEIGHT));
        return table;
    }

    private JPanel makeAddToTablePanel() {

        _sysNametext = new JTextField();
        _userNametext = new JTextField();
        ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addToTable();
                }
            };
        return new jmri.jmrit.beantable.AddNewDevicePanel(
                    _sysNametext, _userNametext, "addToTable", listener);
    }
    void addToTable() {
        String name = _sysNametext.getText();
        if (name != null && name.length() > 0) {
            JTable table = _tables[_tabPane.getSelectedIndex()];
            PickListModel model = (PickListModel)table.getModel();
            jmri.NamedBean bean = model.addBean(name, _userNametext.getText());
            int setRow = model.getIndexOf(bean);
            table.setRowSelectionInterval(setRow, setRow);
            ((JScrollPane)_tabPane.getSelectedComponent()).getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
        }
        _sysNametext.setText("");
    }


    public void valueChanged(ListSelectionEvent e) {
        if (log.isDebugEnabled()) log.debug("ListSelectionEvent from "+e.getSource().getClass().getName()
                                            +" idx= "+e.getFirstIndex());
        //PickListModel model =  (PickListModel)e.getSource();
        int row = e.getFirstIndex();
        /*
        if (row >= 0) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(rb.getString("ToolTipPickFromTable"));
            if (log.isDebugEnabled()) log.debug("_addButton.setEnabled(false): row= "+row);
        }
        */
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PickPanel.class.getName());
}

