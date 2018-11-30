function saveOptions(e) {
	e.preventDefault();
	browser.storage.local.set({
		bhWebExtensionPort: document.querySelector("#bhWebExtensionPort").value
	});
}

function restoreOptions() {
	function setCurrentChoice(result) {
		document.querySelector("#bhWebExtensionPort").value = result.bhWebExtensionPort || "35990";
	}

	function onError(error) {
		console.log(`Error: ${error}`);
	}

	var getting = browser.storage.local.get("bhWebExtensionPort");
	getting.then(setCurrentChoice, onError);
}

document.addEventListener("DOMContentLoaded", restoreOptions);
document.querySelector("form").addEventListener("submit", saveOptions);
