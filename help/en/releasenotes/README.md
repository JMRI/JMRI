This directory is where we build up the release note for the next JMRI test release.

Please write a bit about your change(s) in the appropriate section of the current-draft-note.shtml file using simple HTML.  Then commit that, along with your changes, and include in your PR.

If you have a new warning for this release, please add it to the current-draft-warnings.shtml file. 

During development, the current-draft-note.shtml and current-draft-warnings.shtml files are included in the draft release note which can be found [on the web](http://jmri.org/releasenotes/latestNote.php) or as a [source file on GitHub](http://jmri.org/releasenotes/latestGitSrc.php)

During the release process, those partial files are copied to the final release note, and new partial files is created from a note master which is specific to the release sequence, i.e. the jmri4.15-master.shtml file, and a common warnings master i.e. warnings-master.shtml.  (This was defined this way so that we can handle the overlap when 
creating the last release of a sequence and the first release of a new sequence; in practice, it might not be necessary)

