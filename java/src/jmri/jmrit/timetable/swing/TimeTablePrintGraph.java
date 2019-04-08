package jmri.jmrit.timetable.swing;

import java.awt.*;
import java.awt.print.*;

/**
 * Print a timetable graph.
 * @author Dave Sand Copyright (c) 2019
 */
public class TimeTablePrintGraph  implements Printable {

    /**
     * Initialize the data used by the printing methods
     * @param segmentId The segment to be displayed.  For multiple segment
     * layouts separate graphs are required.
     * @param scheduleId The schedule to be used for this graph.
     * @param showTrainTimes When true, include the minutes portion of the
     * train times at each station.
     * @param twoPage When true, format the output for two pages.
     */
    public TimeTablePrintGraph(int segmentId, int scheduleId, boolean showTrainTimes, boolean twoPage) {
        _segmentId = segmentId;
        _scheduleId = scheduleId;
        _showTrainTimes = showTrainTimes;
        _twoPage = twoPage;
    }

    final int _segmentId;
    final int _scheduleId;
    final boolean _showTrainTimes;
    final boolean _twoPage;

    /**
     * Setup the printer selection and start the print job.
     */
    void printGraph() {
         PrinterJob job = PrinterJob.getPrinterJob();
         job.setPrintable(this);
         boolean ok = job.printDialog();
         if (ok) {
             try {
                job.print();
             } catch (PrinterException ex) {
                log.warn("A printer exception occurred: ", ex);
             }
         }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        int pageCount = _twoPage ? 2 : 1;
        if (page > pageCount - 1) {     // Page number is zero based
            return NO_SUCH_PAGE;
        }
        pf.setOrientation(PageFormat.LANDSCAPE);

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        Graphics2D g2d;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
        } else {
            throw new IllegalArgumentException();
        }




//         Graphics2D g2d = (Graphics2D) g;



        g2d.translate(pf.getImageableX(), pf.getImageableY());

        TimeTableGraphCommon graph = new TimeTableGraphCommon();
        graph.init(_segmentId, _scheduleId, _showTrainTimes, pf.getImageableHeight(), pf.getImageableWidth() * pageCount, false);
        if (page == 1) {
            // Move the image left to hide the first page
            Double w = pf.getImageableWidth();
            g2d.translate(-w.intValue(), 0);
        }
        graph.doPaint(g2d);

        return PAGE_EXISTS;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTablePrintGraph.class);
}