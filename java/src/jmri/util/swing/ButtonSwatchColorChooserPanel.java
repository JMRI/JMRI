package jmri.util.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JRadioButton;

/**
 * Abstract Color Chooser extension that presents a swatch sample of the color
 * and a button to set the color. Clicking the button results in a 
 * JColorChooser launching.
 *
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
public class ButtonSwatchColorChooserPanel extends AbstractColorChooserPanel {

    private JLabel swatch = null;
    private JButton setButton = null;
    private static final int ICON_DIMENSION = 10;

    @Override
    public void updateChooser(){
        Color color = getColorFromModel();
        // update the Swatch to have the right color showing.
        int ICON_DIMENSION = 10;
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();
        // set completely transparent
        g.setColor(color);
        g.fillRect(0, 0, ICON_DIMENSION, ICON_DIMENSION);

        ImageIcon icon = new ImageIcon(image); 

        swatch.setIcon(icon);
    }

    @Override
    protected void buildChooser(){
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();
        // set completely transparent
        g.setColor(getColorFromModel());
        g.fillRect(0, 0, ICON_DIMENSION, ICON_DIMENSION);

        ImageIcon icon = new ImageIcon(image); 
        swatch = new JLabel(icon);
        add(swatch);
        setButton = new JButton(Bundle.getMessage("SetColor"));
        setButton.addActionListener((ActionEvent e) -> {
            Color desiredColor = JColorChooser.showDialog(this,
                                 Bundle.getMessage("SetColor"),
                                 getColorFromModel());
            if (desiredColor!=null) {
                getColorSelectionModel().setSelectedColor(desiredColor);
            }
        });
        add(setButton);
    }

    @Override
    public String getDisplayName() {
         return Bundle.getMessage("ButtonSwatchColorChooserName");
    }

    @Override
    public Icon getSmallDisplayIcon(){
       return null;
    }

    @Override
    public Icon getLargeDisplayIcon(){
       return null;
    }
}
