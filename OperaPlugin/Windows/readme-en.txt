Look first if in the folder %appdata%\Opera\Opera\profile\menu\ the file standard_menu.ini exists. If not, then copy the file from this Zip-File to the folder.

Now, in the standard_menu.ini you have to add following line to the section [Document Popup Menu]:
Item, "Download files with BH" = Execute program, "C:\BHTransmit.exe","-u %u"

"C:\BHTransmit.exe" needs to be changed to the folder where you extracted the BHTransmit.exe

Notes:
- The standard_menu.ini in this archive does not contain the line described above!

- You need to activate cookies in the settings of BilderHerunterlader, otherwise the download from websites, that require a login, will not work.
