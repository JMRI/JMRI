// just print a confirmation
var Turnout = Java.type("jmri.Turnout")
var logger = Java.type("org.apache.log4j.Logger").getLogger("JavaScriptTest.js")

logger.warn("JavaScriptPart1Test: Turnout.THROWN is "+Turnout.THROWN+" (WARN OK here)")
