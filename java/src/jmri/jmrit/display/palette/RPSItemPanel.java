package jmri.jmrit.display.palette;

//import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JLabel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.RpsPositionIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPSItemPanel extends FamilyItemPanel {

    public RPSItemPanel(DisplayFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "active");
    }

    /*
     * ****************************************************
     */
    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return new IconDragJLabel(flavor, map, icon);
    }

    protected class IconDragJLabel extends DragJLabel {

        HashMap<String, NamedIcon> iconMap;

        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
            super(flavor, icon);
            iconMap = new HashMap<>(map);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }

            if (log.isDebugEnabled()) {
                log.debug("IconDragJLabel.getTransferData");
            }
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                RpsPositionIcon r = new RpsPositionIcon(_editor);
                r.setActiveIcon(new NamedIcon(iconMap.get("active")));
                r.setErrorIcon(new NamedIcon(iconMap.get("error")));
                r.setSize(r.getPreferredSize().width, r.getPreferredSize().height);
                r.setLevel(Editor.SENSORS);
                return r;                
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icons");
                return  sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RPSItemPanel.class);

}
