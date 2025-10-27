chrome.runtime.onInstalled.addListener(() => {
	createContextMenus();
	console.log("BilderHerunterlader context menus created");
});

function createContextMenus() {
	const items = [
		{ id: "bh-fire-links", title: chrome.i18n.getMessage("contextMenuItemBHFireLinks") },
		{ id: "bh-fire-images", title: chrome.i18n.getMessage("contextMenuItemBHFireImages") },
		{ id: "bh-fire-parsepage", title: chrome.i18n.getMessage("contextMenuItemBHFireParsePage") },
		{ id: "bh-open-options-page", title: chrome.i18n.getMessage("contextMenuItemBHOpenOptionsPage") }
	];

	for (const item of items) {
		chrome.contextMenus.create({
			id: item.id,
			title: item.title,
			contexts: ["all"]
		});
	}
}

// Handle context menu clicks
chrome.contextMenus.onClicked.addListener((info, tab) => {
	switch (info.menuItemId) {
		case "bh-fire-links":
			executeScript(tab.id, "background/bh_SendLinksFromTabToBH.js");
			break;
		case "bh-fire-images":
			executeScript(tab.id, "background/bh_SendImagesFromTabToBH.js");
			break;
		case "bh-fire-parsepage":
			executeScript(tab.id, "background/bh_SendParsePageFromTabToBH.js");
			break;
		case "bh-open-options-page":
			chrome.runtime.openOptionsPage();
			break;
	}
});

// Handle keyboard shortcut
chrome.commands.onCommand.addListener((command) => {
	if (command === "bhSendLinksFromTabToBH") {
		chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
			if (tabs[0]) {
				executeScript(tabs[0].id, "background/bh_SendLinksFromTabToBH.js");
			}
		});
	}
});

function executeScript(tabId, file) {
	if (!tabId) {
		return;	
	}
	
	chrome.scripting.executeScript(
		{
			target: { tabId: tabId },
			files: [file]
		},
		(results) => {
			if (chrome.runtime.lastError) {
				console.error("Error executing ${file}:", chrome.runtime.lastError.message);
			} else {
				console.log("Executed ${file}", results);
			}
		}
	);
}