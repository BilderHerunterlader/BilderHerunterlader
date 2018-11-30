browser.contextMenus.create({
	id: "bh-fire-links",
	title: browser.i18n.getMessage("contextMenuItemBHFireLinks"),
	contexts: ["all"],
	onclick: bhFireLinksMenuAction
});

browser.contextMenus.create({
	id: "bh-fire-images",
	title: browser.i18n.getMessage("contextMenuItemBHFireImages"),
	contexts: ["all"],
	onclick: bhFireImagesMenuAction
});

browser.contextMenus.create({
	id: "bh-fire-parsepage",
	title: browser.i18n.getMessage("contextMenuItemBHFireParsePage"),
	contexts: ["all"],
	onclick: bhFireParsePageMenuAction
});

browser.contextMenus.create({
	id: "bh-open-options-page",
	title: browser.i18n.getMessage("contextMenuItemBHOpenOptionsPage"),
	contexts: ["all"],
	onclick: bhOpenOptionsPage
});

browser.commands.onCommand.addListener((command) => {
	if (command === "bhSendLinksFromTabToBH") {
		bhFireLinks(null, browser.tabs.getCurrent());
	}
});

function bhFireLinksMenuAction(info, tab) {
	console.log("bhFireLinksMenuAction");
	var executing = browser.tabs.executeScript(tab.id, {
		file: "/background/bh_SendLinksFromTabToBH.js",
		allFrames: false
	});
	executing.then(onExecutedBHFireLinks, onErrorBHFireLinks);
}

function onExecutedBHFireLinks(result) {
	console.log("Executed bhFireLinks");
}

function onErrorBHFireLinks(error) {
	console.log("bhFireLinks Error: " + error);
}

function bhFireImagesMenuAction(info, tab) {
	console.log("bhFireImagesMenuAction");
	var executing = browser.tabs.executeScript(tab.id, {
		file: "/background/bh_SendImagesFromTabToBH.js",
		allFrames: false
	});
	executing.then(onExecutedBHFireImages, onErrorBHFireImages);
}

function onExecutedBHFireImages(result) {
	console.log("Executed bhFireImages");
}

function onErrorBHFireImages(error) {
	console.log("bhFireImages Error: " + error);
}

function bhFireParsePageMenuAction(info, tab) {
	console.log("bhFireParsePageMenuAction");
	var executing = browser.tabs.executeScript(tab.id, {
		file: "/background/bh_SendParsePageFromTabToBH.js",
		allFrames: false
	});
	executing.then(onExecutedBHFireParsePage, onErrorBHFireParsePage);
}

function onExecutedBHFireParsePage(result) {
	console.log("Executed bhFireParsePage");
}

function onErrorBHFireParsePage(error) {
	console.log("bhFireParsePage Error: " + error);
}

function bhOpenOptionsPage() {
	browser.runtime.openOptionsPage();
}
