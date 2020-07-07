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

Developer builds will identify themselves with versions strings like `JMRI-dev-major+private+jake+20200625T1452Z+Rbab33d0f33` where `dev-major` denotes the branch, `private` denotes a developers own private build, `jake` the local username of the specific developer, and `+R` is followed by the first eight digits of the SHA of HEAD as built.

Jenkins 'nightly' builds will identify themselves with version strings like `JMRI-dev-minor+CI+Jenkins+20200625T1452Z+Rbab33d0f33` where `dev-major` denotes the branch, `Jenkins` that this was built by the Jenkins CI environment, and `+R` is followed by the first eight digits of the SHA of HEAD as built.

A released version is denoted as a `vI.J.K` (i.e. `v21.4.2`) tag in our common repository.

Distributed installers built from a released version will identify themselves with version strings like `JMRI.21.4.2+Rbab3d0f33` where the `+R` indicates 8 digits of the tag SHA.  

Periodically, a release version is flagging as a default for new/novice users to install by default.  It will be referred to by year and month: YYYY-MM, i.e 2020-07. Its contents will be identical to an already-released version.  This will be denoted as a `rYYYY-MM` (i.e. `r2020-07`) tag in our common repository. Distributed installers will identify themselves using first the date, then the underlying release, i.e. `JMRI.2020-07-21.4.2`

## Identifying Change Types

Each PR that's merged for inclusion can require an increment of the first, second or third digit.  For this to work, we need a very reliable way to identify the right one, and make sure it takes effect.

 - We'll define four new labels for GitHub PRs:
   - `Breaking Change` - requires a major version change because it breaks outside code
   - `Feature` - changes the visible API
   - `Fix` - no change to visible API, but triggers a version change absent other non-chore changes
   - `Chore` - no change to Java code or published artifacts (e.g. updates build scripts and CI processes), does not trigger version change
 - A PR must have exactly one of those applied for it to be merged
 - The author of the PR can propose a label, in which case only one reviewer is required.
 - If the author of the PR does not propose a label, two reviewers must sign off on the right label. (We may relax this eventually, but its included now to get people thinking about what category various changes might be in)
 
Since we'll now be requiring a review, the reviewer can also encourage reasonable additions to the release note. A more-automated process for creating release notes with useful content would help this succeed.

There's some judgement involved in this labelling process.  An algorithmic change might change behavior enough to add a new feature (`Feature`) or cause issues for downstream code (`Breaking Change`).  On the other hand, changing a class API by marking methods as deprecated for future removal is not a breaking change _yet_, and can be marked as `Fix`.

