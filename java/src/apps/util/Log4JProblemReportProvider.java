package apps.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import jmri.util.problemreport.LogProblemReportProvider;

import org.apache.log4j.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide Log4J information in the problem report.
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = LogProblemReportProvider.class)
public class Log4JProblemReportProvider implements LogProblemReportProvider {

    @Override
    public File[] getFiles() {
        ArrayList<File> list = new ArrayList<>();
        // search for an appender that stores a file
        for (Enumeration<?> en = Logger.getRootLogger().getAllAppenders(); en.hasMoreElements();) {
            // does this have a file?
            Object a = en.nextElement();
            // see if it's one of the ones we know
            log.debug("check appender {}", a);
            if (a instanceof FileAppender) {
                FileAppender f = (FileAppender) a;
                log.debug("find file: {}", f.getFile());
                list.add(new File(f.getFile()));
            }
        }
        return list.toArray(new File[list.size()]);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4JProblemReportProvider.class);
}
