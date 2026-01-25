package jmri.util.davidflanagan;

import java.awt.*;
import java.awt.JobAttributes.DefaultSelectionType;
import java.awt.JobAttributes.SidesType;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jmri.util.FileUtil;
import jmri.util.JmriJFrame;

/**
 * Provide graphic output to a screen/printer.
 * <p>
 * This is from Chapter 12 of the O'Reilly Java book by David Flanagan with the
 * alligator on the front.
 *
 * @author David Flanagan
 * @author Dennis Miller
 */
public class HardcopyWriter extends Writer {

    // instance variables
    protected PrintJob job;
    protected Graphics page;
    protected String jobname;
    protected String line;
    protected int fontsize;
    protected String time;
    protected Dimension pagesizePixels;
    protected Dimension pagesizePoints;
    protected Font font, headerfont;
    protected String fontName = "Monospaced";
    protected int fontStyle = Font.PLAIN;
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
    protected int charoffset = 0;
    protected int pagenum = 0;
    protected int prFirst = 1;
    protected Color color = Color.black;
    protected boolean printHeader = true;

    protected boolean isPreview;
    protected Image previewImage;
    protected Vector<Image> pageImages = new Vector<>(3, 3);
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

    // save state between invocations of write()
    private boolean last_char_was_return = false;

    // A static variable to hold prefs between print jobs
    // private static Properties printprops = new Properties();
    // Job and Page attributes
    JobAttributes jobAttributes = new JobAttributes();
    PageAttributes pageAttributes = new PageAttributes();

    // constructor modified to add print preview parameter
    public HardcopyWriter(Frame frame, String jobname, int fontsize, double leftmargin, double rightmargin,
            double topmargin, double bottommargin, boolean isPreview) throws HardcopyWriter.PrintCanceledException {
        hardcopyWriter(frame, jobname, fontsize, leftmargin, rightmargin, topmargin, bottommargin, isPreview);
    }

    // constructor modified to add default printer name, page orientation, print header, print duplex, and page size
    public HardcopyWriter(Frame frame, String jobname, int fontsize, double leftmargin, double rightmargin,
            double topmargin, double bottommargin, boolean isPreview, String printerName, boolean isLandscape,
            boolean isPrintHeader, SidesType sidesType, Dimension pagesize)
            throws HardcopyWriter.PrintCanceledException {

        // print header?
        this.printHeader = isPrintHeader;

        // set default print name
        jobAttributes.setPrinter(printerName);

        if (sidesType != null) {
            jobAttributes.setSides(sidesType);
        }
        if (isLandscape) {
            pageAttributes.setOrientationRequested(PageAttributes.OrientationRequestedType.LANDSCAPE);
        }

        hardcopyWriter(frame, jobname, fontsize, leftmargin, rightmargin, topmargin, bottommargin, isPreview);
    }