We recommend, but do not require, that people use [Conventional Commits rules](https://www.conventionalcommits.org/en/v1.0.0/) in their commit comments to help identify how a PR should be labelled.  GitHub makes the comment for the first commit in a PR particularly visible.  To do this:

 - Put `[fix]` at the start of your commit message if the commit should be labelled `Fix` and included in the next I.J.++K release
 - Put `[chore]` at the start of your commit message if the commit should be labelled `Chore` and included in the next I.J.++K release
 - Put `[feat]` at the start of your commit message if the commit has sufficient changes that it should be should be labelled `Feature` and included in the next I.++J.0 release
 - Put `[feat]` at the start of your commit message and `BREAKING CHANGE` somewhere in the commit message if the commit should be labelled `Breaking Change` and included in the next +I.0.0 release

We can't count on the commit messages instead of a label completely, because the impact of a PR might have changed since its first commit, and/or because the proper label for the PR might not be what the developer thought when writing the commit comment(s). 

## Use of Git to Accumulate Changes
 
 - The HEAD of `JMRI/master` will always be the most highest numbered release made so far
     - Usually this aill be the most recent
     - Specifically, if 5.3.1 is released after 5.4.0 or even 6.0.0 is released, those will remain HEAD of master
     - This means that new Git users who checkout `master` will be working on a mergeable base for the next release(s)

  - PRs labelled with `Breaking Change` will be merged to a `dev-major` branch, those labelled `Feature` will be merged to a `dev-minor` branch and those labelled with `Fix` will be merged to a `dev-update` branch.
  - Often, though perhaps not on every PR, the branches will be merged upwards: `dev-update` into `dev-minor`, `dev-minor` into `dev-major`

`Chore` PRs should be put into affect in the infrastucture as soon as possible, but we don't want them to burden developers with minimal git capability, i.e. working directly from `master`. Hence they should be merged to the `dev-update`, and from there merged upward to the other branches as needed.  They'll then get back to `master` when a branch is next released. (Keeping master exactly fixed helps us check for and prevent inadvertant merges of other changes via PRs)
  
The goal of this is to make it possible to work on i.e. updates from a stable base of either the last numbered release (`master`, which people get by default) or the current contents of the relevant branch.  Because it makes all three branches available, it allows accumulating and collaborating on all three kinds of changes.

The periodic upward merges are meant to find conflicts where a major change turns out to be incompatible with ongoing bug fixes and minor changes.  The longer between major-change releases, the more likely those conflicts are.  This just forces them to the surface earlier for resolution.

 ## Merging and Release Process Examples
 
 In all of these, assume that the most recent release numbers are 4.23.1, 4.24.0 and 5.0.0; various people are test/using all three.
 
 There are two approaches to handling overlapped features and fixes, which we discuss here as alternatives. In the following diagrams, time runs downwards.  We start by showing a few changes applied to the branches, and a 4.23.2 fix is published.
 
|           |   `dev-update` |   `dev-minor`  |  `dev-update` |
| :-----:   |   :----------: |   :----------: |  :----------: |
| Numbered: |  pre-4.23.2    |   pre-4.25.0   |   pre-6.0.0   |
|   Fix     |       A        |        A       |      A        |
|   Fix     |       B        |        B       |      B        |
|   Feat    |                |        C       |      C        |
| Br Change |                |                |      D        |
|   Pub     |    4.23.2      |                |               |
|           |  pre-4.23.3    |                |               |
|   Feat    |                |        E       |      E        |
|   Feat    |                |        F       |      F        |

 Assume it's time to publish 4.25.0 from `dev-minor`. At this point, there are two choices:

 - since 4.25.0 is thought to be worth publishing, it should form the basis for `dev-update` from now:  Any "fix" PRs (i.e. "fix F" via H) there will be eventually be the basis for a 4.25.1, 4.25.2, etc. Meanwhile, "feature" PRs go on the `dev-minor` branch on the way to 4.26.0, which is the next published from here.  

|           |   `dev-update` |   `dev-minor`  |  `dev-update` |
| :-----:   |   :----------: |   :----------: |  :----------: |
| Numbered: |  pre-4.23.3    |   pre-4.25.0   |   pre-6.0.0   |
|   Pub     |                |     4.25.0     |               | 
|           |      \|-<       |       <--<     |               |
|           |  pre-4.25.1    |   pre-4.26.0   |               |
|   Feat    |                |        G       |      G        |
|  Fix  F   |       H        |        H       |      H        |
|           |                |                |               | 

 
 - Or, we could say we're not that certain about `dev-minor`, and put both fixes (i.e. "fix F" via H) and features there which we can release as 4.25.1 when needed. (Leaving `dev-update` as the place to make further fixes to the 4.23.* series as needed).  

|           |   `dev-update` |   `dev-minor`  |  `dev-update` |
| :-----:   |   :----------: |   :----------: |  :----------: |
| Numbered: |  pre-4.23.3    |   pre-4.25.0   |   pre-6.0.0   |
|   Pub     |                |     4.25.0     |               | 
|           |                |   pre-4.25.1   |               |
|   Feat    |                |        G       |      G        |
|  Fix F    |                |        H       |      H        |
|           |                |                |               | 
 
 The second has the problem that the 4.25.1 could include include features not present in 4.25.0 (i.e. G), which is not consistent with the Semantic Versioning pattern.  On the other hand, it's easier to issue fixes to keep making changes to the 4.23.* series after 4.25.0 is out. That can be important, but we can do almost as good a version of that by only updating `master` later.  So in the following, we proposed the 1st form.
 
 
 ### Update
 
Somebody as a small bug to fix.  He edits in the fix on a branch from `master`.  This gets merged into all three branches. (The upward merging will probably be a separate step; perhaps only a check for that possibility will be made when merging the PR) If it's significant enough, or enough has accumulated, this will get released as part of 4.24.2, in which case it'll be tagged there as v4.24.2 and `master` will be reset to that tag.
 
  ### Minor Change
  
Somebody has a minor change (new feature that appears in API). He edits it in on a branch from `master`. This gets merged via PR into the `dev-minor` branch and `dev-major` branch. (The upward merging will probably be a separate step; perhaps only a check for that possibility will be made when merging the PR) When that, and perhaps other, minor changes are ready to be made available to the user community, a 4.25.0 release will be made, tagged as v4.25.0. 

The `dev-update` branch is updated to this point and marked to build 4.25.1 next. 

The `dev-minor` branch is marked to build 4.26.0 next.
  
Before this, `master` was at 4.22.2 to serve as a clean base for all three branches. It can stay there for a little while 4.25.0 isn't going to get a lot of use (i.e. if there's likely to be multiple fixes to 4.22.2 for a 4.22.3, etc)  But shortly it should be updated to 4.25.0 to be closer to the development head of the branches.
  
  #### Fix to Minor Change
  
