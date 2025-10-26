<div class="features">
<b>{t}Download-Selection{/t}</b>
<ul>
	<li>{t}Selection of files to download{/t}</li>
	<li>{t}Files which have already been downloaded are deselected automatically{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Files which have already been downloaded are marked blue, others red{/t}</li>
			<li>{t}This is only working if in the settings under download "Save logs" is enabled{/t}</li>
		</ul>
	</li>
	<li>{t}Manual selection of the download directory (for all files){/t}</li>
	<li>{t}Automatic selection of the download directory based on keywords{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Can be activated by enabling "Automatic recognition"{/t}</li>
			<li>{t}Recognition is done by title or filenames{/t}</li>
			<li>{t}The directory path is marked green, if a keyword was found{/t}</li>
		</ul>
	</li>
	<li>{t}Manual selection of the download-directory by selecting a keyword or directory-selection and change of the filename{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Right click with the mouse on a file and to open the context-menu{/t}</li>
		</ul>
	</li>
	<li>{t}Buttons for fast select or deselect of files{/t}</li>
	<li>{t}Button for adding the title to the download-directory-path{/t}</li>
	<li>{t}Textfield and a button to add the content of the textfield to the download-directory-path{/t}</li>
	<li>{t}Button for the creation of new keywords{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}If there is something selected in the title-textfield, the selection is taken as name and directory for the keyword, but that can still be changed{/t}</li>
		</ul>
	</li>
	<li>{t}Filenames in the download-selection could look ugly, but shortly before the download begins they are mostly changed to a nice one. The reason for that is that for some file-hosts the possibility to retrieve the original filename is only shortly before the download can begin.{/t}</li>
</ul>
</div>

<div class="features">
<b>{t}Download-Queue{/t}</b>
<ul>
	<li>{t}Stop-Button{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}One click will stop files that are waiting for a free slot immediatly, but running downloads will be completed.{/t}</li>
			<li>{t}If you click twice also running downloads are stopped immediatly.{/t}</li>
		</ul>
	</li>
	<li>{t}Simultaneous Downloads{/t}</li>
	<li>Context-Menu for every file with useful actions</li>
	<li>Import</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Links from HTML-file{/t}</li>
			<li>{t}Links from text-file{/t}</li>
			<li>{t}Links from a dialog-window{/t}</li>
		</ul>
	</li>
	<li>{t}Import / Export of Downloads{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Downloads can be exported to a text-file and imported again{/t}</li>
		</ul>
	</li>
	<li>{t}Sort of files on the harddisk by using keywords{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Files are treated as normal downloads. But instead of downloading, the files are just moved to the corresponding directory on working of the queue.{/t}</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<b>{t}Download-Log{/t}</b>
<ul>
	<li>{t}Logging of downloads (Date and time, Link, Directory and filename, Filesize){/t}</li>
	<li>{t}Context-Menu for every file with useful actions{/t}</li>
	<li>{t}Import / Export{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Export log to a text-file{/t}</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<b>{t}Keywords{/t}</b>
<ul>
	<li>{t}Are used to download files directly to the desired directory{/t}</li>
	<li>{t}For every keyword a title can be given and you can define one or more (key)words (seperated by ; ){/t}</li>
	<li>{t}A keyword can have an absolute or relative path (relative to the standard-download-directory){/t}</li>
	<li>{t}Context-Menu for every file with useful actions{/t}</li>
	<li>{t}Import{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Keywords from a text-file{/t}</li>
			<li class="noListStyle">
				<ul>
					<li>
					{t}The lines of the text-file must look like that:{/t}
					<p style="border: 1px solid #ff0000">
					{t}Title&nbsp;&nbsp;&nbsp;&nbsp;Keywords&nbsp;&nbsp;&nbsp;&nbsp;'Directory with absolute path'&nbsp;&nbsp;&nbsp;&nbsp;'Directory with relative path'&nbsp;&nbsp;&nbsp;&nbsp;relative path(true/false){/t}
					</p>
					{t}The individual entries must be separated by tabs.{/t}<br />
					{t}If you don't want to specify an entrie, you have to write a #.{/t}
					</li>
				</ul>
			</li>
		</ul>
	</li>
	<li>{t}Export to a text-file{/t}</li>
</ul>
</div>

<div class="features">
<b>{t}Rules{/t}</b>
<ul>
	<li>{t}With Rules support for unsupported websites / file hosts can be added{/t}</li>
	<li>{t}The creation is described here:{/t} <a href="?loc=bilderherunterlader/rules&amp;lng=<?=$lng?>"><?=$lang["rules"][$lng]?></a></li>
</ul>
</div>

<div class="features">
<b>{t}Host-Plugins{/t}</b>
<ul>
	<li>{t}With Host-Plugins support for unsupported websites / file hosts can be added{/t}</li>
	<li>{t}Host-Plugins are programmed in Java{/t}</li>
	<li>{t}The creation is described here:{/t} <a href="?loc=bilderherunterlader/rules&amp;lng=<?=$lng?>#hostplugins"><?=$lang["hostplugins"][$lng]?></a></li>
</ul>
</div>

<div class="features">
<b>{t}Settings{/t}</b>
<ul>
	<li>{t}There are different options to customize the behavior of the program for your needs{/t}</li>
	<li>{t}Some settings are described here:{/t} <a href="?loc=bilderherunterlader/tutorial&amp;lng=<?=$lng?>#settings"><?=$lang["options"][$lng]?></a></li>
</ul>
</div>

<div class="features">
<b>{t}Update-Function{/t}</b>
<ul>
	<li>{t}Update of the program{/t}</li>
	<li>{t}Update of Host-Plugins and Rules{/t}</li>
	<li>{t}Can also done automatically on the start of program, but that must be activated in the settings{/t}</li>
</ul>
</div>
