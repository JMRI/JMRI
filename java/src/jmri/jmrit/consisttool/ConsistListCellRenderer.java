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
            
            BufferedImage bi = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB);            
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
                    if (icon != null) {                        
                        BufferedImage ti = new BufferedImage( bi.getWidth() + icon.getIconWidth(), Math.max(bi.getHeight(), icon.getIconHeight()), BufferedImage.TYPE_INT_ARGB);
                        Graphics g = ti.createGraphics();
                        g.drawImage(icon.getImage(), 0, 0, null);
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
                    BufferedImage li =  new BufferedImage(  label.length()*f.getSize(),  19, BufferedImage.TYPE_INT_ARGB);
                    Graphics g = li.createGraphics();
                    g.setFont(f);
                    g.drawString(label, 0, li.getHeight()-2);
                    g.dispose();
                    BufferedImage ti = new BufferedImage( bi.getWidth() + g.getFontMetrics().stringWidth(label), Math.max(bi.getHeight(), li.getHeight()), BufferedImage.TYPE_INT_ARGB);
                    g = ti.createGraphics(); 
                    g.drawImage(li, 0, 0, null);
                    g.drawImage(bi, g.getFontMetrics().stringWidth(label), 0, null);
                    g.dispose(); 
                    bi = ti;
                }
                                                 
            }
            setIcon(new ImageIcon(bi));
        }
        return this;        
    }    
    
    private static final Logger log = LoggerFactory.getLogger(ConsistListCellRenderer.class);
}
