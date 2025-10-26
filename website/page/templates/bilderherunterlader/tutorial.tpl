<h1>Usage</h1>
<div class="features">
<ul>
	<li>{t}Download Java and install it, if not already done{/t}</li>
	<li>{t}Download BilderHerunterlader and install it{/t}</li>
	<li>{t}Download and install the extension or plugin for your browser, if available.{/t}</li>
	<li>{t}Start BilderHerunterlader{/t}</li>
	<li>{t}On the first start the default settings are used and you should change those to your needs.{/t}<br />
	{t}The most important options are:{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}Connection{/t} -> {t}Direct Connection / Proxy: If you use a proxy, you must define that, otherwise the program is not working.{/t}</li>
			<li>{t}Folders{/t} -> {t}Standard Savepath: Here you must choose a folder, in which you want to download the files.{/t}</li>
			<li>{t}Other{/t} -> {t}Updates: Many hosts change their websites from time to time, so that the download with BilderHerunterlader is not working anymore. To fix that, there is a program internal Update-Function which updates BilderHerunterlader and the Rules and Host-Plugins. If the option is activated, the program checks on every start if there are upates. If you don't want that, you can still check for updates manually, in order to do that go to Info -> Update.{/t}</li>
		</ul>
	</li>
	<li>{t}Open a website which contains links to Images / Files in your browser{/t}</li>
	<li class="noListStyle">
		<ul>
			<li>{t}In PaleMoon: Rightclick -> Download files with BH{/t}</li>
			<li>{t}In Firefox: Rightclick -> BilderHerunterlader -> Download files with BH{/t}</li>
			<li>{t}In Opera: Rightclick -> Download files with BH{/t}</li>
			<li>{t}In the Internet Explorer: Rightclick -> Download files with BH{/t}</li>
			<li>{t}Alternative: Copy the URL of the website into the clipboard (ctrl + c). In order make it working that way, you need to enable the "Clipboard - Check for particular links" option in the settings.{/t}<br/>
			{t}If nothing happens, click on "Check Clipboard now" on the System-Tray-Icon-Menu or in the menubar.{/t}</li>
			<li>{t}Another alternative: Select the images / links in the browser and drag and drop the selection with the mouse to the program-window.{/t}</li>
		</ul>
	</li>
	<li>{t}Now a new window opens, which shows the available files{/t}</li>
	<li>{t}Here you can change the download-directory or let it changed automatically by keywords. You can also select which files are downloaded and which not. And you can define some other things.{/t}<br />
	{t}For a detailed description of the possibilities you should read the feature-page{/t}: <a href="?loc=bilderherunterlader/features&amp;lng={$pageLanguage}">{t}Features{/t}</a></li>
	<li>{t}Now click on OK and the files are added to the download-queue{/t}</li>
	<li>{t}Now click on Start on the download-queue and the files are downloading{/t}</li>
</ul>
</div>
<br />
<div class="features">
<h2>{t}Host-Plugins{/t}</h2>
<ul>
	<li>{t}Some Host-Plugins have own settings{/t}</li>
	<li>{t}Those are not in the settings dialog, but under the Host-Plugins tab. In the list there is a "Settings" button if the Host-Plugin has settings.{/t}</li>
	<li>HostDefaultFiles</li>
	<li class="noListStyle">
		<ul>
			<li>{t}This Host-Plugin is not for a specific hoster, but is used to detect any links.{/t}</li>
			<li>{t}In the settings it can be chosen, which type of files to detect. It can also be configured to accept any kind of links{/t}</li>
			<li>{t}There is one more way to define more links to be detected by this Host-Plugin. It is described under "Directly linked images" on this page:{/t} <a href="?loc=bilderherunterlader/rules&amp;lng={$pageLanguage}#DirectLinkedImages">{t}Rules{/t}</a></li>
		</ul>
	</li>
</ul>
</div>