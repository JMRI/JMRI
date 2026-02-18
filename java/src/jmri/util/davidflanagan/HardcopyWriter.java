package jmri.util.davidflanagan;

import java.awt.*;
import java.awt.JobAttributes.DefaultSelectionType;
import java.awt.JobAttributes.SidesType;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jmri.util.JmriJFrame;
import jmri.util.PaperUtils;

/**
 * Provide graphic output to a screen/printer.
 * <p>
 * This is from Chapter 12 of the O'Reilly Java book by David Flanagan with the
 * alligator on the front. This has been extensively modified by Philip
 * Gladstone to improve the print preview functionality.
 *
 * @author David Flanagan
 * @author Dennis Miller
 * @author Philip Gladstone
 */
public class HardcopyWriter extends Writer {

    // instance variables
    protected PrintJob job;
    protected Graphics printJobGraphics;
    protected Graphics page;
    protected String jobname;
    protected String line = "";
    protected int useFontSize = 7;
    protected String time;
    protected Dimension pagesizePixels;
    protected Dimension pagesizePoints;
    protected Font font, headerfont;
    protected String useFontName = "Monospaced";
    protected boolean isMonospacedFont = true;
    protected int useFontStyle = Font.PLAIN;
    protected FontMetrics metrics;
    protected FontMetrics headermetrics;
    protected int x0, y0;
    protected int height, width;
    protected int width_including_right_margin;
    protected int headery;
    protected float charwidth;
    protected int lineheight;
    protected int lineascent;
    protected int chars_per_line;
    protected int v_pos = 0; // The offset of the current line from the top margin.
    protected int max_v_pos = 0; // The maximum offset of the current line from the top margin.
    protected int pagenum = 0;
    protected int[][] prPages = {{1, Integer.MAX_VALUE}};
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

    protected Column[] columns = {new Column(0, Integer.MAX_VALUE)};
    protected int columnIndex = 0;

    protected double pixelScale = 1;
    protected Integer screenResolution;

    // save state between invocations of write()
    private boolean last_char_was_return = false;

    // Job and Page attributes
    JobAttributes jobAttributes = new JobAttributes();
    PageAttributes pageAttributes = new PageAttributes();

    // constructor modified to add default printer name, page orientation, print header, print duplex, and page size
    // All length parameters are in points. 
    // various combinations of parameters can be set. The paper size/shape is set by the pagesize
    // parameter unless it is null, in which case the specified printer paper size is used.
    // The page orientation is set by the isLandscape parameter unless it is null, in which 
    // case the specified printer page orientation is used.
    /**
     * Constructor for HardcopyWriter
     * 
     * @param frame         The AWT Frame
     * @param jobname       The name to print in the title of the page
     * @param fontName      The name of the font to use (if null, default is
     *                      used)
     * @param fontStyle     The style of the font to use (if null, default is
     *                      used)
     * @param fontsize      The size of the font to use (if null, default is
     *                      used)
     * @param leftmargin    The left margin in points
     * @param rightmargin   The right margin in points
     * @param topmargin     The top margin in points
     * @param bottommargin  The bottom margin in points
     * @param isPreview     Whether to preview the print job
     * @param printerName   The name of the printer to use (if null, default is
     *                      used)
     * @param isLandscape   Whether to print in landscape mode (if null, default
     *                      is used)
     * @param isPrintHeader Whether to print the header (if null, default is
     *                      used)
     * @param sidesType     The type of duplexing to use (if null, default is
     *                      used)
     * @param pagesize      The size of the page to use (if null, default is
     *                      used)
     * @throws HardcopyWriter.PrintCanceledException If the print job gets
     *                                               cancelled.
     */
    public HardcopyWriter(Frame frame, String jobname, String fontName, Integer fontStyle, Integer fontsize,
            double leftmargin, double rightmargin,
            double topmargin, double bottommargin, boolean isPreview, String printerName, Boolean isLandscape,
            Boolean isPrintHeader, SidesType sidesType, Dimension pagesize)
            throws HardcopyWriter.PrintCanceledException {

        if (isPreview) {
            GraphicsConfiguration gc = frame.getGraphicsConfiguration();
            AffineTransform at = gc.getDefaultTransform();
            pixelScale = at.getScaleX();
        }

        // print header?
        if (isPrintHeader != null) {
            this.printHeader = isPrintHeader;
        }

        // set default print name
        jobAttributes.setPrinter(printerName);

        if (sidesType != null) {
            jobAttributes.setSides(sidesType);
        }
        if (isLandscape != null) {
            if (isLandscape) {
                pageAttributes.setOrientationRequested(PageAttributes.OrientationRequestedType.LANDSCAPE);
            } else {
                pageAttributes.setOrientationRequested(PageAttributes.OrientationRequestedType.PORTRAIT);
            }
        }

        this.isPreview = isPreview;
        this.frame = frame;

        // set default to color. 
        // ISSUE: Ought this to be dependent on printer capabilities?
        pageAttributes.setColor(PageAttributes.ColorType.COLOR);

        // Get the screen resolution and cache it. This also allows us to override
        // the default resolution for testing purposes.
        getScreenResolution();

        if (pagesize == null) {
            pagesizePixels = getPagesizePixels();
            pagesizePoints = getPagesizePoints();
        } else {
            pixelScale = 1;
            pagesizePoints = pagesize;
            // Assume 100 DPI scale factor. This is used for testing only. If !isPreview, then things
            // are set according to the printer's capabilities.
            screenResolution = 100;
            pagesizePixels = new Dimension(pagesizePoints.width * screenResolution / 72,
                    pagesizePoints.height * screenResolution / 72);
        }

        // skip printer selection if preview
        if (!isPreview) {
            Toolkit toolkit = frame.getToolkit();

            PaperUtils.syncPageAttributesToPrinter(pageAttributes);

            jobAttributes.setDialog(JobAttributes.DialogType.NATIVE);

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
                prPages = jobAttributes.getPageRanges();
            }
        } else {
            if (pageAttributes.getOrientationRequested().equals(PageAttributes.OrientationRequestedType.LANDSCAPE)) {
                pagesizePoints = new Dimension(pagesizePoints.height, pagesizePoints.width);
                pagesizePixels = new Dimension(pagesizePixels.height, pagesizePixels.width);
            }
        }

