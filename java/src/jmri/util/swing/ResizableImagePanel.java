package jmri.util.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A class extending JPanels to have a image display in a panel, supports:
 *     + drag'n drop of image file
 *     + can resize container
 *     + can scale content to size
 *     + respect aspect ratio by default (when resizing content)
 *
 * (overrides paintComponent for performances)
 *
 * @author Lionel Jeanson - Copyright 2009
 */
public class ResizableImagePanel extends JPanel implements ComponentListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4576214324220842001L;
	private String _imagePath;
    protected JLabel bgImg = null;
    private BufferedImage image = null;
    private BufferedImage scaledImage = null;
    private boolean _resizeContainer = false;
    private boolean _respectAspectRatio = true;
    static private Color BackGroundColor = Color.BLACK ;
    boolean toResize = false;
    final static Dimension smallDim = new Dimension(10,10);
    
    /** 
     * Default constructor.
     *
     */
    public ResizableImagePanel() {
        super();
        super.setBackground(BackGroundColor);
        setVisible(false);
    }
    
    /** 
     * Constructor with initial image file path as parameter.
     * 		Component will be (preferred) sized to image sized
     *		
     *
     * @param imagePath Path to image to display 
     */
    public ResizableImagePanel(String imagePath) {
        super();
        super.setBackground(BackGroundColor);
        setImagePath(imagePath);
    }

    /** 
     * Constructor for ResizableImagePanel with forced initial size
     *
     * @param imagePath Path to image to display
     * @param w Panel width
     * @param h Panel height
     */
    public ResizableImagePanel(String imagePath, int w, int h) {
        super();
        setPreferredSize(new Dimension(w, h));
        setSize(w, h);
        super.setBackground(BackGroundColor);
        setImagePath(imagePath);
    }
    
    public void setBackground(Color bckCol) {
    	super.setBackground(bckCol);
    	setScaledImage();
    }

    /**
     * Allows this ResizableImagePanel to force resize of its container
     *
     * @param b
     */
    public void setResizingContainer(boolean b) {
        _resizeContainer = b;
    }

    /**
     * Can this DnDImagePanel resize its container?
     *
     */
    public boolean isResizingContainer() {
        return _resizeContainer;
    }
    
    /**
     * Is this DnDImagePanel respecting aspect ratio when resizing content?
     *
     */
    public boolean isRespectingAspectRatio() {
    	return _respectAspectRatio;
    }
    
    /**
     * Allow this ResizableImagePanel to respect aspect ratio when resizing content
     *
     * @param b
     */
    public void setRespectAspectRatio(boolean b) {
    	_respectAspectRatio = b;
    }
    
    /**
     * Return curent image file path
     *
     */
    public String getImagePath() {
        return _imagePath;
    }

    /**
     * Set image file path, display will be updated
     * If passed value is null, blank image
     *
     */
    public void setImagePath(String s) {
    	if (s==null) {
    		if ((_imagePath!=null) && (! (new File(_imagePath)).isDirectory()))
    			_imagePath = (new File(_imagePath)).getParent() ; 
    		else 
    			if (_imagePath==null)
    				_imagePath = "/"  ;
    	}
    	else
    		_imagePath = s;
    	if (log.isDebugEnabled()) log.debug("Image path is now : "+_imagePath);
        try {
            image = ImageIO.read(new File(_imagePath));
        } catch (Exception ex) {
        	if (log.isDebugEnabled()) log.debug(_imagePath + " is not a valid image file, exception: " + ex);
        	image = null;
            scaledImage = null;
        }
        if (isResizingContainer())
        	resizeContainer();       
        setScaledImage();       
        setVisible(true);
        repaint();
        if (getParent() != null)
        	getParent().repaint();
    }
	
    //
    // componentListener methods, for auto resizing and scaling
    //
    public void componentResized(ComponentEvent e) {
    	if ( ! (isResizingContainer()))
    		if ( e.getComponent().isVisible() ) {
    			setSize(e.getComponent().getSize());
    			setPreferredSize(e.getComponent().getSize());
    			setScaledImage();
    			toResize = false;
    		}
    		else
    			toResize = true;
        repaint();
        if (getParent() != null)
        	getParent().repaint();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    	if (isResizingContainer())
    		resizeContainer();
    	else {
    		if ((toResize) || (scaledImage==null)) {
    			setSize(e.getComponent().getSize());
    			setPreferredSize(e.getComponent().getSize());
    			setScaledImage();
    			toResize = false;
    		}
        }
    }

    public void componentHidden(ComponentEvent e) {
    	log.debug("Component hidden");
    	if (isResizingContainer()) {
    		resizeContainer(smallDim);
    	}
    }
	
	private void resizeContainer(Dimension d) {
		log.debug("Resizing container");
    	Container p1 = getParent();
    	if ((p1 != null) && (image!=null))
    	{
    		setPreferredSize(d);
    		setSize(d);
    		p1.setPreferredSize(d);
    		p1.setSize(d);
    		if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window))
    			((Window) getTopLevelAncestor()).pack(); // yes, lucky hack, possibly dirty
    	}
	}
    
	private void resizeContainer() {
		if (scaledImage != null)
			resizeContainer( new Dimension(scaledImage.getWidth(null), scaledImage.getHeight(null)) );
		else
			if (image != null)
				resizeContainer( new Dimension(image.getWidth(null), image.getHeight(null)) );
	}

	//override paintComponent
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	if (scaledImage != null)
    		g.drawImage(scaledImage, 0, 0, this);
    	else
			g.clearRect(0,0, (int)getSize().getWidth(), (int)getSize().getHeight());
    }
    
    /**
     * Get curent scaled Image 
     */
    public BufferedImage getScaledImage() {
    	return scaledImage;
    }

    private void setScaledImage() {
    	if (image != null) {
    		if ((getSize().getWidth() != 0) && (getSize().getHeight() != 0) && 
    				((getSize().getWidth() != image.getWidth(null)) || (getSize().getHeight() != image.getHeight(null)))) {
    			int newW = (int)getSize().getWidth();
    			int newH = (int)getSize().getHeight();
    			int new0x = 0;
    			int new0y = 0;
    			if (log.isDebugEnabled()) log.debug("Actually resizing image "+this.getImagePath()+" to "+newW+"x"+newH);
    			scaledImage = new BufferedImage(newW, newH,  image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType());  
    			Graphics2D g = scaledImage.createGraphics();
    			g.setBackground(getBackground());
    			g.clearRect(0, 0, newW, newH);
    			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY ); 
    			if ( _respectAspectRatio )
    				if (  (getSize().getWidth() / getSize().getHeight()) > ((double)image.getWidth(null) / (double)image.getHeight(null)) )
    				{ // Fill on height
    					newW = image.getWidth(null)*newH/image.getHeight(null);
    					new0x = (int)(getSize().getWidth()-newW)/2;
    				}
    				else
    				{ // Fill on width
    					newH = image.getHeight(null)*newW/image.getWidth(null);
    					new0y = (int)(getSize().getHeight()-newH)/2;
    				}
    			g.drawImage(image, new0x, new0y, new0x+newW, new0y+newH, 0, 0, image.getWidth(), image.getHeight(), this);              
    			g.dispose();  
    		} else {
    			scaledImage = image;
    		}
    	}
    }
    
    static private Logger log = LoggerFactory.getLogger(ResizableImagePanel.class.getName());
}
