window.addEventListener("load", bhInit, false);

Components.utils.import("resource://gre/modules/devtools/Console.jsm");
Components.utils.import("resource://gre/modules/XPCOMUtils.jsm");
Components.utils.import("resource://gre/modules/Services.jsm");

function bhInit() {
	var contextMenu = document.getElementById("contentAreaContextMenu");
	contextMenu.addEventListener("popupshowing", contextMenuDisplayed, false);
}

function contextMenuDisplayed() {
	var prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
	
	var fireImages = document.getElementById("ContextMenu_BilderHerunterlader_FireImages");
	var fireLinks = document.getElementById("ContextMenu_BilderHerunterlader_FireLinks");
	var fireParsePage = document.getElementById("ContextMenu_BilderHerunterlader_FireParsePage");
	
	fireImages.hidden = !prefManager.getBoolPref("extensions.bilderherunterlader.fireImages");
	fireLinks.hidden = !prefManager.getBoolPref("extensions.bilderherunterlader.fireLinks");
	fireParsePage.hidden = !prefManager.getBoolPref("extensions.bilderherunterlader.fireParsePage");
}

function sendURLsToBH(urlssend, plainText, embeddedImages) {
	//Get the path to the port.txt file, which contains the port on which BH is listening
	var uFile = Components.classes["@mozilla.org/file/directory_service;1"].getService(Components.interfaces.nsIProperties).get("Home", Components.interfaces.nsIFile);
	uFile.append(".BH");
	uFile.append("port.txt");
	console.debug('Port file: ' + uFile.path);
	
	if ( (!uFile.exists()) || (!uFile.isReadable()) ) {
		showBHError('File not found or file not readable\nFile: ' + uFile.path + '\nPlease start BH or set permissions for the File');
		console.error('File not found or file not readable\nFile: ' + uFile.path + '\nPlease start BH or set permissions for the File');
		return;
	}
	
	//Open the port.txt and read out the port
	var istream = Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance(Components.interfaces.nsIFileInputStream);
	istream.init(uFile, 0x01, 0444, 0);
	istream.QueryInterface(Components.interfaces.nsILineInputStream);
	
	var line = {}, lines = [], hasmore;
	do {
		hasmore = istream.readLine(line);
		lines.push(line.value);
		console.debug('Port file line: ' + line.value);
	} while(hasmore);
	istream.close();
	
	var port = -1;
	var bPort = false;
	var version = "";
	if (lines.length == 1 ) {
		port = parseInt(lines[0]);
		if (port != "NaN") {
			if ( (port > 0) && (port < 65536) ) {
				bPort = true;
			}
		}
	} else if (lines.length == 2) {
		port = parseInt(lines[0]);
		if (port != "NaN") {
			if ( (port > 0) && (port < 65536) ) {
				bPort = true;
			}
		}
		version = lines[1];
	}
	console.info('Port: ' + port + ', bPort: ' + bPort + ', BH-Version: ' + version);
	if (bPort === false) {
		showBHError('Port not found\nFile: ' + uFile.path + '\nPlease start BH or check the settings of your firewall and allow java');
		console.error('Port not found\nFile: ' + uFile.path + '\nPlease start BH or check the settings of your firewall and allow java');
		return;
	}
	
	//Open Socket-Connection to BH
	var transportService;
	var transport;
	var host = "localhost";
	var outstream;
	var os;
	
	try {
		console.info('Opening connection to BH. Host: ' + host + ', Port: ' + port);
		transportService = Components.classes["@mozilla.org/network/socket-transport-service;1"].getService(Components.interfaces.nsISocketTransportService);
		transport = transportService.createTransport(null,0,host,port,null);
		transport.setEventSink({
			onTransportStatus: function(transport, status, progress, progressMax) {
				if (status == 0x804b0003) {
					console.debug('BH Socket Status: STATUS_RESOLVING ' + status);
				} else if (status == 0x804b000b) {
					console.debug('BH Socket Status: STATUS_RESOLVED ' + status);
				} else if (status == 0x804b0007) {
					console.debug('BH Socket Status: STATUS_CONNECTING_TO ' + status);
				} else if (status == 0x804b0004) {
					console.debug('BH Socket Status: STATUS_CONNECTED_TO ' + status);
				} else if (status == 0x804b0005) {
					console.debug('BH Socket Status: STATUS_SENDING_TO ' + status);
				} else if (status == 0x804b000a) {
					console.debug('BH Socket Status: STATUS_WAITING_FOR ' + status);
				} else if (status == 0x804b0006) {
					console.debug('BH Socket Status: STATUS_RECEIVING_FROM ' + status);
				} else {
					console.debug('BH Socket Status: Unknown Status ' + status);
				}
			},
			QueryInterface: XPCOMUtils.generateQI([Components.interfaces.nsITransportEventSync])
		}, Services.tm.currentThread);
		//1 = OPEN_BLOCKING
		outstream = transport.openOutputStream(1,0,0);
		var charset = "UTF-8";
		os = Components.classes["@mozilla.org/intl/converter-output-stream;1"].createInstance(Components.interfaces.nsIConverterOutputStream);
		os.init(outstream, charset, 4096, 0x0000);
	} catch (e) {
		showBHError('Connection could not be established (' + host + ':' + port + ')\nPlease start BH or check the settings of your firewall and allow java. Exception: ' + e);
		console.error('Connection could not be established (' + host + ':' + port + ')\nPlease start BH or check the settings of your firewall and allow java. Exception: ' + e);
		return;
	}
	
	//Prepair Header Data
	var prefix = "BH{af2f0750-c598-4826-8e5f-bb98aab519a5}\n";
	var loc = window.content.document.location + "\n";
	var title = getPageTitle(loc) + "\n";
	
	var data = prefix + title + loc;
	
	//Send the URLs to BH
	try {
		var dataStart = "FULLLISTTHUMBS\nSOF\n";
		if (plainText == true) {
			dataStart = "SOF\n";
		}
		var dataEnd = "EOF\n";
		
		os.writeString(dataStart);
		console.debug('Send Data to BH:' + dataStart);
		
		if (plainText == true) {
			if (embeddedImages == true) {
				os.writeString("IMG:" + loc); //loc already has \n at the end
				console.debug('Send Data to BH:' + "IMG:" + loc);
			} else {
				os.writeString("URL:" + loc); //loc already has \n at the end
				console.debug('Send Data to BH:' + "URL:" + loc);
			}
		} else {
			os.writeString(data);
			console.debug('Send Header-Data to BH:' + data);
			
			for(var xx = 0; xx < urlssend.length; xx++) {
				var dataSend = urlssend[xx];
				os.writeString(dataSend);
				//console.debug('Send Data to BH:' + dataSend);
			}
			console.debug('Sent ' + urlssend.length + ' URLs to BH');
		}
		
		os.writeString(dataEnd);
		console.debug('Send Data to BH:' + dataEnd);
		os.close();
		outstream.close();
		console.info('Data successfully sent to BH');
	} catch (e) {
		showBHError('Links could not be transfered\n' + e);
		console.error('Links could not be transfered\n' + e);
		try {
			outstream.close();
		} catch (e) {
		}
	}
}

