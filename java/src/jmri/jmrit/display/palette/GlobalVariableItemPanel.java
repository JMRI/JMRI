package jmri.jmrit.display.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.GlobalVariableComboIcon;
import jmri.jmrit.display.GlobalVariableIcon;
import jmri.jmrit.display.GlobalVariableInputIcon;
import jmri.jmrit.display.GlobalVariableSpinnerIcon;
import jmri.jmrit.display.PreviewPanel;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.JmriJOptionPane;

public class GlobalVariableItemPanel extends TableItemPanel<GlobalVariable> implements ChangeListener {

    enum Type {
        READONLY, READWRITE, SPINNER, COMBO
    }
    JSpinner _spinner;
    String[] list = {"Item1", "Item2", "Item3"};

    public GlobalVariableItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<GlobalVariable> model) {
        super(parentFrame, type, family, model);
    }

    @Override
    public void init() {
        if (!_initialized) {
            add(initTablePanel(_model));
            initIconFamiliesPanel();
            add(_iconFamilyPanel);
            _previewPanel = new PreviewPanel(_frame, _dragIconPanel, _iconPanel, true);
            add(_previewPanel);
            _initialized = true;
        }
    }

    @Override
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("AddGlobalVariableToPanel")));
        blurb.add(new JLabel(Bundle.getMessage("GlobalVariableDragStart")));
        blurb.add(new JLabel(Bundle.getMessage("GlobalVariableDragFix")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("DecorateGlobalVariable")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    protected void hideIcons() {
    }

    /**
     * CENTER Panel
     */
    GlobalVariableIcon _readMem;
    GlobalVariableInputIcon _writeMem;
    GlobalVariableSpinnerIcon _spinMem;
    GlobalVariableComboIcon _comboMem;

    @Override
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        _iconFamilyPanel.setOpaque(true);
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }

        makeDragIconPanel();
        makeDndIcon(null);
        log.debug("initIconFamiliesPanel done");
    }

    @Override
    protected void makeDndIcon(java.util.HashMap<String, NamedIcon> iconMap) {
        if (_update) {
            return;
        }
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new java.awt.GridBagLayout());
        Editor editor = _frame.getEditor();
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;

        JLabel label = new JLabel(Bundle.getMessage("ReadWriteGlobalVariable"));
        label.setOpaque(false);
        panel.add(label, c);
        c.gridy = 1;
        _writeMem = new GlobalVariableInputIcon(5, editor);
        panel.add(makeDragIcon(_writeMem, Type.READWRITE), c);

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        label = new JLabel(Bundle.getMessage("ReadGlobalVariable"));
        label.setOpaque(false);
        panel.add(label, c);
        c.gridy = 1;
        _readMem = new GlobalVariableIcon("_____", editor);
        panel.add(makeDragIcon(_readMem, Type.READONLY), c);

        c.gridx = 2;
        c.gridy = 0;
        label = new JLabel(Bundle.getMessage("SpinnerGlobalVariable"));
        label.setOpaque(false);
        panel.add(label, c);
        c.gridy = 1;
        _spinMem = new GlobalVariableSpinnerIcon(editor);
        panel.add(makeDragIcon(_spinMem, Type.SPINNER), c);

        c.gridx = 3;
        c.gridy = 0;
        label = new JLabel(Bundle.getMessage("ComboGlobalVariable"));
        label.setOpaque(false);
        panel.add(label, c);
        c.gridy = 1;
        String[] list = {"item1", "Item2", "Item3"};
        _comboMem = new GlobalVariableComboIcon(editor, list);
        panel.add(makeDragIcon(_comboMem, Type.COMBO), c);

        _spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JTextField field = ((JSpinner.DefaultEditor) _spinner.getEditor()).getTextField();
        field.setColumns(2);
        field.setText("5");
        _spinner.setMaximumSize(_spinner.getPreferredSize());
        _spinner.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        panel.add(_spinner, c);

        c.gridy = 3;
        c.anchor = java.awt.GridBagConstraints.NORTH;
        label = new JLabel(Bundle.getMessage("GlobalVariableWidth"));
        label.setOpaque(false);
        panel.add(label, c);

        _dragIconPanel.add(panel);
        _dragIconPanel.invalidate();
    }

    private JPanel makeDragIcon(JComponent mem, Type type) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JPanel comp;
        try {
            comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), type, mem);
            comp.setOpaque(false);
            comp.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            comp = new JPanel();
        }
        panel.add(comp);
        return panel;
    }

    /*
     * Set column width for InputMemoryIcon.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("stateChanged: class= {}, value= {}", _spinner.getValue().getClass().getName(),
                    _spinner.getValue());
        }
        Integer nCols = (Integer) _spinner.getValue();
        _writeMem.setNumColumns(nCols);
    }

    /**
     * ListSelectionListener action from table.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        log.debug("Table valueChanged: row= {}", row);
        if (row >= 0) {
            if (_updateButton != null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
        } else {
            if (_updateButton != null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
            _dragIconPanel.removeAll();
            makeDragIconPanel();
            makeDndIcon(null); // use override
        }
        validate();
    }

    protected IconDragJComponent getDragger(DataFlavor flavor, Type type, JComponent comp) {
        return new IconDragJComponent(flavor, type, comp);
    }

    protected class IconDragJComponent extends DragJComponent {

        Type _memType;

        public IconDragJComponent(DataFlavor flavor, Type type, JComponent comp) {
            super(flavor, comp);
            _memType = type;
        }

        @Override
        protected boolean okToDrag() {
            GlobalVariable bean = getDeviceNamedBean();
            if (bean == null) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            GlobalVariable bean = getDeviceNamedBean();
            if (bean == null) {
                log.error("IconDragJComponent.getTransferData: GlobalVariable is null!");
                return null;
            }

            Editor editor = _frame.getEditor();
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                int numCols = 5;
                try {
                    ((JSpinner.DefaultEditor) _spinner.getEditor()).commitEdit();
                    SpinnerNumberModel spinModel = (SpinnerNumberModel) _spinner.getModel();
                    if (log.isDebugEnabled()) {
                        log.debug("GlobalVariableDnD.createTransferable: spinCols= {}", spinModel.getNumber().intValue());
                    }
                    numCols = spinModel.getNumber().intValue();
                } catch (java.text.ParseException pe) {
                    log.error("GlobalVariableDnD.createTransferable: ", pe);
                }
                switch (_memType) {
                    case READONLY:
                        GlobalVariableIcon m = new GlobalVariableIcon("", editor);
                        m.setGlobalVariable(bean.getDisplayName());
//                        m.setSize(m.getPreferredSize().width, m.getPreferredSize().height);
                        m.getPopupUtility().setFixedWidth(numCols*10);
                        m.setLevel(Editor.MEMORIES);
                        return m;
                    case READWRITE:
                        GlobalVariableInputIcon mi = new GlobalVariableInputIcon(numCols, editor);
                        mi.setGlobalVariable(bean.getDisplayName());
                        mi.setSize(mi.getPreferredSize().width, mi.getPreferredSize().height);
                        mi.setLevel(Editor.MEMORIES);
                        return mi;
                    case SPINNER:
                        GlobalVariableSpinnerIcon ms = new GlobalVariableSpinnerIcon(editor);
                        ms.setGlobalVariable(bean.getDisplayName());
                        ms.setSize(ms.getPreferredSize().width, ms.getPreferredSize().height);
                        ms.setLevel(Editor.MEMORIES);
                        return ms;
                    case COMBO:
                        GlobalVariableComboIcon mc = new GlobalVariableComboIcon(editor, list);
                        mc.setGlobalVariable(bean.getDisplayName());
                        mc.setSize(mc.getPreferredSize().width, mc.getPreferredSize().height);
                        mc.setLevel(Editor.MEMORIES);
                        return mc;
                    default:
                        // fall through
                        break;
                }
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return _itemType + " icons for \"" + bean.getDisplayName() + "\"";
            }
            return null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalVariableItemPanel.class);

}
