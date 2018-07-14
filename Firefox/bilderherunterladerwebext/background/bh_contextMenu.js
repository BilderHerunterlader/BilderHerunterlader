browser.contextMenus.create({
	id: "bh-fire-links",
	title: browser.i18n.getMessage("contextMenuItemBHFireLinks"),
	contexts: ["all"],
	onclick: bhFireLinks
});

browser.contextMenus.create({
	id: "bh-fire-images",
	title: browser.i18n.getMessage("contextMenuItemBHFireImages"),
	contexts: ["all"],
	onclick: bhFireImages
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

function bhFireLinks(info, tab) {
	console.log("bhFireLinks");
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

function bhFireImages(info, tab) {
	console.log("bhFireImages");
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

function bhOpenOptionsPage() {
	browser.runtime.openOptionsPage();
}
