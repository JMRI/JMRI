package jmri.jmrit.display;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.display.palette.Bundle;
import jmri.util.swing.ImagePanel;

/**
 * Companion class to DisplayFrame.
 * @author peteCressman 2020
 * @author Egbert Broerse
 */
public class PreviewPanel extends JPanel {

    DisplayFrame _parent;
    JComboBox<String> _bgColorBox;

    public PreviewPanel(DisplayFrame parent, ImagePanel panel1, ImagePanel panel2, boolean hasComboBox) {
        super();
        _parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        if (hasComboBox) {
            add(makeBgButtonPanel(panel1, panel2));
        }
        if (panel1 != null) {
            add(panel1);
        }
        if (panel2 != null) {
            add(panel2);
        }
    }

    public void setBackgroundSelection(int index) {
        if (_bgColorBox != null) {
            _bgColorBox.setSelectedIndex(index);
        }
    }

    private JPanel makeBgButtonPanel(ImagePanel preview1, ImagePanel preview2) {
        _bgColorBox = new JComboBox<>();
        _bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, but too long for combo
        _bgColorBox.addItem(Bundle.getMessage("White"));
        _bgColorBox.addItem(Bundle.getMessage("LightGray"));
        _bgColorBox.addItem(Bundle.getMessage("DarkGray"));
        _bgColorBox.addItem(Bundle.getMessage("Checkers"));
        _bgColorBox.setSelectedIndex(_parent.getPreviewBg()); // Global field, starts as 0 = panel bg color

        JPanel bkgdBoxPanel = new JPanel();
        bkgdBoxPanel.setLayout(new BoxLayout(bkgdBoxPanel, BoxLayout.Y_AXIS));
        bkgdBoxPanel.add(new JLabel(Bundle.getMessage("setBackground"), SwingConstants.RIGHT));
        bkgdBoxPanel.add(_bgColorBox);

        _bgColorBox.addActionListener((ActionEvent e) -> {
            log.debug("PreviewPanel _bgColorBox action");
            int previewBgSet = _bgColorBox.getSelectedIndex();
            _parent.setPreviewBg(previewBgSet); // notify user choice in field on children override
            // load background image
            log.debug("PreviewPanel color #{} set", previewBgSet);
            if (preview1 != null) {
                preview1.setImage(_parent.getPreviewBackground());
                preview1.revalidate(); // force redraw
            }
            if (preview2 != null) {
                preview2.setImage(_parent.getPreviewBackground());
                preview2.revalidate(); // force redraw
            }
        });
        JPanel panel = new JPanel();
        panel.add(bkgdBoxPanel);
        return panel;
    }

    private final static Logger log = LoggerFactory.getLogger(PreviewPanel.class);

}
