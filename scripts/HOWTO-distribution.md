This first section is a discussion note on how to build releases using Git instead of SVN.  Please edit as desired.  Eventually, when we build the 1st Git release, we'll follow whatever emerges here - BobJ

In SVN, we used "branches" to record the contents of a release. The "Release-4-1-1" branch was used to accumulate the content of that release, and then finally to record that content. In CVS, we'd used tags to record the content of a release, but SVN "tag" and "branch" constructs don't really differ.

Git branches are more ephemeral than SVN branches, and aren't considered a good way to record content.  Git tags, on the other hand, are great for that.

So the proposed procedure is:

( ) In an up-to-date Git repository, create a branch named "release-n.n.n" (note initial lower case). Commit an update to release.properties with release.is_branched=true

( ) Use a pull request to move that back to JMRI/JMRI with conventional title "Develop release-n.n.n" The pull request is to create a record; pulling a branch pulls whatever the current contents are, so it can be updated as we go along

( ) Create an Issue to hold discussion. Link in the pull request (that way we can find it) Or is this too heavyweight, and we just have to settle on a convention for the pull request title?

( ) Switch to 'master' branch and update release.properties with new version number. Commit back and then switch checkout back to branch.

( ) Jenkins build from that branch.  (That’s basically the same Jenkins job, expect for changing to checkout from Github)

( ) As needed, bring in any additional commits requested to branch from master - probably use 'git cherry-pick {commit hash}' to do this. Sometimes also “git merge”, though we’ll have to see exactly how “release.properties” works with this. With SVN, we sometimes created the release a few days in advance to make sure Jenkins was set up, etc, then do final “svn merge” at the appointed time; we might also do this from GitHub. When using 'git merge' would also work we need to be aware of it stamping over 'release.properties'  and the correct procedure to revert that.

( ) When it’s done, directly publish the files in the usual SF.net way, etc.

( ) Turn off the Jenkins job; this is so it doesn't fail after later steps

( ) If need be, merge back changes from the branch to the master as needed. This would be done in a personal fork repo. Might not necessarily need this step if we do changes in 'master' and 'cherry-pick' into branch.

( ) Put a tag titled Release-n.n.n-tag (note initial upper case) on the end of the branch. This starts in a personal fork, then gets pushed back to JMRI/JMRI. This Tag provides a way to get back if somebody accidentally adds to the branch. 

The -tag on the end of the name isn't strictly necessary. Git tags and branches are separate name spaces; you can have a tag and branch with the same name. But it can be _very_ confusing when accessing them, because the git command line tools make varying assumptions about the context of their arguments. You end up having to specify whether you’re using a tag or branch most of the time (if there are both with the same name), that’s not how people usually work, and mistakes can happen. From an immediate readability perspective, the '-tag' suffix does easily tell us what it is rather than just the capitalisation or lack thereof. (We will need to clean up the history to suit whichever convention we finally decide on)

( ) Delete the branch (to clean up the list of branches in various GUI tools), which can be done in the main JMRI/JMRI repo via the web interface or in a fork & pushed back

It might be a good idea to keep the production release branches around throughout the next test phases, but we should be able to prune each test release branch once we've tagged it. So, we'd keep the 4.0 (and 4.0.1) branch around throughout the 4.1.x phase and then prune it when moving to 4.2. Each 4.1.x branch should be pruned once tagged as, if we need to do changes to a test release, we just release a new version on it's own branch from 'master’.

How about the case near the end of a development cycle when we’re doing incremental releases? E.g. 4.1.9 might be 4.1.8+deltas?  Should that be a branch from a branch? We'll need to develop and document this as needed.

When we are ending the development cycle, we should keep the final 'from master' branch around at least until we've built the Production release.

There could be value on keeping some of these branches around a bit longer - perhaps we keep them for, say, one additional dev cycle? Let's see how the discussion pans out.

How best to deal with bringing additional commits into the branch from master - do we 'cherry-pick' or 'merge'? Everybody is using pull requests, which makes it pretty easy to get the hash numbers needed for cherry-pick (that can be messy with direct commits, because you’re not always sure which commits go together). We can play this by ear, but if somebody says “can you include PR 123?” I think that lends itself to a cherry pick pretty nicely.


============================================================================

# Existing SVN Build Instructions

This is the HOWTO file to make a complete SourceForge downloadable
distribution.

If you're building locally:
    You need to have installed NSIS from http://nsis.sourceforge.net (we use version 2.44)

    Either make sure that 'makensis' is in your path, or set nsis.home in your local.properties
    file to the root of the nsis installation:
        nsis.home=/opt/nsis/nsis-2.46/

To simplify making branches in sourceforge, put your sourceforge userid & password in the local.properties file (this does not get checked in, so your ID remains safe).

    sourceforge.userid=zoo
    sourceforge.password=IMNOTTELLING

