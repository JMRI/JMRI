package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import jmri.util.swing.JmriColorChooser;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;

/**
 * JPanels for the Panel Backgrounds.
 */
public class BackgroundItemPanel extends IconItemPanel {

    JColorChooser _chooser;
    JButton       _colorButton;
    JPanel        _colorPanel;
    Color         _color;

    /**
     * Constructor for background of icons or panel color.
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type, should be "Background"
     */
    public BackgroundItemPanel(DisplayFrame parentFrame, String type) {
        super(parentFrame, type);
        _level = Editor.BKG;
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _colorPanel = makeColorPanel();
            add(_colorPanel);
        }
    }

    @Override
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(new JLabel(Bundle.getMessage("DragIconPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconCatalog")));
        blurb.add(javax.swing.Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JLabel label = new JLabel(Bundle.getMessage("BackgroundIcons"));
        label.setForeground(Color.RED);
        blurb.add(label);
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    protected JPanel makeItemButtonPanel() {
        JPanel panel = super.makeItemButtonPanel();
        panel.add(makeColorButton());
        return panel;
    }

    @Override
    protected JPanel makeSpecialBottomPanel(boolean update) {
        JPanel _bottom2Panel = super.makeSpecialBottomPanel(update);
        _bottom2Panel.add(makeColorButton(), 1);
        return _bottom2Panel;
    }

    @Override
    protected void initLinkPanel() {
    }

    @Override
    protected void showCatalog() {
        _colorPanel.setVisible(false);
        _colorPanel.invalidate();
        super.showCatalog();
        
    }
    private void showColorPanel() {
        deselectIcon();
        _chooser.setColor(_frame.getCurrentColor());
        Dimension totalDim = _frame.getSize();
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        _catalog.setVisible(false);
        _catalog.invalidate();
        _colorPanel.setVisible(true);
        _colorPanel.invalidate();
        _bottomPanel.setVisible(false);
        _bottomPanel.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
        _colorButton.setText(Bundle.getMessage("HideColorPanel"));
    }

    private void hideColorPanel() {
        _chooser.setColor(_frame.getCurrentColor());
        Dimension totalDim = _frame.getSize();
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        _colorPanel.setVisible(false);
        _colorPanel.invalidate();
        _bottomPanel.setVisible(true);
        _bottomPanel.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
        _colorButton.setText(Bundle.getMessage("ShowColorPanel"));
    }

    private JButton makeColorButton() {
        _colorButton = new JButton(Bundle.getMessage("ButtonShowColorPanel"));
        _colorButton.addActionListener(a -> {
            if (_colorPanel.isVisible()) {
                hideColorPanel();
            } else {
                showColorPanel();
            }
        });
        _colorButton.setToolTipText(Bundle.getMessage("ToolTipColor"));
        return _colorButton;
    }

    protected JPanel makeColorButtonPanel() {
        JPanel panel = new JPanel();
        JButton button = new JButton(Bundle.getMessage("ButtonBackgroundColor"));
        button.addActionListener(a -> setColor());
        button.setToolTipText(Bundle.getMessage("ToColorBackground"));
        panel.add(button);

        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener(a -> hideColorPanel());
        button.setToolTipText(Bundle.getMessage("ToColorBackground"));
        panel.add(button);
        panel.setToolTipText(Bundle.getMessage("ToColorBackground"));
        return panel;
    }
 
    private JPanel makeColorPanel() {
        JPanel panel =new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        _chooser = new JColorChooser(_frame.getCurrentColor());
        _chooser.setPreviewPanel(new JPanel());
        _chooser.getSelectionModel().addChangeListener((ChangeEvent e) -> colorChange());
        _chooser.setColor(_frame.getCurrentColor());
        JmriColorChooser.extendColorChooser(_chooser);
        panel.add(_chooser);

        panel.add(makeColorButtonPanel());
        panel.setToolTipText(Bundle.getMessage("ToColorBackground"));
        panel.setVisible(false);
        return panel;
    }

    private void colorChange() {
        _color = _chooser.getColor();
        _iconPanel.setImage(DrawSquares.getImage(500, 400, 10, _color, _color));
        _iconPanel.invalidate();
    }

    private void setColor() {
        if (_color == null) {
            JOptionPane.showMessageDialog(_frame, 
                    Bundle.getMessage("ToColorBackground", Bundle.getMessage("ButtonBackgroundColor")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        _frame.getEditor().setBackgroundColor(_color);
        _frame.updateBackground(_frame.getEditor());
        hideColorPanel();
    }

    @Override
    protected void previewColorChange() {
        if (_initialized) {
            ImagePanel iconPanel = _catalog.getPreviewPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_frame.getPreviewBackground());
            }
            _iconPanel.setImage(_frame.getPreviewBackground());
            _iconPanel.setImage(_frame.getBackground(0));
        }
    }

}
