var countersFolder;

function getCounter(name) {
	var companyhome = search.luceneSearch("PATH:\"/app:company_home\"")[0];
	countersFolder = companyhome.childByNamePath("/Counters");
	if (countersFolder == null) {
		countersFolder = companyhome.createFolder("Counters");
	}
	return countersFolder.childByNamePath("/" + name);
}

function main() {
	var value;
	var name = "redapo";
	
	if(document.typeShort == "rd:case") {
	
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
		
		value = 1 + counterNode.properties["rd:count"];
		
		counterNode.properties["rd:count"] = value;
		counterNode.save();
		
		logger.log("New PO counter value " + value);
			
		var docName = document.name;
		document.name = "PO Case" + "-"+value;
		document.save();
	
	} else {
		throw ("Not PO Case folder");
	}
}

main();