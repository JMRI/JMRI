package jmri.util;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import jmri.util.iharder.dnd.*;

import java.io.*;
import java.awt.*;

import javax.swing.*;
import javax.imageio.*;

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
public class ResizableImagePanel extends JPanel implements FileDrop.Listener, ComponentListener {

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

    /** 
     * Default constructor.
     *
     */
    public ResizableImagePanel() {
        super();
        setDnd(false);
        setBackground(BackGroundColor);
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
        setBackground(BackGroundColor);
        setImagePath(imagePath);
        setDnd(false);
    }

    /** 
     * Constructor for DnDImagePanel with forced initial size
     *
     * @param imagePath Path to image to display
     * @param w Panel width
     * @param h Panel height
     */
    public ResizableImagePanel(String imagePath, int w, int h) {
        super();
        setPreferredSize(new Dimension(w, h));
        setSize(w, h);
        setBackground(BackGroundColor);
        setImagePath(imagePath);
        setDnd(false);
    }
    
    /**
     * Enable or disable drag'n drop, dropped files will be copied in latest used image path top folder
     * 
     * @param dnd
     */
    public void setDnd(boolean dnd)
    {
    	if (dnd)
    		new FileDrop(this, this);
    	else
    		FileDrop.remove(this);
    }

    /**
     * Allows this DnDImagePanel to force resize of its container
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
     * Allow this DnDImagePanel to respect aspect ratio when resizing content
     *
     * @param b
     */
    public void setRespectAspectRatio(boolean b) {
    	_respectAspectRatio = b;
    }
    
    public String getImagePath() {
        return _imagePath;
    }

    public void setImagePath(String s) {
    	if (s==null)
    		_imagePath = (new File(_imagePath)).getParent() ; 
    	else
    		_imagePath = s;
        loadImage();
    }

    boolean toResize = false;
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
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    	log.debug("Component shown");
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

    final static Dimension smallDim = new Dimension(10,10);
    
    public void componentHidden(ComponentEvent e) {
    	log.debug("Component hidden");
    	if (isResizingContainer()) {
    		resizeContainer(smallDim);
    	}
    }

	private boolean loadImage() {
        try {
            image = ImageIO.read(new File(_imagePath));
        } catch (Exception ex) {
        	if (log.isDebugEnabled()) log.debug(_imagePath + " is not a valid image file, exception: " + ex);
            unsetImage();
            return false;
        }
        if (isResizingContainer())
        	resizeContainer();       
        setScaledImage();
        setVisible(true);
        return true;
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
	
    public void unsetImage()
    {
    	image = null;
        scaledImage = null;
        repaint();
    }

	//override paintComponent
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	if (scaledImage != null)
    		g.drawImage(scaledImage, 0, 0, this);       
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
    			log.debug("Background color is "+getBackground());
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

    private static void copyFile(File in, File out) throws Exception {
        if (!out.getParentFile().mkdirs()) // make directorys, check success
            log.error("failed to make directories to copy file");
    	FileInputStream fis = new FileInputStream(in);
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
        	fis.close();
        	fos.close();            
        }
    }

    public void filesDropped(File[] files) {
        if (files == null) {
            return;
        }
        if (files.length == 0) {
            return;
        }
        
        File orig = new File(_imagePath);
        String parent;
        if (orig.isDirectory()) 
        	parent = orig.getPath();
        else
        	parent = orig.getParent();
        File dest = new File(parent + File.separatorChar + files[0].getName());
        try {
            if (parent.compareTo(files[0].getParent()) != 0) {
                copyFile(files[0], dest);
            }
        } catch (Exception ex) {
            log.error("filesDropped: error while copying new file " + ex);
            return;
        }
        setImagePath(dest.getPath());
    }

    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ResizableImagePanel.class.getName());
}
