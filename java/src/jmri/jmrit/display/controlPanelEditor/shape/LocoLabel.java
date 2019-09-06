package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPopupMenu;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackPaths;
import jmri.jmrit.logix.OBlock;

public class LocoLabel extends PositionableRoundRect {

    private IndicatorTrackPaths _pathUtil;
    private OBlock _block;

    public LocoLabel(Editor editor) {
        super(editor);
        setEditable(false);
    }

    public void setBlock(OBlock b, IndicatorTrackPaths pu) {
        _block = b;
        _pathUtil = pu;
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
    public void remove() {
        IndicatorTrackPaths temp = _pathUtil;
        _pathUtil = null;
        if (temp != null) {
            temp.removeLocoIcon();
        }
        if (_block != null) {
            _block.setValue(null);
            _block.setState(_block.getState() & ~OBlock.RUNNING);
        }
        _block = null;
        super.remove();
    }

        @Override
    public boolean storeItem() {
        return false;
    }
}
