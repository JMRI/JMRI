package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LinkingLabel is a PositionableLabel that opens a link
 * to another window or URL when clicked
 *
 * @author Bob Jacobsen Copyright (c) 2013
 * @version $Revision: 22576 $
 */

public class LinkingLabel extends PositionableLabel implements LinkingObject {

    public LinkingLabel(String s, Editor editor, String url) {
        super(s, editor);
        this.url = url;
        setPopupUtility(new PositionablePopupUtil(this, this));
    }
    public LinkingLabel(NamedIcon s, Editor editor, String url) {
        super(s, editor);
        this.url = url;
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    public Positionable deepClone() {
        PositionableLabel pos;
        if (_icon) {
            NamedIcon icon = new NamedIcon((NamedIcon)getIcon());
            pos = new LinkingLabel(icon, _editor, url);
        } else {
            pos = new LinkingLabel(_unRotatedText, _editor, url);
        }
        return finishClone(pos);
    }
    public Positionable finishClone(Positionable p) {
    	LinkingLabel pos = (LinkingLabel)p;
        return super.finishClone(pos);
    }

    String url;
    public String getUrl() {return url;}
    public void setUrl(String u) {url = u;}
    
    public boolean setLinkMenu(JPopupMenu popup) {
        popup.add(CoordinateEdit.getLinkEditAction(this, "EditLink"));
    	return true;
    }
    
    // overide where used - e.g. momentary
//    public void doMousePressed(MouseEvent event) {}
//    public void doMouseReleased(MouseEvent event) {}
    public void doMouseClicked(MouseEvent event) {
        log.debug("click to "+url);
        try {
            if (url.startsWith("frame:")) {
                // locate JmriJFrame and push to front
                String frame = url.substring(6);
                final jmri.util.JmriJFrame jframe = jmri.util.JmriJFrame.getFrame(frame);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jframe.toFront();
                        jframe.repaint();
                    }
                });                
            } else if (url!=null && url.length()>0) {
                jmri.util.ExternalLinkContentViewerUI.activateURL(new java.net.URL(url));
            }
        } catch (Throwable t) { log.error("Error handling link", t); }
        super.doMouseClicked(event);
    }
    
    static Logger log = LoggerFactory.getLogger(LinkingLabel.class.getName());

}
