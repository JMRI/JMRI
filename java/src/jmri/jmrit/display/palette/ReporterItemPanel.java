package jmri.jmrit.display.palette;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ReporterIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporterItemPanel extends TableItemPanel {

    /**
     *
     */
    private static final long serialVersionUID = 7260181246347519378L;
    ReporterIcon _reporter;

    public ReporterItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    public void init() {
        if (!_initialized) {
            super.init();
        }
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

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }
        _iconPanel = new JPanel(new FlowLayout());
        _iconFamilyPanel.add(_iconPanel);
        makeDndIconPanel(null, null);
        _iconFamilyPanel.add(_dragIconPanel);
    }

    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _reporter = new ReporterIcon(_editor);
        JPanel panel = new JPanel();
        JPanel comp;
        try {
            comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            comp.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            comp = new JPanel();
        }
        comp.add(_reporter);
        panel.add(comp);
        panel.revalidate();
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        _dragIconPanel = panel;
        _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
    }

    protected JPanel makeItemButtonPanel() {
        return new JPanel();
    }

    /**
     * ListSelectionListener action from table
     */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= " + row);
        }
        if (row >= 0) {
            if (_updateButton != null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            NamedBean bean = getNamedBean();
            _reporter.setReporter(bean.getDisplayName());
        } else {
            if (_updateButton != null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
        }
        validate();
    }

    protected IconDragJComponent getDragger(DataFlavor flavor) {
        return new IconDragJComponent(flavor, _reporter.getPreferredSize());
    }

    protected class IconDragJComponent extends DragJComponent {

        /**
         *
         */
        private static final long serialVersionUID = -7459600899859373554L;

        public IconDragJComponent(DataFlavor flavor, Dimension dim) {
            super(flavor, dim);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean == null) {
                log.error("IconDragJLabel.getTransferData: NamedBean is null!");
                return null;
            }

            ReporterIcon r = new ReporterIcon(_editor);
            r.setReporter(bean.getDisplayName());
            r.setLevel(Editor.REPORTERS);
            return r;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterItemPanel.class.getName());
}
