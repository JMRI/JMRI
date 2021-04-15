package jmri.jmrit.display.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.Reporter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ReporterIcon;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporterItemPanel extends TableItemPanel<Reporter> {

    private final JPanel _dragPanel;  // appearance never changes - just make it once

    public ReporterItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.Reporter> model) {
        super(parentFrame, type, family, model);
        _dragPanel = makeDraggerPanel(parentFrame);
        _currentIconMap = new HashMap<>();
    }

    private JPanel makeDraggerPanel(DisplayFrame frame) {
        JPanel panel = new JPanel();
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        panel.setOpaque(false);
        JPanel dragger;
        ReporterIcon reporter = new ReporterIcon(frame.getEditor());
        try {
            dragger = new IconDragJComponent(new DataFlavor(Editor.POSITIONABLE_FLAVOR), reporter);
        } catch (java.lang.ClassNotFoundException cnfe) {
            log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            dragger = new JPanel();
        }
        dragger.setOpaque(false);
        dragger.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        panel.add(dragger);
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        return panel;
    }

    @Override
    protected void makeDndIcon(HashMap<String, NamedIcon> iconMap) {
        _dragIconPanel.add(_dragPanel);
    }

    @Override
    protected JPanel makeItemButtonPanel() {
        JPanel panel = new JPanel();
        panel.add(makeShowIconsButton());
        return panel;
    }

    @Override
    protected JPanel makeSpecialBottomPanel(boolean update) {
        JPanel panel = new JPanel();
        panel.add(makeShowIconsButton());
        return panel;
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
                ReporterIcon r = new ReporterIcon(_frame.getEditor());
                r.setReporter(bean.getDisplayName());
                r.setLevel(Editor.REPORTERS);
                return r;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return _itemType + " icon for \"" + bean.getDisplayName() + "\"";
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterItemPanel.class);
}
