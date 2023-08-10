package apps.util;

import java.io.File;
import java.util.*;

import jmri.util.problemreport.LogProblemReportProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.Logger; // not org.apache.logging.log4j.Logger

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
        Map<String,Appender> appenderMap = ((Logger) LogManager.getLogger()).getAppenders();
        appenderMap.forEach((key, a) -> {
            if (a instanceof FileAppender) {
                FileAppender f = (FileAppender) a;
                log.debug("find file: {}", f.getFileName());
                list.add(new File(f.getFileName()));
            }
        });
        return list.toArray(new File[list.size()]);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4JProblemReportProvider.class);
}
