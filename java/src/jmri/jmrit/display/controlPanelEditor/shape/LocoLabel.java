package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import jmri.jmrit.display.Editor;
import jmri.jmrit.logix.OBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


    public class LocoLabel extends PositionableRoundRect {

        private static final long serialVersionUID = -5874790860514345475L;
        OBlock _block;

        public LocoLabel(Editor editor) {
            super(editor);
        }

        public LocoLabel(Editor editor, Shape shape) {
            super(editor, shape);
        }

        public void setBlock(OBlock b) {
            _block = b;
        }

        public OBlock getBlock() {
            return _block;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (_block==null) {
                return;
            }
            String trainName = (String) _block.getValue();
            if (trainName == null) {
                return;
            }
            Graphics2D g2d = (Graphics2D) g;
            Font font = _block.getMarkerFont();
            if (font == null) {
                font = getFont();
            }
            g2d.setFont(font);
            int textWidth = getFontMetrics(font).stringWidth(trainName);
            int textHeight = getFontMetrics(font).getHeight();
            int hOffset = Math.max((maxWidth() - textWidth) / 2, 0);
            int vOffset = Math.max((maxHeight() - textHeight) / 2, 0) + getFontMetrics(font).getAscent();
            g2d.setColor(_block.getMarkerForeground());
            g2d.drawString(trainName, hOffset, vOffset);
        }
        
        private final static Logger log = LoggerFactory.getLogger(LocoLabel.class.getName());
    }
