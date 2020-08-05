function errorMsg() {
	alert("Mozilla oder Netscape ab v6 ist notwendig,\num das Plugin zu installieren.");
}

function addEngine(name,ext,cat) {
	if ((typeof window.sidebar == "object") && (typeof window.sidebar.addSearchEngine == "function")) {
		window.sidebar.addSearchEngine(
			"http://searchplugin.erweiterungen.de/"+name+".src",
			"http://searchplugin.erweiterungen.de/"+name+"."+ext,
			name,
			cat );
	} else {
		errorMsg();
	}
}

function install(file, extName) {

	var params = new Array();
		params[extName] = { 
			URL: file,
			IconURL: 'http://www.erweiterungen.de/img/icons/xpinstallItemDE32.png',
			toString: function () { return this.URL; }
		};
	
	InstallTrigger.install(params);

	return false;
}