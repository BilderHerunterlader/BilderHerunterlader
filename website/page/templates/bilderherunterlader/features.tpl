<div class="features">
<b>Download-Selection</b>
<ul>
	<li>Selection of files to download</li>
	<li>Files which have already been downloaded are deselected automatically</li>
	<li class="noListStyle">
		<ul>
			<li>Files which have already been downloaded are marked blue, others red</li>
			<li>This is only working if in the settings under download "Save logs" is enabled</li>
		</ul>
	</li>
	<li>Manual selection of the download directory (for all files)</li>
	<li>Automatic selection of the download directory based on keywords</li>
	<li class="noListStyle">
		<ul>
			<li>Can be activated by enabling "Automatic recognition"</li>
			<li>Recognition is done by title or filenames</li>
			<li>The directory path is marked green, if a keyword was found</li>
		</ul>
	</li>
	<li>Manual selection of the download-directory by selecting a keyword or directory-selection and change of the filename</li>
	<li class="noListStyle">
		<ul>
			<li>Click with the mouse on a file and rightclick to open the context-menu</li>
		</ul>
	</li>
	<li>Buttons for fast select or deselect of files</li>
	<li>Button for adding the title to the download-directory-path</li>
	<li>Textfield and a button to add the content of the textfield to the download-directory-path</li>
	<li>Automatical selection of the download-directory by a Irada-Biwi-File</li>
	<li class="noListStyle">
		<ul>
			<li>The file is created by the upload-program Irada</li>
			<li>The file contains the links and the corresponding directory and filename</li>
			<li>If you have a nice directory-structure and you upload all the files to a filehoster, the structure is lost for the people that are downloading the files, 
			because file-hosts only save the original filename (sometimes not even that).
			With the Irada-Biwi-File it is possible to restore the directory-structure and the original filenames.
			</li>
		</ul>
	</li>
	<li>Button for the creation of new keywords</li>
	<li class="noListStyle">
		<ul>
			<li>If there is something selected in the title-textfield, the selection is taken as name and directory for the keyword, but that can still be changed</li>
		</ul>
	</li>
	<li>Filenames in the download-selection could look ugly, but shortly before the download begins they are mostly changed to a nice one. The reason for that is that for some file-hosts the possibility to retrieve the original filename is only shortly before the download can begin.</li>
</ul>
</div>

<div class="features">
<b>Download-Queue</b>
<ul>
	<li>Stop-Button</li>
	<li class="noListStyle">
		<ul>
			<li>One click will stop files that are waiting for a free slot immediatly, but running downloads will be completed.</li>
			<li>If you click twice also running downloads are stopped immediatly.</li>
		</ul>
	</li>
	<li>Automatical work off of the downloads in the queue</li>
	<li>Simultaneous Downloads</li>
	<li>Context-Menu for every file</li>
	<li class="noListStyle">
		<ul>
			<li>Change download-directory and filename</li>
			<li>Copy link or open the link in the browser</li>
			<li>Open the referrer-link (Website, which contains the link) in the browser</li>
			<li>Activate and deactivate downloads</li>
			<li>Remove a download from the queue</li>
		</ul>
	</li>
	<li>Import</li>
	<li class="noListStyle">
		<ul>
			<li>Links from HTML-file</li>
			<li>Links from text-file</li>
			<li>Links from a dialog-window</li>
			<li>Import queue from Picnicker</li>
		</ul>
	</li>
	<li>Import / Export of Downloads</li>
	<li class="noListStyle">
		<ul>
			<li>Downloads can be exported to a text-file and imported again</li>
		</ul>
	</li>
	<li>Sort of files on the harddisk by using keywords</li>
	<li class="noListStyle">
		<ul>
			<li>Files are treated as normal downloads.
			But instead of downloading, the files are just moved to the corresponding directory on working of the queue.</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<b>Download-Log</b>
