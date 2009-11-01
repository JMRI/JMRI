package jmri.util;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
    private Image image = null;
    private Image scaledImage = null;
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

    public void componentResized(ComponentEvent e) {
        setSize(e.getComponent().getSize());
        setPreferredSize(e.getComponent().getSize());
        setScaledImage();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }


	private boolean loadImage() {
        try {
            image = ImageIO.read(new File(_imagePath));
        } catch (Exception ex) {
        	if (log.isDebugEnabled())
        		log.debug(_imagePath + " is not a valid image file, exception: " + ex);
            unsetImage();
            return false;
        }
        if (isResizingContainer()) {
        	Container p1 = getParent();
        	if (p1 != null)
        	{
        		Dimension d = new Dimension(image.getWidth(null), image.getHeight(null)) ;
        		p1.setPreferredSize(d);
        		setPreferredSize(d);
        		setSize(d);
        		while (p1.getParent() != null) 	p1=p1.getParent();
        		try {
        			((Window)p1).pack(); // yes, lucky hack, possibly dirty
        		}
        		catch(Exception e)
        		{
        			log.error("loadImage() no top parent pack to force resize");
        		}
        	}
        }
        setScaledImage();
        setVisible(true);
        return true;
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
        if (scaledImage != null) {
        	if ( _respectAspectRatio )
        	{
        		g.drawImage(scaledImage, (int)( getSize().getWidth() / 2 ) - ( scaledImage.getWidth(null)/2 ), 
        								 (int)( getSize().getHeight() / 2 ) - ( scaledImage.getHeight(null)/2 ), this);
        	}
        	else
        		g.drawImage(scaledImage, 0, 0, this);
        }
    }

    private void setScaledImage() {
        if (image != null) {
            if ((getSize().getWidth() != image.getWidth(null)) || (getSize().getHeight() != image.getHeight(null))) {
            	if ( _respectAspectRatio )
            		if (  (getSize().getWidth() / getSize().getHeight()) > ((double)image.getWidth(null) / (double)image.getHeight(null)) )
            			scaledImage = image.getScaledInstance( -1, (int)getSize().getHeight(), java.awt.Image.SCALE_SMOOTH);
            		else
            			scaledImage = image.getScaledInstance((int) getSize().getWidth(), -1, java.awt.Image.SCALE_SMOOTH);
            	else
            		scaledImage = image.getScaledInstance((int) getSize().getWidth(), (int) getSize().getHeight(), java.awt.Image.SCALE_SMOOTH);
            } else {
                scaledImage = image;
            }
        }
    }

    private static void copyFile(File in, File out) throws Exception {
        out.getParentFile().mkdirs();
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
