package jmri.util.swing;

import com.alexandriasoftware.swing.JSplitButton;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * Abstract Color Chooser extension that presents a split button
 * to set the color. Clicking the button results in a
 * JColorChooser launching.
 *
 * @author Paul Bender Copyright (C) 2018
 * @since 4.1.1
 */
public class SplitButtonColorChooserPanel extends AbstractColorChooserPanel {

    //private JLabel swatch = null;
    private JSplitButton setButton = null;
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

        //setButton.setImage(image);
        ImageIcon icon = new ImageIcon(image);
        setButton.setIcon(icon);
        setButton.setPopupMenu(new ColorListPopupMenu(getColorSelectionModel()));
        g.dispose();
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

        setButton = new JSplitButton(Bundle.getMessage("SetColor") + "      ",icon);  // NOI18N
        setButton.addButtonClickedActionListener((ActionEvent e) -> {
            Color desiredColor = JColorChooser.showDialog(setButton.getParent(),
                    Bundle.getMessage("SetColor"), // NOI18N
                    getColorFromModel());
            if (desiredColor != null) {
                getColorSelectionModel().setSelectedColor(desiredColor);
            }
        });
        setButton.addSplitButtonClickedActionListener((ActionEvent e) -> {
            //Color desiredColor = JColorChooser.showDialog(this,
            //                     Bundle.getMessage("SetColor"),
            //                     getColorFromModel());
            //if (desiredColor!=null) {
            //    getColorSelectionModel().setSelectedColor(desiredColor);
        });

        //setButton.setImage(image);
        setButton.setPopupMenu(new ColorListPopupMenu(getColorSelectionModel()));
        add(setButton);
    }

    @Override
    public String getDisplayName() {
         return Bundle.getMessage("SplitButtonColorChooserName");  // NOI18N
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
