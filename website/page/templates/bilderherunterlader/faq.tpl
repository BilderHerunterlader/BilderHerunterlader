<pre>
<b>Will further hosts be supported</b>
Yes, when it's technically possible. Send me an e-mail if you know a host, that isn't supported yet. And send also links with images hosted on that host.

<b>The program caused an error. What should i do?</b>
Send me an e-mail with an exact description of the error.
Send me also the logfile (BH.log) as attechment. The logfile is stored in your profile folder in subfolder .BH.

<b>Where is my profile folder?</b>
Under Windows you can find the folder, when you type %userprofile%\.BH in the Explorer.
Under Linux you can find the folder over ~/.BH

<b>For what all are these files in the profile folder?</b>
<i>BH.lock</i> -> That file is locked by the program to make sure only one instance of the program is running.
<i>BH.log</i> -> Logfile in which error messages are stored.
For every day one logfile is created. (e.g. BH.log.2007-05-20)
<i>BH-Downloads.db4o</i> -> Database in which the download-queue is stored.
<i>BH-Keywords.db4o</i> -> Database in which the keywords are stored.
<i>BH-Logs.txt</i> -> That file contains information about downloaded images.
<i>settings.xml</i> -> That file contains the settings of the program.
<i>port.txt</i> -> That file contains the port which is used by the program (needed for Firefox/Seamonkey-Extension, IE-Plugin)

<b>A image is downloaded, but the progressbar ins't moving. Why?</b>
Some server doesn't send the size of the image. Without the size the progressbar can't be shown correctly. So the progressbar isn't moving.

<b>When starting the program and JRE6 is used, a problem with the System tray arises. What should i do?</b>
The commandline argument "-noTray" is starting the program without using the system tray.

<b>Are there any other commandline arguments?</b>
Yes. Here is the full list:
-noTray -> BH will start without using SystemTray
-noDebug -> Errors were put to console and not the the debug window

And these 2 arguments are only for automating the setup creaton:
-version -> Prints the Version of BH (e.g. 1.2.0)
-versionNumber -> Prints the VersionNumber of BH (e.g. 120)

<b>What's the meaning of BilderHerunterlader?</b>
BilderHerunterlader is german and translated in english it means image downloader.
</pre>