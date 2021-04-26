As of 1 August 2020, we are using semantic versioning as documented below.

# What is an API?

The API (Application Programming Interface) is a contract with a set of interfaces, classes, methods and fields that can be used by software that is using the JMRI library. An example of such software is [CATS (Computer Automated Traffic System)](http://cats4ctc.wikidot.com/).

The API is a contract to other developers, saying that if you want to use jmri.jar in your project, you can use these interfaces/classes/methods/fields under these conditions, for example that a particular method is available, but may change in the next minor version, while another method is granted to stay the same at least until next major version.

# JMRI Versions

The JMRI library uses [semantic versioning](https://semver.org/) meaning that the jmri.jar file has
a version number, like jmri-1.2.3.jar, there the first number is the MAJOR version,
the second number is the MINOR version and the third number is the PATCH version.

For more information on MAJOR, MINOR and PATCH, see https://semver.org/

The JMRI project implements this as follows:

Each public or protected interface/class/method/field that is part of the API of JMRI
is annotated with `@API` from the [@API Guardian](https://github.com/apiguardian-team/apiguardian)
project. The annotation’s status attribute can be assigned one of the following values.

Status | Description
------ | -----------
INTERNAL | Must not be used by any code other than JMRI itself. Might be removed without prior notice.
DEPRECATED | Should no longer be used; might disappear in the next minor release.
EXPERIMENTAL | Intended for new, experimental features where we are looking for feedback. Use this element with caution; it might be promoted to MAINTAINED or STABLE in the future, but might also be removed without prior notice, even in a patch.
MAINTAINED | Intended for features that will not be changed in a backwards- incompatible way for **at least** the next minor release of the current major version. If scheduled for removal, it will be demoted to DEPRECATED first.
STABLE | Intended for features that will not be changed in a backwards- incompatible way in the current major version (5.*).

If the `@API` annotation is present on a type, it is considered to be applicable for all public members of that type as well. A member is allowed to declare a different status value of lower stability.

## JMRI Versions

* The MAJOR version changes when something annotated with `@API(MAJOR)` changes. For example: jmri-5.4.3.jar -> jmri-6.0.0.jar
* The MINOR version changes when someting annotated with `@API(MAINTAINED)` changes or something marked with `@API(DEPRECATED)` is removed. For example: jmri-5.4.3.jar -> jmri-5.5.0.jar
* The PATCH version increases for every release, except when MAJOR or MINOR version changes. For example: jmri-5.4.3.jar -> jmri-5.4.4.jar

Changes that are still backwards compatible does not trigger a new MAJOR or MINOR version. For example, adding a new default method on an interface does not result in a new MAJOR or MINOR version. But adding a new method without a default implementation on an interface marked with `@API(MAJOR)` or `@API(MAINTAINED)` will trigger a new MAJOR or MINOR version, since all classes that inherits that interface will need to implement it, unless their parents implement it.

Non official releases (test releases) is marked with SNAPSHOT, for example jmri-4.3.5-SNAPSHOT.jar.

Developer releases are marked with the developers GitHub user name (if that's possible) or some other identifier. For example jmri-4.3.5-SNAPSHOT+danielb987.jar.

# Recommended practices for developers of the JMRI project

Unannotated interfaces/classes are considered EXPERIMENTAL.

Unannotated methods/fields follows the same annotation as its interface/class. So if the interface/class is annotated with MAINTAINED, every unannotated method in that interface/class is MAINTAINED. Unannotated methods/fields in unannotaded interfaces/classes are considered EXPERIMENTAL.
