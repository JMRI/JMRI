package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renderer for Bean Icons, formatting them to similar height for use in tables.
 * Not a permanent location for this class, location likely to change in near future.
 */ 
public class CbusBeanRenderer  {
    
    private ImageIcon tOff;
    private ImageIcon tOn;
    private ImageIcon sOff;
    private ImageIcon sOn;
    private ImageIcon lOff;
    private ImageIcon lOn;
    
    /**
     * Create a new Renderer Instance.
     * @param iconHeight Target height of Icons
     */
    public CbusBeanRenderer(int iconHeight) {
        initImages(iconHeight);
    }
    
    /**
     * Get the Bean Icon, scaled to standard height.
     * 
     * @param beanTypeChar Standard Bean Type, currently TSL supported.
     * @param beanState Bean State
     * @return Image of the Bean, else null ( with error log ) if unavailable.
     */
    public ImageIcon getBeanIcon(String beanTypeChar, int beanState){

        ImageIcon img = null;
        switch (beanTypeChar) {
            case "T":
                img = ( beanState==jmri.DigitalIO.ON ? tOn : tOff);
                break;
            case "S":
                img = ( beanState==jmri.DigitalIO.ON ? sOn : sOff);
                break;
            case "L":
                img = ( beanState==jmri.DigitalIO.ON ? lOn : lOff);
                break;
            default:
                log.error("no image for bean {} state {}", beanTypeChar, beanState);
                break;
        }
        return img;
    }

    private final static String ROOTPATH = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor

    private static ImageIcon getTurnoutIcon(String file, int iconHeight) throws IOException {
        BufferedImage bigImage = ImageIO.read(new File(ROOTPATH+file));
        return new ImageIcon(bigImage.getScaledInstance((int)Math.round(iconHeight*1.5), iconHeight-2, Image.SCALE_DEFAULT));
    }

    private static ImageIcon getSensorIcon(String file, int iconHeight) throws IOException {
        BufferedImage bufimg = ImageIO.read(new File(ROOTPATH+file));
        BufferedImage img = bufimg.getSubimage(11, 23, 59, 60);
        BufferedImage copyOfImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        Graphics g = copyOfImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        
        return new ImageIcon(img.getScaledInstance(iconHeight+2, iconHeight+2, Image.SCALE_DEFAULT));
    }

    private static ImageIcon getLightIcon(String file, int iconHeight) throws IOException {

        BufferedImage img = ImageIO.read(new File(ROOTPATH + file)).getSubimage(11, 23, 59, 60);
        BufferedImage copyOfImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        Graphics g = copyOfImage.createGraphics();
        g.drawImage(img, 0, 0, null);

        return new ImageIcon(img.getScaledInstance(iconHeight-3, iconHeight-3, Image.SCALE_DEFAULT));
    }

    private void initImages(int iconHeight){
        try {
            tOn = getTurnoutIcon("T-on-s.png",iconHeight);
            tOff = getTurnoutIcon("T-off-s.png",iconHeight);
            sOn = getSensorIcon("S-on-s.png",iconHeight);
            sOff = getSensorIcon("S-off-s.png",iconHeight);
            lOn = getLightIcon("L-on-s.png",iconHeight);
            lOff = getLightIcon("L-off-s.png",iconHeight);
        } catch (IOException ex) {
            log.error("Error creating Bean Icon Images: {}",ex);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusBeanRenderer.class);

}
