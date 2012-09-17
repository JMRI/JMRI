package jmri.jmrit.display.palette;


import java.awt.Color;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.catalog.NamedIcon;

import jmri.NamedBean;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemoryIcon;
import jmri.jmrit.display.MemoryInputIcon;
import jmri.jmrit.display.MemorySpinnerIcon;

public class MemoryItemPanel extends TableItemPanel implements ChangeListener, ListSelectionListener {

    enum Type { READONLY, READWRITE, SPINNER }
    JSpinner _spinner;

    public MemoryItemPanel(ItemPalette parentFrame, String type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    public void init() {
        add(initTablePanel(_model, _editor));
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(ItemPalette.rbp.getString("AddToPanel")));
        blurb.add(new JLabel(ItemPalette.rbp.getString("DragIconPanel")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(ItemPalette.rbp.getString("nullMemoryIcon"), 
                             NamedIcon.getIconByName("resources/icons/misc/X-red.gif"),
                             javax.swing.SwingConstants.TRAILING));
        blurb.add(new JLabel(ItemPalette.rbp.getString("emptyMemoryIcon")));
        blurb.add(new JLabel(ItemPalette.rbp.getString("emptyMemoryFix")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    /**
    *  CENTER Panel
    */
    MemoryIcon      _readMem;
    MemoryInputIcon _writeMem;
    JPanel _writePanel;
    MemorySpinnerIcon   _spinMem;

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }
        _dragIconPanel = new JPanel();
        makeDndIconPanel(null, null);

        _iconFamilyPanel.add(_dragIconPanel);
    }

    protected void makeDndIconPanel(java.util.Hashtable<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;

        panel.add(new JLabel(ItemPalette.rbp.getString("ReadMemory")), c);
        c.gridy = 1;
        _readMem = new MemoryIcon(NamedIcon.getIconByName("resources/icons/misc/X-red.gif"), _editor);
        panel.add(makeDragIcon(_readMem, Type.READONLY), c);

        c.gridx = 1;
        c.gridy = 0;
        panel.add(new JLabel(ItemPalette.rbp.getString("ReadWriteMemory")), c);
        c.gridy = 1;

        _writeMem = new MemoryInputIcon(5, _editor);
        _writePanel = makeDragIcon(_writeMem, Type.READWRITE);
        panel.add(_writePanel, c);

        JPanel p2 = new JPanel();
        _spinner = new JSpinner(new SpinnerNumberModel(0,0,100,1));
        JTextField field = ((JSpinner.DefaultEditor)_spinner.getEditor()).getTextField();
        field.setColumns(2);
        field.setText("5");
        _spinner.setMaximumSize(_spinner.getPreferredSize());
        _spinner.addChangeListener(this);
        p2.add(new JLabel(ItemPalette.rb.getString("NumColsLabel")));
        p2.add(_spinner);
        c.gridy = 2;
        panel.add(p2, c);

        c.gridx = 2;
        c.gridy = 0;
        panel.add(new JLabel(ItemPalette.rbp.getString("SpinnerMemory")), c);
        c.gridy = 1;

        _spinMem = new MemorySpinnerIcon(_editor);
        panel.add(makeDragIcon(_spinMem, Type.SPINNER), c);
        _dragIconPanel = panel;
//        add(panel, 1);
    }
    private JPanel makeDragIcon(JComponent mem, Type type) {
        JPanel panel = new JPanel();
        /*
        String borderName = ItemPalette.convertText("dragToPanel");
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                         borderName));
                                                         */
        JPanel comp;
        try {
            comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), type);
            comp.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            comp = new JPanel();
        }
        comp.add(mem);
        panel.add(comp);
        panel.validate();
        /*
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        */
        return panel;
    }

    /*
    * Set column width for InputMemoryIcon
    */
    public void stateChanged(ChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("stateChanged: class= "+_spinner.getValue().getClass().getName()+
                                            ", value= "+_spinner.getValue());

        Integer nCols = (Integer)_spinner.getValue();
        _writeMem.setNumColumns(nCols.intValue());
//        _writeMem.validate();
        _writePanel.validate();
    }

    /**
    *  ListSelectionListener action from table
    */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) log.debug("Table valueChanged: row= "+row);
        if (row >= 0) {
            if (_updateButton!=null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            NamedBean bean = getNamedBean();
            _readMem.setMemory(bean.getDisplayName());
            _writeMem.setMemory(bean.getDisplayName());
            _spinMem.setMemory(bean.getDisplayName());
        } else {
            if (_updateButton!=null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
            }
        }
        validate();
    }

    protected IconDragJComponent getDragger(DataFlavor flavor, Type type) {
        return new IconDragJComponent(flavor, type);
    }

    protected class IconDragJComponent extends DragJComponent {
        Type _memType;

        public IconDragJComponent(DataFlavor flavor, Type type) {
            super(flavor);
            _memType = type;
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean==null) {
                log.error("IconDragJComponent.getTransferData: NamedBean is null!");
                return null;
            }

            switch (_memType) {
                case READONLY:
                    MemoryIcon m = new MemoryIcon("", _editor);
                    m.setMemory(bean.getDisplayName());
                    m.setSize(m.getPreferredSize().width, m.getPreferredSize().height);
                    m.setLevel(Editor.MEMORIES);
                    return m;
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
                    mi.setLevel(Editor.MEMORIES);
                    return mi;
                case SPINNER:
                    MemorySpinnerIcon ms = new MemorySpinnerIcon(_editor);
                    ms.setMemory(bean.getDisplayName());
                    ms.setSize(ms.getPreferredSize().width, ms.getPreferredSize().height);
                    ms.setLevel(Editor.MEMORIES);
                    return ms;
            }
            return null;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryItemPanel.class.getName());
}