    private void hardcopyWriter(Frame frame, String jobname, int fontsize, double leftmargin, double rightmargin,
            double topmargin, double bottommargin, boolean isPreview) throws HardcopyWriter.PrintCanceledException {

        this.isPreview = isPreview;
        this.frame = frame;

        // set default to color
        pageAttributes.setColor(PageAttributes.ColorType.COLOR);

        pagesizePixels = getPagesizePixels();
        pagesizePoints = getPagesizePoints();

        // skip printer selection if preview
        if (!isPreview) {
            Toolkit toolkit = frame.getToolkit();

            job = toolkit.getPrintJob(frame, jobname, jobAttributes, pageAttributes);

            if (job == null) {
                throw new PrintCanceledException("User cancelled print request");
            }
            pagesizePixels = job.getPageDimension();
            int printerDpi = job.getPageResolution();
            pagesizePoints =
                    new Dimension((72 * pagesizePixels.width) / printerDpi, (72 * pagesizePixels.height) / printerDpi);
            // determine if user selected a range of pages to print out, note that page becomes null if range
            // selected is less than the total number of pages, that's the reason for the page null checks
            if (jobAttributes.getDefaultSelection().equals(DefaultSelectionType.RANGE)) {
                prFirst = jobAttributes.getPageRanges()[0][0];
            }
        }

        x0 = (int) (leftmargin * 72);
        y0 = (int) (topmargin * 72);
        width = pagesizePoints.width - (int) ((leftmargin + rightmargin) * 72);
        height = pagesizePoints.height - (int) ((topmargin + bottommargin) * 72);

        // get body font and font size
        font = new Font(fontName, fontStyle, fontsize);
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
        headery = y0 - (int) (0.125 * 72) - headermetrics.getHeight() + headermetrics.getAscent();

        // compute date/time for header
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        df.setTimeZone(TimeZone.getDefault());
        time = df.format(new Date());

        this.jobname = jobname;
        this.fontsize = fontsize;

        if (isPreview) {
            previewFrame = new JmriJFrame(Bundle.getMessage("PrintPreviewTitle") + " " + jobname);
            previewFrame.getContentPane().setLayout(new BorderLayout());
            toolBarInit();
            previewToolBar.setFloatable(false);
            previewFrame.getContentPane().add(previewToolBar, BorderLayout.NORTH);
            previewPanel = new JPanel();
            previewPanel.setSize(pagesizePixels.width, pagesizePixels.height);
            // add the panel to the frame and make visible, otherwise creating the image will fail.
            // use a scroll pane to handle print images bigger than the window
            previewFrame.getContentPane().add(new JScrollPane(previewPanel), BorderLayout.CENTER);
            // page width 660 for portrait
            previewFrame.setSize(pagesizePixels.width + 48, pagesizePixels.height + 100);
            previewFrame.setVisible(true);
        }
    }

    /**
     * Create a print preview toolbar.
     */
    protected void toolBarInit() {
        previousButton = new JButton(Bundle.getMessage("ButtonPreviousPage"));
        previewToolBar.add(previousButton);
        previousButton.addActionListener((ActionEvent actionEvent) -> {
            pagenum--;
            displayPage();
        });
        nextButton = new JButton(Bundle.getMessage("ButtonNextPage"));
        previewToolBar.add(nextButton);
        nextButton.addActionListener((ActionEvent actionEvent) -> {
            pagenum++;
            displayPage();
        });
        pageCount = new JLabel(Bundle.getMessage("HeaderPageNum", pagenum, pageImages.size()));
        pageCount.setBorder(new EmptyBorder(0, 10, 0, 10));
        previewToolBar.add(pageCount);
        closeButton = new JButton(Bundle.getMessage("ButtonClose"));
        previewToolBar.add(closeButton);
        closeButton.addActionListener((ActionEvent actionEvent) -> {
            if (page != null) {
                page.dispose();
            }
            previewFrame.dispose();
        });
    }

    /**
     * Display a page image in the preview pane.
     * <p>
     * Not part of the original HardcopyWriter class.
     */
    protected void displayPage() {
        // limit the pages to the actual range
        if (pagenum > pageImages.size()) {
            pagenum = pageImages.size();
        }
        if (pagenum < 1) {
            pagenum = 1;
        }
        // enable/disable the previous/next buttons as appropriate
        previousButton.setEnabled(true);
        nextButton.setEnabled(true);
        if (pagenum == pageImages.size()) {
            nextButton.setEnabled(false);
        }
        if (pagenum == 1) {
            previousButton.setEnabled(false);
        }
        previewImage = pageImages.elementAt(pagenum - 1);
        previewFrame.setVisible(false);
        previewIcon.setImage(previewImage);
        previewLabel.setIcon(previewIcon);
        // put the label in the panel (already has a scroll pane)
        previewPanel.add(previewLabel);
        // set the page count info
        pageCount.setText(Bundle.getMessage("HeaderPageNum", pagenum, pageImages.size()));
        // repaint the frame but don't use pack() as we don't want resizing
        previewFrame.invalidate();
        previewFrame.revalidate();
        previewFrame.setVisible(true);
    }

