
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

import java.awt.*;
import javax.swing.*;

/**
 * @version 1.0 02/26/99
 */
public class DetachPanelIcon implements Icon {
  
  private static final int WIDTH = 13;
  private static final int HEIGHT = 13;

  private boolean pressed;
  private Color highlight;
  private Color shadow;
  private Color fill;

  public DetachPanelIcon(boolean isPressedView) {
    pressed = isPressedView;
    if (isPressedView) {
      init( UIManager.getColor("controlDkShadow"),
            UIManager.getColor("controlLtHighlight"),
            UIManager.getColor("controlShadow"));
    } else {
      init( UIManager.getColor("controlShadow"),
            UIManager.getColor("controlHighlight"),
            UIManager.getColor("control"));
    }
  }
  
  // The following ctor from the original file is never used locally
  // We're commenting it out, instead of deleting it, because it was present there.
  // private DetachPanelIcon(Color shadow, Color highlight, Color fill) {
  //   init(shadow, highlight, fill);
  //  }

  private void init(Color shadow, Color highlight, Color fill) {
    this.highlight = highlight;
    this.shadow = shadow;
    this.fill = fill;
  }

  @Override
public void paintIcon(Component c, Graphics g, int x, int y) {
    if (pressed)
      g.setColor(highlight);
    else
      g.setColor(fill);
    g.drawRect(x,y,WIDTH,HEIGHT); // background
    
    g.setColor(highlight);
    g.drawRect(x+2,y+6,2,4); // main highlight
    g.setColor(fill);
    g.fillRect(x+3,y+7,5,3); // main fill
    g.setColor(shadow);
    g.drawLine(x+11,y+3,x+11,y+7); // aux shadow
    g.drawLine(x+5,y+8,x+11,y+8); // aux shadow
    g.setColor(Color.black);
    g.drawRect(x+1,y+4,7,6);
    g.drawLine(x+1,y+5,x+5,y+5); // main frame
    g.setColor(highlight);
    g.fillRect(x+5,y+4,5,3); // aux highlight
    g.setColor(fill);
    g.fillRect(x+6,y+5,4,2); // aux fill
    g.setColor(Color.black);
    g.drawRect(x+4,y+2,6,5);
    g.drawLine(x+5,y+3,x+10,y+3); // aux frame
  }

  @Override
public int getIconWidth() {
    return WIDTH;
  }

  @Override
public int getIconHeight() {
    return HEIGHT;
  }
 
}
