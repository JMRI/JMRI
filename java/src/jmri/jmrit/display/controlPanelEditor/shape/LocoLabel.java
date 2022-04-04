package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import jmri.jmrit.display.Editor;
import jmri.jmrit.logix.OBlock;

public class LocoLabel extends PositionableRoundRect {

    private OBlock _block;

    public LocoLabel(Editor editor) {
        super(editor);
        setEditable(false);
    }

    public void setBlock(OBlock b) {
        _block = b;
        invalidateShape();
    }

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        setRotateMenu(popup);
        getEditor().setRemoveMenu(this, popup);
        return true;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (_block == null) {
            return;
        }
        String trainName = (String) _block.getValue();
        if (trainName == null) {
            return;
        }
        Font font = _block.getMarkerFont();
        if (font == null) {
            font = getFont();
        }
        g.setFont(font);
        int textWidth = getFontMetrics(font).stringWidth(trainName);
        int textHeight = getFontMetrics(font).getHeight();
        int hOffset = Math.max((maxWidth() - textWidth) / 2, 0);
        int vOffset = Math.max((maxHeight() - textHeight) / 2, 0) + getFontMetrics(font).getAscent();
        g.setColor(_block.getMarkerForeground());
        g.drawString(trainName, hOffset, vOffset);
    }

    @Override
    protected void paintHandles(Graphics2D g2d) {
    }

    @Override
    protected boolean doHandleMove(MouseEvent event) {
        return false;
    }

    @Override
    public boolean storeItem() {
        return false;
    }
}
