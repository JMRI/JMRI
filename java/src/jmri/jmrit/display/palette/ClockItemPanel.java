package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.AnalogClock2Display;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for Clocks.
 */
public class ClockItemPanel extends IconItemPanel {

    public ClockItemPanel(DisplayFrame parentFrame, String type) {
        super(parentFrame, type);
        setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
    }

    @Override
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("AddClockToPanel", Bundle.getMessage("FastClock"))));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    public void init() {
        if (!_initialized) {
            initIconFamiliesPanel();
            initLinkPanel();
            add(Box.createVerticalGlue());
        }
        _previewPanel.invalidate();
        hideIcons();
    }

    @Override
    protected JPanel makeIconDisplayPanel(String key, HashMap<String, NamedIcon> iconMap, boolean dropIcon) {
        NamedIcon icon = iconMap.get(key);
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JLabel label;
        if (dropIcon) {
            try {
            label = new ClockDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR), icon);
            } catch (ClassNotFoundException cnfe) {
                label = new JLabel(cnfe.toString());
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            }
        } else {
            label = new JLabel(icon);
        }
        if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
            label.setText(Bundle.getMessage("invisibleIcon"));
            label.setForeground(Color.lightGray);
        }
        wrapIconImage(icon, label, panel, key);
        return panel;
    }

    public class ClockDragJLabel extends DragJLabel {

        public ClockDragJLabel(DataFlavor flavor, NamedIcon icon) {
            super(flavor, icon);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon) getIcon()).getURL();
            log.debug("DragJLabel.getTransferData url= {}", url);
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                AnalogClock2Display c;
                String link = _linkName.getText().trim();
                if (link.length() == 0) {
                    c = new AnalogClock2Display(_frame.getEditor());
                } else {
                    c = new AnalogClock2Display(_frame.getEditor(), link);
                }
                c.setOpaque(false);
                c.update();
                c.setLevel(Editor.CLOCK);
                return c;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return _itemType + " icon \"" + url + "\"";
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ClockItemPanel.class);

}
