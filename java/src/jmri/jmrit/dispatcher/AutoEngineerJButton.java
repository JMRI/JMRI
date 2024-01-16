package jmri.jmrit.dispatcher;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import jmri.jmrit.catalog.NamedIcon;

public class AutoEngineerJButton extends JButton {
    private NamedIcon currentOriginalImage = null;
    public AutoEngineerJButton(NamedIcon namedIcon) {
        super(namedIcon);
        currentOriginalImage  = namedIcon;
    }

    /**
     * Resizes the baseimage to the button size
     *
     * @param buttonDimension size of the image and therefore button
     */
    public void reSizeIcon(Dimension buttonDimension) {
        ImageIcon icon = new ImageIcon(
                    currentOriginalImage.getOriginalImage().getScaledInstance((int)buttonDimension.getWidth(), (int)buttonDimension.getHeight(),
                            java.awt.Image.SCALE_FAST));
            super.setIcon(icon);
        }

    /**
     * Set the Named Icon base Image
     *
     * @param namedIcon Base image for building icon.
     */
    public void setIcon(NamedIcon namedIcon) {
        currentOriginalImage = namedIcon;
        super.setIcon(namedIcon);
    }
}
