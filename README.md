Raccoon - Google Play Desktop client.
-------------------------------------

Allows you to download Android APK files to your desktop PC and archive them 
there. Reasons for using this app:

* You want to check out apps anonymously without Google knowing you installed 
  them (e.g. because your better half is not suppose to spot them in your 
  account or because you don't want app suggestions based on your test run).
* You want to install an app on a secondary device which does not have access
  to Google Play or is not officially supported by the app in question.
* You want to have the option of going back to a previous version in case an 
  update causes trouble for you.
* You don't have access to unlimited data plan and want to conserve bandwidth
  by keeping a local cache (from which to distribute the apps to all the 
  devices in your household).
* You don't have your device at hand, but want to check the permissions of
  an app from your desktop PC.


Usage
-----

When starting for the first time, Raccoon will ask you to create a new archive.
The archive is basically your download folder (several can be created if 
desired). Archives store downloaded apps and user credentials. You will need
a valid Google Play account and (optionally) an Android ID. The account can be 
your regular one, but doesn't have to be. If you use your regular account, you
should also use an Android ID from one of your devices. Not providing an ID will
result in Raccoon creating one and linking it up with a pseudo device.

Building
--------

You could either use gradle or simply run "make". Previously this repo does not have a build script.
I ported the build script from Raccoon 4 repo, and added a Makefile.

Changes
-------

A few changes were made to customize this version of Raccoon and to support out work at Carnegie Mellon
University. We added an optional switch, "-g", to let users choose to skip OBB files while obtaining
the APK.
