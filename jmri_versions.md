# JMRI Versions

The JMRI library uses a semantic versioning system meaning that the jmri.jar file has
a version number, like jmri.1.2.3.jar, there the first number is the MAJOR version,
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


# What is an API?

An API (Application Programming Interface) is a set of interfaces, classes, methods and fields that is declared to be used by software that is using the API.



I suggest we use the annotations from [API Guardian](https://github.com/apiguardian-team/apiguardian)
to indicate what's usable by design by implementers and scripters and declare everything else to be
"implementation details" subject to change at will. By using these annotations we can allow what must
be public to compile, but add a declarative layer over that to indicate that some public classes/methods/fields should not be relied upon.