    /**
     * Function to get the current page size if this is a preview. This is the
     * pagesize in points (logical units)
     * 
     * @return The page size in points
     */
    private Dimension getPagesizePoints() {
        return new Dimension(612, 792); // This 8.5 * 11.0 -- i.e. US Letter
    }

    /**
     * Function to get the current page size if this is a preview. This is the
     * pagesize in pixels (and not points)
     * 
     * @return The page size in pixels
     */
    private Dimension getPagesizePixels() {
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        Dimension pagesizePoints = getPagesizePoints();
        return new Dimension(pagesizePoints.width * dpi / 72, pagesizePoints.height * dpi / 72);
    }

    /**
     * Send text to Writer output.
     *
     * @param buffer block of text characters
     * @param index  position to start printing
     * @param len    length (number of characters) of output
     */
    @Override
    public void write(char[] buffer, int index, int len) {
        synchronized (this.lock) {
            // loop through all characters passed to us
            line = "";
            for (int i = index; i < index + len; i++) {
                // if we haven't begun a new page, do that now
                if (page == null) {
                    newpage();
                }

                // if the character is a line terminator, begin a new line
                // unless its \n after \r
                if (buffer[i] == '\n') {
                    if (!last_char_was_return) {
                        newline();
                    }
                    continue;
                }
                if (buffer[i] == '\r') {
                    newline();
                    last_char_was_return = true;
                    continue;
                } else {
                    last_char_was_return = false;
                }

                if (buffer[i] == '\f') {
                    pageBreak();
                }

                // if some other non-printing char, ignore it
                if (Character.isWhitespace(buffer[i]) && !Character.isSpaceChar(buffer[i]) && (buffer[i] != '\t')) {
                    continue;
                }
                // if no more characters will fit on the line, start new line
                if (charoffset >= width) {
                    newline();
                    // also start a new page if needed
                    if (page == null) {
                        newpage();
                    }
                }

                // now print the page
                // if a space, skip one space
                // if a tab, skip the necessary number
                // otherwise print the character
                // We need to position each character one-at-a-time to
                // match the FontMetrics
                if (buffer[i] == '\t') {
                    int tab = 8 - (charnum % 8);
                    charnum += tab;
                    charoffset = charnum * metrics.charWidth('m');
                    for (int t = 0; t < tab; t++) {
                        line += " ";
                    }
                } else {
                    line += buffer[i];
                    charnum++;
                    charoffset += metrics.charWidth(buffer[i]);
                }
            }
            if (page != null && pagenum >= prFirst) {
                page.drawString(line, x0, y0 + (linenum * lineheight) + lineascent);
            }
        }
    }

    /**
     * Write a given String with the desired color.
     * <p>
     * Reset the text color back to the default after the string is written.
     *
     * @param c the color desired for this String
     * @param s the String
     * @throws java.io.IOException if unable to write to printer
     */
    public void write(Color c, String s) throws IOException {
        charoffset = 0;
        if (page == null) {
            newpage();
        }
        if (page != null) {
            page.setColor(c);
        }
        write(s);
        // note that the above write(s) can cause the page to become null!
        if (page != null) {
            page.setColor(color); // reset color
        }
    }

    @Override
    public void flush() {
    }

    /**
     * Handle close event of pane. Modified to clean up the added preview
     * capability.
     */
    @Override
    public void close() {
        synchronized (this.lock) {
            if (isPreview) {
                // new JMRI code using try / catch declaration can call this close twice
                // writer.close() is no longer needed. Work around next line.
                if (!pageImages.contains(previewImage)) {
                    pageImages.addElement(previewImage);
                }
                // set up first page for display in preview frame
                // to get the image displayed, put it in an icon and the icon in a label
                pagenum = 1;
                displayPage();
            }
            if (page != null) {
                page.dispose();
            }
            if (job != null) {
                job.end();
            }
        }
    }

