# TackProfiler

Hacks up TickProfiler to use a local copy of its LibLoader-fetched dependencies, instead of fetching them from the internet.

Works on 1.12 and should work on earlier versions. 1.7 versions don't use LibLoader.

## Problem statement

TickProfiler uses a library called [LibLoader](https://github.com/MinimallyCorrect/LibLoader) which attempts to fetch extra dependencies from the Internet; specifically, from JCenter.

There are a number of reasons this can fail:

* You're offline.
* You're on a version of Java which does not have the correct root certificates to connect to JCenter.
  * this manifests like `javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target`
  * see [this redditpost](https://old.reddit.com/r/feedthebeast/comments/1clbv19/tickprofiler_a_very_old_mod_needs_to_be_removed/).
* If you're reading this from the future: maybe JCenter finally died!

Solutions to the problem include:

* Go online when you first start the game. (LibLoader won't try to download if the files already exist.)
* Update past Java 8u91.
  * This version [added the relevant DigiCert root certificates](https://www.oracle.com/java/technologies/javase/8u91-relnotes.html) which JCenter uses and you should be able to connect.
* Install TackProfiler.

## Usage

Add `# #TackProfiler.jar` to your mods folder. The weird filename is important; it must sort alphabetically before LibLoader.

TackProfiler won't appear in the mods list in-game. This is normal. If you want to see if it's loading, check the log for "`TackProfiler initializing`".

## Method

Probably more hacky than it needs to be.

At build time, the Gradle `downloadLibLoaderLibs` task scans `./ref` for LibLoader-using mods, and then emulates LibLoader's behavior a little, fetching all libraries that need to be downloaded from the Internet and putting them in `src/main/resources/cached-libloader-libs/`. I ~~dumped a bunch of LibLoader-using mods into the `ref` folder~~ (actually all the released versions use the same 6 libraries). Of course this only works because the URLs still resolve. This task mainly exists for reproducibility so you can verify where the random jars come from.

These libraries then get included into the TackProfiler jar.

At runtime, I first hack up `LaunchClassLoader.transformerExceptions` so that I can transform `LibLoaderChained$Library`, then add a hook: if a library thinks it needs to be downloaded but TackProfiler has a local copy of that library, it'll use some builtin-to-LibLoader functionality to load my copy instead of fetching it from the internet.

## Testing in development

Sorta involved, because TickProfiler won't function without a `-deobf` jar in-dev, but the deobf jar doesn't have any of the libloader manifest keys in it

* Download tickprofiler off of curse or whatever, put it in `./run/mods`
* Download a `-deobf` jar fetched from [the jenkins](https://jenkins.nallar.me/job/TickProfiler/). 
* Copy `LibLoader.jar` from the regular, non-deobf tickprofiler jar and paste it into `./run/mods`
  * this is because the deobf jar doesn't contain LibLoader
* Copy `META-INF/MANIFEST.MF` from the regular jar and paste it over `MANIFEST.MF` in the deobf jar
  * this is because the deobf jar doesn't mention any libloader dependencies in its manifest

You don't need to do this to *test* TackProfiler; it's just that TickProfiler's patches don't function properly unless you have the deobf jar