/*
 * Open the current session log in the OS X Console app.
 */

// define the Java File object that represents the current log
var log = FileUtil.getFile('settings:log/session.log');
// define a Java Desktop object representing the current OS X desktop
var desktop = Java.type('java.awt.Desktop').getDesktop();
// open the log using the default handler provided by the desktop
desktop.open(log)

// this could all have been the following one liner:
//Java.type('java.awt.Desktop').getDesktop().open(FileUtil.getFile('settings:log/session.log'));