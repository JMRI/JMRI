# How we build and release JMRI distributions
[Online version](https://github.com/JMRI/JMRI/blob/master/scripts/HOWTO-distribution.md)

Our release procedure is, in outline:

* Create a GitHub Issue to hold discussion with conventional title "Develop release-n.n.n". 

* Do lots of updates and merge completely onto master

* In an up-to-date Git repository, update the release.properties file with the new version number

* Create a branch named "release-n.n.n" (note initial lower case). 

* Push the branch back to the JMRI/JMRI repository (you can't use a pull request until the branch exists), then switch back to master so you don't accidentally change the release branch

* Jenkins build from release-n.n.n branch.  

* If needed, fetch and merge again from master to get necessary updates. This means that the test release is always from the HEAD of master. 

* After Jenkins has processed the final set of changes, directly publish the files in the usual SF.net way, etc.

* Turn off the Jenkins job; this is so it doesn't fail after later steps

* We're also using GitHub releases for distributing the files; this is being done in parallel, so the SF.net file method will co-exist for quite a while

* Put a tag titled vn.n.n (note initial lower case) on the end of the branch. This starts in a personal fork, then gets pushed back to JMRI/JMRI. 

* Delete the branch (to clean up the list of branches in various GUI tools), which can be done in the main JMRI/JMRI repo via the web interface


### Issues

- [ ] It might be a good idea to keep the production release branches around throughout the next test phases, but we should be able to prune each test release branch once we've tagged it. So, we'd keep the 4.4 (and 4.4.1) branch around throughout the 4.5.x phase and then prune it when moving to 4.6. Each 4.5.x branch should be pruned once tagged as, if we need to do changes to a test release, we just release a new version on it's own branch from 'master’. How about the case near the end of a development cycle when we’re doing incremental releases? E.g. 4.5.9 might be 4.5.8+deltas?  Should that be a branch from a branch? We'll need to develop and document this as needed.

### Suggested approach (2016) to handling near-production test releases:

As part of building e.g. release 4.13.8, we create a "release-4.13.9-suggested-patches" branch off the final v4.13.8 tag. The sequence is then:

- A developer notices some issue needing to be resolved post 4.13.8
- Developer makes own development branch from 'release-4.13.9-suggested-patches'
- Developer makes necessary changes, commits and then pushes to own fork.
- Developer then creates PR from own development branch onto 'JMRI/JMRI/release-4.13.9-suggested-patches'
- Developer additionally creates second PR from own development branch onto 'JMRI/JMRI/master' (*) - could also be performed by the Release Pumpkin meaning the
developer need only create a single PR between 'needed-patches' (communityu decision needed here)
- If decisions is to include this, Release Pumpkin merges first PR into 'JMRI/JMRI/release-4.13.9-suggested-patches'
- 4.13.9 is eventually built (and if need be, rebuilt) from release-4.13.9-suggested-patches
- Maintainer merges second PR into 'JMRI/JMRI/master' whenever.

This has the nice property that if multiple things arise, they can definitely be handled separately. It still gets a bit tricky if there's a difference (e.g. due to a conflict with another change) that arises in either PR.  We'll have to manage that a little carefully. One way to handle that is to _not_ merge any conflicts on master (_any_ PRs to master, not just in these dual-hatted PRs) until after the test release is done and merged back.

The problem is that there are production people who aren't used to working off the master branch.

### Note on file names

Our filenames are generated with [semantic versioning](http://semver.org) format in which the `+R202c9ee` indicates build meta-data, specifically the hash for the tag point.  Originally, the GitHub binary-file release system changed the '+' to a '.', so our scripts below did that too.  That was fixed at the start of 2018, so we no longer do it, but there are some old releases with that filename format.

============================================================================

# Detailed Instructions

The Ant task that handles the Git operations (see below) invokes Git via direct command line execution. This means that you need to have a working command-line git tool installed and configured.  Try "git status" to check.  

People building releases for distribution need permission to directly operate with the JMRI/JMRI GitHub repository.

If you're attempting to perform this on MS Windows, refer to the MS Windows notes section at the bottom of this document.

================================================================================
## Update Instructions

- Update this note by executing the following line in your JMRI repository directory while you _don't_ have this file open in an editor. There are more details in the update-HOWTO.sh comments; arguments when you run it should be last release, this release you're making, the next release; you may need to update what's below:
```
  ./scripts/update-HOWTO.sh 4.17.6 4.17.7 4.17.7
```
then manually update the end of that line above in this document to be this version being made today, next version to be made later, one after that; i.e. when starting to do *.4, the arguments _after__ you edit it here are *.4 *.5 *.6

- To check the script ran OK, the following should be the release you're doing now: 4.17.6

================================================================================
## Notification

- Create a [GitHub Issue](https://github.com/JMRI/JMRI/issues) to hold discussion with conventional title "Create release-4.17.6". (This might already exist, if it was properly created at the end of the last build cycle)

================================================================================
## Update Content

- Go to the master branch on your local repository. Pull back from the main JMRI/JMRI repository to make sure you're up to date. 

 - Make sure `git status` shows "up to date", not "ahead".

- If it's a new year, update copyright dates (done for 2018):
    JMRI:
    * build.xml in the jmri.copyright.years property value
    * xml/XSLT/build.xml
    * A flock of places in xml/XSLT/
    * scripts//WinInstallFiles/LaunchJMRI.nsi
    website:
    * Copyright.shtml (3 places)
    * Footer Footer.shtml
    * (grep -r for the previous year in the web site, xml; don't change copyright notices!)

- Bring in all possible GitHub JMRI/JMRI [pull requests](https://github.com/JMRI/JMRI/pulls)

- We no longer check [sf.net patches](https://sourceforge.net/p/jmri/patches/)

- Check if the decoder definitions have changed since the previous release (almost always true) If so, remake the decoder index.

```
        ant clean remakedecoderindex
```
 
  Check 'session.log' and 'messages.log' located in current directory as, in case of errors they might not always be output to the console.

```
        git diff xml/decoderIndex.xml
        git commit -m"update decoder index" xml/decoderIndex.xml
```

- Update the help/en/Acknowledgements.shtml help page with any recent changes

- Commit any changes in your local web site directory, as these can end up in help, xml, etc (See the JMRI page on local web sites for details)

- Remake the help map, search, index and TOC by doing
```
        cd help/en/
        ant
        cd ../..

```

Be patient, it might take a couple minutes. That will pop some frames, etc, but should be entirely automatic. 

- [ ] We need to consider whether to do the above in help/fr, the French translation; there will perhaps be eventually other translations too, so keep that in mind. (As of 10/2017, the help/fr files crash JHelpDev for an unknown reason)

- Run PanelPro and make sure help works from the Help menu.

- Commit this back:
```
        git commit -m"JavaHelp indexing update" help/en/
```

================================================================================
## General Maintenance Items

We roll some general code maintenance items into the release process.

- Check for any files with multiple UTF-8 Byte-Order-Marks.  This shouldn't usually happen but when it does can be a bit tricky to find. The following command scans from the root of the repository. Use a Hex editor to remove the erroneous extra Byte-Order-Marks found - a valid UTF-8 file should only have either one 3-byte BOM (EF BB BF) or no BOM at all.

```
        grep -rlI --exclude-dir=.git '^\xEF\xBB\xBF\xEF\xBB\xBF' .
```

- Check for any scripts with tabs. If you find them, use an editor with a "DeTab" tool that can replace them with alignment on 4-character tab columns; do not just replace them with four spaces.

```
        grep -lr '\t' jython/ | grep '\.py'
```

- Check for Nullable annotations, which should be @CheckForNull instead (OK to have two and four in FindBugsCheck respectively, others should be removed)
```
        grep -r javax.annotation.Nullable java/src java/test
        grep -r @Nullable java/src java/test
```

- Check for code that's using native Java Timers; see jmri.util.TimerUtil for background (requires code has been built; should only mention jmri/util/TimerUtil.class):
```
        grep -rl 'java.util.Timer\x01' target/
```

- Run "ant alltest"; make sure they all pass; fix problems and commit back (might also take the jvisualvm data below)

- Run "ant decoderpro"; check for no startup errors, right version, help index present and working OK. Fix problems and commit back.

- This is a good place to check that the decoder XSLT transforms work

```
        cd xml/XSLT
        ant
```

- This is a good place to make sure CATS still builds, see the [doc page](https://www.jmri.org/help/en/html/doc/Technical/CATS.shtml) - note that CATS has not been updated to compile cleanly with JMRI 4.*
        
- If you fixed anything, commit it back. 

- Commit the current copy of these notes, the push directly back to master on GitHub.

```
git commit -m"for 4.17.6" scripts/HOWTO-distribution.md
git push github
```

================================================================================
## Create the Next Milestone and Release Note


- Create a [new milestone](https://github.com/JMRI/JMRI/milestones) with the _next_ release number, dated the 2nd Saturday of the month (might be already there, we've been posting them a few in advance)

- Merge all relevant [PRs in the JMRI/website repository](https://github.com/JMRI/website/pulls) to ensure release note draft is up to date
     
- Create the _next_ release note, so that people will document new (overlapping) changes there. Best way to do this is to copy the current release note now, before you prune out all the headers and other info where changes weren't made. (We need to work through automation of version number values below) (If you're creating a production version, its release note is made from a merge of the features of all the test releases; also create the *.*.1 note for the next test release)

```    
        cd (local web copy)/releasenotes
        git checkout master
        git pull 
        cp jmri4.17.6.shtml jmri4.17.7.shtml
        (edit the new release note accordingly)
            change numbers throughout
            move new warnings to old
            remove old-version change notes
        git add jmri4.17.7.shtml
        git commit -m"start new 4.17.7 release note" jmri4.17.7.shtml
        git push github
        cd ../../(local JMRI copy)
```

- Check if any section headings were added to the release-note fragment

    diff help/en/releasenotes/current-draft-note.shtml help/en/releasenotes/jmri4.17-master.shtml
    
    If there were, update the master

- Merge the release note body from help/en/releasenotes/current-draft-note.shtml in the JMRI/JMRI repository into the actual release note in website repository:
     bbedit help/en/releasenotes/current-draft-note.shtml ../website/releasenotes/jmri4.17.6.shtml
     
- Merge the new warnings (if any) from help/en/releasenotes/current-warnings.shtml in the JMRI/JMRI repository into the actual release note in website repository:
     bbedit help/en/releasenotes/current-draft-warnings.shtml ../website/releasenotes/jmri4.17.6.shtml
     
 - add any new warnings to the old warnings section of the next (4.17.7) release note:
    bbedit ../website/releasenotes/jmri4.17.6.shtml ../website/releasenotes/jmri4.17.7.shtml
       
- Clean out the unneeded sections from the release note

- Create the new draft note section

    cp help/en/releasenotes/jmri4.17-master.shtml help/en/releasenotes/current-draft-note.shtml
    cp help/en/releasenotes/warnings-master.shtml help/en/releasenotes/current-draft-warnings.shtml
    git commit -m"start for 4.17.7 release note" help/en/releasenotes/current-draft-*.shtml

- Commit release note, push and pull back

    cd ../website/releasenotes
    git commit -m"updated 4.17.7 release note" jmri4.17.6.shtml jmri4.17.7.shtml
    git push github
    git pull
    cd ../../JMRI

- Check that the correct milestone is on all merged pulls. This is needed for the release note. Start with the list of PRs merged since the last test release was started:
```
https://github.com/JMRI/JMRI/pulls?utf8=✓&q=is%3Apr+is%3Amerged+no%3Amilestone++merged%3A%3E2019-10-31+
```
where the date at the end should be the date (and optionally time) of the last release. For each, if it doesn't have the right milestone set, and is a change to the release code (e.g. isn't just a change to the CI settings or similar), add the current milestone.  

================================================================================
## Create the Release Branch

- one more check that everything is committed (you should _not_ have any modified and added (e.g. green) files showing in `git status`, which might interfere)

```
        git checkout master
        git status
        (commit as needed)
        git push github
        git pull
```

- Start the release by creating a new "release branch" using Ant.  (If you need to make a "branch from a branch", such as nearing the end of the development cycle, this will need to be done manually rather than via ant.)  (There's a summary of the steps involved in this at the bottom)

```
        ant make-test-release-branch
```


- (MANUAL STEP FOR NOW)  Update the <version> element in pom.xml to say the next release:
```
    <version>4.17.7-SNAPSHOT</version>
```
Commit, and push back directly to master (this should be the only change, and has to be before the next step)
```
git commit -m"for next release 4.17.7" pom.xml
git push github
```

- Close the [current milestone](https://github.com/JMRI/JMRI/milestones) with the current release number. If there are any items open still (except the main "create release" one) either close them or change/remove the milestone.  We do this now so that maintainers will put the next milestone on future PRs

- Put the following comment in the release GitHub item saying the branch exists, and all future changes should be documented in the new release note: (NOT FOR THE LAST TEST RELEASE FROM MASTER BEFORE A PRODUCTION RELEASE, see just below)

```
The release-4.17.6 branch has been created. 

Maintainers, please set the 4.17.7 milestone on pulls from now on, as that will be the next test release from the HEAD of the master branch.

Jenkins will be creating files shortly at the [CI server](http://builds.jmri.org/jenkins/job/TestReleases/job/4.17.6/)
```

FOR THE LAST TEST RELEASE FROM MASTER BEFORE A PRODUCTION RELEASE:

```
The release-4.17.6 branch has been created. 

Maintainers, please set the 4.17.7 milestone on pulls from now on, as that will be the next test release from the HEAD of the master branch.

Jenkins will be creating files shortly at the [CI server](http://builds.jmri.org/jenkins/job/TestReleases/job/4.17.7/)

If you're developing any additional (post-4.17.7) changes that you want in the JMRI 4.16 production release, please start from this branch, i.e. do `git checkout -b release-4.17.6` to start your work.
```

- Pull back to make sure your repository is fully up to date

================================================================================
## Build Files with Jenkins

(If you can't build with Jenkins, see the "Local Build Alternative" section near the bottom)

- Log in to the [Jenkins CI engine](http://builds.jmri.org/jenkins/job/TestReleases/) "Releases" section

- Click "New Item"

- Click "Copy Existing Item", and enter the name of the most recent release. Fill out the new release name at the top. Click "OK"

- Update

        Project Name
        Description
        Source Code Management:
           Branch Specified:  4.17.6
    
- Check under Source Code Management, Additional Behaviours, Advanced Clone Behaviours "Shallow Clone" is checked, Shallow Clone Depth is 1, and time out is 20.

- Click "Save". If needed, click "Enable".

- The build will start shortly (or click "Build Now"). Wait for it to complete.

================================================================================
## Capture A Profile

- On your local machine, open jvisualvm. Do 

    unsetenv JMRI_OPTIONS
    ant alltest
    
and attach jvisualvm to the AllTest class when it appears. When that's done, put a screen-shot of the four monitor graphs into the "Create Test Release 4.17.6" Github issue so that historical resource usage info is available.

================================================================================
## Put Files Out For Checking

- Change the release note to point to the just-built files (in CI or where you put them), commit, wait (or force via ["Build Now"](http://builds.jmri.org/jenkins/job/Web%20Site/job/Website%20from%20JMRI%20GitHub%20website%20repository/) update). Confirm visible on web.

- Announce the file set via email to jmri@jmri-developers.groups.io with a subject line 

```
"First 4.17.6 files available":

First JMRI 4.17.6 files are available in the usual way at:

http://builds.jmri.org/jenkins/job/TestReleases/job/4.17.6

Feedback appreciated. I would like to release this later today or tomorrow morning if the files are OK.
```

- *Wait for some replies* before proceeding

================================================================================
## Further Changes to Contents Before Release

If anybody wants to add a change from here on in, they should

- Ideally, start the work on either the release-4.17.6 branch (if working after that was started) or on a branch-from-master that's _before_ the release-4.17.6 branch was created.  That way, the change can be cleanly included in the release branch, and also directly onto master.

- Commit their changes to that branch, and push as needed to get it to their GitHub fork.

- On the GitHub web site, make _two_ pull requests:  

   - One to master, as usual
   
   - One to the release branch e.g. "release-4.17.6".  The comment on this PR should explain why this should be included instead of waiting for the next release.
   
   Merging the PR to the master makes those changes available on further developments forever; the one on the release, if accepted, includes the change and kicks off new runs of the various CI and build jobs.

   Note: The GitHub automated CI tests do their build after doing a (temporary) merge with the target branch. If the release branch and master have diverged enough that a single set of changes can't be used with both, a more complicated procedure than above might be needed.  In that case, try a PR onto the release branch of the needed change, and then pull the release branch back onto the master branch before fixing conflicts.

If somebody has merged their change into master (or it's branched from master later than the release tag), you have two choices:

- Merge master into the release-4.17.6 branch.  This will bring _everything_ that's been merged in, so remember to update the version markers on those PRs.  Effectively, you've just started the release process later.  Note that the `release.properties` and `pom.xml` files will have the wrong minor number in them:  You'll have to edit and commit that to get the right number in the release.

- `git cherrypick` just the changes you want. *This is not the recommended approach, as it is error-prone; we've had to withdraw releases in the past due to this.*  Read the documentation on that command carefully and double check your work. If possible, check the contents of the release branch on the GitHub web site to make sure only the changes you wanted were included.

====================================================================================
## Create zipped .properties (experimental)
 
The following will take several minutes, so be patient:

```
git checkout release-4.17.6
ant clean compile
cd target
rm -f properties.4.17.6.zip

foreach x ( `find classes -name \*.properties` )
printf '%s\n' 0a '# from tag v4.17.6' . x | ex $x
end

find classes -name \*.properties | zip -@ properties.4.17.6.zip
cd ..
mkdir release
mv target/properties.4.17.6.zip release/
ls -lt release/
git checkout master

```

====================================================================================
## Format file-release information

Run a script to download the created files, create checksums and create text for release notes, etc
```
./scripts/releasesummary 4.17.6
```

This will print a bunch of text in several sections. Save that for later and edit it into the website/releaselist, release note files and GitHub info below.

====================================================================================
## Create GitHub Release and upload files

This puts the right tag on the branch, then removes the branch.  

Note: Once a GitHub Release is created it is *not* possible to change it to refer to different contents. *Once this step is done, you need to move on to the next release number.*

- Disable the Jenkins release-build job; this is so it doesn't fail after later steps

- on GitHub JMRI/JMRI go to the "releases" link, then click "Draft a new release" e.g.
```
    https://github.com/JMRI/JMRI/releases/new
```

- Fill out form:

   - "tag version field" gets v4.17.6 (e.g. leading lower-case "v")
   - @ branch: select the release-4.17.6 release branch
   - "Release title" field gets "Prod/Test Release 4.17.6"
   - Description should contain text like (the releasesummary script above provided the correct filenames and hashes):

```   

[Release notes](https://www.jmri.org/releasenotes/jmri4.17.6.shtml)

Checksums:

File | SHA256 checksum
---|---
[JMRI.4.17.6+Rc861f38.dmg](https://github.com/JMRI/JMRI/releases/download/v4.17.6/JMRI.4.17.6+Rc861f38.dmg) | 7dce65a0bf9df31ed43148105614f159e13f35f6fea44bcc3756a20c5e7093fd
[JMRI.4.17.6+Rc861f38.exe](https://github.com/JMRI/JMRI/releases/download/v4.17.6/JMRI.4.17.6+Rc861f38.exe) | 01596ffdf16c443b7a600fd7e442b53642ab8604653cc2232a23ec1da4de4abb
[JMRI.4.17.6+Rc861f38.tgz](https://github.com/JMRI/JMRI/releases/download/v4.17.6/JMRI.4.17.6+Rc861f38.tgz) | e293859e9b3096cb70265e75940f9e581c08a6120805b69f404dcfa4018ef508


```

- Attach files by selecting them or dragging them in. Make sure that the Linux one is .tgz, not .tar.

- [ ] it's slow to upload from a typical home connection,; we wish we had a way to cross-load them from Jenkins (see below)

Note there's a little progress bar that has to go across & "Uploading your release now..." has to complete before you publish; make sure all four files (three installers plus properties) are there.
    
- Click "Publish Release"

- Wait for completion, which might be a while with big uploads

====================================================================================
## Check for Unmerged Changes

If there were changes once the release was tagged, it's important that those changes also get onto master. Normally this happens automatically with the procedure in "Further Changes" above. But we need to check. Start with your Git repository up to date on master and the release branch, and then (*need a cleaner, more robust mechanism for this*; maybe GitX or a PR?):

```
git fetch
git checkout master
git pull
git checkout -b temp-master
git merge origin/release-4.17.6
```

Note that you're testing the merge of the release branch back onto master.  This should report "Already up-to-date.", i.e. no changes, with the possible exception of some auto-generated files:
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
   
- You can delete that temp-master local branch now

====================================================================================
## Update GitHub Status items

- Create the [next GitHub Issue](https://github.com/JMRI/JMRI/issues) to hold discussion with conventional title "Create Test Release 4.17.7". Add the next release milestone (created above) to it. Typical text (get the date from the [milestone page](https://github.com/JMRI/JMRI/milestones)); for later releases in the series copy specific text from the milestone page:
```
This is the next release in the 4.17 cycle. It's intended to be created around (July 12) from the `HEAD` of the `master` branch.
```

- Confirm that the tag for the current release (v4.17.6 for release 4.17.6) is in place via the [tags page](https://github.com/JMRI/JMRI/tags), then manually delete the current release branch (release-4.17.6) via the [GitHub branches page](https://github.com/JMRI/JMRI/branches).  (N.B. We are experimenting with having the `release*` branches protected, in which case you may have to go to Setting; Branches; then edit the release* branch name to releaseX* to disable the protection before removing the branch.  If you do that, remember to replace the protection!)

- Go to the GitHub PR and Issues [labels list](https://github.com/JMRI/JMRI/labels) and remove any "afterNextTestRelease" (and "afterNextProductionRelease" if appropriate) labels from pending items

- If this is a production release, update the "Downloads" badge in the JMRI/JMRI README.md file in the JMRI and website repositories and commit back.

====================================================================================
## Branches for preparation of Production Releases

Lastly, if this release is one of the special series at the end of a development cycle that leads to a test release, create the next release branch now.  Those test releases are made cumulatively from each other, rather than each from master. We start the process now so that people can open pull requests for it, and discuss whether changes should be included.

(Maybe we should change their nomenclature to get this across?  E.g. instead of 4.5.5, 4.5.6, 4.5.7, 4.6 where the last two look like regular "from master" test releases, call them 4.5.6, 4.5.6.1, 4.5.6.2, 4.6 - this will make the operations clearer, but Version.java doesn't currently support it)

   - Create the next pre-production branch (*pre-production case only*):

```
git checkout (release-n.n.n)
git pull
git checkout -b (release-n.n.n+1)
git push github
```

====================================================================================
## Associated Documentation

- Update the release note page: change date, comment out "draft release", make sure links work and proper sections are commented/not commented out

- Update the web site front page and downloads page:
```
     index.shtml download/Sidebar download/index.shtml releaselist
```

- Commit site, push to github
```
    git commit -m"4.17.6 web site" .
    git push github
    git pull
```

- Wait for update on JMRI web server (or [ask Jenkins](http://builds.jmri.org/jenkins/job/WebSite/) to speed it along; note there are multiple components that need to run)

- Check the [web page](https://www.jmri.org) just in case you didn't push properly, etc

====================================================================================
## Announcement and Post-release Steps

- Mail announcement to jmriusers@groups.io

    Subject is "Test version 4.17.6 of JMRI/DecoderPro is available for download" or "JMRI 4.16 is available for download"

    Content:
    
Test version 4.17.6 of JMRI/DecoderPro is available for download.

This is the next in a series of test releases that will culminate in a production release, hopefully in early December 2019.

- Alt: There have been a lot of updates in this version, so it should be considered experimental.
- Alt: We're getting close to the end of the development series, so we'd appreciate feedback on whether or not this release works for your layout.

If you are currently using JMRI 4.9.6 or earlier, we strongly recommend that you first update to JMRI 4.12 and make sure that's running OK before updating to this test release. There have been a number of changes in serial port support, panel file format and configuration options since those earlier releases, and moving to the stable JMRI 4.12 release is a good way to work through any possible problems.
<https://www.jmri.org/releasenotes/jmri4.12.shtml>

If you use JMRI on Linux or Mac and are updating from JMRI 4.7.3 or earlier, there’s a necessary migration step. (Not needed on Windows) Please see the JMRI 4.12 release note for details: <https://www.jmri.org/releasenotes/jmri4.12.shtml#migration>

For more information on the issues, new features and bug fixes in 4.17.6 please see the release note:   
<https://www.jmri.org/releasenotes/jmri4.17.6.shtml>

Note that JMRI is made available under the GNU General Public License. For more information, please see our copyright and licensing page.
<https://www.jmri.org/Copyright.html>

The download links, along with lots of other information which we hope you'll read, can be found on the release note page:
<https://www.jmri.org/releasenotes/jmri4.17.6.shtml>

- Close the [4.17.6 release GitHub Issue](https://github.com/JMRI/JMRI/issues) with a note saying that
```
JMRI 4.17.6 has been released. Files are available in the GitHub release section.

```

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

- Take a break!



====================================================================================

# Additional Information

The rest of the document provides information about specific cases.

====================================================================================
## Local-build Alternative

If you can't use Jenkins for the actual build, you can create the files locally:

If you're building locally:
* You need to have installed NSIS from http://nsis.sourceforge.net (we use version 3.01 with long string support; see [Issue 3913](https://github.com/JMRI/JMRI/issues/3913) for instructions on how to build that for Linux). On macOS, install (currently [version 3.03](https://formulae.brew.sh/formula/makensis) via Homebrew
 - brew install makensis --with-large-strings
 - add an entry in "local.properties" nsis.home=/usr/local/bin/

* Either make sure that 'makensis' is in your path, or set nsis.home in your local.properties file to the root of the nsis installation:

```
        nsis.home=/opt/nsis/nsis-3.01/
```

- Get the release in your local work directory

```
    git checkout release-4.17.6
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

To do a direct download:

```
curl -o release.zip "http://builds.jmri.org/jenkins/job/TestReleases/job/4.17.6/lastSuccessfulBuild/artifact/dist/release/*zip*/release.zip"" 
```
and expansion; 

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

================================================================================
Manual process for making help file indexes:

```
        cd help/en/
        rm ~/.jhelpdev    (to make sure the right preferences are chosen)
        ./JHelpDev.csh   (See the doc page for setup) <-- for Windows, use JHelpDev.bat
        (navigate to JHelpDev.xml in release html/en/ & open it; might take a while)
        (click "Create All", takes a bit of time, wait for button to release)
        (quit)
        ant index TOC
```

================================================================================

`ant make-test-release-branch` does (more or less) the following actions (assumes 'github' is a remote pointing at https://github.com/JMRI/JMRI.git ):

```    
        git checkout master
        git pull
        (commit a version number increment to master)
        git checkout -b {branch}
        git push github {branch}
        git checkout master    
        git pull
```

================================================================================

Possibilities for automating GitHub release creation:


Alternatively, if you have shell access to the Jenkins server, you perhaps can upload directly from there, once the initial draft release has been created (this hasn't been tested):

```
github-release upload -s {github_secret} -u JMRI -r JMRI -t v4.17.6 -n "JMRI.4.17.7+Rd144052.dmg" -f /var/lib/jenkins/jobs/TestReleases/jobs/4.17.7/workspace/dist/release/JMRI.4.17.6+Rd144052.dmg 
github-release upload -s {github_secret} -u JMRI -r JMRI -t v4.17.6 -n "JMRI.4.17.6+Rd144052.exe" -f /var/lib/jenkins/jobs/TestReleases/jobs/4.17.6/workspace/dist/release/JMRI.4.17.6+Rd144052.exe 
github-release upload -s {github_secret} -u JMRI -r JMRI -t v4.17.6 -n "JMRI.4.17.6+Rd144052.tgz" -f /var/lib/jenkins/jobs/TestReleases/jobs/4.17.7/workspace/dist/release/JMRI.4.17.6+Rd144052.tgz 
```

(It might be possible to automate this in Ant, see http://stackoverflow.com/questions/24585609/upload-build-artifact-to-github-as-release-in-jenkins )
    


================================================================================

Instructions for uploading javadoc, XSLT if not being done automatically


- Create and upload the Javadocs (As of May 2016, the [Jenkins server](http://builds.jmri.org/jenkins/job/WebSite/job/generate-website/) was updating these from git weekly, in which case just start a run of that Jenkins job. Note that if you're doing this locally, it this might take an hour or more to upload on a home connection, and it's OK to defer the uploadjavadoc step): 
```
    ant javadoc-uml uploadjavadoc
```

- Create and upload the XSLT'd decoder pages
```
    (cd xml/XSLT; ant xslt upload)
```

Note: the very first time doing this on a new machine, it will be required to run the rsync command manually as the ssh fingerprint for the server wil need to be added to the local machine. Without this, it will fail via ant.

================================================================================


