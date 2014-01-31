z-stack
=======

backend stack maintained by landz itself

## Get started

### Current Status
Now Landz are in its raw Java project form. When Java 8 GA, Landz will publish a simple module systen and experimental module repository.

As raw Java projects, it means you just use Landz as a library. The binaries are temporarily not provided. You just simply rely on the IDE or plain javac(in fact, javac is etremely easy to use from Java 6-7 to now Java 8) for compliation/package. As a Java developer coming here, it is assumed you know how to do this.

The repo keeps some IDEA configurations for users or contributors' quick picking-up/hacking. But these files is based on the IDEA 13, and can be updated only by the Landz team. [Download latest IDEA 13 here](http://confluence.jetbrains.com/display/IDEADEV/IDEA+13+EAP).
(Note: IMHO, according to my understanding to the current status of IDE support, I only recommend the IDEA 13.)

All required 3rd library are inlcuded in this git repo's Landz modules(a.k.a. projects).

### Build Requirememt
* You only need Java 8 EA release, [download here](https://jdk8.java.net/download.html). 

### Run Notes
* landz.znr.linux.x64 and landz.net.base, landz.net.http modules on top of landz.znr.linux.x64 only supports linux x86-64 arch. landz.znr.linux.x64 module is just for interacting with linux natives. So, I guess you are not very disappointed about this. In the future, Landz will extract a common API for Windows and MacOS. But...
* com.github.jnr.udis86 module requires the linux [udis86](http://udis86.sourceforge.net/) binary. It is used to debug the jnr/jffi generated codes. But, it is trivial to simply comment it out. It will be made runtime optional in the near future. You can consult google for how to get it installed on your distribution. (For Arch, there is an "udis86-git" in AUR.)

### Resources

* Discuss Landz with at [Landz's group](http://groups.google.com/d/forum/landz). I like the mail list way, but you can also choose the web UI.
* Other channels, like stackoverflow, will be possible also.
* [Landz page](http://landz.github.io/).

### Licenses
Landz kernel and its own stack are released under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

Runtime jffi/jnr and test-time 3rd dependencies are seen in /licenses directory.



