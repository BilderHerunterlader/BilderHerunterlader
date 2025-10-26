<pre>
{t}BilderHerunterlader can also be used from an USB-Stick. But an installed JRE on the computers or a portable Java Runtime like jPortable is still required.

To use BH from an USB-Stick, you have to create a textfile in the programfolder. The name of that textfile has to be: directories.properties
In this file you can define the directories in which the databases, settings, logs and downloads are saved. If you wouldn't do that, then this data would be saved on the userprofile, but i think you want to have this data also an on the USB-Stick. So you need to create that textfile.
There should be already a "directories.properties.example" file in the program folder, which you can just rename to "directories.properties" and edit it.

The directories.properties file must look like this:{/t}
</pre>
<div class="code">
<pre>
DatabasePath=data/
SettingsPath=data/
DownloadLogPath=data/
LogsPath=data/
DownloadPath=downloads/
</pre>
</div>
<pre>
{t}In the example above the databases, settings and logs would be saved in the programmfolder in the subfolder "data". And downloaded files in subfolder "downloads".

You can leave out some paths, then that data would be saved in the userprofile as usual.
In the example above all posible paths are listed. There are only this 5 paths, which can be configured.

If you want use the program from an USB-Stick, you have to define the directories relative to the programfolder.
But it would also be possible to define the directories by absolute paths.{/t}
</pre>
