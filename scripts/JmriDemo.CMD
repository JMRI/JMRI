/*
   Start Java application, after optionally extending ClassPath

   Author: Rob Hamerling (r.hamerling@planet.nl)

   To be started in the directory with application, unless
   the application is in a Classpath directory

   Expects max 2 parameters:
     first:  name of application
             default: JMRIdemo.
     second: library (jar-file) for application
             default: AppClasses.jar

   Parameters are checked and modified if appropriate as follows:
   - '.class' removed from application specification
   - explicit path removed from application specification
   - '.jar' added to library specification
*/

DefaultApplication = "JMRIdemo"         /* default application */

j1 = "JMRI.jar;"
j2 = "lib\log4j.jar;lib\collections.jar;"
j3 = "lib/xerces.jar; lib/jdom-jdk11.jar;"  /* default Classpath prepend */
DefaultClassPath = j1||j2||j3

parse arg app lib .                     /* scan commandline (max 2 tokens) */

if app \= "" then do                    /* application is specified */
  off = lastpos(".class", app)          /* check on .class extension */
  if off > 0 then do                    /* string '.class' found */
    app = substr(app,1,off-1)           /* strip this extension */
  end
  app = filespec('name', app)           /* leave only filename.ext */
end
else do                                 /* application not specified */
  app = DefaultApplication              /* take default application */
end

if lib = "" then do                     /* no class lib(s) parameter spec'd */
  lib = DefaultClassSpec                /* take default */
end
lib = libpath(lib)                      /* parse lib spec */
if lib = "" then do                     /* one or more libs not found */
  return 1
end

cls = value(classpath,,"OS2ENVIRONMENT")   /* obtain current classpath */
off = pos(lib,cls)                      /* check if added before */
if off = 0 then do                      /* string not found */
  lib = lib||cls                        /* prepend classpath with lib */
  cls = value(classpath,lib,"OS2ENVIRONMENT")   /* set new classpath */
  say "New classpath:" value(classpath,,"OS2ENVIRONMENT")
end

say "Starting" app ". . ."
'@java' app

return 0




/* -----------------------------------------
   Parse classlib string.
   For every individual lib:
     - add '.jar' extension if not present
     - check existence of every lib in string

   Input: library string to be added to ClassPath

   Parsing: For every individual lib:
     - add '.jar' extension if not present
     - check existence of every lib in string

   Return - lib string (possibly modified), or
          - empty string to indicate any lib not found
*/

libpath: procedure

parse arg libs .                                /* one parameter expected */
libpath = ""                                    /* initial return string */
do while length(libs) > 0                       /* still lib in string */
  parse value libs with lib ";" libs            /* extract first lib */
  off = lastpos(".jar",lib)                     /* search '.jar' */
  if off = 0 then do                            /* missing */
    lib = lib".jar"                             /* add '.jar' extension */
  end
  path = stream(lib, 'c', 'query exists')       /* check and add path */
  if path = "" then do                          /* not found */
    say "Library" lib "not found"
    return ""                                   /* empty: signal problem! */
  end
  else do                                       /* lib found */
    libpath = libpath||lib';'                   /* add to returnstring */
  end
end

return libpath                                  /* return lib spec */
