package jmri.jmrit.display.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LinkingLabel;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for text labels.
 * @see ItemPanel palette class diagram
 */
public class TextItemPanel extends ItemPanel /*implements ActionListener */ {

    DecoratorPanel _decorator;

    public TextItemPanel(ItemPalette parentFrame, String type, Editor editor) {
        super((ItemPalette) parentFrame, type, editor);
        setToolTipText(Bundle.getMessage("ToolTipDragText"));
    }

    @Override
    public void init() {
        if (!_initialized) {
            if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
            Thread.yield();
            JPanel blurb = new JPanel();
            blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
            blurb.add(new JLabel(Bundle.getMessage("addTextAndAttrs")));
            blurb.add(new JLabel(Bundle.getMessage("ToolTipDragText")));
            blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
            blurb.add(new JLabel(Bundle.getMessage("ToLinkToURL", "Text")));
            blurb.add(new JLabel(Bundle.getMessage("enterPanel")));
            blurb.add(new JLabel(Bundle.getMessage("enterURL")));
            JPanel p = new JPanel();
            p.add(blurb);
            add(p);
            makeDecoratorPanel();
            initLinkPanel();
            _paletteFrame.pack();
            super.init();
        }
    }

    private void makeDecoratorPanel() {
        if (_decorator != null) {
            _decorator.removeAll();
            _decorator.updateSamples();
        } else {
            DragDecoratorLabel sample = new DragDecoratorLabel(Bundle.getMessage("sample"), _editor);
            _decorator = new DecoratorPanel(_editor, null);
            _decorator.initDecoratorPanel(sample);
            add(_decorator, 1);
        }
    }

    @Override
    protected void setEditor(Editor ed) {
        super.setEditor(ed);
        if (_initialized) {
            makeDecoratorPanel();
        }
    }

    /**
     * Export a Positionable item from panel.
     */
    class DragDecoratorLabel extends PositionableLabel implements DragGestureListener, DragSourceListener, Transferable {

        DataFlavor dataFlavor;

        public DragDecoratorLabel(String s, Editor editor) {
            super(s, editor);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this,
                    DnDConstants.ACTION_COPY, this);
            try {
                dataFlavor = new DataFlavor(Editor.POSITIONABLE_FLAVOR);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }

        /**
         * ************** DragGestureListener **************
         */
        @Override
        public void dragGestureRecognized(DragGestureEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("DragPositionable.dragGestureRecognized ");
            }
            //Transferable t = getTransferable(this);
            e.startDrag(DragSource.DefaultCopyDrop, this, this);
        }

        /**
         * ************** DragSourceListener ***********
         */
        @Override
        public void dragDropEnd(DragSourceDropEvent e) {
        }

        @Override
        public void dragEnter(DragSourceDragEvent e) {
        }

        @Override
        public void dragExit(DragSourceEvent e) {
        }

        @Override
        public void dragOver(DragSourceDragEvent e) {
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent e) {
        }

        /**
         * ************* Transferable ********************
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            //if (log.isDebugEnabled()) log.debug("DragPositionable.getTransferDataFlavors ");
            return new DataFlavor[]{dataFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("DragPositionable.isDataFlavorSupported ");
            return dataFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String link = _linkName.getText().trim();
            PositionableLabel l;
            if (link.length() == 0) {
                l = new PositionableLabel(getText(), _editor);
            } else {
                l = new LinkingLabel(getText(), _editor, link);
            }
            _decorator.setAttributes(l);
            PositionablePopupUtil util = _decorator.getPositionablePopupUtil();
            l.setPopupUtility(util.clone(l, l.getTextComponent()));
            // l.setFont(util.getFont().deriveFont(util.getFontStyle()));
            if (util.hasBackground()) { // unrotated
                l.setOpaque(true);
            }
            l.setLevel(this.getDisplayLevel());
            return l;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TextItemPanel.class);

}
