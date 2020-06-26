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

__Question__: if the first commit to a PR follows [Conventional Commits rules](https://www.conventionalcommits.org/en/v1.0.0/), can that be a replacement for the label?

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
 
## Other questions?

 - What should populate the web? 
   - By doing that from `master`, it'll automatically be stable; some people like that.
   - We don't know a-priori what will be released next, so we can't populate it from the _next_ changes as they're developed
   - If desired, we could have all four (master, dev-major, dev-minor, dev-branch) on the web at slightly different URLs; that just moves the question to which should be the default
    

 ## Other Things to Note
 
JMRI has, for a long time, followed a Linux-like release numbering system where odd-numbered minor releases were for development and tests, whilst even numbered releases were for production. That distinction is no longer present here.

There is no longer an explicit [deprecation cycle](https://www.jmri.org/help/en/html/doc/Technical/RP.shtml#deprecating). One can certain mark parts of the API as deprecated in a update or minor change; that's polite.  But when the change gets into the major branch, the deprecations should have been removed:  A breaking change is a breaking change.

This entire system is well suited to "point" releases to fix things.  For example, say the most recent releases have been 5.6.3, 5.7.0 and 6.0.0.  Then
 - Jim finds a bug and fixes it starting with master (5.6.6)
 - Because it started on master, that can be merged anywhere
 - It's a _bug_, so it can be released as 5.6.4 so that people using that as a long-term thing get the fix with minor disruption.
 - But it will also be included in a 5.7.1 and 6.0.1 when those get released, so we can decide when to release them and make them available.

It's even possible to make e.g. 5.6.3.1 with _just_ a specific change, and not the other updates that have accumulated on 5.6.3 since it was created.
 
 