    /**
     * Free up resources .
     * <p>
     * Added so that a preview can be canceled.
     */
    public void dispose() {
        synchronized (this.lock) {
            if (page != null) {
                page.dispose();
            }
            previewFrame.dispose();
            if (job != null) {
                job.end();
            }
        }
    }

    public void setFontStyle(int style) {
        synchronized (this.lock) {
            // try to set a new font, but restore current one if it fails
            Font current = font;
            try {
                font = new Font(fontName, style, fontsize);
                fontStyle = style;
            } catch (Exception e) {
                font = current;
            }
            // if a page is pending, set the new font, else newpage() will
            if (page != null) {
                page.setFont(font);
            }
        }
    }

    public int getLineHeight() {
        return this.lineheight;
    }

    public int getFontSize() {
        return this.fontsize;
    }

    public int getCharWidth() {
        return this.charwidth;
    }

    public int getLineAscent() {
        return this.lineascent;
    }

    public void setFontName(String name) {
        synchronized (this.lock) {
            // try to set a new font, but restore current one if it fails
            Font current = font;
            try {
                font = new Font(name, fontStyle, fontsize);
                fontName = name;
                metrics = frame.getFontMetrics(font);
                lineheight = metrics.getHeight();
                lineascent = metrics.getAscent();
                charwidth = metrics.charWidth('m');

                // compute lines and columns within margins
                chars_per_line = width / charwidth;
                lines_per_page = height / lineheight;
            } catch (RuntimeException e) {
                font = current;
            }
            // if a page is pending, set the new font, else newpage() will
            if (page != null) {
                page.setFont(font);
            }
        }
    }

    /**
     * sets the default text color
     *
     * @param c the new default text color
     */
    public void setTextColor(Color c) {
        color = c;
    }

    /**
     * End the current page. Subsequent output will be on a new page
     */
    public void pageBreak() {
        synchronized (this.lock) {
            if (isPreview) {
                pageImages.addElement(previewImage);
            }
            if (page != null) {
                page.dispose();
            }
            page = null;
            newpage();
        }
    }

    /**
     * Return a boolean indicating if this is a preview or not
     *
     * @return the value of isPreview
     */

    public boolean getIsPreview() {
        return isPreview;
    }

    /**
     * Return the number of columns of characters that fit on a page.
     *
     * @return the number of characters in a line
     */
    public int getCharactersPerLine() {
        return this.chars_per_line;
    }

    /**
     * Return the number of lines that fit on a page.
     *
     * @return the number of lines in a page
     */
    public int getLinesPerPage() {
        return this.lines_per_page;
    }

    /**
     * Internal method begins a new line method modified by Dennis Miller to add
     * preview capability
     */
    protected void newline() {
        if (page != null && pagenum >= prFirst) {
            page.drawString(line, x0, y0 + (linenum * lineheight) + lineascent);
        }
        line = "";
        charnum = 0;
        charoffset = 0;
        linenum++;
        if (linenum >= lines_per_page) {
            if (isPreview) {
                pageImages.addElement(previewImage);
            }
            if (page != null) {
                page.dispose();
            }
            page = null;
            newpage();
        }
    }

