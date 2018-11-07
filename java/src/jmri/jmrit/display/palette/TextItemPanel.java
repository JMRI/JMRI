package jmri.jmrit.display.palette;

import java.awt.Color;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LinkingLabel;
import jmri.jmrit.display.Positionable;
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

    /**
     * Constructor for Text Labels.
     *
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type, should be "Text"
     * @param editor      Editor that called this ItemPalette
     */
    public TextItemPanel(DisplayFrame parentFrame, String type, Editor editor) {
        super(parentFrame, type, editor);
        setToolTipText(Bundle.getMessage("ToolTipDragText"));
    }

    @Override
    public void init() {
        if (!_initialized) {
            JPanel blurb = new JPanel();
            blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
            blurb.add(new JLabel(Bundle.getMessage("addTextAndAttrs")));
            blurb.add(new JLabel(Bundle.getMessage("ToolTipDragText")));
            blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(blurb);
            add(p);
            DragDecoratorLabel sample = new DragDecoratorLabel(Bundle.getMessage("sample"), _editor);
            _decorator = new DecoratorPanel(_editor, _paletteFrame);
            _decorator.initDecoratorPanel(sample);
            add(_decorator);
            _paletteFrame.pack();
            if (log.isDebugEnabled()) {
                log.debug("end init: TextItemPanel size {}", getPreferredSize());
            }
            super.init();
        }
        if (_decorator != null) {
            _decorator._bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg());
        }
    }

    public void init(ActionListener doneAction, Positionable pos) {
        _decorator = new DecoratorPanel(_editor, _paletteFrame);
        _decorator.initDecoratorPanel(pos);
    }

    @Override
    public void init(ActionListener doneAction) {
    }

    @Override
    protected void updateBackground0(BufferedImage im) {
        if (_decorator != null) {
            _decorator._bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg());
        }
    }

    @Override
    protected void setPreviewBg(int index) {
        if (_decorator != null) {
            _decorator._bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg());
        }
    }

    protected JPanel makeDoneButtonPanel(ActionListener doneAction) {
        JPanel panel = new JPanel();
        JButton updateButton = new JButton(Bundle.getMessage("updateButton")); // custom update label
        updateButton.addActionListener(doneAction);
        updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        panel.add(updateButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                closeDialogs();
            }
        });
        panel.add(cancelButton);
        return panel;
    }

    @Override
    protected void setEditor(Editor ed) {
        super.setEditor(ed);
        if (_decorator != null) {
            Color panelBackground = _editor.getTargetPanel().getBackground();
            // set Panel background color
            _decorator.setBackgrounds(makeBackgrounds(_decorator.getBackgrounds(), panelBackground));
            _decorator._bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg());
        }
    }
    
    public void updateAttributes(PositionableLabel l) {
        _decorator.setAttributes(l);
        PositionablePopupUtil util = _decorator.getPositionablePopupUtil();
        l.setPopupUtility(util.clone(l, l.getTextComponent()));
        l.setFont(util.getFont().deriveFont(util.getFontStyle()));
        if (util.hasBackground()) { // unrotated
            l.setOpaque(true);
        }
    }

    @Override
    public void closeDialogs() {
        if (_decorator != null) {
            _decorator.setSuppressRecentColor(false);
        }
        super.closeDialogs();
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
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
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
            updateAttributes(l);
            l.setLevel(this.getDisplayLevel());
            return l;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TextItemPanel.class);

}
