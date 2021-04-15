package jmri.util.swing;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.util.FileUtil;
import jmri.util.iharder.dnd.URIDrop;

import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditableResizableImagePanel extends ResizableImagePanel implements URIDrop.Listener {

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
    public final void setDnd(boolean dnd) {
        if (dnd) {
            new URIDrop(this, this);
            if (myMouseAdapter == null) {
                myMouseAdapter = new MyMouseAdapter(this);
            }
            addMouseListener(myMouseAdapter);
        } else {
            URIDrop.remove(this);
            if (myMouseAdapter != null) {
                removeMouseListener(myMouseAdapter);
            }
        }
    }
    
    /**
     * Add a "open system file browser to path" menu item to the contextual menu
     *
     * @param menuEntry the menu entry string
     * @param path the path to browse to
     * @return the added menu item
     */
     public JMenuItem addMenuItemBrowseFolder(String menuEntry, String path) {
        if (myMouseAdapter == null) {
            return null;
        }
        JMenuItem  mi = new JMenuItem(menuEntry);
        mi.addActionListener((ActionEvent e) -> {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                log.error("Browse to action "+ex.getMessage());
            }
        });
        myMouseAdapter.addMenuItem(mi);
        return mi;
    }
     
    /**
     * Remove a given menu item from the contextual menu
     *
     * @param mi the JMenuItem to remove
     */
     public void removeMenuItemBrowseFolder(JMenuItem mi) {
        if (myMouseAdapter == null) {
            return ;
        }
        myMouseAdapter.removeMenuItem(mi);
    }

    //
    // For contextual menu
    private static class MyMouseAdapter implements MouseListener {

        private JPopupMenu popUpMenu;
        private final JMenuItem removeMenuItem;

        public MyMouseAdapter(ResizableImagePanel resizableImagePanel) {
            popUpMenu = new JPopupMenu();
            removeMenuItem = new JMenuItem("Remove");
            removeMenuItem.addActionListener((ActionEvent e) -> {
                resizableImagePanel.setImagePath(null);
            });
            popUpMenu.add(removeMenuItem);
        }

        public void addMenuItem(JMenuItem item) {
            if (item != null) {
                popUpMenu.add(item);
            }
        }
        
        public void removeMenuItem(JMenuItem item) {
            if (item != null) {
                popUpMenu.remove(item);
            }
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
    public void URIsDropped(java.net.URI[] uris) {
        if (uris == null) {
            log.error("URIsDropped: no URI");
            return;
        }
        if (uris.length == 0) {
            log.error("URIsDropped: no URI");
            return;
        }
        if (uris[0].getPath() == null) {
            log.error("URIsDropped: not a valid URI path: ",uris[0]);
            return;
        }        
        File src = new File(uris[0].getPath());
        File dest = new File(uris[0].getPath());
        if (dropFolder != null) {
            dest = new File(dropFolder + File.separatorChar + src.getName());          
            if (src.getParent().compareTo(dest.getParent()) != 0) {
                // else case would be droping from dropFolder, so no copy
                BufferedInputStream in = null;
                BufferedInputStream out = null;
                FileOutputStream fileOutputStream = null;
                try {
                    // prepare source reader
                    boolean srcIsFile;
                    FileUtil.createDirectory(dest.getParentFile().getPath());                    
                    if (uris[0].getScheme() != null && (uris[0].getScheme().equals("content") || uris[0].getScheme().equals("file"))) {
                        in = new BufferedInputStream(uris[0].toURL().openStream());
                        srcIsFile = true;
                    } else { // let's avoid some 403 by passing a user agent
                        HttpURLConnection httpcon = (HttpURLConnection) uris[0].toURL().openConnection();
                        httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
                        in = new BufferedInputStream(httpcon.getInputStream());
                        srcIsFile = false;
                    }
                    // maybe we don't need to copy?
                    boolean shouldCopy = true;
                    if ( dest.exists() && (srcIsFile) && (dest.length() == src.length()) ) {
                        out = new BufferedInputStream( dest.toURI().toURL().openStream() );
                        byte dataBufferIn[] = new byte[4096];
                        byte dataBufferOut[] = new byte[4096];
                        int bytesReadIn;
                        int bytesReadOut;
                        shouldCopy = false;
                        // file comparison loop
                        while ((bytesReadIn = in.read(dataBufferIn, 0, 4096)) != -1) {
                            bytesReadOut = out.read(dataBufferOut, 0, 4096);
                            if ( (bytesReadIn != bytesReadOut) || ( ! Arrays.equals(dataBufferIn, dataBufferOut) ) ) {
                                shouldCopy = true;
                                break;
                            }
                        }
                        out.close();
                    } 
                    if (shouldCopy) {
                        // avoid overwrite
                        int i = 0;
                        while (dest.exists()) {
                            i++;
                            dest = new File(dropFolder + File.separatorChar + i+"-"+src.getName());
                        }
                        // finally create file and copy data
                        fileOutputStream = new FileOutputStream(dest);                    
                        byte dataBuffer[] = new byte[4096];
                        int bytesRead;
                        // file copy loop
                        while ((bytesRead = in.read(dataBuffer, 0, 4096)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                    }
                } catch (IOException e) {
                    log.error("URIsDropped: error while copying new file, using original file");
                    log.error("URIsDropped: Error : {}", e.getMessage());
                    log.error("URIsDropped: URI : {}", uris[0]);
                    dest = src;
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }                     
                    } catch (IOException ex) {
                        log.error("URIsDropped: error while closing copy destination file : ", ex.getMessage());
                    }
                    try {
                        if (in != null) {
                            in.close(); 
                        }
                    } catch (IOException ex) {
                        log.error("URIsDropped: error while closing copy source file : ", ex.getMessage());
                    }
                    try {                    
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException ex) {
                        log.error("URIsDropped: error while closing duplicate check file : ", ex.getMessage());
                    }                                            
                }
            }        
        }
        setImagePath(dest.getPath());
    }    

    private final static Logger log = LoggerFactory.getLogger(EditableResizableImagePanel.class);
}