        x0 = (int) leftmargin;
        y0 = (int) topmargin;
        width = pagesizePoints.width - (int) (leftmargin + rightmargin);
        height = pagesizePoints.height - (int) (topmargin + bottommargin);

        // Create a graphics context that we can use to get font metrics
        Graphics g = getGraphics();

        if (fontsize != null) {
            useFontSize = fontsize;
        }

        if (fontName != null) {
            useFontName = fontName;
        }

        if (fontStyle != null) {
            useFontStyle = fontStyle;
        }

        // get body font and font size
        font = new Font(useFontName, useFontStyle, useFontSize);
        metrics = frame.getFontMetrics(font);
        lineheight = metrics.getHeight();
        lineascent = metrics.getAscent();
        Rectangle2D bounds = metrics.getStringBounds("m".repeat(100), g);
        charwidth = (float) (bounds.getWidth() / 100.0);

        // compute lines and columns within margins
        // ISSUE: I really don't want to expose chars_per_line 
        chars_per_line = (int) (width / charwidth);

        // header font info
        headerfont = new Font("SansSerif", Font.ITALIC, useFontSize);
        headermetrics = frame.getFontMetrics(headerfont);
        headery = y0 - (int) (0.125 * 72) - headermetrics.getHeight() + headermetrics.getAscent();

        // compute date/time for header
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        df.setTimeZone(TimeZone.getDefault());
        time = df.format(new Date());

        this.jobname = jobname;

