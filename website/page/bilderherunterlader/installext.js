function bhInstExt(url, extensionName) {
	var params = new Array();
	params[extensionName] = {
			URL: url
	};
	InstallTrigger.install(params);
	return false;
}
