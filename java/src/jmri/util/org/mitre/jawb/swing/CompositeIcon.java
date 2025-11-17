
/* ----------------------------------------------------------------------
 * 
 * Copyright (c) 2002-2009 The MITRE Corporation
 * 
 * Except as permitted below
 * ALL RIGHTS RESERVED
 * 
 * The MITRE Corporation (MITRE) provides this software to you without
 * charge to use for your internal purposes only. Any copy you make for
 * such purposes is authorized provided you reproduce MITRE's copyright
 * designation and this License in any such copy. You may not give or
 * sell this software to any other party without the prior written
 * permission of the MITRE Corporation.
 * 
 * The government of the United States of America may make unrestricted
 * use of this software.
 * 
 * This software is the copyright work of MITRE. No ownership or other
 * proprietary interest in this software is granted you other than what
 * is granted in this license.
 * 
 * Any modification or enhancement of this software must inherit this
 * license, including its warranty disclaimers. You hereby agree to
 * provide to MITRE, at no charge, a copy of any such modification or
 * enhancement without limitation.
 * 
 * MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS
 * OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY,
 * MERCHANTABILITY, OR FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN
 * NO EVENT WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL,
 * INDIRECT, INCIDENTAL, EXEMPLARY OR SPECIAL DAMAGES, EVEN IF MITRE HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You accept this software on the condition that you indemnify and hold
 * harmless MITRE, its Board of Trustees, officers, agents, and
 * employees, from any and all liability or damages to third parties,
 * including attorneys' fees, court costs, and other related costs and
 * expenses, arising out of your use of this software irrespective of the
 * cause of said liability.
 * 
 * The export from the United States or the subsequent reexport of this
 * software is subject to compliance with United States export control
 * and munitions control restrictions. You agree that in the event you
 * seek to export this software you assume full responsibility for
 * obtaining all necessary export licenses and approvals and for assuring
 * compliance with applicable reexport restrictions.
 * 
 * ----------------------------------------------------------------------
 * 
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-09-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995).
 * 
 * (c) 2009 The MITRE Corporation. All Rights Reserved.
 * 
 * ----------------------------------------------------------------------
 *
 */
/*
 * Copyright (c) 2002-2006 The MITRE Corporation
 * 
 * Except as permitted below
 * ALL RIGHTS RESERVED
 * 
 * The MITRE Corporation (MITRE) provides this software to you without
 * charge to use for your internal purposes only. Any copy you make for
 * such purposes is authorized provided you reproduce MITRE's copyright
 * designation and this License in any such copy. You may not give or
 * sell this software to any other party without the prior written
 * permission of the MITRE Corporation.
 * 
 * The government of the United States of America may make unrestricted
 * use of this software.
 * 
 * This software is the copyright work of MITRE. No ownership or other
 * proprietary interest in this software is granted you other than what
 * is granted in this license.
 * 
 * Any modification or enhancement of this software must inherit this
 * license, including its warranty disclaimers. You hereby agree to
 * provide to MITRE, at no charge, a copy of any such modification or
 * enhancement without limitation.
 * 
 * MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS
 * OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY,
 * MERCHANTABILITY, OR FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN
 * NO EVENT WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL,
 * INDIRECT, INCIDENTAL, EXEMPLARY OR SPECIAL DAMAGES, EVEN IF MITRE HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You accept this software on the condition that you indemnify and hold
 * harmless MITRE, its Board of Trustees, officers, agents, and
 * employees, from any and all liability or damages to third parties,
 * including attorneys' fees, court costs, and other related costs and
 * expenses, arising out of your use of this software irrespective of the
 * cause of said liability.
 * 
 * The export from the United States or the subsequent reexport of this
 * software is subject to compliance with United States export control
 * and munitions control restrictions. You agree that in the event you
 * seek to export this software you assume full responsibility for
 * obtaining all necessary export licenses and approvals and for assuring
 * compliance with applicable reexport restrictions.
 */

package jmri.util.org.mitre.jawb.swing;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.SwingConstants;

/**
 * CompositeIcon is an Icon implementation which draws two icons with a
 * specified relative position.<p>
 *
 * <code>LEFT, RIGHT, TOP, BOTTOM</code> specify how icon1 is drawn relative
 * to icon2<p>
 *
 * CENTER: icon1 is drawn first, icon2 is drawn over it and with horizontal
 * and vertical orientations within the alloted space<p>
 *
 * It's useful with VTextIcon when you want an icon with your text: if icon1
 * is the graphic icon and icon2 is the VTextIcon, you get a similar effect to
 * a JLabel with a graphic icon and text
 *
 * @see <a href="http://www.macdevcenter.com/pub/a/mac/2002/03/22/vertical_text.html">http://www.macdevcenter.com/pub/a/mac/2002/03/22/vertical_text.html</a>
 */
public class CompositeIcon implements Icon, SwingConstants {
  Icon fIcon1, fIcon2;
  int fPosition, fHorizontalOrientation, fVerticalOrientation;
        