If you're attempting to perform this on MS Windows, refer to the MS Windows notes section at the bottom of this document.

================================================================================
First, we merge in as much tentative content as possible to the SVN trunk.

( ) If it's a new year, update copyright dates (done for 2012):

    * build.xml (3) in the jmri.copyright.years property value
    * site/Copyright.html (3 places)
    * xml/XSLT/build.xml in the property value, index.html, CSVindex.html
    * (grep -r for the previous year in the web site, xml; don't change copyright notices!)

( ) Bring in all possible sf.net patches, including decoders

( ) Check if the decoder definitions have changed since the previous release (almost always true) If so, remake the decoder index.

        ant remakedecoderindex

        (Check 'session.log' and 'messages.log' located in current directory as,
        in case of errors they might not always be output to the console)

        svn diff as a check.
        Commit.

( ) Update the help/en/Acknowledgements.shtml help page with any recent changes

( ) Commit any changes in your local web site directory, these can end up in help, xml, etc

( ) Remake the help index  (need a command line approach, so can put in ant!)

    cd help/en/
    rm ~/.jhelpdev    (to make sure the right preferences are chosen)
    ./JHelpDev.csh   (See the doc page for setup) <-- for Windows, use JHelpDev.bat
    (navigate to JHelpDev.xml in release html/en/ & open it; might take a while)
    (click "Create All", takes a bit of time, wait for button to release)
    (quit)

    In that same directory, also remake the index and toc web
    pages by doing invoking ant (no argument needed).

    ant

    (Need to consider whether to do this in help/fr, and eventually others)

    Run the program and make sure help works.

    (for Windows, run updated files through dos2unix to convert line endings)

    Commit any changed files.

================================================================================
This group of items it just general code maintenance that we roll into the
release process.  They can be skipped occasionally.

( ) Check for line ends with the scripts/checkCR.sh script.  Fix those that you find wrong.  (So far, not found a way to do this on Windows...)
    
    (To scan, e.g the xml tree, do:
        find xml -type f -exec ./scripts/checkCR.sh {} \; -print
    )

    Also, do 

    svn propset svn:eol-style native <file-path-name>

    to fix it for good. (if you hit a 'has binary mime type`, svn propdel svn:mime-type <filename> )

( ) Confirm that properties, html, etc files have "CVS" properties set. (We don't do this for Git; remove item) They should, but new ones might not.

    find java/src -name \*.properties -exec svn propset -q svn:keywords "Date Revision Version Id Author" {} \;
    find jython -name \*.py -exec svn propset -q svn:keywords "Date Revision Version Id Author" {} \;
    find xml -name \*.xml -exec svn propset -q svn:keywords "Date Revision Version Id Author" {} \;
    find help -name \*html -exec svn propset -q svn:keywords "Date Revision Version Id Author" {} \;

    You'll get a message for each file, but will only need to commit changes.
    svn commit -m"setting SVN keywords" java/src jython xml help

( ) Check for any files with multiple UTF-8 Byte-Order-Marks.  This shouldn't usually happen but when it does can be a bit tricky to find. Scan from the root of the repository and fix any files found:

        grep -rlI --exclude-dir=.svn $'\xEF\xBB\xBF\xEF\xBB\xBF'

It might be necessary to use a Hex editor to remove the erroneous extra Byte-Order-Marks - a valid UTF-8 file should only have either one 3-byte BOM (EF BB BF) or no BOM at all.

( ) Confirm that all the above changes have been committed back to SVN trunk

( ) Run "ant alltest"; make sure they all pass; fix problems and commit back

( ) Run "ant decoderpro"; check for no startup errors, right version, help index present and working OK. Fix problems and commit back.

( ) This is a good place to check that the decoder XSLT transforms work

        cd xml/XSLT
        ant
        
If you fix anything, commit it back.

( ) This is a good place to make sure CATS still builds

        http://jmri.org/help/en/html/doc/Technical/CATS.shtml
        
If you fix anything, commit it back.


================================================================================
Second, we build the release branch:

( ) Start the release by copying the current HEAD onto a new SVN "release branch" (This step doesn't seem to work under Cygwin, so needs to be done from the Windows command line) (If needing to make a "branch from a branch", such as nearing the end of the development cycle, this will need to be done manually rather than via ant.)

    ant make-test-release-branch

(You need to have provided both userid and password in the local.properties file, or it will silently hang; if you don't want to put them in local.properties, you can use this form:

    ant -Dsourceforge.userid='you' -Dsourceforge.password='password' make-test-release-branch

instead)  
    
(Alternately, change the svnSetting element in build.xml to:
    
        <svnSetting
        id="svn.settings"
        javahl="false"
        svnkit="false"/>
    
to have it use your default command line setup; note that this doesn't include user name and password)
    
(messages like "Missing 'javahl' dependencies on the classpath !" are normal)

This will do (more or less) the following actions:

    svn cp -m"start release" ${SVNREPO}/trunk/jmri ${SVNREPO}/branches/jmri/releases/3.9.5
    <check in an update of the version number of trunk/HEAD>
    <check in an update of the version number of the new release>

( ) If using the CI system, set up CI builds for that branch

    Sign on to Jenkins
    Under Test Releases, select "New Item",
        create the job with the new release name, 
        using Copy Existing Item with the previous test release
    Then fix the version number in a couple of places:
        Description, 
        and then Source Code Management|Repository URL.
    Start the first build manually, and make sure it went OK

( ) Move to the releases/ part of the SVN tree on your local machine
    and update to get the release copy:

    cd ../../branches/jmri/releases
    svn update 3.11.6

If you don't have this, check out the specific section with 
    
    svn co ${SVNREPO}/branches/jmri/releases/3.11.6
    
Also, don't forget to copy your 'local.properties' file from HEAD

================================================================================
If you're doing the build using the CI engine, configure it to build the new release:

( ) Log in to the CI engine at 

    http://builds.jmri.org/jenkins/job/Test%20Releases/

( ) Click "New Item"

( ) Click "Copy Existing Item", and enter the name of the most recent release. Fill out the new release name at the top. Click "OK"

( ) Update

        Project Name
        Description
        Subversion Modules Repository URL
    
and click "Save"

The build will start shortly.

====================================================================================
For local builds, these are the build instructions; CI builds will already be running)

( ) edit release.properties to say release.official=true (last line)

( ) setenv SVN_REVISION 23699

    ant -Dnsis.home="" clean packages

    (You can get the SVN_REVISION from http://sourceforge.net/p/jmri/code/HEAD/tree/branches/jmri/releases ; don't prefix an 'r')

Ant will do the various builds, construct the distribution directories, and finally construct the Linux, Mac OS X and Windows distribution files in dist/releases/


( ) Put the Linux, Mac OS X and Windows files where developers can take a quick look, send an email to the developer list, and WAIT FOR SOME REPLIES
     
The main JMRI web site gets completely overwritten by Jenkins, so one approach:
    
        ssh user,jmri@shell.sf.net create
        scp dist/release/JMRI.* user,jmri@shell.sf.net:htdocs/release/
    
puts them at
    
        http://user.users.sf.net/release
    
(The user has to have put the htdocs link in their SF.net account)

================================================================================

If anybody discovers a problem from here on in, they should fix it at the HEAD and have you merge it into the release branch.  CI will automatically rebuild, or you'll have to redo a manual build manually.

To merge everything to date: 

    svn merge ^/trunk/jmri

If you do this, beware, the first time you'll also merge in the new release.properties file, which you do NOT want to do. You can undo that merge with 'svn revert release.properties'.

To merge a single later revision: 

    svn merge -c <changeset#> ^/trunk/jmri )

================================================================================
Third, we do the release-specific updates.

    (we need to work through automation of version number values)

( ) Create the _next_ release note, so that people will document new (overlapping) changes there.
    
        cd (local web copy)/releasenotes
        svn update
        svn cp jmri3.11.5.shtml jmri3.11.6.shtml
        (edit the new release note accordingly)
        svn commit -m"for release"

( ) Change the release note to point to the just-built files (in CI or where you put them), commit, wait (or force) update. Confirm visible on web.

( ) Announce the file set to jmri-developers with a download URL like:

    http://builds.jmri.org/jenkins/job/Test%20Releases/job/3.11.6/

( ) WAIT FOR SOME REPLIES before going to "Actual release steps" below.

====================================================================================
Actual release steps:


( ) Upload the Linux, Mac OS X and Windows files to sourceforge

Download from CI, check integrity (make sure compressed files not expanded), then do (replace "user" with your SourceForge.net user name; must have SSH keys for SourceForge.net set up)

    (If you use a browser to download instead of curl, make sure the .tgz wasn't auto-expanded)
    (the "./testrelease 3.11.2" local script on shell.sf.net does those steps)

    ssh user,jmri@shell.sf.net create
    ssh user,jmri@shell.sf.net
    curl -o release.zip "http://builds.jmri.org/jenkins/job/Test%20Releases/job/3.11.6/ws/jmri/dist/release/*zip*/release.zip"
    rm release/JMRI*
    unzip release.zip
    cd release
    sha256sum JMRI*   (use 'shasum -a 256' on shell.sf.net)
        (add the calculated hashes for each file to the release note; if a prod release, also on on the direct link on index.html)
    scp JMRI.* ${USER}@"frs.sourceforge.net:/home/frs/project/j/jm/jmri/test\ files/"
        (the scp is needed even if on SF.net, so that the FRS system knows you've added something; using cp is NFG)
        (for production release, use ".../production\ files/")
    
    (clean up and logout)

( ) Create and upload the JavaDocs (as of late 2013, CloudBees was updating these from SVN weekly: 
                
                              https://jmri.ci.cloudbees.com/job/Development/job/Web%20Site/job/Generate%20Website%20Components/

so please skip this step if that's working)

    ant javadoc-uml uploadjavadoc

( ) Create and upload the XSLT'd decoder pages

    (cd xml/XSLT; ant xslt upload)

Note: the very first time doing this on a new machine, it will be required to run the rsync command manually as the ssh fingerprint for the server wil need to be added to the local machine. Without this, it will fail via ant.

( ) Format the release note page: change date, comment out "draft release", make sure links work and proper sections are commented/not commented out

( ) Wait until the downloads have propagated to the mirrors; check by trying to download each file

( ) Commit release note file(s) to the web site SVN directory,

( ) Wait for update on JMRI web server

( ) Complete all the above before continuing

( ) Update the web site front page and downloads page:

     index.html download/Sidebar download/index.shtml releaselist

( ) Commit site

( ) Consider submitting an anti-virus white-list request at:

        https://submit.symantec.com/whitelist/isv/

    If you don't, a bunch of Windows users are likely to whine

( ) For production releases, file copyright registration

    https://eco.copyright.gov/eService_enu/   (Firefox only!)

( ) For production releases, make a new version of the Manual files (procedure to be developed!)

( ) Wait for update on JMRI web server

( ) Mail announcement to jmriusers@yahoogroups.com

Subject is "Test version 3.9.5 of JMRI/DecoderPro is available for download" or "JMRI 3.8 is available for download"

( ) If a production version, update the SF automatic download icon by selecting default in SF.net FRS (3 times)

( ) Wait a day for complaints

( ) If production release, mail announcement to jmri-announce@lists.sourceforge.net

( ) Decide if worth announcing elsewhere (production version or big system-specific fix/feature):

        RailRoadSoftware&yahoogroups.com
        MAC_DCC@yahoogroups.com
        loconet_hackers@yahoogroups.com
        digitrax@yahoogroups.com
        NCE-DCC@yahoogroups.com
        NCE-SYS1@yahoogroups.com
        easydcc@yahoogroups.com
        Model_TRAINS_DCC_Software@yahoogroups.com
        DigitalPlusbyLenz@yahoogroups.com
        linux-dcc@yahoogroups.com
        rrsoftware@yahoogroups.com
        Apple MacOS Software

( ) Commit back any changes made to this doc

( ) Take a break!


================================================================================

Notes for those attempting this on MS Windows platform:

Given that many of the steps involved assume the behaviour of certain POSIX commands (for which there are either no direct equivalent or have subtle behavioural differences), it is easiest to perform these tasks via Cygwin:

    https://cygwin.com/index.html

Cygwin is a set of tools to provide a POSIX-like experience on MS Windows.

(Better still, if possible, setup a Linux Virtual Machine under Windows - this will enable you to follow the 'normal' steps for non-Windows platforms)

Download the setup file for your platform (x86 or x86_64) and then run the installer, mainly using the defaults, but also add the following packages:

    curl
    dos2unix
    openssh
    rsync
    subversion
    tcsh
    unzip

Other useful packages could be:

    nano        <-- a text editor
    ssh-pageant <-- allows use of PuTTY Pageant for sharing ssh key storage
                    between Windows and Cygwin sessions. Without, OpenSSH will
                    be used independently in Cygwin vs. Windows.
                    For set-up, see: https://github.com/cuviper/ssh-pageant

It will also be necessary to set-up/verify various environment variables:

    JAVA_HOME   <-- Location of JDK (i.e. C:\Program Files\Java\jdk1.8.0_25)
    PATH        <-- Needs to contain path towards ant

        PATH example (assuming Ant from NetBeans 8.0.1):
            for ant, add ";C:\Program Files\NetBeans 8.0.1\extide\ant\bin"

Once launched, it will be necessary to navigate to your local Windows drive:

    cd /cygdrive/c

Alternatively, if your work files are stored in 'Documents\JMRIDevelopment', you can create a symlink via:

    ln -s /cygdrive/c/Users/[userid]/Documents/JMRIDevelopment JMRIDevelopment

This will allow you to navigate straight from the Cygwin home to the JMRIDev directory via:

    cd JMRIDevelopment

Also, it will be necessary to work in a Cygwin-specific SVN repository as one checked-out under MS Windows will have CRLF line-endings whereas one checked-out within Cygwin (and using the Cygwin svn tools) will have LF line-endings.

Some of the operations that are performed will still generate files with CRLF line-ends (even within the Cygwin environment) - for these, run the changed files through 'dos2unix'. To get a list of changed files, use 'svn st' at top of repo.
