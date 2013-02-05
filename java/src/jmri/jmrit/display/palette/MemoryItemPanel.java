package jmri.jmrit.display.palette;

import org.apache.log4j.Logger;
import java.awt.Dimension;
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
import jmri.jmrit.display.MemoryComboIcon;

public class MemoryItemPanel extends TableItemPanel implements ChangeListener, ListSelectionListener {

    enum Type { READONLY, READWRITE, SPINNER, COMBO }
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
        blurb.add(new JLabel(Bundle.getMessage("AddToPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconPanel")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("nullMemoryIcon"), 
                             NamedIcon.getIconByName("resources/icons/misc/X-red.gif"),
                             javax.swing.SwingConstants.TRAILING));
        blurb.add(new JLabel(Bundle.getMessage("emptyMemoryIcon")));
        blurb.add(new JLabel(Bundle.getMessage("emptyMemoryFix")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("comboMemory1")));
        blurb.add(new JLabel(Bundle.getMessage("comboMemory2")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    /**
    *  CENTER Panel
    */
    MemoryIcon      	_readMem;
    MemoryInputIcon 	_writeMem;
    JPanel 				_writePanel;
    MemorySpinnerIcon   _spinMem;
    MemoryComboIcon 	_comboMem;

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }
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

        panel.add(new JLabel(Bundle.getMessage("ReadWriteMemory")), c);

        _writeMem = new MemoryInputIcon(5, _editor);
        JPanel p0 = makeDragIcon(_writeMem, Type.READWRITE);
        _spinner = new JSpinner(new SpinnerNumberModel(0,0,100,1));
        JTextField field = ((JSpinner.DefaultEditor)_spinner.getEditor()).getTextField();
        field.setColumns(2);
        field.setText("5");
        _spinner.setMaximumSize(_spinner.getPreferredSize());
        _spinner.addChangeListener(this);
        JPanel p1 = new JPanel();
        p1.add(new JLabel(ItemPalette.rb.getString("NumColsLabel")));
        p1.add(_spinner);
        JPanel p2 =new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(p0);
        p2.add(p1);
        c.gridy = 1;
        panel.add(p2, c);
        
        c.gridx = 1;
        c.gridy = 0;
        panel.add(new JLabel(Bundle.getMessage("ReadMemory")), c);
        c.gridy = 1;
        _readMem = new MemoryIcon(NamedIcon.getIconByName("resources/icons/misc/X-red.gif"), _editor);
        panel.add(makeDragIcon(_readMem, Type.READONLY), c);
        
        c.gridx = 2;
        c.gridy = 0;
        panel.add(new JLabel(Bundle.getMessage("SpinnerMemory")), c);
        c.gridy = 1;
        _spinMem = new MemorySpinnerIcon(_editor);
        panel.add(makeDragIcon(_spinMem, Type.SPINNER), c);
        
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth  = 4;
        panel.add(new JLabel(Bundle.getMessage("ComboMemory")), c);
        c.gridy = 3;
        _comboMem = new MemoryComboIcon(_editor, null);
        panel.add(makeDragIcon(_comboMem, Type.COMBO), c);
        
        _dragIconPanel = panel;
    }
    
    private JPanel makeDragIcon(JComponent mem, Type type) {
        JPanel panel = new JPanel();
        JPanel comp;
        try {
            comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), type,
            			mem.getPreferredSize());
            comp.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            comp = new JPanel();
        }
        comp.add(mem);
        panel.add(comp);
        panel.validate();
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
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
        }
        validate();
    }

    protected IconDragJComponent getDragger(DataFlavor flavor, Type type, Dimension dim ) {
        return new IconDragJComponent(flavor, type, dim);
    }

    protected class IconDragJComponent extends DragJComponent {
        Type _memType;

        public IconDragJComponent(DataFlavor flavor, Type type, Dimension dim) {
            super(flavor, dim);
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
                case COMBO:
                    MemoryComboIcon mc = new MemoryComboIcon(_editor, null);
                    mc.setMemory(bean.getDisplayName());
                    mc.setSize(mc.getPreferredSize().width, mc.getPreferredSize().height);
                    mc.setLevel(Editor.MEMORIES);
                    return mc;
            }
            return null;
        }
    }

    static Logger log = Logger.getLogger(MemoryItemPanel.class.getName());
}