  /**
   * Create a CompositeIcon from the specified Icons, using the default relative
   * position (icon1 above icon2) and orientations (centered horizontally and
   * vertically)
   * 
   * @param icon1 icon 1
   * @param icon2 icon 2
   */
  public CompositeIcon(Icon icon1, Icon icon2) {
    this(icon1, icon2, TOP);
  }
        
  /**
   * Create a CompositeIcon from the specified Icons, using the specified
   * relative position and default orientations (centered horizontally and
   * vertically)
   * 
   * @param icon1    icon 1
   * @param icon2    icon 2
   * @param position icon position
   */
  public CompositeIcon(Icon icon1, Icon icon2, int position) {
      this(icon1, icon2, position, CENTER, CENTER);
  }
        
  /**
   * Create a CompositeIcon from the specified Icons, using the specified
   * relative position and orientations
   * 
   * @param icon1                 icon 1
   * @param icon2                 icon 2
   * @param position              icon positin
   * @param horizontalOrientation horizontal orientation
   * @param verticalOrientation   vertical orientation
   */
  public CompositeIcon(Icon icon1, Icon icon2, int position,
          int horizontalOrientation, int verticalOrientation) {
    fIcon1 = icon1;
    fIcon2 = icon2;
    fPosition = position;
    fHorizontalOrientation = horizontalOrientation;
    fVerticalOrientation = verticalOrientation;
  }
        
  /**
   * Draw the icon at the specified location.  Icon implementations
   * may use the Component argument to get properties useful for 
   * painting, e.g. the foreground or background color.
   */
  @Override
public void paintIcon(Component c, Graphics g, int x, int y) {
    int width = getIconWidth();
    int height = getIconHeight();
    if (fPosition == LEFT || fPosition == RIGHT) {
      Icon leftIcon, rightIcon;
      if (fPosition == LEFT) {
        leftIcon = fIcon1;
        rightIcon = fIcon2;
      }
      else {
        leftIcon = fIcon2;
        rightIcon = fIcon1;
      }
      // "Left" orientation, because we specify the x position
      paintIcon(c, g, leftIcon, x, y,
                width, height, LEFT, fVerticalOrientation);
      paintIcon(c, g, rightIcon, x + leftIcon.getIconWidth(), y,
                width, height, LEFT, fVerticalOrientation);                  
    }
    else if (fPosition == TOP || fPosition == BOTTOM) {
      Icon topIcon, bottomIcon;
      if (fPosition == TOP) {
        topIcon = fIcon1;
        bottomIcon = fIcon2;
      }
      else {
        topIcon = fIcon2;
        bottomIcon = fIcon1;
      }
      // "Top" orientation, because we specify the y position
      paintIcon(c, g, topIcon, x, y,
                width, height, fHorizontalOrientation, TOP);
      paintIcon(c, g, bottomIcon, x, y + topIcon.getIconHeight(),
                width, height, fHorizontalOrientation, TOP);                        
    }
    else {
      paintIcon(c, g, fIcon1, x, y, width, height,
                fHorizontalOrientation, fVerticalOrientation);
      paintIcon(c, g, fIcon2, x, y, width, height,
                fHorizontalOrientation, fVerticalOrientation);                     
    }
  }
    
  /** Paints one icon in the specified rectangle with the given orientations
 * @param c component
 * @param g graphic
 * @param icon icon
 * @param x x location
 * @param y y location
 * @param width width of icon
 * @param height height of icon
 * @param horizontalOrientation horizontal orientation
 * @param verticalOrientation vertical orientation
   */
  void paintIcon(Component c, Graphics g, Icon icon, int x, int y,
                 int width, int height,
                 int horizontalOrientation, int verticalOrientation) {
                        
    int xIcon, yIcon;
    switch (horizontalOrientation) {
    case LEFT:
      xIcon = x;
      break;
    case RIGHT: 
      xIcon = x + width - icon.getIconWidth();
      break;
    default:
      xIcon = x + (width - icon.getIconWidth()) / 2;
      break;
    }
    switch (verticalOrientation) {
    case TOP:
      yIcon = y;
      break;
    case BOTTOM:
      yIcon = y + height - icon.getIconHeight();
      break;
    default:
      yIcon = y + (height - icon.getIconHeight()) / 2;
      break;                          
    }
    icon.paintIcon(c, g, xIcon, yIcon);             
  }
        
  /**
   * Returns the icon's width.
   *
   * @return an int specifying the fixed width of the icon.
   */
  @Override
public int getIconWidth() {
    if (fPosition == LEFT || fPosition == RIGHT)
      return fIcon1.getIconWidth() + fIcon2.getIconWidth();
                                
    return Math.max(fIcon1.getIconWidth(), fIcon2.getIconWidth());
  }
        
  /**
   * Returns the icon's height.
   *
   * @return an int specifying the fixed height of the icon.
   */
  @Override
public int getIconHeight() {
    if (fPosition == TOP || fPosition == BOTTOM)
      return fIcon1.getIconHeight() + fIcon2.getIconHeight();
                                
    return Math.max(fIcon1.getIconHeight(), fIcon2.getIconHeight());
  }
        
}
