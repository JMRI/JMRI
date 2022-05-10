# a quick check of logging from Jython
import org.slf4j

log = org.slf4j.LoggerFactory.getLogger("LoggingTest.py")
log.warn("This WARN is OK, it's emitted from LoggingTest.py on purpose")
