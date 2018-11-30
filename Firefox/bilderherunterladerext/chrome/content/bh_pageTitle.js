function getPageTitle(loc) {
	var title = window.content.document.title;

	/* On mansion-of-celebs the title of the post is split into title and description. Because there are most useless titles, we add here the description to the page-title. */
	if (loc.toString().search("http://(www\.)?mansion-of-celebs\.com/viewtopic\.php.*") > -1) {
		title = getTitleForMoC(title);
	}
	
	return title;
}

function getTitleForMoC(title) {
	var divPageBody = window.content.document.getElementById("page-body");
	
	var divPageBodyPContent = "";
	var divPageBodyP = divPageBody.getElementsByTagName("p");
	
	for(var i = 0; i < divPageBodyP.length; i++) {
		divPageBodyPContent = divPageBodyP[i].firstChild.nodeValue;
		break;
	}
	
	var descFirstPart = new Array(4);
	descFirstPart[0] = "Prominente in diesem Thema:";
	descFirstPart[1] = "Celebs in this topic:";
	descFirstPart[2] = "Celebs en este tema:";
	descFirstPart[3] = "Hírességek ebben a témában:";
	
	var posDesc = -1;
	for (var i = 0; i < descFirstPart.length; i++) {
		posDesc = divPageBodyPContent.indexOf(descFirstPart[i]);
		if (posDesc > -1) {
			//Description not available
			divPageBodyPContent = "";
			break;
		}
	}
	
	var titleFirstPart = new Array(4);
	titleFirstPart[0] = "Thema anzeigen - ";
	titleFirstPart[1] = "View topic - ";
	titleFirstPart[2] = "Ver Tema - ";
	titleFirstPart[3] = "Téma megtekintése - ";
	
	var posTitle = -1;
	for (var i = 0; i < titleFirstPart.length; i++) {
		posTitle = title.indexOf(titleFirstPart[i]);
		if (posTitle > -1) {
			title = title.substring(posTitle + titleFirstPart[i].length);
			break;
		}
	}
	
	if (divPageBodyPContent.length > 0) {
		title += " " + divPageBodyPContent;
	}
	title += " - MoC";
	return title;
}