<ul>
	<li>Logging of downloads (Date and time, Link, Directory and filename, Filesize)</li>
	<li>Context-Menu for every file</li>
	<li class="noListStyle">
		<ul>
			<li>Copy link or open link in browser</li>
		</ul>
	</li>
	<li>Import / Export</li>
	<li class="noListStyle">
		<ul>
			<li>Import log from Picnicker</li>
			<li>Export log to a text-file</li>
		</ul>
	</li>
	<li>Button to delete all logs</li>
	<li>Only 100 files are displayed in the window, but over buttons you can go throught the logs</li>
</ul>
</div>

<div class="features">
<b>Keywords</b>
<ul>
	<li>Are used to download files directly to the desired directory</li>
	<li>For every keyword a title can be given and you can define one or more (key)words (seperated by ; )</li>
	<li>A keyword can have an absolute or relative path (relative to the standard-download-directory)</li>
	<li>View can be filtered</li>
	<li>After changes of keywords they must be saved by clicking on the Save-Button, otherwise the changes are lost</li>
	<li>Context-Menu</li>
	<li class="noListStyle">
		<ul>
			<li>Apply title as keyword</li>
			<li>Apply title as directory</li>
			<li>Apply title as relative directory-name</li>
			<li>Absolute directory-name as relative directory-name</li>
		</ul>
	</li>
	<li>Import</li>
	<li class="noListStyle">
		<ul>
			<li>Keywords from a text-file</li>
			<li class="noListStyle">
				<ul>
					<li>
					The lines of the text-file must look like that:
					<p style="border: 1px solid #ff0000">
					Title&nbsp;&nbsp;&nbsp;&nbsp;Keywords&nbsp;&nbsp;&nbsp;&nbsp;'Directory with absolute path'&nbsp;&nbsp;&nbsp;&nbsp;'Directory with relative path'&nbsp;&nbsp;&nbsp;&nbsp;relative path(true/false)
					</p>
					The individual entries must be separated by tabs.<br />
					If you don't want to specify an entrie, you have to write a #.
					</li>
				</ul>
			</li>
			<li>Keyword-Database of Picnicker</li>
		</ul>
	</li>
	<li>Export in a text-file</li>
</ul>
</div>

<div class="features">
<b>Rules</b>
<ul>
	<li>With Rules support for unsupported websites / file hosts can be added</li>
	<li>The creation of Rules is described here: <a href="?loc=bilderherunterlader/rules&amp;lng=<?=$lng?>"><?=$lang["rules"][$lng]?></a></li>
</ul>
</div>

<div class="features">
<b>Host-Plugins</b>
<ul>
	<li>With Host-Plugins support for unsupported websites / file hosts can be added</li>
	<li>Host-Plugins are programmed in Java</li>
	<li>The creation of Host-Plugins is described here: <a href="?loc=bilderherunterlader/rules&amp;lng=<?=$lng?>#hostplugins"><?=$lang["hostplugins"][$lng]?></a></li>
</ul>
</div>

<div class="features">
<b>Settings</b>
<ul>
	<li>There are different options to customize the behavior of the program for your needs</li>
	<li>The settings are described here: <a href="?loc=bilderherunterlader/tutorial&amp;lng=<?=$lng?>#settings"><?=$lang["options"][$lng]?></a></li>
</ul>
</div>

<div class="features">
<b>Update-Function</b>
<ul>
	<li>Update of the program</li>
	<li>Update of Host-Plugins and Rules</li>
	<li>Can also done automatically on the start of program, but that must be activated in the settings</li>
</ul>
</div>

<div class="features">
<b>Other Features</b>
<ul>
	<li>System-Tray (Only available with JRE 6 or higher)</li>
	<li>Multi-Language (At the moment only German and English)</li>
	<li>Proxy-Support</li>
</ul>
</div>

<div class="features">
<b>Known Problems</b>
<ul>
	<li>Because of a Bug in the JavaVM, the program sometimes doesn't checks the clipboard for new data. In that case you must click on the system tray menu item "Check Clipboard" to enforce clipboard checking.</li>
</ul>
</div>
