package jmri.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.util.FileUtil;
import jmri.util.iharder.dnd.FileDrop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditableResizableImagePanel extends ResizableImagePanel implements FileDrop.Listener {

    private transient MyMouseAdapter myMouseAdapter = null;
    private String dropFolder;

    /**
     * Default constructor.
     *
     */
    public EditableResizableImagePanel() {
        super();
        setDnd(true);
    }

    /**
     * Constructor with initial image file path as parameter. Component will be
     * (preferred) sized to image sized
     *
     * @param imagePath Path to image to display
     */
    public EditableResizableImagePanel(String imagePath) {
        super(imagePath);
        setDnd(true);
    }

    /**
     * Constructor for DnDImagePanel with forced initial size
     *
     * @param imagePath Path to image to display
     * @param w         Panel width
     * @param h         Panel height
     */
    public EditableResizableImagePanel(String imagePath, int w, int h) {
        super(imagePath, w, h);
        setDnd(true);
    }

    /**
     * Enable or disable drag'n drop, dropped files will be copied in latest
     * used image path top folder when dnd enabled, also enable contextual menu
     * with remove entry.
     *
     * @param dnd true to enable, false to disable
     */
    public void setDnd(boolean dnd) {
        if (dnd) {
            new FileDrop(this, this);
            if (myMouseAdapter == null) {
                myMouseAdapter = new MyMouseAdapter(this);
            }
            addMouseListener(myMouseAdapter);
        } else {
            FileDrop.remove(this);
            if (myMouseAdapter != null) {
                removeMouseListener(myMouseAdapter);
            }
        }
    }

    //
    // For contextual menu remove
    static class MyMouseAdapter implements MouseListener {

        private final JPopupMenu popUpMenu;
        private final JMenuItem removeMenuItem;

        public MyMouseAdapter(ResizableImagePanel resizableImagePanel) {
            popUpMenu = new JPopupMenu();
            removeMenuItem = new JMenuItem("Remove");
            removeMenuItem.addActionListener((ActionEvent e) -> {
                resizableImagePanel.setImagePath(null);
            });
            popUpMenu.add(removeMenuItem);
        }

        void setRemoveMenuItemEnable(boolean b) {
            removeMenuItem.setEnabled(b);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popUpMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void setDropFolder(String s) {
        dropFolder = s;
    }

    public String getDropFolder() {
        return dropFolder;
    }

    /**
     * Callback for the dnd listener
     */
    @Override
    public void filesDropped(File[] files) {
        if (files == null) {
            return;
        }
        if (files.length == 0) {
            return;
        }
        File dest = files[0];
        if (dropFolder != null) {
            dest = new File(dropFolder + File.separatorChar + files[0].getName());
            if (files[0].getParent().compareTo(dest.getParent()) != 0) {
                try {
                    FileUtil.createDirectory(dest.getParentFile().getPath());
                    FileUtil.copy(files[0], dest);
                } catch (IOException ex) {
                    log.error("filesDropped: error while copying new file, using original file");
                    dest = files[0];
                }
            }
        }
        setImagePath(dest.getPath());
    }

    private final static Logger log = LoggerFactory.getLogger(EditableResizableImagePanel.class);
}
