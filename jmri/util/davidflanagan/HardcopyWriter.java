// HardcopyWriter.java

package jmri.util.davidflanagan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.JButton;
import jmri.util.JmriJFrame;

/**
 * This is from Chapter 12 of the O'Reilly Java book by
 * David Flanagan with the alligator on the front.
 *
 * @author		David Flanagan
 * @version             $Revision: 1.9 $
 */
public class HardcopyWriter extends Writer {

// instance variables
    protected PrintJob job;
    protected Graphics page;
    protected String jobname;
    protected int fontsize;
    protected String time;
    protected Dimension pagesize;
    protected int pagedpi;
    protected Font font, headerfont;
    protected FontMetrics metrics;
    protected FontMetrics headermetrics;
    protected int x0, y0;
    protected int height, width;
    protected int headery;
    protected int charwidth;
    protected int lineheight;
    protected int lineascent;
    protected int chars_per_line;
    protected int lines_per_page;
    protected int charnum = 0, linenum = 0;
    protected int pagenum = 0;
    
    protected static boolean isPreview;
    protected Graphics previewedPage;
    protected Image previewImage;
    protected Graphics previewImagegr;
    protected Vector pageImages = new Vector(3,3);
    protected JmriJFrame previewFrame;
    protected JPanel previewPanel;
    protected ImageIcon previewIcon = new ImageIcon();
    protected JLabel previewLabel = new JLabel();
    protected JToolBar previewToolBar = new JToolBar();
    protected Frame frame;
    protected JButton nextButton;
    protected JButton previousButton;
    protected JButton closeButton;
    protected JLabel pageCount = new JLabel();
    protected JLabel totalPages = new JLabel();

    // save state between invocations of write()
    private boolean last_char_was_return = false;

    // A static variable to hold prefs between print jobs
    protected static Properties printprops = new Properties();

    // constructor modified to add print preview parameter
    public HardcopyWriter(Frame frame, String jobname, int fontsize,
                        double leftmargin, double rightmargin,
                        double topmargin, double bottommargin, boolean preview)
                    throws HardcopyWriter.PrintCanceledException {
        
    	isPreview = preview;
    	
        Toolkit toolkit = frame.getToolkit();
        synchronized(printprops) {
            job = toolkit.getPrintJob(frame, jobname, printprops);
        }
        if (job==null)
            throw new PrintCanceledException("User cancelled print request");

        pagesize = job.getPageDimension();
        pagedpi = job.getPageResolution();

        // Bug workaround
        if (System.getProperty("os.name").regionMatches(true, 0, "windows", 0, 7)) {
            //
        }

        x0 = (int) (leftmargin * pagedpi);
        y0 = (int) (topmargin * pagedpi);
        width = pagesize.width - (int)((leftmargin + rightmargin)*pagedpi);
        height = pagesize.height - (int)((topmargin + bottommargin)*pagedpi);

        // get body font and font size
        font = new Font("Monospaced", Font.PLAIN, fontsize);
        metrics = frame.getFontMetrics(font);
        lineheight = metrics.getHeight();
        lineascent = metrics.getAscent();
        charwidth = metrics.charWidth('m');

        // compute lines and columns within margins
        chars_per_line = width / charwidth;
        lines_per_page = height / lineheight;

        // header font info
        headerfont = new Font("SansSerif", Font.ITALIC, fontsize);
        headermetrics = frame.getFontMetrics(headerfont);
        headery = y0 - (int)(0.125*pagedpi) -
                    headermetrics.getHeight()+headermetrics.getAscent();

        // compute date/time for header
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                    DateFormat.SHORT);
        df.setTimeZone(TimeZone.getDefault());
        time = df.format(new Date());

        this.jobname = jobname;
        this.fontsize = fontsize;
        
