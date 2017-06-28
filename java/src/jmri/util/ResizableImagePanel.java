package jmri.util;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import jmri.util.iharder.dnd.FileDrop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class extending JPanels to have a image display in a panel, supports: +
 * drag'n drop of image file + can resize container + can scale content to size
 * + respect aspect ratio by default (when resizing content)
 *
 * (overrides paintComponent for performances)
 *
 * @author Lionel Jeanson - Copyright 2009
 */
public class ResizableImagePanel extends JPanel implements FileDrop.Listener, ComponentListener {

    private String _imagePath;
    protected JLabel bgImg = null;
    private BufferedImage image = null;
    private BufferedImage scaledImage = null;
    private boolean _resizeContainer = false;
    private boolean _respectAspectRatio = true;
    static private Color BackGroundColor = Color.BLACK;
    private MyMouseAdapter myMouseAdapter = null;
    boolean toResize = false;
    private String dropFolder;
    final static Dimension smallDim = new Dimension(10, 10);

    /**
     * Default constructor.
     *
     */
    public ResizableImagePanel() {
        super();
        setDnd(false);
        super.setBackground(BackGroundColor);
        setVisible(false);
    }

    /**
     * Constructor with initial image file path as parameter. Component will be
     * (preferred) sized to image sized
     *
     *
     * @param imagePath Path to image to display
     */
    public ResizableImagePanel(String imagePath) {
        super();
        super.setBackground(BackGroundColor);
        setImagePath(imagePath);
        setDnd(false);
    }

    /**
     * Constructor for DnDImagePanel with forced initial size
     *
     * @param imagePath Path to image to display
     * @param w         Panel width
     * @param h         Panel height
     */
    public ResizableImagePanel(String imagePath, int w, int h) {
        super();
        setPreferredSize(new Dimension(w, h));
        setSize(w, h);
        super.setBackground(BackGroundColor);
        setImagePath(imagePath);
        setDnd(false);
    }

    @Override
    public void setBackground(Color bckCol) {
        super.setBackground(bckCol);
        setScaledImage();
    }

    /**
     * Enable or disable drag and drop, dropped files will be copied in latest
     * used image path top folder when DND enabled, also enable contextual menu
     * with remove entry.
     *
     * @param dnd true to enable drag and drop
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
    class MyMouseAdapter implements MouseListener {

        private JPopupMenu popUpMenu;
        private JMenuItem removeMenuItem;
        private ResizableImagePanel rip;

        public MyMouseAdapter(ResizableImagePanel resizableImagePanel) {
            rip = resizableImagePanel;
            popUpMenu = new JPopupMenu();
            removeMenuItem = new JMenuItem("Remove");
            removeMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rip.setImagePath(null);
                    removeMenuItem.setEnabled(false);
                }
            });
            if (rip.image == null) {
                removeMenuItem.setEnabled(false);
            }
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

    /**
     * Allows this DnDImagePanel to force resize of its container
     *
     * @param b true if the container can be resized
     */
    public void setResizingContainer(boolean b) {
        _resizeContainer = b;
    }

    /**
     * Can this DnDImagePanel resize its container?
     *
     * @return true if the container can be resized
     */
    public boolean isResizingContainer() {
        return _resizeContainer;
    }

    /**
     * Is this DnDImagePanel respecting aspect ratio when resizing content?
     *
     * @return true if the aspect ratio respected when resizing
     */
    public boolean isRespectingAspectRatio() {
        return _respectAspectRatio;
    }

    /**
     * Allow this DnDImagePanel to respect aspect ratio when resizing content.
     *
     * @param b true if aspect ratio needs to be respected
     */
    public void setRespectAspectRatio(boolean b) {
        _respectAspectRatio = b;
    }

    /**
     * Return current image file path
     *
     * @return the path
     */
    public String getImagePath() {
        return _imagePath;
    }

    /**
     * Set image file path, display will be updated if passed value is null,
     * blank image.
     *
     * @param s the path
     */
    public void setImagePath(String s) {
        if (s == null) {
            if ((_imagePath != null) && (!(new File(_imagePath)).isDirectory())) {
                _imagePath = (new File(_imagePath)).getParent();
            } else if (_imagePath == null) {
                _imagePath = "/";
            }
        } else {
            _imagePath = s;
        }
        log.debug("Image path is now : {}", _imagePath);
        try {
            image = ImageIO.read(new File(_imagePath));
            if (myMouseAdapter != null) {
                myMouseAdapter.removeMenuItem.setEnabled(true);
            }
        } catch (Exception ex) {
            log.debug("{} is not a valid image file, exception: ", _imagePath, ex);
            image = null;
            scaledImage = null;
        }
        if (isResizingContainer()) {
            resizeContainer();
        }
        setScaledImage();
        setVisible(true);
        repaint();
        if (getParent() != null) {
            getParent().repaint();
        }
    }

