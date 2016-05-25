package jmri.jmrit.display.palette;

//import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.RpsPositionIcon;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for for plain icons and backgrounds
 */
public class RPSItemPanel extends FamilyItemPanel {

    /**
     *
     */
    private static final long serialVersionUID = 2633287040269806654L;

    /**
     * Constructor for plain icons and backgrounds
     */
    public RPSItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    public void init() {
        if (!_initialized) {
            JPanel panel = new JPanel();
            add(panel);
            super.init();
        }
    }

    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "active");
    }

    /**
     * ****************************************************
     */
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map) {
        return new IconDragJLabel(flavor, map);
    }

    protected class IconDragJLabel extends DragJLabel {

        /**
         *
         */
        private static final long serialVersionUID = -4933936822216537874L;
        HashMap<String, NamedIcon> iconMap;

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2") // icon map is within package 
        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }

            if (log.isDebugEnabled()) {
                log.debug("IconDragJLabel.getTransferData");
            }
            RpsPositionIcon r = new RpsPositionIcon(_editor);
            r.setActiveIcon(new NamedIcon(iconMap.get("active")));
            r.setErrorIcon(new NamedIcon(iconMap.get("error")));
            r.setSize(r.getPreferredSize().width, r.getPreferredSize().height);
            r.setLevel(Editor.SENSORS);
            return r;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RPSItemPanel.class.getName());
}
