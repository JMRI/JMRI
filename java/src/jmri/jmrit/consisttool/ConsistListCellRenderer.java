package jmri.jmrit.consisttool;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.roster.*;
import jmri.util.gui.GuiLafPreferencesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A ListCellRenderer for the ConsistComboBox
 *
 * @author Lionel Jeanson Copyright (c) 2023
 * 
 */
public class ConsistListCellRenderer extends JLabel implements ListCellRenderer<Object> {
    private final static int IMAGE_HEIGHT = 19;
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {        
        if (value == null) {
            return null;
        }
        
        if (value instanceof String) {
            setText((String) value);
            setIcon(null);
            return this;            
        }
        
        if (value instanceof LocoAddress) {            
            LocoAddress consistAddress = (LocoAddress) value;
            setText(consistAddress.toString());
            
            BufferedImage bi = new BufferedImage( 1, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);            
            Consist consist =  InstanceManager.getDefault(jmri.ConsistManager.class).getConsist(consistAddress);
            Font f = new Font("Monospaced", Font.PLAIN, InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize());
            
            for (DccLocoAddress loco : consist.getConsistList()) {
                log.debug("Loco : {} - Position : {}", loco, consist.getPosition(loco));
                String reName = consist.getRosterId(loco);
                String label = null;
                if (reName != null) {
                    ImageIcon icon;
                    Boolean dir = consist.getLocoDirection(loco);
                    if (dir) {
                        icon = InstanceManager.getDefault(RosterIconFactory.class).getIcon(reName);
                    } else {
                        icon = InstanceManager.getDefault(RosterIconFactory.class).getReversedIcon(reName);
                    }
                    if ( (icon != null) && (bi.getWidth() + icon.getIconWidth() > 0) ) {                        
                        BufferedImage ti = new BufferedImage(bi.getWidth() + icon.getIconWidth(), IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                        Graphics g = ti.createGraphics();
                        g.drawImage(icon.getImage(), ti.getHeight()/2-icon.getIconHeight()/2, 0, null);
                        g.drawImage(bi, icon.getIconWidth(), 0, null);           
                        g.dispose(); 
                        bi = ti;
                    } else {
                        label = "["+reName+"]";
                    }
                } else {
                     label = "["+loco.toString()+"]";
                }
                if (label != null) {
                    // write label for that locomotive in a buffered image                    
                    BufferedImage li =  new BufferedImage( label.length()*f.getSize(),  IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                    Graphics gli = li.createGraphics();
                    gli.setFont(f);
                    gli.setColor(this.getForeground());
                    gli.drawString(label, 0, IMAGE_HEIGHT/2 + gli.getFontMetrics().getMaxAscent()/2);                    
                    // expand existing buffered image
                    BufferedImage ti = new BufferedImage( gli.getFontMetrics().stringWidth(label) + bi.getWidth() +2, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                    Graphics gti = ti.createGraphics(); 
                    gti.drawImage(li, 0, 0, null);
                    gti.drawImage(bi, gli.getFontMetrics().stringWidth(label)+2, 0, null);
                    // update and free ressources
                    bi = ti;
                    gli.dispose();
                    gti.dispose();                     
                }
            }
            setIcon(new ImageIcon(bi));
        }
        return this;        
    }    
    
    private static final Logger log = LoggerFactory.getLogger(ConsistListCellRenderer.class);
}
