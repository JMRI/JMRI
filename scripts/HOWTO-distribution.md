# How we build and release JMRI distributions
[Online version](https://github.com/JMRI/JMRI/blob/master/scripts/HOWTO-distribution.md)

In SVN, we used "branches" to record the contents of a release. The "Release-4-1-1" branch was used to accumulate the content of that release, and then finally to record that content. In CVS, we'd used tags to record the content of a release, but SVN "tag" and "branch" constructs don't really differ.

Git branches are more ephemeral than SVN branches. They're great for development work, but aren't considered a good way to record content.  Git tags, on the other hand, are great for that.

So our releases procedure is, in outline:

* Create a GitHub Issue to hold discussion with conventional title "Develop release-n.n.n". 

* Do lots of updates and merge completely onto master

* In an up-to-date Git repository, update the release.properties file with the new version number

* Create a branch named "release-n.n.n" (note initial lower case). 

* Push the branch back to the JMRI/JMRI repository (you can't use a pull request until the branch exists), then switch back to master so you don't accidentally change the release branch

* Jenkins build from release-n.n.n branch.  (That’s basically the same Jenkins job, except for changing to checkout from Github)

* As needed, bring in any additional commits requested to branch from master - probably use 'git cherry-pick {commit hash}' to do this. Sometimes also “git merge”, though we’ll have to see exactly how “release.properties” works with this. With SVN, we sometimes created the release a few days in advance to make sure Jenkins was set up, etc, then do final “svn merge” at the appointed time; we might also do this from GitHub. When using 'git merge' would also work we need to be aware of it stamping over 'release.properties'  and the correct procedure to revert that. 

* After Jenkins has processed the final set of changes, directly publish the files in the usual SF.net way, etc.

* We're also looking at using GitHub releases for distributing the files; this is being done in parallel, so the SF.net file method will co-exist at first.

* Turn off the Jenkins job; this is so it doesn't fail after later steps

* Put a tag titled vn.n.n (note initial lower case) on the end of the branch. This starts in a personal fork, then gets pushed back to JMRI/JMRI. 

* Delete the branch (to clean up the list of branches in various GUI tools), which can be done in the main JMRI/JMRI repo via the web interface or in a fork & pushed back


### Issues

- [ ] Are we using [GitHub milestones](https://github.com/JMRI/JMRI/milestones)?  How? Only maintainers can set them on pull requests. The list is being referenced in the release note now. Do we have the release builder add milestones on [closed PRs](https://github.com/JMRI/JMRI/pulls?q=is%3Apr+is%3Aclosed)?

- [ ] How best to deal with bringing additional commits into the branch from master - do we 'cherry-pick' or 'merge'? Everybody is using pull requests, which makes it pretty easy to get the hash numbers needed for cherry-pick (that can be messy with direct commits, because you’re not always sure which commits go together). We can play this by ear, but if somebody says “can you include PR 123?” I think that lends itself to a cherry pick pretty nicely. Best of all - get people to do their work on a branch, which can be separately merged.

- [ ] It might be a good idea to keep the production release branches around throughout the next test phases, but we should be able to prune each test release branch once we've tagged it. So, we'd keep the 4.4 (and 4.4.1) branch around throughout the 4.5.x phase and then prune it when moving to 4.6. Each 4.5.x branch should be pruned once tagged as, if we need to do changes to a test release, we just release a new version on it's own branch from 'master’. How about the case near the end of a development cycle when we’re doing incremental releases? E.g. 4.5.9 might be 4.5.8+deltas?  Should that be a branch from a branch? We'll need to develop and document this as needed.

============================================================================

# Detailed Instructions

The Ant task that handles the Git operations (see below) invokes Git via direct command line execution. This means that you need to have a working command-line git tool installed and configured.  Try "git status" to check.  

People building releases for distribution need permission to directly operate with the JMRI/JMRI GitHub repository.

If you're attempting to perform this on MS Windows, refer to the MS Windows notes section at the bottom of this document.

================================================================================
## Notification

- Create a [GitHub Issue](https://github.com/JMRI/JMRI/issues) to hold discussion with conventional title "Create release-n.n.n". (This might already exist, if it was properly created at the end of the last build cycle)


================================================================================
## Update Content

- Go to the master branch on your local repository. Pull back from the main JMRI/JMRI repository to make sure you're up to date.

- If it's a new year, update copyright dates (done for 2016):
    JMRI:
    * build.xml in the jmri.copyright.years property value
    * xml/XSLT/build.xml in the property value, index.html, CSVindex.html
    website:
    * Copyright.html (3 places)
    * index.html
    * Footer
    * contact/index.html
    * (grep -r for the previous year in the web site, xml; don't change copyright notices!)

- Bring in all possible GitHub JMRI/JMRI [pull requests](https://github.com/JMRI/JMRI/pulls)

- [ ] Do we want to add a step about the use of milestones here?  If we're using them, we should make sure that all closed pull requests have the right one(s) set.

- Bring in all possible [sf.net patches](https://sourceforge.net/p/jmri/patches/), including decoders

- Check if the decoder definitions have changed since the previous release (almost always true) If so, remake the decoder index.

```
        ant remakedecoderindex
```
 
  Check 'session.log' and 'messages.log' located in current directory as, in case of errors they might not always be output to the console.

```
        git diff xml/decoderIndex.xml
        git commit -m"update decoder index" xml/decoderIndex.xml
```

- Update the help/en/Acknowledgements.shtml help page with any recent changes

- Commit any changes in your local web site directory, as these can end up in help, xml, etc (See the JMRI page on local web sites for details)

- Remake the help index (need a command line approach, so can put in ant!)

```
        cd help/en/
        rm ~/.jhelpdev    (to make sure the right preferences are chosen)
        ./JHelpDev.csh   (See the doc page for setup) <-- for Windows, use JHelpDev.bat
        (navigate to JHelpDev.xml in release html/en/ & open it; might take a while)
        (click "Create All", takes a bit of time, wait for button to release)
        (quit)
```

- In that same directory, also remake the index and toc web pages by doing invoking ant (no argument needed).

```
        ant
```

- [ ] We need to consider whether to do this in help/fr, the French translation; there will perhaps be eventually other translations too, so keep that in mind

- Run the program and make sure help works.

```
        git commit -m"JavaHelp indexing update" .
        cd ../..
```

================================================================================
## General Maintenance Items

We roll some general code maintenance items into the release process.  They can be skipped occasionally.

- Check for any files with multiple UTF-8 Byte-Order-Marks.  This shouldn't usually happen but when it does can be a bit tricky to find. Scan from the root of the repository and fix any files found:

```
        grep -rlI --exclude-dir=.git '^\xEF\xBB\xBF\xEF\xBB\xBF' .
```
 
  It might be necessary to use a Hex editor to remove the erroneous extra Byte-Order-Marks - a valid UTF-8 file should only have either one 3-byte BOM (EF BB BF) or no BOM at all.

- Run "ant alltest"; make sure they all pass; fix problems and commit back

- Run "ant decoderpro"; check for no startup errors, right version, help index present and working OK. Fix problems and commit back.

- This is a good place to check that the decoder XSLT transforms work

```
        cd xml/XSLT
        ant
```

- This is a good place to make sure CATS still builds, see the (doc page)[http://jmri.org/help/en/html/doc/Technical/CATS.shtml] - note that CATS has not been updated to compile cleanly with JMRI 4.*
        
- If you fixed anything, commit it back.

================================================================================
## Create the Release Branch

- Commit any remaining changes, push to your local repository, bring back to the main JMRI/JMRI repository with a pull request, wait for the CI test to complete, and merge the pull request.

- Create a [new milestone](https://github.com/JMRI/JMRI/milestones) with the _next_ release number, dated the 2nd Saturday of the month (might be already there, we've been posting them a few in advance)

- Merge all relevant PRs in the [JMRI/website repository](https://github.com/JMRI/website) to ensure release note draft is up to date

- Create the _next_ release note, so that people will document new (overlapping) changes there. (We need to work through automation of version number values below) (If you're creating a production version, its release note is made from a merge of the features of all the test releases; also create the *.*.1 note for the next test release)

```    
        cd (local web copy)/releasenotes
        git pull 
        cp jmri4.5.1.shtml jmri4.5.2.shtml
        (edit the new release note accordingly)
            change numbers throughout
            move new warnings to old
            remove old-version change notes
        git add jmri4.5.2.shtml
        git commit -m"start new 4.5.2 release note" jmri4.5.2.shtml
        PR-and-merge (or direct push) and pull back.
        cd (local JMRI copy)
```

- Pull back to make sure your repository is fully up to date

- Check that the correct milestone is on all merged pulls. This is needed for the release note. Start with the list of PRs merged since the last test release was started:
````
https://github.com/JMRI/JMRI/pulls?utf8=✓&q=is%3Apr%20is%3Aclosed%20merged%3A%3E2016-08-13
````
where the date at the end should be the date (and optionally time) of the last release. For each, if it doesn't have the right milestone set, and is a change to the release code (e.g. isn't just a change to the CI settings or similar), add the current milestone.  

- Start the release by creating a new "release branch" using Ant.  (If you need to make a "branch from a branch", such as nearing the end of the development cycle, this will need to be done manually rather than via ant.)

```
        ant make-test-release-branch
```
 
  This will have done (more or less) the following actions:

```    
        git checkout master
        git pull
        (commit a version number increment to master)
        git push JMRI/JMRI master
        git checkout -b {branch}
        git push JMRI/JMRI {branch}
        git checkout master    
        git pull
```

- Put a comment in the release GitHub item saying the branch exists, and all future changes should be documented in the new release note

```
The release-4.5.6 branch has been created. 

From now on, please document your changes in the [jmri4.5.7.shtml](https://github.com/JMRI/website/blob/master/releasenotes/jmri4.5.7.shtml) release note file.

Maintainers, please set the 4.5.7 milestone on pulls from now on, as that will be the next test release from the HEAD of the master branch.

Jenkins will be creating files shortly at the [CI server](http://jmri.tagadab.com/jenkins/job/TestReleases/job/4.5.6/)
````

================================================================================
## Build Files with Jenkins

- Log in to the [Jenkins CI engine](http://jmri.tagadab.com/jenkins/job/TestReleases/)

- Click "New Item"

- Click "Copy Existing Item", and enter the name of the most recent release. Fill out the new release name at the top. Click "OK"

- Update

        Project Name
        Description
        Git Modules: Branch
    
    and click "Save". If needed, click "Enable".

- The build will start shortly (or click "Build Now"). Wait for it to complete.

====================================================================================
#### Local-build Alternative

If you can't use Jenkins for the actual build, you can create the files locally:

If you're building locally:
* You need to have installed NSIS from http://nsis.sourceforge.net (we use version 2.44)
* Either make sure that 'makensis' is in your path, or set nsis.home in your local.properties file to the root of the nsis installation:

```
        nsis.home=/opt/nsis/nsis-2.46/
```

- Get the release in your local work directory

```
    git checkout release-4.5.2
```

- edit release.properties to say release.official=true (last line)

- Do the build:

```
    ant -Dnsis.home="" clean packages
```
 
    Ant will do the various builds, construct the distribution directories, and finally construct the Linux, Mac OS X and Windows distribution files in dist/releases/

- Put the Linux, Mac OS X and Windows files where developers can take a quick look, send an email to the developer list, and WAIT FOR SOME REPLIES
 
    The main JMRI web site gets completely overwritten by Jenkins, so one approach:

 ```   
        ssh user,jmri@shell.sf.net create
        scp dist/release/JMRI.* user,jmri@shell.sf.net:htdocs/release/
 ```
 
    puts them at

```    
        http://user.users.sf.net/release
```
 
    (The user has to have put the htdocs link in their SF.net account)

================================================================================
## Put Files Out For Checking

- Change the release note to point to the just-built files (in CI or where you put them), commit, wait (or force via ["Build Now"](http://jmri.tagadab.com/jenkins/job/Web%20Site/job/Website%20from%20JMRI%20GitHub%20website%20repository/) update). Confirm visible on web.

- Announce the file set via email to jmri-developers@lists.sf.net with a subject line "First 4.5.6 files available":

First JMRI 4.5.6 files are available in the usual way at:

http://jmri.tagadab.com/jenkins/job/TestReleases/job/4.5.6

Feedback appreciated. I would like to release this later today or tomorrow morning. 

- *Wait for some replies* before proceeding

================================================================================
## Further Changes to Contents

If anybody wants to add a change from here on in, they should

- commit it to a branch of their own, and push as needed to get it to their GitHub fork.

- On the GitHub web site, make _two_ pull requests:  

   - One to master, as usual
   
   - One to the release branch e.g. "release-4.5.2".  The comment on this PR should explain why this should be included instead of waiting for the next release.
   
  Merging the PR to the master makes those changes available on further developments forever; the one on the release, if accepted, includes the change and kicks off new runs of the various CI and build jobs.

  Note: The GitHub automated CI tests do their build after doing a (temporary) merge with the target branch. If the release branch and master have diverged enough that a single set of changes can't be used with both, a more complicated procedure than above might be needed.  In that case, try a PR onto the release branch of the needed change, and then pull the release branch back onto the master branch before fixing conflicts.

(The following is tentative text for this section from a 4/2016 jmri-developers discussion on how to do this for the run-up to 4.4, starting with 4.3.7 - Bob)

As part of building e.g. release 4.3.7, we create a "release-4.3.8-suggested-patches" branch off the final v4.3.7 tag.

- Developer notices issue needing to be resolved post 4.3.7
- Developer makes own development branch from 'release-4.3.8-suggested-patches'
- Developer makes necessary changes, commits and then pushes to own fork.
- Developer then creates PR from own development branch onto 'JMRI/JMRI/release-4.3.8-suggested-patches'
- Developer additionally creates second PR from own development branch onto 'JMRI/JMRI/master' (*) - could also be performed by the Release Pumpkin meaning the
developer need only create a single PR between 'needed-patches' - decision needed
- If decisions is to include this, Release Pumpkin merges first PR into 'JMRI/JMRI/release-4.3.8-suggested-patches'
- 4.3.8 is eventually built (and if need be, rebuilt) from release-4.3.8-suggested-patches
- Maintainer merges second PR into 'JMRI/JMRI/master'

It still gets a bit tricky if there’s a difference (e.g. due to a conflict with another change) that arises in either PR.  We’ll have to manage that a little carefully. One way to handle that is to _not_ merge any conflicts on master (_any_ PRs to master, not just in these dual-hatted PRs) until after the test release is done and merged back.


====================================================================================
## Release Files on SF.net


- Upload the Linux, Mac OS X and Windows files to sourceforge

 - Download from CI, check integrity (make sure compressed files not expanded), then do (replace "user" with your SourceForge.net user name; must have SSH keys for SourceForge.net set up)

 - (If you use a browser to download instead of curl, make sure the .tgz wasn't auto-expanded)

 - (The "./testrelease 4.5.6" local script on shell.sf.net does the following steps, except for the edit, of course)
```
    ssh user,jmri@shell.sf.net create
    ssh user,jmri@shell.sf.net
    curl -o release.zip "http://jmri.tagadab.com/jenkins/job/Test%20Releases/job/4.5.6/ws/dist/release/*zip*/release.zip"
        (use the following instead if building on second Jenkins server)
    curl -o release.zip "http://jmri.tagadab.com/jenkins/job/TestReleases/job/4.5.6/ws/dist/release/*zip*/release.zip"
    rm release/JMRI*
    unzip release.zip
    cd release
    sha256sum JMRI*   (use 'shasum -a 256' on shell.sf.net)
        (add the calculated hashes for each file to the release note; if a prod release, also on on the direct link on index.html)
    scp JMRI.* ${USER}@"frs.sourceforge.net:/home/frs/project/j/jm/jmri/test\ files/"
        (the scp is needed even if on SF.net, so that the FRS system knows you've added something; using cp is NFG)
        (for production release, use ".../production\ files/")
    
    (clean up and logout)
```

- Create and upload the JavaDocs (As of May 2016, the [Jenkins server](http://jmri.tagadab.com/jenkins/job/WebSite/job/generate-website/) was updating these from git weekly, in which case just have that run. Note that if you're doing this locally, it this might take an hour or more to upload on a home connection, and it's OK to defer the uploadjavadoc step): 
```
    ant javadoc-uml uploadjavadoc
```

- Create and upload the XSLT'd decoder pages
```
    (cd xml/XSLT; ant xslt upload)
```

  Note: the very first time doing this on a new machine, it will be required to run the rsync command manually as the ssh fingerprint for the server wil need to be added to the local machine. Without this, it will fail via ant.

- Wait until the downloads have propagated to the mirrors; check by trying to download each file

====================================================================================
## Create GitHub Release

This puts the right tag on the branch, then removes the branch.  If we decide not to use GitHub releases, those two steps need to be included in separate instructions.

Note: Unlike releasing files to SourceForge, once a GitHub Release is created it is *not* possible to change it to refer to different contents. *Once this step is done, you need to move on to the next release number.*

- Disable the Jenkins release-build job; this is so it doesn't fail after later steps

- Close the [current milestone](https://github.com/JMRI/JMRI/milestones) with the current release number

- on GitHub JMRI/JMRI go to the "releases" link, then click "Draft a new release" e.g.
```
    https://github.com/JMRI/JMRI/releases/new
```

- Fill out form:

   - "tag version field" gets vN.N.N (e.g. leading lower-case "v")
   - @ branch: select the release-n.n.n release branch
```
"Release title" field gets "Test/Prod Release N.N.N"
```
   - Description content (really need to automate this!):
```    
[Release notes](http://jmri.org/releasenotes/jmri4.5.6.shtml)

Checksums:

File | SHA256 checksum
---|---
[JMRI.4.5.6-R9bfae82.dmg](https://github.com/JMRI/JMRI/releases/download/v4.5.6/JMRI.4.5.6-R9bfae82.dmg) | e7223f2ba8163f4b607f1d77d8817eeaff6227b0345d16a92c8cba961f837809
[JMRI.4.5.6-R9bfae82.exe](https://github.com/JMRI/JMRI/releases/download/v4.5.6/JMRI.4.5.6-R9bfae82.exe) | 27c8542568624dec65943b7787e80235f3cad73f7e598ea1c883573cf7837263
[JMRI.4.5.6-R9bfae82.tgz](https://github.com/JMRI/JMRI/releases/download/v4.5.6/JMRI.4.5.6-R9bfae82.tgz) | 84cb9ad5411eda97802a86ecaca53e718ec23353b8a57767fa287359bd5d4057

```

- Attach files by dragging them in (you might have to have downloaded them above via e.g. a separate 
```
curl -o release.zip "http://jmri.tagadab.com/jenkins/job/TestReleases/job/4.5.6/lastSuccessfulBuild/artifact/dist/release/*zip*/release.zip"" 
```
and expansion; it's slow to upload from a typical home machine, though, so wish we had a way to cross-load from somewhere fast - if release.zip is still on SF.net, you can do
```
ssh user,jmri@shell.sf.net create
scp user,jmri@shell.sf.net:release.zip .
```
then expand the release.zip file and drag-and-drop the three files onto the web page one at a time.

Note there's a little progress bar that has to go across & "Uploading your release now..." has to complete before you publish; make sure all three files show.

Alternatively, if you have shell access to the Jenkins server, you can upload directly from there, once the initial draft release has been created:

```
github-release upload -s {github_secret} -u JMRI -r JMRI -t v4.5.6 -n "JMRI.4.5.6-Rd144052.dmg" -f /var/lib/jenkins/jobs/TestReleases/jobs/4.5.6/workspace/dist/release/JMRI.4.5.6-Rd144052.dmg 
github-release upload -s {github_secret} -u JMRI -r JMRI -t v4.5.6 -n "JMRI.4.5.6-Rd144052.exe" -f /var/lib/jenkins/jobs/TestReleases/jobs/4.5.6/workspace/dist/release/JMRI.4.5.6-Rd144052.exe 
github-release upload -s {github_secret} -u JMRI -r JMRI -t v4.5.6 -n "JMRI.4.5.6-Rd144052.tgz" -f /var/lib/jenkins/jobs/TestReleases/jobs/4.5.6/workspace/dist/release/JMRI.4.5.6-Rd144052.tgz 
```
    
- Click "Publish Release"
- Wait for completion, which might be a while with big uploads

(It might be possible to automate this in Ant, see http://stackoverflow.com/questions/24585609/upload-build-artifact-to-github-as-release-in-jenkins )
    

### Final Branch Management

It's important that any changes that were made on the branch also get onto master. Normally this happens automatically with the procedure in "Further Changes" above. But we need to check. Start with your Git repository up to date on master and the release branch, and then (*need a cleaner, more robust mechanism for this*; maybe GitX?):

```
git fetch
git checkout master
git pull
git checkout -b temp-master
git merge (release-n.n.n)
```

Note that you're testing the merge of the release branch back onto master.  This should show no changes, with the possible exception of some auto-generated files:
```
xml/decoderIndex.xml
help/en/webindex.shtml
help/en/webtoc.shtml
help/en/Map.jhm
help/en/JavaHelpSearch/*
```
If there are any changes in other files, do both of:

   - Make sure they get moved back to the master branch

   - Figure out what went wrong and fix it in these instructions

Lastly, if this release is one of the special series at the end of a development cycle that leads to a test release, create the next release branch now.  Those test releases are made cumulatively from each other, rather than each from master. We start the process now so that people can open pull requests for it, and discuss whether changes should be included.

(Maybe we should change their nomenclature to get this across?  E.g. instead of 4.5.5, 4.5.6, 4.5.7, 4.6 where the last two look like regular "from master" test releases, call them 4.5.6, 4.5.6.1, 4.5.6.2, 4.6 - this will make the operations clearer)

   - Create the next pre-production branch (*pre-production case only*):

```
git checkout (release-n.n.n)
git pull
git checkout -b (release-n.n.n+1)
git push github
```

- Create the next [GitHub Issue](https://github.com/JMRI/JMRI/issues) to hold discussion with conventional title "Create release-n.n.n+1". Add the next release milestone (created above) to it.

- Confirm that the tag for the current release (release-4.5.6) is in place, then manually delete the current release branch via the [GitHub UI](https://github.com/JMRI/JMRI/branches).

- Go to the GitHub PR and Issues [labels list](https://github.com/JMRI/JMRI/labels) and remove any "afterNextTestRelease" (and "afterNextProductionRelease" if appropriate) labels from done items

====================================================================================
## Associated Documentation

- Format the release note page: change date, comment out "draft release", make sure links work and proper sections are commented/not commented out

- Update the web site front page and downloads page:
```
     index.html download/Sidebar download/index.shtml releaselist
```

- Commit site, push, etc.

- Consider submitting an anti-virus white-list request at:
```
        https://submit.symantec.com/whitelist/isv/
```

If you don't, a bunch of Windows users are likely to whine

- Wait for update on JMRI web server (or [ask Jenkins](http://jmri.tagadab.com/jenkins/job/WebSite/) to speed it along; note there are multiple components that need to run)

====================================================================================
## Announcement and Post-release Steps

- Mail announcement to jmriusers@yahoogroups.com

    Subject is "Test version 4.5.6 of JMRI/DecoderPro is available for download" or "JMRI 4.4 is available for download"

- If a production version, update the SF automatic download icon by selecting default in SF.net FRS (3 times)

- Wait a day for complaints

- If production release, mail announcement to jmri-announce@lists.sourceforge.net

- For production releases, file copyright registration

    https://eco.copyright.gov/eService_enu/   (Firefox only!)

- Decide if worth announcing elsewhere (production release only, generally we don't do this):
```
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
```

- Commit back any changes made to this doc

- Close the [GitHub issue](https://github.com/JMRI/JMRI/issues) with a comment that sums up the release-build experience

- Take a break!


================================================================================

## Notes for those attempting this on MS Windows platform:

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
