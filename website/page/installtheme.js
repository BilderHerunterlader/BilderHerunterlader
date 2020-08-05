function installTheme(file, themeName) {
	if (!(InstallTrigger.installChrome(InstallTrigger.SKIN, file, themeName))) {
		alert('Could not install the extension, make sure you allow this site/domain to install this extension.');
	}

	return false;
}
