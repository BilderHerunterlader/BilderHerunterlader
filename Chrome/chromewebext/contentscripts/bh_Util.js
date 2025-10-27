/*
	URL and Page-Title
*/
function bhGetPageTitle() {
	return window.top.document.title;
}

function bhGetPageURL() {
	return window.top.document.location;
}

/*
	Data-URL-Attributes
*/
function bhGetURLsFromAttributes(elements, attributes, excludes) {
	var urlsToSend = [];
	
	var currentIndex = 0;
	var dataUrl = "";
	for (var i = 0; i < elements.length; i++) {
		if (excludes.indexOf(elements[i].tagName.toUpperCase()) > -1) {
			continue;
		}
		
		for (var a = 0; a < attributes.length; a++) {
			dataUrl = elements[i].getAttribute(attributes[a]);
			if ((typeof dataUrl == 'string') && (dataUrl.length > 0)) {
				urlsToSend[currentIndex] = new BHURLData(dataUrl, null);
				currentIndex++;
			}
		}
	}
	return urlsToSend;
}


/*
	Links
*/
function bhGetLinksArray(links) {
	var urlsToSend = [];
	
	for(var i = 0; i < links.length; i++) {
		var currentLink = links[i];
		
		var cx = 0;
		var tx = null;
		for(var x = 0; x < currentLink.childNodes.length; x++) {
			if (currentLink.childNodes[x].nodeName == "IMG") {
				tx = currentLink.childNodes[x].src;
				cx++;
            }
        }
		
		var thumb = null;
		if (cx == 1) {
			thumb = tx;
		}
		urlsToSend[i] = new BHURLData(currentLink.href, thumb);
	}
	return urlsToSend;
}

function bhGetLinksToSend(bFrames, bIFrames, bDataURLAttributes) {
	var links = window.top.document.links;
	var urlsToSend = bhGetLinksArray(links);
	
	if (bFrames === true) {
		//Get Links from Frames if available
		var frames = window.top.document.getElementsByTagName('frame');
		if (frames.length > 0) {
			for(var i = 0; i < frames.length; i++) {
				var frameDoc = frames[i].contentDocument;
				if (frameDoc == null) {
					continue;
				}
				var frameLinks = frameDoc.links;
				var urlssendFrame = bhGetLinksArray(frameLinks);
				urlsToSend = urlsToSend.concat(urlssendFrame);
			}
		}
	}
	
	if (bIFrames === true) {
		//Get Links from iFrames if available
		var iframes = window.top.document.getElementsByTagName('iframe');
		if (iframes.length > 0) {
			for(var i = 0; i < iframes.length; i++) {
				var iframeDoc = iframes[i].contentDocument;
				if (iframeDoc == null) {
					continue;
				}
				var iframeLinks = iframeDoc.links;
				var urlssendiFrame = bhGetLinksArray(iframeLinks);
				urlsToSend = urlsToSend.concat(urlssendiFrame);
			}
		}
	}
	
	if (bDataURLAttributes === true) {
		var allElements = window.top.document.getElementsByTagName("*");
		var urlssendDataUrlAttributes = bhGetURLsFromAttributes(allElements, ["data-url", "data-imgsrc", "previewurl", "gi-preview-image"], ['IMG']);
		urlsToSend = urlsToSend.concat(urlssendDataUrlAttributes);
	}
	
	return urlsToSend;
}

/*
	Images
*/
function bhGetImagesArray(images) {
	var urlsToSend = [];
	
	for(var i = 0; i < images.length; i++) {
		var currentImage = images[i];
		urlsToSend[i]= new BHURLData(currentImage.src, null);
	}
	return urlsToSend;
}

function bhGetImagesToSend(bFrames, bIFrames, bDataURLAttributes) {
	var images = window.top.document.images;
	var urlsToSend = bhGetImagesArray(images);
	
	if (bFrames === true) {
		//Get Images from Frames if available
		var frames = window.top.document.getElementsByTagName('frame');
		if (frames.length > 0) {
			for(var i = 0; i < frames.length; i++) {
				var frameDoc = frames[i].contentDocument;
				if (frameDoc == null) {
					continue;
				}
				var frameImages = frameDoc.images;
				var urlssendFrame = bhGetImagesArray(frameImages);
				urlsToSend = urlsToSend.concat(urlssendFrame);
			}
		}
	}
	
	if (bIFrames === true) {
		//Get Images from iFrames if available
		var iframes = window.top.document.getElementsByTagName('iframe');
		if (iframes.length > 0) {
			for(var i = 0; i < iframes.length; i++) {
				var iframeDoc = iframes[i].contentDocument;
				if (iframeDoc == null) {
					continue;
				}
				var frameImages = iframeDoc.images;
				var urlssendiFrame = bhGetImagesArray(frameImages);
				urlsToSend = urlsToSend.concat(urlssendiFrame);
			}
		}
	}
	
	if (bDataURLAttributes === true) {
		var imgElements = window.top.document.getElementsByTagName("img");
		var urlssendDataUrlAttributes = bhGetURLsFromAttributes(imgElements, ["data-url", "data-imgsrc", "previewurl", "gi-preview-image"], []);
		urlsToSend = urlsToSend.concat(urlssendDataUrlAttributes);
	}
	
	return urlsToSend;
}
