/* ================================================================

   Start Java application after optionally extending CLASSPATH.

   Author: Rob Hamerling (email r.hamerling@planet.nl)

   Version: 0.8

   Made from version 0.7 of JmriDemo.CMD
   
   To be started in directory with application, unless
   application is in a CLASSPATH directory.

   Expects max 2 parameters:
     First:  Name of application.
             Default: see variable 'DefaultApplication' below.
     Second: Library (jar-file) needed by application.
             Multiple libraries may be specified as a single string
             with a semicolon between individual libraries.
             Default: see variable 'DefaultClassPath' below

   Parameters are checked and modified when appropriate as follows:
   - '.class' extension removed from application specification
   - explicit path removed from application specification
   - '.jar' added to library specifications

   Notes:
   - Libraries will be prepended to current CLASSPATH.

   History (most recent at top):
   - loads crimson.jar instead of xerces.jar
   - Made from version 0.7 of JmriDemo.CMD
  
   ================================================================ */

DefaultApplication = "apps.DecoderPro.DecoderPro"         /* default application */

j1 = "jmri.jar;"                        /* JMRI files */
j2 = "lib\log4j.jar;lib\collections.jar;"
j3 = "lib\xerces.jar;lib\jdom-jdk11.jar;"
DefaultClassPath = j1||j2||j3           /* Default classpath prepend */

parse arg app lib .                     /* scan commandline (max 2 parms) */

if app = "" then do                     /* no application specified */
  app = DefaultApplication              /* take default application */
end
else do                                 /* application is specified */
  app = filespec("Name", app)           /* strip possible path spec */
  parse upper var app uapp              /* to uppercase for compare */
  offset = lastpos(".CLASS", uapp)      /* search for '.class' extension */
  if offset > 0 then do                 /* when found */
    app = substr(app,1,offset-1)        /* strip it (leaves name only) */
  end
end

if lib = "" then do                     /* no classlib(s) spec'd */
  lib = DefaultClassPath                /* take default */
end
lib = ParseClassLib(lib)                /* parse (/modify) library string */
if lib = "Error!" then do               /* 'Error!' result */
  return 2                              /* lib not found */
end
if lib <> "" then do                    /* non-empty string returned */
  ccp = value(CLASSPATH,,"OS2ENVIRONMENT")   /* obtain current classpath */
  ccp = value(CLASSPATH,lib||ccp,"OS2ENVIRONMENT")  /* set new classpath */
  say "New classpath:" value(CLASSPATH,,"OS2ENVIRONMENT")  /* inform user */
end

say "Starting" app ". . ."              /* inform user */
'@java' app                             /* start java application */

return rc                               /* result of java command */


/* ======== end of mainline procedure =============================== */



/* -------------------------------------------------------------
   Parse ClassPath prepend string.

   Input:   Library string to be prepended to current CLASSPATH

   Parsing: For every individual library in library-string:
            - add '.jar' extension if not present
            - check existence of jar-file

   Returns: - String of prepended libraries.
              Libraries which are already in current CLASSPATH will be
              skipped, so a shorter or even empty string may be returned.
            - String "Error!" when any library not found.
   ------------------------------------------------------------- */

ParseClassLib: procedure

parse arg libstring .                           /* single string expected */
prepend = ""                                    /* empty prepend */
do while length(libstring) > 0                  /* still lib in string */
  parse value libstring with lib ';' libstring  /* separate first lib */
  parse upper var lib ulib                      /* to uppercase for compare */
  if lastpos(".JAR",ulib) = 0 then do           /* no '.jar' extension */
    lib = lib".jar"                             /* add it */
  end
  path = stream(lib, 'c', "query exists")       /* determine existence */
  if path = "" then do                          /* library file not found */
    say "Library" lib "not found"               /* inform user */
    return "Error!"                             /* return string 'Error!' */
  end
  ccp = value(CLASSPATH,,"OS2ENVIRONMENT")      /* obtain current classpath */
  if pos(lib,ccp) = 0 then do                   /* when not already in */
    prepend = prepend||lib';'                   /* add lib to prepend */
  end
end

return prepend                                  /* return prepend string */

/* ------------ end of procedure ParseClassLib ------------------------ */