Say that something was broken by the minor change above.  Because `dev-update` contains 4.25.0, the developer can edit in that fix on a branch from `dev-update` (or master, if easier), make a PR against the `dev-update` branch, get it merged, and then a v4.25.1 can be released.  That can be made the base for development or not as needed.
     
   ### Major Changes
   
Major changes go basically as above.  They're created off `master` if possible, or off the `dev-major` branch if (more likely) they're cumulative on other major changes. Then the various operations go through as above.

The hardest thing for "major change" releases will be developing a consensus around when they should be published.  That's a community feature vs cost and quality control issue, not a Git technology one.

## Release Timing

We need a process to decide when releases should be made from the branches. Making a release, which can be done from any of the three branches is basically just making new installers (with a different name injected, as we do now) and changing a few web pages, so with a bit more automation it'll be quick to do.  

How do we decide when to do that if we don't have a monthly cadence?

 - How often (on what basis) should we be creating "Update Only" branch releases, hence updating that digit?  Is this effectively our nightly build process for development releases now, i.e. somebody who needs a bug fix is encouraged to "pick up the most recent update build"? Or do we publish these whenever there's a significant fix?
 
 - How often (on what basis) should we be creating "Minor Change" branch releases, hence updating that digit?  Is this done i.e. every two weeks if there have been PRs? Every month? When requested?  This should be a pretty quick cadence to get them out where people can test them; our users are our best testers. 
 
 - How often (on what basis) should we be creating "Major Change" branch releases, hence updating that digit? Since we distribute applications to users, who are primarily _application_ users, most major changes may not even be visible.  We need to get them out to people before too much change accumulates, but too many might discourage people.  Maybe every three months? Six months?

 - What process is needed to identify when a release is good enough as one that should be made the default? Both the large scale, so we can talk in advance about the "October 2020" (2020-10) release, and at the event, as we try to get something usable to converge. For the last few years, we've needed several test releases to converge on a quality level; June 2020 was no different in that respect. How do we get that done in this model?  Are these made from a long-time accumulation of minor releases?
 
One possible sequence of development and release:
 - Publish from `dev-update` whenever enough has accumulated; no specific cadence
 - Publish from `dev-minor` roughly 4-6 weeks and strongly encourage use to get feedback; significant problems can be fixed via `dev-update`
 - Publish from `dev-major` on targeted dates with major changes.

To it another way:  Publish the `dev-minor` branch to users often and consistently, using the `dev-update` to get fixes out more often.  This is basically "test releases with fixes". Eventually, effort focuses on one set of fixes until that particular head of `dev-update` is good enough to call a production release.

One could also imagine a "periodic production release from `dev-major` with fixes" model, but the key question remains:  How do we use these numbers to simultaneously get fixes to users _and_ encourage them to test new features?

## Web Contents

