// ExternalLinkContentViewerUI.java

package jmri.util;


/**
 * A UI subclass that will open external links (website 
 * or mail links) in an external browser
 * <P>
 * To use:
 * SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
 * 
 @see http://forums.sun.com/thread.jspa?threadID=728061
 @since JMRI 2.5.3 (or perhaps later, please check CVS)
 */
 
/* public class ExternalLinkContentViewerUI extends BasicContentViewerUI{ */
/*     public ExternalLinkContentViewerUI(JHelpContentViewer x){ */
/* 		super(x); */
/* 	} */
/*      */
/*     public static javax.swing.plaf.ComponentUI createUI(JComponent x){ */
/*         return new ExternalLinkContentViewerUI((JHelpContentViewer)x); */
/*     } */
/*  */
/*     public void hyperlinkUpdate(HyperlinkEvent he){ */
/*         if(he.getEventType()==HyperlinkEvent.EventType.ACTIVATED){ */
/*             try{ */
/*                 URL u = he.getURL(); */
/*                 if(u.getProtocol().equalsIgnoreCase("mailto")||u.getProtocol().equalsIgnoreCase("http")||u.getProtocol().equalsIgnoreCase("ftp")){ */
/*                     java.awt.Desktop.getDesktop().browse(new URI(u.toString())); */
/*                     return; */
/*                 } */
/*             } */
/*             catch(Throwable t){} */
/*         } */
/*         super.hyperlinkUpdate(he); */
/*     } */
/* } */