        if (isPreview) {
            previewFrame = new JmriJFrame(Bundle.getMessage("PrintPreviewTitle") + " " + jobname);
            previewFrame.getContentPane().setLayout(new BorderLayout());
            toolBarInit();
            previewToolBar.setFloatable(false);
            previewFrame.getContentPane().add(previewToolBar, BorderLayout.NORTH);
            previewPanel = new JPanel();
            previewPanel.setSize((int) (pagesizePixels.width / pixelScale), (int) (pagesizePixels.height / pixelScale));
            // add the panel to the frame and make visible, otherwise creating the image will fail.
            // use a scroll pane to handle print images bigger than the window
            previewFrame.getContentPane().add(new JScrollPane(previewPanel), BorderLayout.CENTER);

            previewFrame.setSize((int) (pagesizePixels.width / pixelScale) + 48,
                    (int) (pagesizePixels.height / pixelScale) + 100);
            previewFrame.setVisible(true);
        }
    }

    /**
     * Get a graphics context for the current page (or the print job graphics
     * context if available) Make sure that this is setup with the appropriate
     * scale factor for the current page.
     * 
     * @return the graphics context
     */
    private Graphics getGraphics() {
        Graphics g = null;
        if (job != null) {
            if (printJobGraphics == null) {
                printJobGraphics = job.getGraphics();
            }
            g = printJobGraphics;
        } else {
            Image img = new BufferedImage(pagesizePixels.width, pagesizePixels.height, BufferedImage.TYPE_INT_RGB);
            g = img.getGraphics();
        }
        if (g == null) {
            throw new RuntimeException("Could not get graphics context");
        }
        setupGraphics(g);
        return g;
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

        // We want to add the paper size / orientation
        if (pagesizePixels.width > pagesizePixels.height) {
            JLabel orientationLabel = new JLabel(Bundle.getMessage("Landscape"));
            orientationLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
            previewToolBar.add(orientationLabel);
        } else {
            JLabel orientationLabel = new JLabel(Bundle.getMessage("Portrait"));
            orientationLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
            previewToolBar.add(orientationLabel);
        }
        String paperSizeName = PaperUtils.getNameFromPoints(pagesizePoints.width, pagesizePoints.height);
        if (paperSizeName != null) {
            try {
                // This converts the paper size name to the appropriate locale
                // but we don't actually know all the possible paper size names.
                paperSizeName = Bundle.getMessage(paperSizeName);
            } catch (MissingResourceException e) {
                log.debug("Paper size name {} not found", paperSizeName);
            }
            JLabel paperSizeLabel = new JLabel(paperSizeName);
            paperSizeLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
            previewToolBar.add(paperSizeLabel);
        }
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
        // previewIcon.setImage(previewImage);
        previewLabel.setIcon(new RetinaIcon(previewImage, pixelScale));
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
     * This function measures the size of the text in the current font. It
     * returns a Rectangle2D.
     * 
     * @param s The string to be measured (no tabs allowed)
     * @return The Rectangle2D object in points
     */

    public Rectangle2D measure(String s) {
        Graphics g = getGraphics();
        Rectangle2D bounds = metrics.getStringBounds(s, g);
        return bounds;
    }

    /**
     * This function returns the bounding box that includes all of the strings
     * passed (when printed on top of each other)
     * 
     * @param stringList A collection of Strings
     * @return The Rectangle2D object in points
     */

    public Rectangle2D measure(Collection<String> stringList) {
        Rectangle2D bounds = null;
        for (String s : stringList) {
            Rectangle2D b = measure(s);
            if (bounds == null) {
                bounds = b;
            } else {
                bounds.add(b);
            }
        }
        return bounds;
    }

    /**
     * Get the current page size in points (logical units).
     * 
     * @return The printable page area in points
     */
    public Dimension getPrintablePagesizePoints() {
        return new Dimension(width, height);
    }

    /**
     * Function to get the current page size if this is a preview. This is the
     * pagesize in points (logical units). If this is not a preview, it still
     * returns the page size for the display. It makes use of the PaperUtils
     * class to get the default paper size (based on locale and/or printer
     * settings).
     * 
     * @return The page size in points
     */
    private Dimension getPagesizePoints() {
        return PaperUtils.getPaperSizeDimension();
    }

    /**
     * Get the screen resolution in pixels per inch. It caches this so that a
     * single value is used for the entire run of the preview/print.
     * 
     * @return The screen resolution in pixels per inch.
     */
    private int getScreenResolution() {
        if (screenResolution == null) {
            screenResolution = (int) (Toolkit.getDefaultToolkit().getScreenResolution() * pixelScale);
        }
        return screenResolution;
    }

    /**
     * Function to get the current page size if this is a preview. This is the
     * pagesize in pixels (and not points). If this is not a preview, it still
     * returns the page size for the display.
     *
     * @return The page size in pixels
     */
    private Dimension getPagesizePixels() {
        int dpi = getScreenResolution();
        Dimension pagesizePoints = getPagesizePoints();
        return new Dimension(pagesizePoints.width * dpi / 72, pagesizePoints.height * dpi / 72);
    }

    /**
     * Function to set the columns for future text output. Output starts in
     * first first column, and advances to the next column on a tab
     * character. This either causes truncation of the text or wrapping to the
     * next line depending on the column alignment type.
     * <p>
     * If no columns are set, then the default is one column, left aligned with the full 
     * width of the page
     * </p>
     * 
     * @param columns Array of Column objects
     */
    public void setColumns(Column[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].maxWidth) {
                if (i + 1 < columns.length) {
                    columns[i].width = columns[i + 1].position - columns[i].position;
                } else {
                    columns[i].width = width - columns[i].position;
                }
            }
            if (columns[i].getPosition() + columns[i].getWidth() > width) {
                throw new IllegalArgumentException("Column off edge of page");
            }
        }
        if (columns.length == 0) {
            columns = new Column[] { new Column(0, width, Align.LEFT) };
        }
        this.columns = columns;
    }

    /**
     * Function to set Columns based on a {@code Collection<Column>} object
     * <p>
     * If no columns are set, then the default is one column, left aligned with the full 
     * width of the page
     * </p>
     * 
     * @param columns Collection of Column objects
     */
    public void setColumns(Collection<Column> columns) {
        setColumns(columns.toArray(new Column[0]));
    }

    /**
     * Send text to Writer output. Note that the text will be aligned to the
     * current column (by default, this is one column, left aligned with the full 
     * width of the page)
     *
     * @param buffer block of text characters
     * @param index  position to start printing
     * @param len    length (number of characters) of output
     */
    @Override
    public void write(char[] buffer, int index, int len) {
        synchronized (this.lock) {
            // loop through all characters passed to us

            for (int i = index; i < index + len; i++) {
                // if we haven't begun a new page, do that now
                ensureOnPage();

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
                    flush_column();
                    pageBreak();
                }

                if (buffer[i] == '\t') {
                    // Compute where the line should go and output it
                    flush_column();
                    continue;
                }

                // if some other non-printing char, ignore it
                if (Character.isWhitespace(buffer[i]) && !Character.isSpaceChar(buffer[i])) {
                    continue;
                }

                line += buffer[i];
            }
            if (page != null && inPageRange(pagenum) && line.length() > 0) {
                // If we have pending text, flush it now
                flush_column();
            }
        }
    }

    /**
     * Flush the current line to the current column. This may be called recursively if
     * the line is too long for the current column and wrapping is enabled. 
     */
    private void flush_column() {
        Column column = columns[columnIndex];

        Rectangle2D bounds = metrics.getStringBounds(line, page);

        int stringStartPos = column.getStartPos(bounds.getWidth());
        int columnWidth = Math.min(column.getWidth(), width - stringStartPos);

        if (bounds.getWidth() > columnWidth) {
            // This text does not fit. This means that we need to split it and wrap it to the next line.

            // First, we need to find where to split the string. 
            // Do this with a binary search to be efficient. We take spaces into account.
            // We want to get the longest possible string that will fit in the column.
            int splitPos = 0;
            int low = 0;
            int high = line.length();
            while (low < high) {
                int mid = (low + high) / 2;
                if (metrics.getStringBounds(line.substring(0, mid), page).getWidth() < columnWidth) {
                    // We know that the first mid characters will fit in the column.
                    low = mid + 1;
                } else {
                    // We know that the first mid characters will not fit in the column.
                    high = mid;
                }
            }
            // low is the first character that causes the string not to fit
            splitPos = low - 1;

            if (column.isWrap()) {
                // We have to back up to a space to avoid splitting a word.
                while (splitPos > 0 && !Character.isSpaceChar(line.charAt(splitPos))) {
                    splitPos--;
                }
                if (splitPos == 0) {
                    // We couldn't find a space to split on, so we have to split on a non-space.
                    splitPos = low - 1;
                }
            }

            if (splitPos < 1) {
                splitPos = 1; // Even if it won't fit, we have to output something.   
            }
            // Now we can split the string and wrap it to the next line.
            String firstLine = line.substring(0, splitPos);
            stringStartPos = column.getStartPos(metrics.getStringBounds(firstLine, page).getWidth());

            // We can now output the first line.
            page.drawString(firstLine, x0 + stringStartPos, y0 + v_pos + lineascent);

            if (column.isWrap()) {
                // Skip any spaces at the split position
                while (splitPos < line.length() - 1 && Character.isSpaceChar(line.charAt(splitPos))) {
                    splitPos++;
                }
                String secondLine = line.substring(splitPos);
                // We can now output the second line.
                v_pos += lineheight;
                line = secondLine;
                int saveColumnIndex = columnIndex;
                flush_column();
                // This has already advanced the column index, so we back it up
                columnIndex = saveColumnIndex;
                max_v_pos = Math.max(max_v_pos, v_pos);
                v_pos -= lineheight;
            }
        } else {
            page.drawString(line, x0 + stringStartPos, y0 + v_pos + lineascent);
        }

        if (columnIndex < columns.length - 1) {
            columnIndex++;
        }

        line = "";
    }

    /**
     * Write a given String with the desired color.
     * <p>
     * Reset the text color back to the default after the string is written.
     * This method is really only good for changing the color of a complete line
     * (or column).
     *
     * @param c the color desired for this String
     * @param s the String
     * @throws java.io.IOException if unable to write to printer
     */
    public void write(Color c, String s) throws IOException {
        ensureOnPage();
        page.setColor(c);
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
            boolean jobEnded = false;
            if (page != null) {
                if (page == printJobGraphics) {
                    // We haven't actually output anything (e.g. person selects page 10 of a 5 page report)
                    job.end();
                    jobEnded = true;
                }
                page.dispose();
            }
            if (job != null && !jobEnded) {
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

    /**
     * Set the font to be used for the next write operation. This really
     * only is good for the next line (or column) of output. Use with caution.
     * <p>
     * If any of the parameters are null, the current value will be used.
     * 
     * @param name  the name of the font
     * @param style the style of the font
     * @param size  the size of the font
     */
    public void setFont(String name, Integer style, Integer size) {
        synchronized (this.lock) {
            if (style == null) {
                style = useFontStyle;
            }
            if (size == null) {
                size = useFontSize;
            }
            if (name == null) {
                name = useFontName;
            }
            font = new Font(name, style, size);
            useFontName = name;
            useFontStyle = style;
            useFontSize = size;
            // if a page is pending, set the new font, else newpage() will
            if (page != null) {
                page.setFont(font);

                refreshMetrics(page);
            }
        }
    }

    /**
     * Check if the given page number is in the range of pages to be printed.
     * 
     * @param pagenum the page number to check
     * @return true if the page number is in the range, false otherwise
     */
    private boolean inPageRange(int pagenum) {
        for (int[] range : prPages) {
            if (pagenum >= range[0] && pagenum <= range[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Refresh the font metrics after changing things like font, size, etc.
     * 
     * @param g the graphics context
     */
    private void refreshMetrics(Graphics g) {
        metrics = frame.getFontMetrics(font);
        lineheight = metrics.getHeight();
        lineascent = metrics.getAscent();

        if (g == null) {
            g = getGraphics();
        }

        Rectangle2D bounds = metrics.getStringBounds("m".repeat(100), g);
        charwidth = (float) (bounds.getWidth() / 100.0);

        // compute lines and columns within margins
        chars_per_line = (int) (width / charwidth);

        int widthI = metrics.charWidth('i');
        int widthW = metrics.charWidth('W');
        int widthM = metrics.charWidth('M');
        int widthDot = metrics.charWidth('.');

        // If the width of 'i' matches 'W', it's almost certainly monospaced
        isMonospacedFont = (widthI == widthW && widthW == widthM && widthM == widthDot);
    }

    /**
     * Get the height of a line of text. This is the amount that the vertical position
     * will advance for each line.
     * 
     * @return the height of a line of text
     */
    public int getLineHeight() {
        return this.lineheight;
    }

    /**
     * Get the size of the font.
     * 
     * @return the size of the font
     */
    public int getFontSize() {
        return this.useFontSize;
    }

    /**
     * Get the width of a character. This is only valid for monospaced fonts.
     * 
     * @return the width of a character, or null if the font is not monospaced
     */
    public Float getCharWidth() {
        if (!isMonospaced()) {
            return null;
        }
        return this.charwidth;
    }

    /**
     * Get the ascent of the font. This is the distance from the baseline to the top of the font.
     * 
     * @return the ascent of the font
     */
    public int getLineAscent() {
        return this.lineascent;
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
        }
    }

    /**
     * Return the number of columns of characters that fit on a page.
     *
     * @return the number of characters in a line or null if the font is not
     *         monospaced
     */
    public Integer getCharactersPerLine() {
        if (!isMonospaced()) {
            return null;
        }
        return this.chars_per_line;
    }

    /**
     * This ensures that the required amount of vertical space is available. If
     * not, a page break is inserted.
     * 
     * @param points The amount of vertical space to ensure in points.
     */
    public void ensureVerticalSpace(int points) {
        if (v_pos + points + lineheight >= height) {
            pageBreak();
        }
    }

    /**
     * This leaves the required amount of vertical space. If not enough space is
     * available, a page break is inserted.
     * 
     * @param points The amount of vertical space to leave in points.
     */
    public void leaveVerticalSpace(int points) {
        v_pos += points;
        ensureVerticalSpace(0);
    }

    /**
     * Internal method begins a new line method modified by Dennis Miller to add
     * preview capability
     */
    protected void newline() {
        if (page != null && inPageRange(pagenum)) {
            flush_column();
        }
        line = "";
        columnIndex = 0;
        v_pos = Math.max(v_pos, max_v_pos);
        max_v_pos = 0;
        v_pos += lineheight;
        // Note that text is printed *below* the current v_pos, so we need to
        // check if we have enough space for that line.
        if (v_pos + lineheight >= height) {
            pageBreak();
        }
    }

    /**
     * Ensure that we have a page object. The page is null when we are before
     * the first page, or between pages.
     */
    protected void ensureOnPage() {
        if (page == null) {
            newpage();
        }
    }

    /**
     * Internal method beings a new page and prints the header method modified
     * by Dennis Miller to add preview capability
     */
    protected void newpage() {
        pagenum++;
        v_pos = 0;
        // get a page graphics or image graphics object depending on output destination
        if (page == null) {
            if (!isPreview) {
                if (inPageRange(pagenum)) {
                    if (printJobGraphics == null) {
                        page = job.getGraphics();
                    } else {
                        page = printJobGraphics;
                        printJobGraphics = null;
                    }
                } else {
                    // The job.getGraphics() method will return null if the number of pages requested is greater than
                    // the number the user selected. Since the code checks for a null page in many places, we need to
                    // create a "dummy" page for the pages the user has decided to skip.
                    JFrame f = new JFrame();
                    f.pack();
                    page = f.createImage(pagesizePixels.width, pagesizePixels.height).getGraphics();
                    if (page instanceof Graphics2D) {
                        Graphics2D g2d = (Graphics2D) page;
                        double scale = getScreenResolution() / 72.0;
                        g2d.scale(scale, scale);
                    }
                }
            } else { // Preview
                previewImage = previewPanel.createImage(pagesizePixels.width, pagesizePixels.height);
                page = previewImage.getGraphics();

                setupGraphics(page);

                page.setColor(Color.white);
                page.fillRect(0, 0, previewImage.getWidth(previewPanel), previewImage.getHeight(previewPanel));
                page.setColor(color);
            }
        }
        if (printHeader && inPageRange(pagenum)) {
            page.setFont(headerfont);
            page.drawString(jobname, x0, headery);

            FontRenderContext frc = page.getFontMetrics().getFontRenderContext();

            String s = "- " + pagenum + " -"; // print page number centered
            Rectangle2D bounds = headerfont.getStringBounds(s, frc);
            page.drawString(s, (int) (x0 + (this.width - bounds.getWidth()) / 2), headery);

            bounds = headerfont.getStringBounds(time, frc);
            page.drawString(time, (int) (x0 + width - bounds.getWidth()), headery);

            // draw a line under the header
            int y = headery + headermetrics.getDescent() + 1;
            page.drawLine(x0, y, x0 + width, y);
        }
        // set basic font
        page.setFont(font);
        refreshMetrics(page);
    }

    /**
     * Gets all the pages as Images.
     * 
     * @return the current page as a BufferedImage
     */
    public Vector<Image> getPageImages() {
        return pageImages;
    }

    /**
     * Gets the current page num -- this can be used to determine when a page
     * break has happened. The page number may be increased whenever a newline
     * is printed.
     * 
     * @return the current page number
     */
    public int getPageNum() {
        return pagenum + (page == null ? 1 : 0);
    }

    /**
     * Setup the graphics context for preview. We want the subpixel positioning
     * for text. This is not used for the actual printing (partly because the
     * Print graphics context is not necessarily a Graphics2D object).
     * 
     * @param g the graphics context to setup
     */
    private void setupGraphics(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            double scale = getScreenResolution() / 72.0;
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
     * Write the decoder pro icon to the output. Method added by P Gladstone.
     * This actually uses the high resolution image. It also advances the v_pos
     * appropriately (unless no_advance is True)
     * <p>
     * The image is positioned on the right side of the paper, at the current
     * height.
     * 
     * @param no_advance if true, do not advance the v_pos
     * @return The actual size in points of the icon that was rendered.
     */
    public Dimension writeDecoderProIcon(boolean no_advance) {
        ImageIcon hiresIcon =
                new ImageIcon(HardcopyWriter.class.getResource("/resources/decoderpro_large.png"));
        Image icon = hiresIcon.getImage();
        Dimension size = writeSpecificSize(icon, new Dimension(icon.getWidth(null) / 6, icon.getHeight(null) / 6));
        if (!no_advance) {
            // Advance the v_pos by the height of the icon
            v_pos += size.height + lineheight;
        }
        return size;
    }

    /**
     * Write the decoder pro icon to the output. Method added by P Gladstone.
     * This actually uses the high resolution image. It also advances the v_pos
     * appropriately.
     * <p>
     * The image is positioned on the right side of the paper, at the current
     * height.
     *
     * @return The actual size in points of the icon that was rendered.
     */
    public Dimension writeDecoderProIcon() {
        return writeDecoderProIcon(false);
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
     * @param requiredSize the dimensions (in points) to scale the image to. The
     *                     image will fit inside the bounding box.
     * @return the dimensions of the image in points
     */
    public Dimension writeSpecificSize(Image c, Dimension requiredSize) {
        // if we haven't begun a new page, do that now
        ensureOnPage();

        float widthScale = (float) requiredSize.width / c.getWidth(null);
        float heightScale = (float) requiredSize.height / c.getHeight(null);
        float scale = Math.min(widthScale, heightScale);

        Dimension d = new Dimension(Math.round(c.getWidth(null) * scale), Math.round(c.getHeight(null) * scale));

        if (isPreview) {
            float pixelsPerPoint = getScreenResolution() / 72.0f;
            c = ImageUtils.getScaledInstance(c, (int) (requiredSize.width * pixelsPerPoint),
                    (int) (requiredSize.height * pixelsPerPoint));
            d = new Dimension((int) (c.getWidth(null) / pixelsPerPoint), (int) (c.getHeight(null) / pixelsPerPoint));
        }

        int x = x0 + width - d.width;
        int y = y0 + v_pos + lineascent;

        if (inPageRange(pagenum)) {
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
        ensureOnPage();

        if (inPageRange(pagenum)) {
            int x = x0;
            int y = y0 + v_pos;
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
     * hStart and hEnd represent the horizontal point positions. The lines
     * actually start in the middle of the character position (to the left) to
     * make it easy to draw vertical lines and space them between printed
     * characters. This is (unfortunately) bad for getting something on the right margin.
     * <p>
     * vStart and vEnd represent the vertical point positions. Horizontal lines
     * are drawn underneath below the vStart position. They are offset so they
     * appear evenly spaced, although they don't take into account any space
     * needed for descenders, so they look best with all caps text. If vStart is 
     * set to the current vPos, then the line is under the current row of text.
     *
     * @param vStart vertical starting position
     * @param hStart horizontal starting position
     * @param vEnd   vertical ending position
     * @param hEnd   horizontal ending position
     */
    public void writeLine(int vStart, int hStart, int vEnd, int hEnd) {
        // if we haven't begun a new page, do that now
        ensureOnPage();

        int xStart = x0 + hStart - useFontSize / 4;
        int xEnd = x0 + hEnd - useFontSize / 4;
        int yStart = y0 + vStart + (lineheight - lineascent) / 2;
        int yEnd = y0 + vEnd + (lineheight - lineascent) / 2;
        if (inPageRange(pagenum)) {
            page.drawLine(xStart, yStart, xEnd, yEnd);
        }
    }

    /**
     * Get the current vertical position on the page
     * 
     * @return the current vertical position of the base of the current line on
     *         the page (in points)
     */
    public int getCurrentVPos() {
        return v_pos;
    }

    /**
     * Print vertical borders on the current line at the left and right sides of
     * the page at pixel positions 0 and width. Border lines are one text line
     * in height. ISSUE: Where should these lines be drawn?
     * <p>
     * This was not in the original class, but was added afterwards by Dennis
     * Miller.
     */
    public void writeBorders() {
        writeLine(v_pos, 0, v_pos + lineheight, 0);
        writeLine(v_pos, width, v_pos + lineheight, width);
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
    }

    /**
     * Returns true if the current font is monospaced
     * 
     * @return true if the current font is monospaced.
     */
    public boolean isMonospaced() {
        return isMonospacedFont;
    }

    public static class PrintCanceledException extends Exception {

        public PrintCanceledException(String msg) {
            super(msg);
        }
    }

    public static class ColumnException extends Exception {
        public ColumnException(String msg) {
            super(msg);
        }
    }

    /**
     * Enum to represent the alignment of text in a column.
     */
    public enum Align {
        LEFT,
        CENTER,
        RIGHT,
        LEFT_WRAP(LEFT),
        CENTER_WRAP(CENTER),
        RIGHT_WRAP(RIGHT);

        private final Align base;

        // Constructor for base values
        Align() {
            this.base = null;
        }

        // Constructor for wrapped values
        Align(Align base) {
            this.base = base;
        }

        /**
         * Gets the base alignment of the column
         * 
         * @return The base alignment of the column
         */
        public Align getBase() {
            return (base == null) ? this : base;
        }

        /**
         * Gets whether the alignment is a wrap alignment
         * 
         * @return true if the alignment is a wrap alignment
         */
        public boolean isWrap() {
            return base != null;
        }
    }

    /**
     * Class to represent a column in the output. This has a start position, width and alignment.
     * This allows left, center or right alignment, with or without wrapping.
     */
    public static class Column {
        int position;
        int width;
        boolean maxWidth = false;
        Align alignment;

        /**
         * Create a Column with specified position, width and alignment
         * 
         * @param position  The position of the column in points
         * @param width     The width of the column in points
         * @param alignment The alignment of the column
         */
        public Column(int position, int width, Align alignment) {
            this.position = position;
            this.width = width;
            this.alignment = alignment;
        }

        /**
         * Create a Column with specified position and alignment. The width will
         * be calculated up to the next column.
         * 
         * @param position  The position of the column in points
         * @param alignment The alignment of the column
         */
        public Column(int position, Align alignment) {
            this.position = position;
            this.maxWidth = true;
            this.alignment = alignment;
        }

        /**
         * Create a Column with specified position and width with LEFT alignment
         * 
         * @param position The position of the column in points
         * @param width    The width of the column in points
         */
        public Column(int position, int width) {
            this(position, width, Align.LEFT);
        }

        /**
         * Sets the width of the column in points
         * 
         * @param width The new width of the column in points
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * Gets the width of the column in points
         * 
         * @return The width of the column in points
         */
        public int getWidth() {
            return width;
        }

        /**
         * Gets the starting position of text of length strlen (in points)
         * 
         * @param strlen The length of the text in points
         * @return The starting position of the text in points
         */
        public int getStartPos(double strlen) {
            switch (alignment.getBase()) {
                case LEFT:
                    return position;
                case CENTER:
                    return (int) (position + width / 2 - strlen / 2);
                case RIGHT:
                    return (int) (position + width - strlen);
                default:
                    throw new IllegalArgumentException("Unknown alignment: " + alignment);
            }
        }

        /**
         * Gets the starting position of the column in points
         * 
         * @return The starting position of the column in points
         */
        public int getPosition() {
            return position;
        }

        /**
         * Gets the alignment of the column
         * 
         * @return The alignment of the column
         */
        public Align getAlignment() {
            return alignment;
        }

        /**
         * Gets whether the column is a wrap column
         * 
         * @return true if the column is a wrap column
         */
        public boolean isWrap() {
            return alignment.isWrap();
        }

        @Override
        public String toString() {
            return "Column{" + "position=" + position + ", width=" + width + ", alignment=" + alignment + "}";
        }

        /**
         * Stretch the columns to fit the specified width. The columns are
         * assumed to be sorted by position. The input widths are treated as
         * ratios. There is a gap between the columns.
         * 
         * @param columns The columns to stretch
         * @param width   The width to stretch to in points
         * @param gap     The gap between the columns in points
         * @return The stretched columns
         */
        public static ArrayList<Column> stretchColumns(Collection<Column> columns, int width, int gap) {
            ArrayList<Column> newColumns = new ArrayList<>();
            double totalWidth = 0;
            for (Column column : columns) {
                totalWidth += column.getWidth();
            }

            double scale = (width - (columns.size() - 1) * gap) / totalWidth;
            // Two passes -- the first to compute the starting column numbers
            // the second to compute the widths
            int accumulatedGap = 0;
            int accumulatedWidth = 0;
            for (Column column : columns) {
                newColumns.add(new Column((int) Math.round(accumulatedWidth * scale) + accumulatedGap, 0,
                        column.getAlignment()));
                accumulatedWidth += column.getWidth();
                accumulatedGap += gap;
            }

            // Now set the widths
            for (int i = 0; i < newColumns.size(); i++) {
                Column column = newColumns.get(i);
                if (i == newColumns.size() - 1) {
                    column.setWidth(width - column.getPosition());
                } else {
                    column.setWidth(newColumns.get(i + 1).getPosition() - column.getPosition() - gap);
                }
            }

            return newColumns;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HardcopyWriter.class);
}
