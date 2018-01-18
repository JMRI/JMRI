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
        _iconPanel = new ImagePanel();
        updateBackgrounds(); // create array of backgrounds

        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon icon = new NamedIcon(entry.getValue()); // make copy for possible reduction
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            String borderName = ItemPalette.convertText(entry.getKey());
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                    borderName));
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
            } catch (java.lang.ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            _iconPanel.add(panel);
        }
        add(_iconPanel, 1);
    }

    @Override
    public void initButtonPanel() {
        add(makeBgButtonPanel(_iconPanel, null, _backgrounds, _paletteFrame));
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
