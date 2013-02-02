package jmri.util.swing;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.util.iharder.dnd.FileDrop;

public class EditableResizableImagePanel extends ResizableImagePanel implements FileDrop.Listener {
    private MyMouseAdapter myMouseAdapter = null;
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
     * Constructor with initial image file path as parameter.
     * 		Component will be (preferred) sized to image sized
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
     * @param w Panel width
     * @param h Panel height
     */
    public EditableResizableImagePanel(String imagePath, int w, int h) {
        super(imagePath,w,h);
        setDnd(true);
    }
    
    /**
     * Enable or disable drag'n drop, dropped files will be copied in latest used image path top folder
     * when dnd enabled, also enable contextual menu with remove entry
     * 
     * @param dnd
     */
    public void setDnd(boolean dnd)
    {
    	if (dnd) {
    		new FileDrop(this, this);
    		if (myMouseAdapter == null)
    			myMouseAdapter = new MyMouseAdapter(this);
    		addMouseListener(myMouseAdapter);
    	}
    	else {
    		FileDrop.remove(this);
    		if (myMouseAdapter != null)
    			removeMouseListener(myMouseAdapter);
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
    			public void actionPerformed(ActionEvent e) {
    				rip.setImagePath(null);
    			}
    		} );
    		popUpMenu.add(removeMenuItem);
    	}
    	void setRemoveMenuItemEnable(boolean b) {
    		removeMenuItem.setEnabled(b);
    	}
		public void mouseClicked(MouseEvent e) {
			maybeShowPopup(e);
		}
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);			
		}
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popUpMenu.show(e.getComponent(),e.getX(), e.getY());
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
    
    public void setDropFolder(String s) {
    	dropFolder = s;
    }
    
    public String getDropFolder() {
    	return dropFolder;
    }
    
    /**
     * Callback for the dnd listener
     */
    public void filesDropped(File[] files) {
        if (files == null) {
            return;
        }
        if (files.length == 0) {
        	return;
        }
        File dest = files[0];
        if (dropFolder!=null) {
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
    
    static private Logger log = Logger.getLogger(EditableResizableImagePanel.class.getName());
}
