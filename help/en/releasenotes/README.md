This directory is where we build up the release note for the next JMRI test release.

Please write a bit about your change(s) in the appropriate section of the current-draft-note.shtml file using simple HTML.  Then commit that, along with your changes, and include in your PR.

During development, the current-draft-note.shtml is included in the draft release note which can be found [on the web](http://jmri.org/releasenotes/latestNote.php) or as a [source file on GitHub](http://jmri.org/releasenotes/latestGitSrc.php)

During the release process, that partial file is copied to the final release note, and a new partial file is created from a master which is specific to the release sequence, i.e. the jmri4.15-master.shtml file.