    //
    // componentListener methods, for auto resizing and scaling
    //
    @Override
    public void componentResized(ComponentEvent e) {
        if (!(isResizingContainer())) {
            if (e.getComponent().isVisible()) {
                setSize(e.getComponent().getSize());
                setPreferredSize(e.getComponent().getSize());
                setScaledImage();
                toResize = false;
            } else {
                toResize = true;
            }
        }
        repaint();
        if (getParent() != null) {
            getParent().repaint();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if (isResizingContainer()) {
            resizeContainer();
        } else if ((toResize) || (scaledImage == null)) {
            setSize(e.getComponent().getSize());
            setPreferredSize(e.getComponent().getSize());
            setScaledImage();
            toResize = false;
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Component hidden");
        if (isResizingContainer()) {
            resizeContainer(smallDim);
        }
    }

    private void resizeContainer(Dimension d) {
        log.debug("Resizing container");
        Container p1 = getParent();
        if ((p1 != null) && (image != null)) {
            setPreferredSize(d);
            setSize(d);
            p1.setPreferredSize(d);
            p1.setSize(d);
            if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
                ((Window) getTopLevelAncestor()).pack(); // yes, lucky hack, possibly dirty
            }
        }
    }

    private void resizeContainer() {
        if (scaledImage != null) {
            resizeContainer(new Dimension(scaledImage.getWidth(null), scaledImage.getHeight(null)));
        } else if (image != null) {
            resizeContainer(new Dimension(image.getWidth(null), image.getHeight(null)));
        }
    }

    //override paintComponent
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scaledImage != null) {
            g.drawImage(scaledImage, 0, 0, this);
        } else {
            g.clearRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());
        }
    }

    /**
     * Get current scaled Image.
     *
     * @return the scaled image
     */
    public BufferedImage getScaledImage() {
        return scaledImage;
    }

    private void setScaledImage() {
        if (image != null) {
            if ((getSize().getWidth() != 0) && (getSize().getHeight() != 0)
                    && ((getSize().getWidth() != image.getWidth(null)) || (getSize().getHeight() != image.getHeight(null)))) {
                int newW = (int) getSize().getWidth();
                int newH = (int) getSize().getHeight();
                int new0x = 0;
                int new0y = 0;
                log.debug("Actually resizing image {} to {}x{}", this.getImagePath(), newW, newH);
                scaledImage = new BufferedImage(newW, newH, image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType());
                Graphics2D g = scaledImage.createGraphics();
                g.setBackground(getBackground());
                g.clearRect(0, 0, newW, newH);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                if (_respectAspectRatio) {
                    if ((getSize().getWidth() / getSize().getHeight()) > ((double) image.getWidth(null) / (double) image.getHeight(null))) { // Fill on height
                        newW = image.getWidth(null) * newH / image.getHeight(null);
                        new0x = (int) (getSize().getWidth() - newW) / 2;
                    } else { // Fill on width
                        newH = image.getHeight(null) * newW / image.getWidth(null);
                        new0y = (int) (getSize().getHeight() - newH) / 2;
                    }
                }
                g.drawImage(image, new0x, new0y, new0x + newW, new0y + newH, 0, 0, image.getWidth(), image.getHeight(), this);
                g.dispose();
            } else {
                scaledImage = image;
            }
        }
    }

    private static void copyFile(File in, File out) throws Exception {
        if (!out.getParentFile().mkdirs()) // make directorys, check success
        {
            log.error("failed to make directories to copy file");
        }
        FileInputStream fis = new FileInputStream(in);
        try {
            FileOutputStream fos = new FileOutputStream(out);
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                fos.close();
            }
        } finally {
            fis.close();
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
                    copyFile(files[0], dest);
                } catch (Exception ex) {
                    log.error("filesDropped: error while copying new file, using original file");
                    dest = files[0];
                }
            }
        }
        setImagePath(dest.getPath());
    }

    private final static Logger log = LoggerFactory.getLogger(ResizableImagePanel.class.getName());
}
