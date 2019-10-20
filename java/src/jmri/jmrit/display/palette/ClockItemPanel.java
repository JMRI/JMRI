package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.AnalogClock2Display;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for Clocks.
 */
public class ClockItemPanel extends IconItemPanel {

    public ClockItemPanel(DisplayFrame parentFrame, String type, Editor editor) {
        super(parentFrame, type, editor);
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
    protected void addIconsToPanel(HashMap<String, NamedIcon> iconMap) {
        if (_iconPanel == null) {
            _iconPanel = new ImagePanel();            
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        } else {
            _iconPanel.removeAll();
        }

        for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
            NamedIcon icon = new NamedIcon(entry.getValue()); // make copy for possible reduction
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            String borderName = ItemPalette.convertText(entry.getKey());
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), borderName));
            try {
                JLabel label = new ClockDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
                if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                    label.setText(Bundle.getMessage("invisibleIcon"));
                    label.setForeground(Color.lightGray);
                } else {
                    icon.reduceTo(100, 100, 0.2);
                }
                label.setIcon(icon);
                label.setName(borderName);
                panel.add(label);
            } catch (ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            }
            _iconPanel.add(panel);
        }
        _iconPanel.setImage(_backgrounds[_paletteFrame.getPreviewBg()]); // pick up shared setting
    }

    public class ClockDragJLabel extends DragJLabel {

        public ClockDragJLabel(DataFlavor flavor) {
            super(flavor);
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
                    c = new AnalogClock2Display(_editor);
                } else {
                    c = new AnalogClock2Display(_editor, link);
                }
                c.setOpaque(false);
                c.update();
                c.setLevel(Editor.CLOCK);
                return c;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icon \"");
                sb.append(url);
                sb.append("\"");
                return sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ClockItemPanel.class);

}
