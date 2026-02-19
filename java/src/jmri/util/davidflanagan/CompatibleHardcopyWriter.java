package jmri.util.davidflanagan;

import java.awt.Frame;
import java.awt.JobAttributes.SidesType;
import java.awt.Dimension;

public class CompatibleHardcopyWriter extends HardcopyWriter {

    // constructor modified to add print preview parameter
    public CompatibleHardcopyWriter(Frame frame, String jobname, int fontsize, double leftmargin, double rightmargin,
            double topmargin, double bottommargin, boolean isPreview) throws HardcopyWriter.PrintCanceledException {
        super(frame, jobname, (String) null, (Integer) null, fontsize,
                leftmargin * 72, rightmargin * 72, topmargin * 72, bottommargin * 72,
                isPreview, (String) null, (Boolean) null, (Boolean) null, (SidesType) null, (Dimension) null);
    }

    // constructor modified to add default printer name, page orientation, print header, print duplex, and page size
    public CompatibleHardcopyWriter(Frame frame, String jobname, int fontsize, double leftmargin, double rightmargin,
            double topmargin, double bottommargin, boolean isPreview, String printerName, boolean isLandscape,
            boolean isPrintHeader, SidesType sidesType, Dimension pagesize)
            throws HardcopyWriter.PrintCanceledException {
        super(frame, jobname, (String) null, (Integer) null, fontsize,
                leftmargin * 72, rightmargin * 72, topmargin * 72, bottommargin * 72,
                isPreview, printerName, isLandscape, isPrintHeader, sidesType, pagesize);
    }

    /**
     * Get the current line number -- this is nasty since it makes the implicit assumption
     * that the line height is constant over the whole page. Further, things like images
     * can throw this off.
     * <p>
     * We may want to adjust the current v_pos to align it with an integer line number (when
     * this is called).
     * 
     * @return the current line number
     */
    public int getCurrentLineNumber() {
        return getCurrentVPos() / getLineHeight();
    }

    /**
     * Get the number of lines per page. Again, this assumes that the line height is constant
     * over the whole page.
     * 
     * @return the number of lines per page
     */
    public int getLinesPerPage() {
        return getPrintablePagesizePoints().height / getLineHeight();
    }

    /**
     * This draws a line from the start position to the end position (in characters/lines).
     * This has the same sort of issues as getCurrentLineNumber(). We may want to tweak this
     * to behave differently.
     *  
     * @param startLine The starting line number
     * @param startColumn The starting column number
     * @param endLine The ending line number
     * @param endColumn The ending column number
     */
    public void write(int startLine, int startColumn, int endLine, int endColumn) {
        int startVPos = startLine * getLineHeight();
        int endVPos = endLine * getLineHeight();
        int startHPos = (int) (startColumn * getCharWidth());
        int endHPos = (int) (endColumn * getCharWidth());

        super.writeLine(startVPos, startHPos, endVPos, endHPos);
    }

    /**
     * Set the font style
        * 
     * @param style The font style
     */
    public void setFontStyle(int style) {
        super.setFont(null, style, null);
    }

    /**
     * Set the font name
     * 
     * @param name The font name
     */
    public void setFontName(String name) {
        super.setFont(name, null, null);
    }

}