        if (isPreview) {
            previewFrame = new JmriJFrame("Print Preview");
            previewFrame.getContentPane().setLayout(new BorderLayout());
            toolBarInit();
            previewToolBar.setFloatable(false);
            previewFrame.getContentPane().add(previewToolBar,
                    BorderLayout.NORTH);
            previewPanel = new JPanel();
            previewPanel.setSize(pagesize.width,pagesize.height);
            // add the panel to the frame and make visible, otherwise creating the image will fail.
            // use a scroll pane to handle print images bigger than the window
            previewFrame.getContentPane().add(new JScrollPane(previewPanel),
                    BorderLayout.CENTER);
            previewFrame.setSize(425, 425);
            previewFrame.setVisible(true);         
        }

    }
    
    /**
     * Creates a print preview toolbar 
     * added by Dennis Miller
     */
    protected void toolBarInit(){
        previousButton = new JButton("Previous Page");
        previewToolBar.add(previousButton);
        previousButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent) {
                pagenum--;
                displayPage();
            }
        });        
        nextButton = new JButton("Next Page");
        previewToolBar.add(nextButton);
        nextButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent) {
                pagenum++;
                displayPage();
            }
        });
        previewToolBar.add(new JLabel("    Page "));
        previewToolBar.add(pageCount);
        previewToolBar.add(new JLabel(" of "));
        previewToolBar.add(totalPages);
        closeButton = new JButton(" Close ");
        previewToolBar.add(closeButton);
        closeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent) {
                page.dispose();
                previewFrame.dispose();
            }
        });
    }
    
    /**
     * Method to display a page image in the preview pane
     * Not in original class but added later by Dennis Miller
     */
    protected void displayPage() {
        // limit the pages to the actual range
        if (pagenum > pageImages.size())pagenum = pageImages.size();
        if (pagenum < 1) pagenum = 1;
        // enable/disable the previous/next buttons as appropriate
        previousButton.setEnabled(true);
        nextButton.setEnabled(true);
        if (pagenum == pageImages.size())nextButton.setEnabled(false);
        if (pagenum == 1) previousButton.setEnabled(false);
        previewImage = (Image) pageImages.elementAt(pagenum-1);
        previewFrame.setVisible(false);
        previewIcon.setImage(previewImage);
        previewLabel.setIcon(previewIcon);
        // put the label in the panel (already has a scroll pane)
        previewPanel.add(previewLabel);
        // set the page count info 
        pageCount.setText("" + pagenum);
        totalPages.setText("" + pageImages.size() + "     ");
        // repaint the frame but don't use pack() as we don't want resizing
        previewFrame.invalidate();
        previewFrame.validate();
        previewFrame.setVisible(true);
    }
    
    /**
     * write method, implemented by all Write subclasses
     */
    public void write(char[] buffer, int index, int len) {
        synchronized(this.lock) {
            // loop through all characters passed to us
            for (int i = index; i<index+len; i++) {
                // if we haven't begun a new page, do that now
                if (page == null) newpage();

                // if the character is a line terminator, begin a new line
                // unless its \n after \r
                if (buffer[i] == '\n') {
                    if (!last_char_was_return) newline();
                    continue;
                }
                if (buffer[i] == '\r') {
                    newline();
                    last_char_was_return = true;
                    continue;
                }
                else last_char_was_return = false;

                // if some other non-printing char, ignore it
                if (Character.isWhitespace(buffer[i]) &&
                    !Character.isSpaceChar(buffer[i]) &&
                        (buffer[i] != '\t') )
                    continue;
                // if no more characters will fit on the line, start new line
                if (charnum >= chars_per_line) {
                    newline();
                    // also start a new page if needed
                    if (page == null) newpage();
                }

                // now print the page
                // if a space, skip one space
                // if a tab, skip the necessary number
                // otherwise print the character
                // We need to position each character one-at-a-time to
                // match the FontMetrics
                if (Character.isSpaceChar(buffer[i])) charnum++;
                else if (buffer[i] == '\t') charnum += 8 - (charnum % 8);
                else {
                    page.drawChars(buffer, i , 1,
                                    x0 + charnum*charwidth,
                                    y0 + (linenum*lineheight) + lineascent);
                    charnum++;
                }
            }
        }
    }

    public void flush() {}

    // method modified by Dennis Miller to add preview capability
    public void close() {
        synchronized(this.lock) {
            if (isPreview) {
                pageImages.addElement(previewImage);
                // set up first page for display in preview frame
                // to get the image displayed, put it in an icon and the icon in a label
                pagenum = 1;
                displayPage();
            }
            if (page!=null) page.dispose();
            job.end();
        }
    }

    public void setFontStyle(int style) {
        synchronized (this.lock) {
            // try to set a new font, but restore current one if it fails
            Font current = font;
            try { font = new Font("Monospaced", style, fontsize); }
            catch (Exception e) { font = current; }
            // if a page is pending, set the new font, else newpage() will
            if (page != null) page.setFont(font);
        }
    }

    /** End the current page. Subsequent output will be on a new page */
    public void pageBreak() { synchronized(this.lock) { newpage(); } }

    /** Return the number of columns of characters that fit on a page */
    public int getCharactersPerLine() { return this.chars_per_line; }

    /** Return the number of lines that fit on a page */
    public int getLinesPerPage() { return this.lines_per_page; }

    /** Internal method begins a new line */
    //  method modified by Dennis Miller to add preview capability
    protected void newline() {
        charnum = 0;
        linenum++;
        if (linenum >= lines_per_page) {
            if (isPreview) pageImages.addElement(previewImage);
            page.dispose();
            page = null;
        	newpage();
        }
    }

    /** Internal method beings a new page and prints the header */
    //  method modified by Dennis Miller to add preview capability
    protected void newpage() {
        // get a page graphics or image graphics object depending on output destination
        if (page==null && !isPreview){
    		page = job.getGraphics();
    		}
        else if (page==null && isPreview){
            previewImage = previewPanel.createImage(pagesize.width, pagesize.height);
            page = previewImage.getGraphics();
            page.setColor(Color.white);
            page.fillRect(0, 0, previewImage.getWidth(previewPanel),
                    previewImage.getHeight(previewPanel));
            page.setColor(Color.black);
            }
        pagenum++;
        linenum = 0; charnum = 0;
        page.setFont(headerfont);
        page.drawString(jobname, x0, headery);

        String s = "- " + pagenum + " -";  // print page number centered
        int w = headermetrics.stringWidth(s);
        page.drawString(s, x0 + (this.width - w)/2, headery);
        w = headermetrics.stringWidth(time);
        page.drawString(time, x0 + width - w, headery);

        // draw a line under the header
        int y = headery + headermetrics.getDescent() + 1;
        page.drawLine(x0, y, x0+width, y);

        // set basic font
        page.setFont(font);
    }

    /**
     * Write a graphic to the printout.
     * <P>This was not in the
     * original class, but was added afterwards by Bob Jacobsen. Modified by D Miller.
     * <P> The image is positioned on the right side of the paper,
     * at the current height.
     */
    public void write(Image c, Component i) {
        // if we haven't begun a new page, do that now
        if (page == null) newpage();

        //D Miller: Scale the icon slightly smaller to make page layout easier and
        //position one character to left of right margin
        int x = x0+width- ((int) c.getWidth(null)*2/3 + charwidth);
        int y = y0+(linenum*lineheight) + lineascent;

        page.drawImage(c, x, y, (int)c.getWidth(null)*2/3, (int)c.getHeight(null)*2/3, null);
    }

    /** A Method to allow a JWindow to print itself at the current line position
    * <P>This was not in the
    * original class, but was added afterwards by Dennis Miller.
    * <P>Intended to allow for a graphic printout of the speed table, but can be
    * used to print any window.  The JWindow is passed to the method and prints itself at the current
    * line and aligned at the left margin.  It also checks for sufficient
    * space left on the page and moves it to the top of the next page if there
    * isn't enough space.
    */

    public void write(JWindow jW) {
    // if we haven't begun a new page, do that now
    if (page == null) newpage();
    int x = x0;
    int y = y0+(linenum*lineheight);
    //shift origin to current printing position
    page.translate(x,y);
    //Window must be visible to print
    jW.setVisible(true);
    // Have the window print itself
    jW.printAll(page);
    //Make it invisible again
    jW.setVisible(false);
    //Get rid of the window now that it's printed and put the origin back where it was
    jW.dispose();
    page.translate(-x,-y);
    }

    /**
     * Draw a line on the printout.
     * <P>This was not in the
     * original class, but was added afterwards by Dennis Miller.
     * <P>colStart and colEnd represent the horizontal character positions.  The
     * lines actually start in the middle of the character position to make it easy to
     * join lines and space them between printed characters.
     * <P>rowStart and rowEnd represent the vertical character positions.  The
     * lines are drawn underneath the row (line) number.  They are offset so they
     * appear evenly spaced, although they don't take into account any space needed
     * for descenders, so they look best with all caps text
     */
    public void write (int rowStart, int colStart,int rowEnd,int colEnd){
        // if we haven't begun a new page, do that now
        if (page == null) newpage();
        int xStart = x0+(colStart-1)*charwidth + charwidth/2;
        int xEnd = x0 + (colEnd-1)*charwidth + charwidth/2;
        int yStart = y0 + rowStart*lineheight + (lineheight-lineascent)/2;
        int yEnd = y0 + rowEnd*lineheight + (lineheight-lineascent)/2;
        page.drawLine(xStart,yStart,xEnd,yEnd);
    }

    /**
     * Get the current linenumber.
     * <P>This was not in the
     * original class, but was added afterwards by Dennis Miller.
     */
    public int getCurrentLineNumber(){
        return this.linenum;
    }

    /**
     * Print vertical borders on the current line at the left and right sides
     * of the page at character positions 1 and chars_per_line + 1.
     * Border lines are one text line in height
     * <P>This was not in the
     * original class, but was added afterwards by Dennis Miller.
     */
    public void writeBorders() {
      write(this.linenum, 0, this.linenum + 1, 0);
      write(this.linenum, this.chars_per_line + 1, this.linenum + 1, this.chars_per_line + 1);
}


    public static class PrintCanceledException extends Exception {
        public PrintCanceledException(String msg) { super(msg); }
    }

}

