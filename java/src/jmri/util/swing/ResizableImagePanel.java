package jmri.util.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.coobird.thumbnailator.ThumbnailParameter;
import net.coobird.thumbnailator.builders.ThumbnailParameterBuilder;
import net.coobird.thumbnailator.filters.ImageFilter;
import net.coobird.thumbnailator.tasks.io.FileImageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class extending JPanels to have a image display in a panel, supports<ul>
 * <li>drag'n drop of image file</li>
 * <li>can resize container</li>
 * <li>can scale content to size</li>
 * <li>respect aspect ratio by default (when resizing content)</li>
 * </ul>
 * (overrides paintComponent for performances)
 *
 * @author Lionel Jeanson - Copyright 2009
 */
public class ResizableImagePanel extends JPanel implements ComponentListener {

    public static final String IMAGE_PATH = "imagePath";

    private String _imagePath;
    protected JLabel bgImg = null;
    private BufferedImage image = null;
    private BufferedImage scaledImage = null;
    private boolean _resizeContainer = false;
    private boolean _respectAspectRatio = true;
    static private Color backgroundColor = Color.BLACK;
    boolean toResize = false;
    final static Dimension SMALL_DIM = new Dimension(10, 10);

    /**
     * Default constructor.
     */
    public ResizableImagePanel() {
        super();
        super.setBackground(backgroundColor);
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
        super.setBackground(backgroundColor);
        setImagePath(imagePath);
    }

    /**
     * Constructor for ResizableImagePanel with forced initial size
     *
     * @param imagePath Path to image to display
     * @param w         Panel width
     * @param h         Panel height
     */
    public ResizableImagePanel(String imagePath, int w, int h) {
        super();
        setPreferredSize(new Dimension(w, h));
        setSize(w, h);
        super.setBackground(backgroundColor);
        setImagePath(imagePath);
    }

    @Override
    public void setBackground(Color bckCol) {
        super.setBackground(bckCol);
        setScaledImage();
    }

    /**
     * Allows this ResizableImagePanel to force resize of its container
     *
     * @param b true if this instance can resize its container; false otherwise
     */
    public void setResizingContainer(boolean b) {
        _resizeContainer = b;
    }

    /**
     * Can this DnDImagePanel resize its container?
     *
     * @return true if container can be resized
     */
    public boolean isResizingContainer() {
        return _resizeContainer;
    }

    /**
     * Is this DnDImagePanel respecting aspect ratio when resizing content?
     *
     * @return true is aspect ratio is maintained
     */
    public boolean isRespectingAspectRatio() {
        return _respectAspectRatio;
    }

    /**
     * Allow this ResizableImagePanel to respect aspect ratio when resizing
     * content.
     *
     * @param b true if aspect ratio should be respected; false otherwise
     */
    public void setRespectAspectRatio(boolean b) {
        _respectAspectRatio = b;
    }

    /**
     * Return current image file path
     *
     * @return The image path or "/" if no image is specified
     */
    public String getImagePath() {
        return _imagePath;
    }

    /**
     * Read image and handle exif information if it exists in the file.
     * 
     * @param file the image file
     * @return the image
     * @throws IOException in case of an I/O error
     */
    private BufferedImage readImage(File file) throws IOException {
        ThumbnailParameterBuilder builder = new ThumbnailParameterBuilder();
        builder.scale(1.0);
        ThumbnailParameter param = builder.build();
        
        FileImageSource fileImageSource = new FileImageSource(file);
        fileImageSource.setThumbnailParameter(param);
        
        BufferedImage img = fileImageSource.read();
        
        // Perform the image filters
        for (ImageFilter filter : param.getImageFilters()) {
            img = filter.apply(img);
        }
        
        return img;
    }

    /**
     * Set image file path, display will be updated if passed value is null,
     * blank image
     *
     * @param s path to image file
     */
    public void setImagePath(String s) {
        String old = _imagePath;
        if (s != null && !s.equals("")) {
            _imagePath = s;
        } else {
            _imagePath = null;
            image = null;
            scaledImage = null;
        }
        log.debug("Image path is now : {}", _imagePath);
        if (_imagePath != null) {
            try {
                image = readImage(new File(_imagePath));
            } catch (IOException ex) {
                log.error("{} is not a valid image file, exception: ", _imagePath, ex);
                image = null;
                scaledImage = null;
            }
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
        this.firePropertyChange(IMAGE_PATH, old, _imagePath);
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
        } else {
            if ((toResize) || (scaledImage == null)) {
                setSize(e.getComponent().getSize());
                setPreferredSize(e.getComponent().getSize());
                setScaledImage();
                toResize = false;
            }
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Component hidden");
        if (isResizingContainer()) {
            resizeContainer(SMALL_DIM);
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
            Container c = getTopLevelAncestor();
            if (c != null && c instanceof Window) {
                ((Window) c).pack();
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
     * Get current scaled Image
     *
     * @return the image resized as specified
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

    private final static Logger log = LoggerFactory.getLogger(ResizableImagePanel.class);
}
