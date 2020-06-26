# (Proposed) JMRI Release Methodology

On the jmri-developers list, it's been proposed to move toward a release process aligned with [semantic versioning](https://semver.org) in which an I.J.K release number indicates the kinds of included changes:

 - Changes that can _break_ outside code cause the first number (I) to be incremented and the other to reset to zero.
 - Other changes that appear in the visible API will increment the 2nd number (J) and reset the third
 - Other changes result in incrementing the third number (K) 
 
## Denoting releases 

JMRI installers can be built in four different scenarios:

* Developer build on their local machine
* Jenkins 'nightly' build as part of the Continuous Integration framework
* Release build distributed as installers on the JMRI website
* _Recommended_ installer builds distributed on the JMRI website and promoted as the main supported version until the next _recommended_ release

Developer builds will identify themselves with versions strings like `JMRI.21.4.2-private-jake-20200625T1452Z+Rbab33d0f33` where `private` denotes a developers own private build, `jake` the username of the specific developer, and `+R` indicates the 8 digits of the SHA of HEAD when built.

Jenkins 'nightly' builds will identify themselves with version strings like `JMRI.21.4.2-dev-Jenkins-20200625T1452Z+Rbab33d0f33` where `dev` denotes a development build, `Jenkins` that this was built by the Jenkins CI environment, and `+R` indicates the 8 digits of the SHA of HEAD when built.

A released version is denoted as a `vI.J.K` (i.e. `v21.4.2`) tag in our common repository.

Distributed installers built from a released version will identify themselves with version strings like `JMRI.21.4.2+Rbab3d0f33` where the `+R` indicates 8 digits of the tag SHA.  

Periodically, a release version is flagging as a default for new/novice users to install by default.  It will be referred to by year and month: YYYY-MM, i.e 2020-07. Its contents will be identical to an already-released version.  This will be denoted as a `rYYYY-MM` (i.e. `r2020-07`) tag in our common repository. Distributed installers will identify themselves using first the date, then the underlying release, i.e. `JMRI.2020-07-21.4.2`

_Before_ a release, the development installers will be named with the branch being built and the data-time of the build (see below for discussion of those), for example `JMRI-dev-major+20200622T0419Z+Re0a5fed223`

## Identifying Change Types

Each PR that's merged for inclusion can require an increment of the first, second or third digit.  For this to work, we need a very reliable way to identify the right one, and make sure it takes effect.

 - We'll define four new labels for GitHub PRs:
   - _Breaking Change_ - requires a major version change because it breaks outside code
   - _Feature_ - changes the visible API
   - _Fix_ - no change to visible API, but triggers a version change absent other non-chore changes
   - _Chore_ - no change to Java code or published artefacts (e.g. updates build scripts and CI processes), does not trigger version change
 - A PR must have exactly one of those applied for it to be merged
 - The author of the PR can propose a label, in which case only one reviewer is required.
 - If the author of the PR does not propose a label, two reviewers must sign off on the right label.
 
Since we'll now be requiring a review, the reviewer can also encourage reasonable additions to the release note. A more-automated process for creating release notes with useful content would help this succeed.