function launchBHTransmit(embeddedImages) {
	var programfilename = "BHTransmit";
	if (isWindowsOS() === true) {
		programfilename = "BHTransmit.exe";
	}
	
	var MY_ID = "{af2f0750-c598-4826-8e5f-bb98aab519a5}";
	var directoryService = Components.classes["@mozilla.org/file/directory_service;1"].getService(Components.interfaces.nsIProperties);
	var bhtransmitpath = directoryService.get("ProfD", Components.interfaces.nsIFile);
	bhtransmitpath.append("extensions");
	bhtransmitpath.append(MY_ID);
	bhtransmitpath.append(programfilename);
	
	var bhtransmit = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
	bhtransmit.initWithPath(bhtransmitpath.path);
	
	var process = Components.classes["@mozilla.org/process/util;1"].createInstance(Components.interfaces.nsIProcess);
	process.init(bhtransmit);
	
	var args = ["-u", window.content.document.location];
	if (embeddedImages === true) {
		args = ["-i", window.content.document.location];
	}
	
	console.info('Executing BHTransmit. Path: ' + bhtransmitpath.path + ', Arguments: ' + args.join(', '));
	process.run(false, args, args.length);
}

function getLinksArray(links) {
	var urlssend = new Array(links.length);
	
	for(var i = 0; i < links.length; i++) {
		var li = links[i];
		var linkURL = li.href;
		var cx = 0;
		var tx = "";
		for(var x = 0; x < li.childNodes.length; x++) {
			if (li.childNodes[x].nodeName == "IMG") {
				tx = li.childNodes[x].src;
				cx++;
            }
        }
		if (cx == 1) {
			linkURL += "\t" + tx;
		} else {
			linkURL += "\t";
		}
		linkURL += "\n";
		urlssend[i] = linkURL;
	}
	return urlssend;
}

function getURLsFromAttributes(elements, attributes, excludes) {
	var urlssend = new Array();
	var currentIndex = 0;
	
	var dataUrl = "";
	for (var i = 0; i < elements.length; i++) {
		if (excludes.indexOf(elements[i].tagName.toUpperCase()) > -1) {
			continue;
		}
		
		for (var a = 0; a < attributes.length; a++) {
			dataUrl = elements[i].getAttribute(attributes[a]);
			if ((typeof dataUrl == 'string') && (dataUrl.length > 0)) {
				urlssend[currentIndex] = dataUrl;
				urlssend[currentIndex] += "\t\n";
				currentIndex++;
			}
		}
	}
	return urlssend;
}

