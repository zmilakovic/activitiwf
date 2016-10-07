<import resource="classpath:alfresco/extension/templates/webscripts/com/reda/counter/counter.js">

function main() {
	var value = args.value;
	var name = url.templateArgs.name;
	var counterNode = getCounter(name);
	if (counterNode == null) {
		counterNode = countersFolder.createNode(name, "rd:counter");
	}
	if (counterNode.islocked || !counterNode.hasPermission("Write")) {
		status.setCode(
			status.STATUS_NOT_AUTHORIZED,
			"Node is locked or you do not have permission to access it"
		);
		return;
	}
	if (value != null) {
		value = parseInt(value);
		if (isNaN(value)) {
			status.setCode(
				status.STATUS_BAD_REQUEST,
				"Invalid value: " + url.templateArgs.value
			);
			return;
		}
	} else {
		value = 1 + counterNode.properties["rd:count"];
	}
	counterNode.properties["rd:count"] = value;
	counterNode.save();
	model.name = name;
	model.value = value;
}
main();