-- Simple script that opens the current JMRI session log in the OS X Console.app.

-- On OS X, this can be invoked within JMRI by assigning it to a button, or by
-- double clicking it within the Finder.
tell application "Finder"
	set JMRI to folder "JMRI" in folder "Preferences" in folder "Library" in home
	open file "session.log" in folder "log" in JMRI
end tell