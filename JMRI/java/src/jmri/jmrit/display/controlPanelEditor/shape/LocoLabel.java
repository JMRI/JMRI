package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Font;
import java.awt.Graphics;
import jmri.jmrit.display.Editor;
import jmri.jmrit.logix.OBlock;

public class LocoLabel extends PositionableRoundRect {

    OBlock _block;

    public LocoLabel(Editor editor) {
        super(editor);
    }

    public void setBlock(OBlock b) {
        _block = b;
        invalidateShape();
    }

    public OBlock getBlock() {
        return _block;
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
    public boolean storeItem() {
        return false;
    }
}
