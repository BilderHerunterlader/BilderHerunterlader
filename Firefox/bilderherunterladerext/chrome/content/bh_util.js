function showBHError(msg) {
	alert("BilderHerunterlader: " + msg);
	logBHScriptError(msg, 0);
}

function logBHScriptError(aMessage, aFlags) {
	//aFlags: 0 = Error, 1 = Warning
	if (typeof aFlags === "undefined" || aFlags === null) {
		aFlags = 0;
	}

	try {
		var scriptError = Components.classes["@mozilla.org/scripterror;1"].createInstance(Components.interfaces.nsIScriptError);
		scriptError.init("BilderHerunterlader: " + aMessage, null, null, null, null, aFlags, null);
	} catch (e) {
		alert(e);
	}
}

function isWindowsOS() {
	// Returns "WINNT" on Windows Vista, XP, 2000, and NT systems;  
	// "Linux" on GNU/Linux; and "Darwin" on Mac OS X.  
	var osName = Components.classes["@mozilla.org/xre/app-info;1"].getService(Components.interfaces.nsIXULRuntime).OS;
	if (osName == "WINNT") {
		return true;
	}
	return false;
}

function isBHTransmitMode() {
	var prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	var retval = prefManager.getBoolPref("extensions.bilderherunterlader.bhtransmitmode");
	return retval;
}

function isBHFrames() {
	var prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	var retval = prefManager.getBoolPref("extensions.bilderherunterlader.frames");
	return retval;
}

function isBHIFrames() {
	var prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	var retval = prefManager.getBoolPref("extensions.bilderherunterlader.iframes");
	return retval;
}

function isBHDataUrlAttributes() {
	var prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	var retval = prefManager.getBoolPref("extensions.bilderherunterlader.dataurlattributes");
	return retval;
}

function isPlainText() {
	var contentType = window.content.document.contentType;
	if (contentType.toUpperCase() === "text/plain".toUpperCase()) {
		return true;
	}
	return false;
}
