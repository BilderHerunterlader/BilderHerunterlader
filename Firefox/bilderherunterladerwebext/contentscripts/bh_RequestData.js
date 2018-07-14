/*
	BH Header Info
*/
function BHHeaderInfo(pageURL, pageTitle) {
    this.pageURL = pageURL;
    this.pageTitle = pageTitle;
}

BHHeaderInfo.prototype.getPageURL = function() {
    return this.pageURL;
};

BHHeaderInfo.prototype.getPageTitle = function() {
    return this.pageTitle;
};

BHHeaderInfo.prototype.setPageTitle = function(pageTitle) {
    this.pageTitle = pageTitle;
};

BHHeaderInfo.prototype.getLogString = function() {
    return "BHHeaderInfo: URL=" + this.pageURL + ", Page-Title=" + this.pageTitle;
};

/*
	BH URL Data
*/
function BHURLData(url, thumb) {
    this.url = url;
    this.thumb = thumb;
}

BHURLData.prototype.getBHString = function() {
    var urlBHString = this.url + "\t";
	if (this.thumb !== null) {
		urlBHString += this.thumb;
	}
	urlBHString += "\n";
	return urlBHString;
};

/*
	BH Request Data
*/
function BHRequestData(bhHeaderInfo, urlsToSend) {
    this.bhHeaderInfo = bhHeaderInfo;
    this.urlsToSend = urlsToSend;
}

BHRequestData.prototype.getHeaderInfo = function() {
    return this.bhHeaderInfo;
};

BHRequestData.prototype.getURLsToSend = function() {
    return this.urlsToSend;
};

BHRequestData.prototype.getBHSendString = function() {
    var bhHeaderInfo = this.getHeaderInfo();
	var bhURLsToSend = this.getURLsToSend();

	//Prepair Header Data
	var prefix = "BH{af2f0750-c598-4826-8e5f-bb98aab519a5}";
	var headerData = prefix + "\n" + bhHeaderInfo.getPageTitle() + "\n" + bhHeaderInfo.getPageURL() + "\n";
	var dataStart = "FULLLISTTHUMBS\nSOF\n";
	var dataEnd = "EOF\n";
	
	var bhString = dataStart;
	bhString += headerData;

	for(var xx = 0; xx < bhURLsToSend.length; xx++) {
		var dataSend = bhURLsToSend[xx].getBHString();
		bhString += dataSend;
	}
	
	bhString += dataEnd;
	
	return bhString;
};