The default web content (from [https://jmri.org](https://jmri.org)) will be from the HEAD of the `dev-update` branch.  This allows minor content fixes to get on the web immediately.

Simultaneously, the HEAD of the `dev-minor` and `dev-major` branches will populate web content starting at [https://jmri.org/minor](https://jmri.org/minor) and [https://jmri.org/major](https://jmri.org/major)  This will allow people to see Javadoc, point people to recently updated pages, etc.

  
 ## Other Things to Note
 
JMRI has, for a long time, followed a Linux-like release numbering system where odd-numbered minor releases were for development and tests, whilst even numbered releases were for production. That distinction is no longer present here.

This entire system is well suited to "point" releases to fix things.  For example, say the most recent releases have been 4.23.1, 4.24.0 and 5.0.0.  Then
 - Jim finds a bug and fixes it starting with `master` (same as 4.23.1)
 - Because it started on `master`, that can be merged anywhere
 - It's a _bug_, so it can be released as 4.23.2 so that people using that as a long-term thing get the fix with minor disruption.
 - But it will also be included in a 4.25.0 and 6.0.0 when those get released, so we can decide when to release them and make them available.

It's even possible to make e.g. 4.23.1.1 with _just_ one specific change, and not the other updates that have accumulated toward 4.23.2 since it was created.

There is no longer an explicit [deprecation cycle](https://www.jmri.org/help/en/html/doc/Technical/RP.shtml#deprecating). One should certain mark parts of the API as deprecated in a update or minor change; that's polite.  But when the change gets into the major branch, the deprecations should have been removed:  That's part of why the changes are considered "breaking changes". Because we're separately releasing versions that have and don't have breaking changes, users can decide when they want to move forward; effectively managing their own deprecation cycles.

## Migration

(This is a temporary section to hold a migration plan.  If/Once we decide to execute, this should be removed from here and made into a JMRI/JMRI Issue to track the work)

 - [ ] Get agreement on private/development build naming, at least for the first few

Once we have agreement
 - [ ] update documentation, which adds this to the main web server
 
The assumption is that we will start with the new process directly after JMRI 4.20.0 is released. That number makes it serve as both the last production release of the old numbering scheme, while also fitting within the new schema.  This would then be followed by 4.21.1 (the .1 isn't quite right, but we've already seen comments for that name) as the first minor feature release of the new process.

 - [ ] Create the new labels
    - [ ] Check if any of the old labels need to be updated/ superceded
    - [ ] Update the [documentation](https://www.jmri.org/help/en/html/doc/Technical/gitdeveloper.shtml)
    
 - [ ] Set Github protections so `master` cannot accidentally be updated by PRs
 
 - [ ] Create the new branches: (we are temporarily leaving `master` as-is just this one time)
    - [ ] `dev-major`, `dev-minor` from the current `master` as a HEAD of current development, freezing `master` at that point
    - [ ] `dev-update` from v4.20 (in case a v4.20.1 is needed quickly)
    
 - [ ] Figure out how numberings will work in practice for Version.java et al and commit changes as needed to number the three branches as 5.0.0, 4.22.0 and 4.21.1 respectively
 - [ ] Figure out and document how release notes will work across these branches, put that mechanism in place
 - [ ] Update the Jenkins webserver tasks to load the three branches
 - [ ] Update Jenkins to build installers from the head of each branch
 - [ ] Update scripts/HOWTO-Distribution.md
 - [ ] Create a GitHub action CI to check the ability to merge `dev-update` -> `dev-minor`, `dev-update` -> `dev-major` and/or `dev-minor` -> `dev-major` as appropriate

 - [ ] (Once it's decided it's ready, based on whatever criteria is chosen) Create and tag release 4.21.1 as the first under this system from the `dev-minor` branch.
    - [ ] Update `master` and `dev-update` to the released 4.21.1
 
What we're trying to avoid is moving the `master` branch pointer backwards in time. This is likely to cause consistency problems for naive Git users. If we need a 4.20.1 (i.e. bug fixes on production), the PR will have to be manually created _on__ the `dev-update` branch this one time, instead of automatically getting it right from the `master`.  After 4.22.0 is created, this system will get `master` into its long-term configuration.

The endpoint of the migration is then:
 - The `dev-update` will have `v4.20` as its head and be marked as pre-4.20.1
 - `dev-minor` will be marked as pre-4.21.1 and will be at the then-current `master`
 - `dev-major` will be marked as pre-5.0.0 and will be at the then-current `master` 
 
When eventually there's a decision to create/publish 4.22.0:
 - `master` will be 4.22.0
 - `dev-update` will be `master` (4.22.0) marked as pre-4.22.1
 - `dev-minor` will be `master` (4.22.0) marked as pre-4.23.0
 - `dev-major` will have changes-to-date merged in, but doesn't change label (pre-5.0.0) at this point
 
 