function fireDownloadLinks() {
	if (isBHTransmitMode() === true) {
		launchBHTransmit(false);
		return;
	}
	
	var urlssend;
	
	if (isPlainText() == true) {
		sendURLsToBH(urlssend, true, false);
		return;
	}
	
	var links = window.content.document.links;
	
	urlssend = getLinksArray(links);
	
	if (isBHFrames() === true) {
		//Get Links from Frames if available
		var frames = window.content.document.getElementsByTagName('frame');
		if (frames.length > 0) {
			for(var i = 0; i < frames.length; i++) {
				var frameDoc = frames[i].contentDocument;
				var frameLinks = frameDoc.links;
				var urlssendFrame = getLinksArray(frameLinks);
				urlssend = urlssend.concat(urlssendFrame);
			}
		}
	}
	
	if (isBHIFrames() === true) {
		//Get Links from iFrames if available
		var iframes = window.content.document.getElementsByTagName('iframe');
		if (iframes.length > 0) {
			for(var i = 0; i < iframes.length; i++) {
				var iframeDoc = iframes[i].contentDocument;
				var iframeLinks = iframeDoc.links;
				var urlssendiFrame = getLinksArray(iframeLinks);
				urlssend = urlssend.concat(urlssendiFrame);
			}
		}
	}
	
	if (isBHDataUrlAttributes() === true) {
		var allElements = window.content.document.getElementsByTagName("*");
		var urlssendDataUrlAttributes = getURLsFromAttributes(allElements, ["data-url", "data-imgsrc", "previewurl", "gi-preview-image"], ['IMG']);
		urlssend = urlssend.concat(urlssendDataUrlAttributes);
	}
	
	sendURLsToBH(urlssend, false, false);
}

function getImagesArray(images) {
	var urlssend = new Array(images.length);
	
	for(var i = 0; i < images.length; i++) {
		var li = images[i];
		var imageURL = li.src;
		imageURL += "\t\n";
		urlssend[i] = imageURL;
	}
	return urlssend;
}

function getVideosArray(videos) {
	var urlssend = [];
	var index = 0;
	for(var i = 0; i < videos.length; i++) {
		var li = videos[i];
		if (li.hasAttribute('src')) {
			var videoURL = li.src;
			videoURL += "\t\n";
			urlssend[index] = videoURL;
			index++;
		}
		var videoSources = li.getElementsByTagName('source');
		for(var x = 0; x < videoSources.length; x++) {
			var lx = videoSources[x];
			if (lx.hasAttribute('src')) {
				var videoURL = lx.src;
				videoURL += "\t\n";
				urlssend[index] = videoURL;
				index++;
			}
		}
	}
	return urlssend;
}

function fireDownloadImages() {
	if (isBHTransmitMode() === true) {
		launchBHTransmit(true);
		return;
	}
	
	var urlssend;
	
	if (isPlainText() == true) {
		sendURLsToBH(urlssend, true, true);
		return;
	}
	
	var images = window.content.document.images;
	urlssend = getImagesArray(images);
	
	var videos = window.content.document.getElementsByTagName('video');
	urlssend = urlssend.concat(getVideosArray(videos));
	
	if (isBHFrames() === true) {
		//Get Images from Frames if available
		var frames = window.content.document.getElementsByTagName('frame');
		if (frames.length > 0) {
			for(var i = 0; i < frames.length; i++) {
				var frameDoc = frames[i].contentDocument;
				
				var frameImages = frameDoc.images;
				var urlssendFrameImages = getImagesArray(frameImages);
				urlssend = urlssend.concat(urlssendFrameImages);
				
				var frameVideos = frameDoc.getElementsByTagName('video');
				var urlssendFrameVideos = getVideosArray(frameVideos);
				urlssend = urlssend.concat(urlssendFrameVideos);
			}
		}
	}
	
	if (isBHIFrames() === true) {
		//Get Images from iFrames if available
		var iframes = window.content.document.getElementsByTagName('iframe');
		if (iframes.length > 0) {
			for(var i = 0; i < iframes.length; i++) {
				var iframeDoc = iframes[i].contentDocument;
				
				var frameImages = iframeDoc.images;
				var urlssendiFrameImages = getImagesArray(frameImages);
				urlssend = urlssend.concat(urlssendiFrameImages);
				
				var frameVideos = iframeDoc.getElementsByTagName('video');
				var urlssendiFrameVideos = getVideosArray(frameVideos);
				urlssend = urlssend.concat(urlssendiFrameVideos);
			}
		}
	}
	
	if (isBHDataUrlAttributes() === true) {
		var imgElements = window.content.document.getElementsByTagName("img");
		var urlssendDataUrlAttributes = getURLsFromAttributes(imgElements, ["data-url", "data-imgsrc", "previewurl", "gi-preview-image"], []);
		urlssend = urlssend.concat(urlssendDataUrlAttributes);
	}
	
	sendURLsToBH(urlssend, false, true);
}

function fireParsePage() {
	if (isBHTransmitMode() === true) {
		launchBHTransmit(false);
		return;
	}
	
	var urlssend;
	sendURLsToBH(urlssend, true, false);
}
