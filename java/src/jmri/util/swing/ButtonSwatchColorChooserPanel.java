package jmri.util.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

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
    private static final int ICON_DIMENSION = 20;

    @Override
    public void updateChooser(){
        Color color = getColorFromModel();
        // update the Swatch to have the right color showing.
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();
        // fill it with its representative color
        g.setColor(color);
        g.fillRect(0, 0, ICON_DIMENSION, ICON_DIMENSION);
        // draw a black border around it
        g.setColor(Color.black);
        g.drawRect(0, 0, ICON_DIMENSION - 1, ICON_DIMENSION - 1);

        ImageIcon icon = new ImageIcon(image); 
        g.dispose();

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
        g.dispose();

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