    /**
     * Internal method beings a new page and prints the header method modified
     * by Dennis Miller to add preview capability
     */
    protected void newpage() {
        pagenum++;
        linenum = 0;
        charnum = 0;
        // get a page graphics or image graphics object depending on output destination
        if (page == null) {
            if (!isPreview) {
                if (pagenum >= prFirst) {
                    page = job.getGraphics();
                } else {
                    // The job.getGraphics() method will return null if the number of pages requested is greater than
                    // the number the user selected. Since the code checks for a null page in many places, we need to
                    // create a "dummy" page for the pages the user has decided to skip.
                    JFrame f = new JFrame();
                    f.pack();
                    page = f.createImage(pagesizePixels.width, pagesizePixels.height).getGraphics();
                    Graphics2D g2d = (Graphics2D) page;
                    double scale = Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
                    g2d.scale(scale, scale);
                }
            } else { // Preview
                previewImage = previewPanel.createImage(pagesizePixels.width, pagesizePixels.height);
                page = previewImage.getGraphics();
                Graphics2D g2d = (Graphics2D) page;
                double scale = Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
                g2d.scale(scale, scale);

                // Enable Antialiasing (Smooths the edges)
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Enable Fractional Metrics (Improves character spacing)
                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                // High Quality Rendering
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);

                // Set Interpolation for the Image (The most important for images)
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                // Enable Antialiasing (Smooths the edges)
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                page.setColor(Color.white);
                page.fillRect(0, 0, previewImage.getWidth(previewPanel), previewImage.getHeight(previewPanel));
                page.setColor(color);
            }
        }
        if (printHeader && page != null && pagenum >= prFirst) {
            page.setFont(headerfont);
            page.drawString(jobname, x0, headery);

            String s = "- " + pagenum + " -"; // print page number centered
            int w = headermetrics.stringWidth(s);
            page.drawString(s, x0 + (this.width - w) / 2, headery);
            w = headermetrics.stringWidth(time);
            page.drawString(time, x0 + width - w, headery);

            // draw a line under the header
            int y = headery + headermetrics.getDescent() + 1;
            page.drawLine(x0, y, x0 + width, y);
        }
        // set basic font
        if (page != null) {
            page.setFont(font);
        }
    }

    /**
     * Write a graphic to the printout.
     * <p>
     * This was not in the original class, but was added afterwards by Bob
     * Jacobsen. Modified by D Miller. Modified by P Gladstone. The image well
     * be rendered at 1.5 pixels per point.
     * <p>
     * The image is positioned on the right side of the paper, at the current
     * height.
     *
     * @param c image to write
     * @param i ignored, but maintained for API compatibility
     */
    public void write(Image c, Component i) {
        writeSpecificSize(c, new Dimension((int) (c.getWidth(null) / 1.5), (int) (c.getHeight(null) / 1.5)));
    }

    /**
     * Write the decoder pro icon to the output. The icon is scaled to the size
     * of the small icon divided by 1.5 (this was in the original code). Method
     * added by P Gladstone. This actually uses the high resolution image.
     * <p>
     * The image is positioned on the right side of the paper, at the current
     * height.
     *
     * @param c image to use purely for the size (and then divided by 1.5). Yes,
     *          this is a bit of a hack.
     * @return The actual size in points of the icon that was rendered.
     */
    public Dimension writeDecoderProIcon(Image c) {
        ImageIcon hiresIcon =
                new ImageIcon(getClass().getResource("/resources/decoderpro_large.png"));
        Image icon = hiresIcon.getImage();
        return writeSpecificSize(icon, new Dimension((int) (c.getWidth(null) / 1.5), (int) (c.getHeight(null) / 1.5)));
    }

    /**
     * Write a graphic to the printout at a specific size (in points)
     * <p>
     * This was not in the original class, but was added afterwards by Kevin
     * Dickerson. Heavily modified by P Gladstone.
     * <p>
     * The image is positioned on the right side of the paper, at the current
     * height. The image aspect ratio is maintained.
     *
     * @param c            the image to print
     * @param requiredSize the dimensions to scale the image to. The image will
     *                     fit inside the bounding box.
     * @return the dimensions of the image in points
     */
    public Dimension writeSpecificSize(Image c, Dimension requiredSize) {
        // if we haven't begun a new page, do that now
        if (page == null) {
            newpage();
        }

        float widthScale = (float) requiredSize.width / c.getWidth(null);
        float heightScale = (float) requiredSize.height / c.getHeight(null);
        float scale = Math.min(widthScale, heightScale);

        int x = x0 + width - (Math.round(c.getWidth(null) * scale) + charwidth);
        int y = y0 + (linenum * lineheight) + lineascent;

        Dimension d = new Dimension(Math.round(c.getWidth(null) * scale), Math.round(c.getHeight(null) * scale));

        if (page != null && pagenum >= prFirst) {
            page.drawImage(c, x, y,
                    d.width, d.height,
                    null);
        }
        return d;
    }

    /**
     * A Method to allow a JWindow to print itself at the current line position
     * <p>
     * This was not in the original class, but was added afterwards by Dennis
     * Miller.
     * <p>
     * Intended to allow for a graphic printout of the speed table, but can be
     * used to print any window. The JWindow is passed to the method and prints
     * itself at the current line and aligned at the left margin. The calling
     * method should check for sufficient space left on the page and move it to
     * the top of the next page if there isn't enough space.
     *
     * @param jW the window to print
     */
    public void write(JWindow jW) {
        // if we haven't begun a new page, do that now
        if (page == null) {
            newpage();
        }
        if (page != null && pagenum >= prFirst) {
            int x = x0;
            int y = y0 + (linenum * lineheight);
            // shift origin to current printing position
            page.translate(x, y);
            // Window must be visible to print
            jW.setVisible(true);
            // Have the window print itself
            jW.printAll(page);
            // Make it invisible again
            jW.setVisible(false);
            // Get rid of the window now that it's printed and put the origin back where it was
            jW.dispose();
            page.translate(-x, -y);
        }
    }

    /**
     * Draw a line on the printout.
     * <p>
     * This was not in the original class, but was added afterwards by Dennis
     * Miller.
     * <p>
     * colStart and colEnd represent the horizontal character positions. The
     * lines actually start in the middle of the character position to make it
     * easy to draw vertical lines and space them between printed characters.
     * <p>
     * rowStart and rowEnd represent the vertical character positions.
     * Horizontal lines are drawn underneath the row (line) number. They are
     * offset so they appear evenly spaced, although they don't take into
     * account any space needed for descenders, so they look best with all caps
     * text
     *
     * @param rowStart vertical starting position
     * @param colStart horizontal starting position
     * @param rowEnd   vertical ending position
     * @param colEnd   horizontal ending position
     */
    public void write(int rowStart, int colStart, int rowEnd, int colEnd) {
        // if we haven't begun a new page, do that now
        if (page == null) {
            newpage();
        }
        int xStart = x0 + (colStart - 1) * charwidth + charwidth / 2;
        int xEnd = x0 + (colEnd - 1) * charwidth + charwidth / 2;
        int yStart = y0 + rowStart * lineheight + (lineheight - lineascent) / 2;
        int yEnd = y0 + rowEnd * lineheight + (lineheight - lineascent) / 2;
        if (page != null && pagenum >= prFirst) {
            page.drawLine(xStart, yStart, xEnd, yEnd);
        }
    }

    /**
     * Get the current linenumber.
     * <p>
     * This was not in the original class, but was added afterwards by Dennis
     * Miller.
     *
     * @return the line number within the page
     */
    public int getCurrentLineNumber() {
        return this.linenum;
    }

    /**
     * Print vertical borders on the current line at the left and right sides of
     * the page at character positions 0 and chars_per_line + 1. Border lines
     * are one text line in height
     * <p>
     * This was not in the original class, but was added afterwards by Dennis
     * Miller.
     */
    public void writeBorders() {
        write(this.linenum, 0, this.linenum + 1, 0);
        write(this.linenum, this.chars_per_line + 1, this.linenum + 1, this.chars_per_line + 1);
    }

    /**
     * Increase line spacing by a percentage
     * <p>
     * This method should be invoked immediately after a new HardcopyWriter is
     * created.
     * <p>
     * This method was added to improve appearance when printing tables
     * <p>
     * This was not in the original class, added afterwards by DaveDuchamp.
     *
     * @param percent percentage by which to increase line spacing
     */
    public void increaseLineSpacing(int percent) {
        int delta = (lineheight * percent) / 100;
        lineheight = lineheight + delta;
        lineascent = lineascent + delta;
        lines_per_page = height / lineheight;
    }

    public static class PrintCanceledException extends Exception {

        public PrintCanceledException(String msg) {
            super(msg);
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(HardcopyWriter.class);
}
