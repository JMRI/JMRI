// HardcopyWriter.java

package jmri.util.davidflanagan;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.JWindow;

/**
 * This is from Chapter 12 of the O'Reilly Java book by
 * David Flanagan with the alligator on the front.
 *
 * @author		David Flanagan
 * @version             $Revision: 1.6 $
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

    // save state between invocations of write()
    private boolean last_char_was_return = false;

    // A static variable to hold prefs between print jobs
    protected static Properties printprops = new Properties();

    public HardcopyWriter(Frame frame, String jobname, int fontsize,
                        double leftmargin, double rightmargin,
                        double topmargin, double bottommargin)
                    throws HardcopyWriter.PrintCanceledException {
        //
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

    public void close() {
        synchronized(this.lock) {
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
    protected void newline() {
        charnum = 0;
        linenum++;
        if (linenum >= lines_per_page) {
            page.dispose();
            page = null;
        }
    }

    /** Internal method beings a new page and prints the header */
    protected void newpage() {
        page = job.getGraphics();
        linenum = 0; charnum = 0;
        pagenum++;
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
    * used to print any window
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
    // Have the windoe print itself
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
     * get the current linenumber.
     * <P>This was not in the
     * original class, but was added afterwards by Dennis Miller.
     */
    public int getCurrentLineNumber(){
        return this.linenum;
    }

    public static class PrintCanceledException extends Exception {
        public PrintCanceledException(String msg) { super(msg); }
    }

}
