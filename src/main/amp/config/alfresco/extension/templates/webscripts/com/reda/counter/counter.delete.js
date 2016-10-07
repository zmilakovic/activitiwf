<import resource="classpath:alfresco/extension/templates/webscripts/com/reda/counter/counter.js">

function main() {
	var name = url.templateArgs.name;
	var counterNode = getCounter(name);
	if (counterNode == null) {
		status.setCode(
			status.STATUS_NOT_FOUND, // 404
			"No counter found with the name " + name
		);
		return;
	}
	if (counterNode.hasPermission("Write")) {
		countersFolder.removeNode(counterNode);
		status.setCode(
			status.STATUS_OK, // 200
			"Counter " + name + " deleted."
		);
	}
}
main();