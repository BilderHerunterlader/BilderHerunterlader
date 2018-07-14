function bhFireDownloadLinks(bFrames, bIFrames, bDataURLAttributes) {
	console.log("bhFireDownloadLinks Start");

	var strPageURL = bhGetPageURL();
	console.log("Page-URL: " + strPageURL);
	var strPageTitle = bhGetPageTitle();
	console.log("Page-Title: " + strPageTitle);

	var bhHeaderInfo = new BHHeaderInfo(strPageURL, strPageTitle);
	console.log("BHHeaderInfo: " + bhHeaderInfo.getLogString());
	
	bhPreparePageTitle(bhHeaderInfo);
	console.log("Prepared BHHeaderInfo: " + bhHeaderInfo.getLogString());
	
	var urlsToSend = bhGetLinksToSend(bFrames, bIFrames, bDataURLAttributes);

	var bhRequestData = new BHRequestData(bhHeaderInfo, urlsToSend);

	//console.log("BHSendString: \n" + bhRequestData.getBHSendString());

	bhSendURLsToBH(bhRequestData);

	console.log("bhFireDownloadLinks End");
}

function bhFireDownloadImages(bFrames, bIFrames, bDataURLAttributes) {
	console.log("bhFireDownloadImages Start");

	var strPageURL = bhGetPageURL();
	console.log("Page-URL: " + strPageURL);
	var strPageTitle = bhGetPageTitle();
	console.log("Page-Title: " + strPageTitle);

	var bhHeaderInfo = new BHHeaderInfo(strPageURL, strPageTitle);
	console.log("BHHeaderInfo: " + bhHeaderInfo.getLogString());
	
	bhPreparePageTitle(bhHeaderInfo);
	console.log("Prepared BHHeaderInfo: " + bhHeaderInfo.getLogString());
	
	var urlsToSend = bhGetImagesToSend(bFrames, bIFrames, bDataURLAttributes);

	var bhRequestData = new BHRequestData(bhHeaderInfo, urlsToSend);

	//console.log("BHSendString: \n" + bhRequestData.getBHSendString());

	bhSendURLsToBH(bhRequestData);

	console.log("bhFireDownloadImages End");
}

/*
	Send Request to BH
*/
function bhSendURLsToBH(bhRequestData) {
	var bhStorageGetOnError = function(error) {
		console.error("Error getting port from storage: " + error);
		var iPort = 35990;
		console.info("Use default port: " + iPort);
		bhExecuteSendURLsToBH(bhRequestData, iPort);
	}
	
	var bhStorageGetOnGot = function(item) {
		console.info("Got port from storage: " + item);
		var iPort = item.bhWebExtensionPort;
		if (typeof(iPort) !== 'undefined' && iPort != null) {
			iPort = item.bhWebExtensionPort;
			console.info("Use port: " + iPort);
		} else {
			iPort = 35990;
			console.info("Use default port: " + iPort);
		}
		bhExecuteSendURLsToBH(bhRequestData, iPort);
	}
	
	var getting = browser.storage.local.get("bhWebExtensionPort");
	getting.then(bhStorageGetOnGot, bhStorageGetOnError);
}

function bhExecuteSendURLsToBH(bhRequestData, iPort) {
	console.info('BH Port: ' + iPort);
	var strURL = "http://localhost:" + iPort + "/BH/DownloadFiles"
	console.info('BH URL: ' + strURL);
	
	var request = new XMLHttpRequest();
    request.open("POST", strURL);
	console.info('Open Connection to BH');
	request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
    
	var strContent = bhRequestData.getBHSendString();
	
	request.onload = function(e) {
		if (request.readyState === 4) {
			if (request.status === 200) {
				console.log('Response-Text: ' + request.responseText);
				console.info('Data successfully sent to BH');
			} else {
				console.error('Status-Text: ' + request.statusText);
				console.error('Data could not be sent to BH');
			}
		}
	};
	request.onerror = function(e) {
		console.error('Data could not be sent to BH. Error:' + e.target.status);
	};
	
    console.info('Sending Data to BH');
    request.send(strContent);
}
