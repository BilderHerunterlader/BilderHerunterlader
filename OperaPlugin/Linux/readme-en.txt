Look first if the folder ~/.opera/menu/ exists, if not create it. Then look if in the folder ~/.opera/menu/ the file standard_menu.ini exists. If not, then copy the file from this Zip-File to the folder.

Now, in the standard_menu.ini you have to add following line to the section [Document Popup Menu]:
Item, "Download files with BH" = Execute program, "/home/username/BHTransmit","-u %u"

"/home/username/BHTransmit" needs to be changed to the folder where you extracted BHTransmit. And you have to take the absolute path!

Notes:
- The standard_menu.ini in this archive does not contain the line described above!

- You need to activate cookies in the settings of BilderHerunterlader, otherwise the download from websites, that require a login, will not work.

- You may have to give BHTransmit execute-rights.
