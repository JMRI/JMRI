package jmri.jmrit.throttle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * A custom slider UI to be used for throttle control panel speed slider
 * Very graphical display to be used on a large screen
 * 
 * @author Lionel Jeanson - 2018
 * 
 */

public class ControlPanelCustomSliderUI extends BasicSliderUI {
    
    // Color are coming from the Tango themes color palette (as well as icons on the Throttle window, for look consistency)
    private final static Color TRACK_COLOR_BACK = new Color(0x88, 0x8a, 0x85, 0x88);
    private final static Color TRACK_COLOR_FRONT = new Color(0xf5, 0x79, 0x00, 0xCC);
    private final static Color TRACK_COLOR_FRONT_DISABLED = new Color(0xf5, 0xf5, 0xf5, 0xCC);
    private final static Color TRACK_COLOR_TICKS = new Color(0x888a85);
    private final static Color THUMB_INNER_COLOR_STOP = new Color(0xcc0000);
    private final static Color THUMB_INNER_COLOR_RUN = new Color(0xd7d27A);
    private final static Color THUMB_INNER_COLOR_DISABLED = new Color(0x101010);
    private final static Color THUMB_CONTOUR_COLOR = new Color(0x555753);
    
    public ControlPanelCustomSliderUI(JSlider b) {
        super(b);
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            super.paint(g, c);
        }
    }

    @Override
    protected Dimension getThumbSize() {
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            return new Dimension(16, contentRect.height - tickRect.height - labelRect.height -trackBuffer*2);
        } else {
            return new Dimension(contentRect.width  - tickRect.width - labelRect.width - trackBuffer*2, 16);
        }
    }

    @Override
    public void paintTrack(Graphics g) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        Paint oldPaint = g2d.getPaint();
        // Track back rectangle
        g2d.setPaint(TRACK_COLOR_BACK);
        g2d.fillRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
        // Track ticks                 
        int[] ticksAt = { 3*slider.getMinimum()/4, slider.getMinimum()/2 , slider.getMinimum()/4, 0, slider.getMaximum()/4, slider.getMaximum()/2, 3*slider.getMaximum()/4 };        
        // only if it fits 
        g2d.setPaint(TRACK_COLOR_TICKS);
        if (slider.getOrientation() == SwingConstants.VERTICAL && trackRect.height > ticksAt.length*8) {
            for (int n : ticksAt ) {
                g2d.drawLine( trackRect.x , 
                        trackRect.y + trackRect.height / ((slider.getMaximum()-slider.getMinimum())/(slider.getMaximum())) - n*trackRect.height / (slider.getMaximum()-slider.getMinimum()) , 
                        trackRect.x + trackRect.width-1, 
                        trackRect.y + trackRect.height / ((slider.getMaximum()-slider.getMinimum())/(slider.getMaximum())) - n*trackRect.height / (slider.getMaximum()-slider.getMinimum()) );
            }
        } else if (slider.getOrientation() == SwingConstants.HORIZONTAL && trackRect.width  > ticksAt.length*8) {
           for (int n : ticksAt ) {
                g2d.drawLine( trackRect.x + trackRect.width - trackRect.width / ((slider.getMaximum()-slider.getMinimum())/(slider.getMaximum())) + n*trackRect.width / (slider.getMaximum()-slider.getMinimum()) , 
                        trackRect.y, 
                        trackRect.x + trackRect.width - trackRect.width / ((slider.getMaximum()-slider.getMinimum())/(slider.getMaximum())) + n*trackRect.width / (slider.getMaximum()-slider.getMinimum()),
                        trackRect.y + trackRect.height );
            }            
        }
        // Track front
        if (slider.isEnabled()) {
            g2d.setPaint(TRACK_COLOR_FRONT);
        } else {
            g2d.setPaint(TRACK_COLOR_FRONT_DISABLED);
        }
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            if (slider.getMinimum()<0 && slider.getMaximum()>0) {
                double doublerel0Pos = Math.abs((double)slider.getMinimum()) / ((double)slider.getMaximum() - (double)slider.getMinimum());
                double x0 = trackRect.x + trackRect.width * doublerel0Pos;
                double widthRect = thumbRect.x + (double)thumbRect.width/2 - x0;
                if (widthRect>0) {
                    g2d.fillRect( (int)Math.round(x0), trackRect.y, (int)Math.round(widthRect), trackRect.height);
                } else {
                    g2d.fillRect( (int)Math.round(x0+widthRect), trackRect.y, (int)Math.round(-widthRect), trackRect.height);
                }
            } else {           
                g2d.fillRect(trackRect.x, trackRect.y, thumbRect.x-thumbRect.width/2, trackRect.height);
            }
        } else {
            if (slider.getMinimum()<0 && slider.getMaximum()>0) {
                double doublerel0Pos = Math.abs((double)slider.getMaximum()) / ((double)slider.getMaximum() - (double)slider.getMinimum());
                double y0 = trackRect.y + trackRect.height * doublerel0Pos ;
                double heightRect = thumbRect.y + (double)thumbRect.height/2 - y0;
                if (heightRect>0) {
                    g2d.fillRect( trackRect.x, (int)Math.round(y0), trackRect.width, (int)Math.round(heightRect));
                } else {
                    g2d.fillRect( trackRect.x, (int)Math.round(y0+heightRect), trackRect.width, (int)Math.round(-heightRect));
                }                
            } else {
                g2d.fillRect(trackRect.x, thumbRect.y+thumbRect.height/2, trackRect.width, trackRect.height - thumbRect.y+thumbRect.height - trackRect.y  );
            }
        }
        g2d.setPaint(oldPaint);
    }

    @Override
    public void paintThumb(Graphics g) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        int x1 = thumbRect.x + 3;
        int x2 = thumbRect.x + thumbRect.width - 5;
        GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        shape.moveTo(x1, thumbRect.y+1);        
        shape.lineTo(x2, thumbRect.y+1);
        shape.lineTo(x2, thumbRect.y+thumbRect.height-2);
        shape.lineTo(x1, thumbRect.y+thumbRect.height-2);
        shape.closePath();
        if (slider.isEnabled()) {
            if (slider.getValue()==0) {
                g2d.setPaint(THUMB_INNER_COLOR_STOP);
            } else {             
                g2d.setPaint( new Color(THUMB_INNER_COLOR_RUN.getRed() - Math.abs(slider.getValue())*100/slider.getMaximum(),THUMB_INNER_COLOR_RUN.getGreen(), THUMB_INNER_COLOR_RUN.getBlue() - Math.abs(slider.getValue()*100/slider.getMaximum()) ));
            }
        } else {
            g2d.setPaint(THUMB_INNER_COLOR_DISABLED);
        }
        g2d.fill(shape);
        Stroke old = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2f));
        g2d.setPaint(THUMB_CONTOUR_COLOR);
        g2d.draw(shape);
        g2d.setStroke(old);
    }      
}
