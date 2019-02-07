package jmri.jmrit.display.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import jmri.Reporter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ReporterIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporterItemPanel extends TableItemPanel<Reporter> {

    ReporterIcon _reporter;

    public ReporterItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.Reporter> model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("PickRowReporter")));
        blurb.add(new JLabel(Bundle.getMessage("DragReporter")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    /**
     * ReporterItemPanel displays no _iconFamilyPanel.
     */
    @Override
    protected void initIconFamiliesPanel() {
        if (_iconFamilyPanel == null) {
            log.debug("new _iconFamilyPanel created");
            _iconFamilyPanel = new JPanel();
            _iconFamilyPanel.setOpaque(true);
            _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
            if (!_update) {
                _iconFamilyPanel.add(instructions());
            }
        }
        makeDragIconPanel(1);
        makeDndIconPanel(null, null);
        if (_iconPanel == null) { // keep an existing panel
            _iconPanel = new ImagePanel(); // never shown, so don't bother to configure, but element must exist
        }
        _iconFamilyPanel.add(makePreviewPanel(null, _dragIconPanel));
    }

    @Override
    protected void makeBottomPanel(ActionListener doneAction) {
        if (doneAction != null) {
            addUpdateButtonToBottom(doneAction);
        }
 //       initIconFamiliesPanel();
        add(_iconFamilyPanel);
        // ReporterItem extends FamilyItemPanel and needs a non-null _showIconsButton for setEditor call
        _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
   }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _reporter = new ReporterIcon(_editor);
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JPanel comp;
        try {
            comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
        } catch (java.lang.ClassNotFoundException cnfe) {
            log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            comp = new JPanel();
        }
        comp.setOpaque(false);
        comp.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        panel.add(comp);
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        _dragIconPanel.add(panel);
        _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
    }

    @Override
    protected JPanel makeItemButtonPanel() {
        return new JPanel();
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
        log.debug("Table valueChanged: row = {}", row);
        if (row >= 0) {
            if (_updateButton != null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            Reporter bean = getDeviceNamedBean();
            _reporter.setReporter(bean.getDisplayName());
        } else {
            if (_updateButton != null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
                _reporter = new ReporterIcon(_editor);
            }
        }
        validate();
    }

    protected IconDragJComponent getDragger(DataFlavor flavor) {
        return new IconDragJComponent(flavor, _reporter);
    }

    protected class IconDragJComponent extends DragJComponent {

        public IconDragJComponent(DataFlavor flavor, JComponent comp) {
            super(flavor, comp);
        }

        @Override
        protected boolean okToDrag() {
            Reporter bean = getDeviceNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            Reporter bean = getDeviceNamedBean();
            if (bean == null) {
                return null;
            }

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                ReporterIcon r = new ReporterIcon(_editor);
                r.setReporter(bean.getDisplayName());
                r.setLevel(Editor.REPORTERS);
                return r;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icon for \"");
                sb.append(bean.getDisplayName());
                sb.append("\"");
                return  sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterItemPanel.class);

}