We recommend that people use [Conventional Commits rules](https://www.conventionalcommits.org/en/v1.0.0/) in their commit comments to help identify how a PR should be labelled.  GitHub makes the comment for the first commit in a PR particularly visible.  We can't count on that instead of a label, though, because the impact of a PR might have changed since its first PR, and/or because the proper label for the PR might not be what the developer thought when writing that first comment. 

## Use of Git to Accumulate Changes
 
 - The HEAD of `JMRI/master` will always be the most highest numbered release made so far
     - Usually this is the last
     - But if 5.3.1 is released after 5.4.0 or even 6.0.0 is released, those will remain HEAD of master
     - This means that new Git users who checkout `master` will be working on a mergeable base for the next release(s)
  - PRs labelled with _Breaking Change_ will be merged to a 'dev-major' branch, those labelled _Feature_ will be merged to a 'dev-minor' branch and those labelled with _Fix_ will be merged to a 'dev-update' branch.
  - Often, those perhaps not on every PR, the branches will be merged upwards: dev-update into dev-minor, dev-minor into dev-major
  - __Question__ Do _Chore_ PRs just get committed to master?
  
The goal of this is to make it possible to work on i.e. updates from a stable base of either the last numbered release (`master`, which people get by default) or the current contents of the relevant branch.  Because it makes all three branches available, it allows accumulating and collaborating on all three kinds of changes.

The periodic upward merges are meant to find conflicts where a major change turns out to be incompatible with ongoing bug fixes and minor changes.  The longer between major-change releases, the more likely those conflicts are.  This just forces them to the surface earlier for resolution.

## Release Timing

We need a process to decide when releases should be made from the branches. Making a release, which can be done from any of the three branches is basically just making new installers (with a different name injected, as we do now) and changing a few web pages, so with a bit more automation it'll be quick to do.  

How do we decide when to do that if we don't have a monthly cadence?

 - How often (on what basis) should we be creating "Update Only" branch releases, hence updating that digit?  Is this effectively our nightly build process for development releases now?
 
 - How often (on what basis) should we be creating "Minor Change" branch releases, hence updating that digit?  Is this done i.e. every two weeks if there have been PRs? Every month? When requested?  This should be a pretty quick cadence to get them out where people can test them; our users are our best testers.
 
 - How often (on what basis) should we be creating "Major Change" branch releases, hence updating that digit? Since we distribute applications to users, who are primarily _application_ users, most major changes may not even be visible.  We need to get them out to people before too much change accumulates, but too many might discourage people.  Maybe every three months? Six months?

 - What process is needed to identify when a release is good enough as one that should be made the default? Both the large scale, so we can talk in advance about the "October 2020" (2020-10) release, and at the event, as we try to get something usable to converge. For the last few years, we've needed several test releases to converge on a quality level; June 2020 was no different in that respect. How do we get that done in this model?
 
## Web Contents

The default web content (from [https://jmri.org](https://jmri.org)) will be from the HEAD of the dev-update branch.  This allows minor content fixes to get on the web immediately.

Simultaneously, the HEAD of the dev-minor and dev-major branches will populate web content starting at [https://jmri.org/minor](https://jmri.org/minor) and [https://jmri.org/major](https://jmri.org/major)  This will allow people to see Javadoc, point people to recently updated pages, etc.

 ## Merging and Release Process Examples
 
 In all of these, assume that the most recent release numbers are 5.6.3, 5.7.0 and 6.0.0; various people are test/using all three.
 
 ### Update
 
Somebody as a small bug to fix.  He edits in the fix on a branch from master.  This gets merged into all three branches. (The upward merging will probably be a separate step; perhaps only a check for that possibility will be made when merging the PR) If it's significant enough, or enough has accumulated, this will get released as part of 5.6.4, in which case it'll be tagged there as v5.6.4 and master will be reset to that tag.
 
  ### Minor Change
  
Somebody has a minor change (new feature that appears in API). He edits it in on a branch from master. This gets merged via PR into the dev-minor branch and dev-major branch. (The upward merging will probably be a separate step; perhaps only a check for that possibility will be made when merging the PR) When that, and perhaps other, minor changes are ready to be made available to the user community, a 5.8.0 release will be made, tagged as v5.8.0.
  
At this point, master remains at 5.6.3. At some later point, when 5.8.0 or perhaps 5.8.1 is viewed as stable enough that it can become the default, both the master and dev-update branches will be reset to (say) 5.8.1 so that becomes the base for further development.
  
  ### Fix to Minor Change
  
Say that something was broken by the minor change above.  A developer can edit in that fix on a branch from v5.8.0, make a PR against the dev-minor branch, get it merged, and then a v5.8.1 can be released.  That can be made the base for development or not as needed.
  
  ### Merging updates to a Minor Change
  
Alternately, after v5.8.0 is out, a developer might want to fix an issue in 5.6.3 that identically affects (because that code was unchanged) the 5.8.0 release.  He edits on a branch off master, does a PR against dev-update, that gets merged and then that branch is merged upward (as usual) into the dev-minor branch.  This allows a v.5.8.1 to be created as needed. That can be made the base for development or not as needed.
   
   ### Major Changes
   
Major changes go basically as above.  They're created off master if possible, or off the dev-major branch if (more likely) they're cumulative on other major changes. Then the various operations go through as above.

The hardest thing for "major change" releases will be developing a consensus around when they should become the default.  That's a community quality control issue, not a git technology one.

  
 ## Other Things to Note
 
JMRI has, for a long time, followed a Linux-like release numbering system where odd-numbered minor releases were for development and tests, whilst even numbered releases were for production. That distinction is no longer present here.

There is no longer an explicit [deprecation cycle](https://www.jmri.org/help/en/html/doc/Technical/RP.shtml#deprecating). One can certain mark parts of the API as deprecated in a update or minor change; that's polite.  But when the change gets into the major branch, the deprecations should have been removed:  That's part of why the changes are considered "breaking changes".

This entire system is well suited to "point" releases to fix things.  For example, say the most recent releases have been 5.6.3, 5.7.0 and 6.0.0.  Then
 - Jim finds a bug and fixes it starting with master (same as 5.6.3)
 - Because it started on master, that can be merged anywhere
 - It's a _bug_, so it can be released as 5.6.4 so that people using that as a long-term thing get the fix with minor disruption.
 - But it will also be included in a 5.7.1 and 6.0.1 when those get released, so we can decide when to release them and make them available.

It's even possible to make e.g. 5.6.3.1 with _just_ one specific change, and not the other updates that have accumulated on 5.6.3 since it was created.

## Migration

(This is a temporary section to hold a migration plan.  If/Once we decide to execute, this should be removed from here and made into a JMRI/JMRI Issue to track the work)

Once we have agreement
 - [ ] update documentation, which adds this to the main web server
 
The assumption is that we will start with the new process directly after JMRI 4.20.0 is released. That number makes it serve as both the last production release of the old numbering scheme, while also fitting within the new schema.  This would then be followed by 4.21.1 (the .1 isn't quite right, but we've already seen comments for that name) as the first of the new process.

 - [ ] Create the new labels
    - [ ] Check if any of the old labels need to be updated/ superceded
    - [ ] Update the [documentation](https://www.jmri.org/help/en/html/doc/Technical/gitdeveloper.shtml)
    
 - [ ] Set Github protections so master cannot be updated by PRs
 
 - [ ] Create the new branches: (we are temporarily leaving master as-is just this one time)
    - [ ] dev-major, dev-minor from v4.20
    - [ ] dev-update from the current `master` as a HEAD of current development (master will be reset to 4.20.1 once it's released)
    
 - [ ] Figure out how numberings will work in Version.java et al and commit changes as needed to number the three branches as 5.0.0, 4.22.0 and 4.21.1 respectively
 - [ ] Figure out and document how release notes will work across these branches, put that mechanism in place
 
 - [ ] Update the Jenkins webserver tasks to load the three branches
 - [ ] Update Jenkins to build installers from the head of each branch
 - [ ] Update scripts/HOWTO-Distribution.md
 - [ ] Create a GitHub action CI to check the ability to merge dev-update -> dev-minor, dev-update -> dev-major and/or dev-minor -> dev-major as appropriate

 - [ ] (Once it's decided it's ready, based on whatever criteria is chosen) Create and tag release 4.21.1 as the first under this system
    - [ ] Update `master` to the released 4.21.1
    - [ ] Update dev-update to be working on 4.22.2
 

