package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Transferable;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.jmrit.display.MemoryIcon;
import jmri.jmrit.display.MemoryInputIcon;
import jmri.jmrit.display.MemorySpinnerIcon;

public class MemoryItemPanel extends TableItemPanel {

    enum Type { READONLY, READWRITE, SPINNER }
    Type _memType;
    JSpinner _spinner;

    public MemoryItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame, itemType, model, editor);
    }

    public void init() {
        initTablePanel(_model, _editor);        // NORTH Panel
        initIconFamiliesPanel();                // CENTER Panel
    }

    /**
    *  NORTH Panel
    */
    protected void initTablePanel(PickListModel model, Editor editor) {
        super.initTablePanel(model, editor);
        _table.setTransferHandler(new MemoryDnD(editor));
    }

    /**
    *  CENTER Panel
    */
    protected void initIconFamiliesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        ButtonGroup group = new ButtonGroup();

        panel.add(new JLabel(ItemPalette.rbp.getString("ReadMemory")), c);
        JRadioButton button = new JRadioButton();
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _memType = Type.READONLY;
                }
            });
        c.gridy = 0;
        c.gridy = 1;
        panel.add(button, c);
        group.add(button);
        button.setSelected(true);
        _memType = Type.READONLY;

        c.gridx = 1;
        c.gridy = 0;
        panel.add(new JLabel(ItemPalette.rbp.getString("ReadWriteMemory")), c);
        button = new JRadioButton();
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _memType = Type.READWRITE;
                }
            });
        c.gridy = 1;
        panel.add(button, c);
        group.add(button);
        JPanel p2 = new JPanel();
        _spinner = new JSpinner(new SpinnerNumberModel(0,0,100,1));
        JTextField field = ((JSpinner.DefaultEditor)_spinner.getEditor()).getTextField();
        field.setColumns(2);
        field.setText("5");
        _spinner.setMaximumSize(_spinner.getPreferredSize());
        p2.add(new JLabel(ItemPalette.rb.getString("NumColsLabel")));
        p2.add(_spinner);
        c.gridy = 2;
        panel.add(p2, c);

        c.gridx = 2;
        c.gridy = 0;
        panel.add(new JLabel(ItemPalette.rbp.getString("SpinnerMemory")), c);
        button = new JRadioButton();
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _memType = Type.SPINNER;
                }
            });
        c.gridy = 1;
        panel.add(button, c);
        group.add(button);

        add(panel, BorderLayout.CENTER);
    }

    /**
    * Extend handler to export from JList and import to PicklistTable
    */
    protected class MemoryDnD extends DnDTableItemHandler {

        MemoryDnD(Editor editor) {
            super(editor);
        }

        public Transferable createPositionableDnD(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("MemoryDnD.createTransferable: from table \""+_itemType+ "\" at ("
                                                +row+", "+col+") for data \""
                                                +table.getModel().getValueAt(row, col)+"\"");
            if (col<0 || row<0) {
                return null;
            }            
            PickListModel model = (PickListModel)table.getModel();
            NamedBean bean = model.getBeanAt(row);
            switch (_memType) {
                case READONLY:
                    MemoryIcon m = new MemoryIcon("", _editor);
                    m.setMemory(bean.getDisplayName());
                    m.setSize(m.getPreferredSize().width, m.getPreferredSize().height);
                    m.setDisplayLevel(Editor.MEMORIES);
                    return new PositionableDnD(m, bean.getDisplayName());
                case READWRITE:
                    int numCols = 5;
                    try {
                        ((JSpinner.DefaultEditor)_spinner.getEditor()).commitEdit();
                        SpinnerNumberModel spinModel = (SpinnerNumberModel)_spinner.getModel();
                        if (log.isDebugEnabled()) log.debug("MemoryDnD.createTransferable: spinCols= " 
                                                            +spinModel.getNumber().intValue());
                       numCols = spinModel.getNumber().intValue();
                    } catch (java.text.ParseException pe) {
                        log.error("MemoryDnD.createTransferable: "+pe);
                    }
                    MemoryInputIcon mi = new MemoryInputIcon(numCols, _editor);
                    mi.setMemory(bean.getDisplayName());
                    mi.setSize(mi.getPreferredSize().width, mi.getPreferredSize().height);
                    mi.setDisplayLevel(Editor.MEMORIES);
                    return new PositionableDnD(mi, bean.getDisplayName());
                case SPINNER:
                    MemorySpinnerIcon ms = new MemorySpinnerIcon(_editor);
                    ms.setMemory(bean.getDisplayName());
                    ms.setSize(ms.getPreferredSize().width, ms.getPreferredSize().height);
                    ms.setDisplayLevel(Editor.MEMORIES);
                    return new PositionableDnD(ms, bean.getDisplayName());
            }
            return null;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryItemPanel.class.getName());
}
