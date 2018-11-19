package jmri.jmrit.display.palette;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.util.swing.ImagePanel;

/**
 * JPanels for the Panel Backgrounds.
 */
public class BackgroundItemPanel extends IconItemPanel {

    /**
     * Constructor for background of icons or panel color.
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type, should be "Background"
     * @param editor      Editor that called this ItemPalette
     */
    public BackgroundItemPanel(DisplayFrame parentFrame, String type, Editor editor) {
        super(parentFrame, type, editor);
        _level = Editor.BKG;
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _iconPanel.setImage(_backgrounds[0]);
        }
    }

    @Override
    protected JPanel instructions() {
        JPanel panel = super.instructions();
        JPanel blurb = (JPanel) panel.getComponent(0);
        blurb.add(new JLabel(Bundle.getMessage("ToColorBackground", Bundle.getMessage("ButtonBackgroundColor"))));
        blurb.add(javax.swing.Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        return panel;
    }

    @Override
    protected void initLinkPanel() {
        JPanel bottomPanel = new JPanel();
        JButton backgroundButton = new JButton(Bundle.getMessage("ButtonBackgroundColor"));
        backgroundButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                hideCatalog();
                ActionListener colorAction = ((ActionEvent event) -> {
                    colorChanged(); // callback
                });
                new ColorDialog(_editor, _editor.getTargetPanel(), ColorDialog.ONLY, colorAction);
            }
        });
        backgroundButton.setToolTipText(Bundle.getMessage("ToolTipEditColor"));
        bottomPanel.add(backgroundButton);
        add(bottomPanel);
    }
    
    private void colorChanged() {
        java.awt.Color c = _editor.getTargetPanel().getBackground();
        java.awt.image.BufferedImage im = jmri.util.swing.DrawSquares.getImage(500, 400, 10, c, c);
        _paletteFrame.updateBackground0(im);
    }

    @Override
    protected void setEditor(Editor ed) {
        super.setEditor(ed);
        if (_iconPanel !=null) {
            _iconPanel.setImage(_backgrounds[0]);
        }
    }

    @Override
    protected JPanel makeBgButtonPanel(ImagePanel preview1, ImagePanel preview2) {
        return null; // no button to set Preview Bg on BackgroundItemPanel
    }
}
