package jmri.jmrit.display;

import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.ItemPalette;
import java.awt.Dimension;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

/**
 * LinkingLabel is a PositionableLabel that opens a link
 * to another window or URL when clicked
 *
 * @author Bob Jacobsen Copyright (c) 2013
 * @version $Revision: 22576 $
 */

public class LinkingLabel extends PositionableLabel {

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
            } else {
                jmri.util.ExternalLinkContentViewerUI.activateURL(new java.net.URL(url));
            }
        } catch (Throwable t) { log.error("Error handling link", t); }
        super.doMouseClicked(event);
    }
//    public void doMouseDragged(MouseEvent event) {}
//    public void doMouseMoved(MouseEvent event) {}
//    public void doMouseEntered(MouseEvent event) {}
//    public void doMouseExited(MouseEvent event) {}


    /************ Methods for Item Popups in Panel editor *************************/
/*    JFrame _iconEditorFrame;
    IconAdder _iconEditor;
    public boolean setEditIconMenu(JPopupMenu popup) {
        if (_icon && !_text) {
            String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
            popup.add(new AbstractAction(txt) {
                    public void actionPerformed(ActionEvent e) {
                        edit();
                    }
                });
            return true;
        }
        return false;
    }

    /**
     * For item popups in Panel Editor
     */
 /*   protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor!=null) {
            _iconEditor = editor;
        } else {
            _iconEditor = new IconAdder(name);
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _iconEditorFrame.dispose();
                    _iconEditorFrame = null;
                }
            });
        _iconEditorFrame.setLocationRelativeTo(pos);
        _iconEditorFrame.toFront();
        _iconEditorFrame.setVisible(true);
    }

    protected void edit() {
        makeIconEditorFrame(this, "Icon", false, null);
        NamedIcon icon = new NamedIcon(_namedIcon);
        _iconEditor.setIcon(0, "plainIcon", icon);
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editIcon();
            }
        };
        _iconEditor.complete(addIconAction, true, false, true);

    }

    protected void editIcon() {
        String url = _iconEditor.getIcon("plainIcon").getURL();
        _namedIcon = NamedIcon.getIconByName(url);
        setIcon(_namedIcon);
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    jmri.util.JmriJFrame _paletteFrame;

    /************ Methods for Item Popups in Control Panel editor ********************/
    /**
     * For item popups in Control Panel Editor
     */
 /*   protected void makePalettteFrame(String title) {
    	jmri.jmrit.display.palette.ItemPalette.loadIcons();

        _paletteFrame = new jmri.util.JmriJFrame(title, false, false);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent e) {
                ImageIndexEditor.checkImageIndex();   // write maps to tree
                }
        });
    }

    public boolean setTextEditMenu(JPopupMenu popup) {
        if (isText()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "EditText"));
            return true;
        }
        return false;
    }
 */   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LinkingLabel.class.getName());

}
