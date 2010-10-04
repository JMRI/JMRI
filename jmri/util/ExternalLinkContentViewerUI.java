// ExternalLinkContentViewerUI.java

package jmri.util;

/**
 * A UI subclass that will open external links (website 
 * or mail links) in an external browser
 * <P>
 * To use:
 * SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
 * <P.
 * Because JMRI is still being built with Java 1.5, this class is commented
 * out in the source, but provided as a .jar file in lib/.
 * To recreate it, compile with Java 1.6, delete the rest of class/ except this,
 * and use the jar target to rebuild.
 *
 @see http://forums.sun.com/thread.jspa?threadID=728061
 @since JMRI 2.5.3 (or perhaps later, please check CVS)
 */
 
//public class ExternalLinkContentViewerUI extends BasicContentViewerUI {
//    public ExternalLinkContentViewerUI(JHelpContentViewer x){
//		super(x);
//	}
//
//    public static javax.swing.plaf.ComponentUI createUI(JComponent x){
//        return new ExternalLinkContentViewerUI((JHelpContentViewer)x);
//    }
//
//    public void hyperlinkUpdate(HyperlinkEvent he){
//        if(he.getEventType()==HyperlinkEvent.EventType.ACTIVATED){
//            try{
//                URL u = he.getURL();
//                if(u.getProtocol().equalsIgnoreCase("mailto")||u.getProtocol().equalsIgnoreCase("http")
//                        ||u.getProtocol().equalsIgnoreCase("ftp")
//                     ){
//                    java.awt.Desktop.getDesktop().browse(new URI(u.toString()));
//                    return;
//                } else if ( u.getProtocol().equalsIgnoreCase("file") && (
//                        u.getFile().endsWith("jpg")
//                        ||u.getFile().endsWith("gif")) ) {
//                    
//                    URI uri = new URI("file:"+System.getProperty("user.dir")+"/"+u.getFile());
//                    java.awt.Desktop.getDesktop().browse(uri);
//                    return;
//                }
//            }
//            catch(Throwable t){}
//        }
//        super.hyperlinkUpdate(he);
//    }
//}

