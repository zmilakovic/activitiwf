function getCounter(counterName) {
	var companyhome = search.luceneSearch("PATH:\"/app:company_home\"")[0];
	countersFolder = companyhome.childByNamePath("/Counters");
	if (countersFolder == null) {
		countersFolder = companyhome.createFolder("Counters");
	}
	return countersFolder.childByNamePath("/" + counterName);
}

function getNextValue(counterName) {
		var counterNode = getCounter(counterName);
		if (counterNode == null) {
			counterNode = countersFolder.createNode(counterName, "rd:counter");
		}
		if (counterNode.islocked) {
			status.setCode(
				status.STATUS_NOT_AUTHORIZED,
				"Node is locked or you do not have permission to access it"
			);
			return;
		}
		
		value = 1 + counterNode.properties["rd:count"];
		
		counterNode.properties["rd:count"] = value;
		counterNode.save();
  
  		//pad value
  		value = padNumber(value,3);
  		return value;
}

function padNumber(num, size) {
    var s = "000000000" + num;
    return s.substr(s.length-size);
}

function main() {

  if(document.typeShort.equals("cm:folder") && document.hasAspect("rd:topsFolder")) {
  
	var value;
    var parentFolder = document.parent.properties["cm:title"];
    //parentFolder = parentFolder.substring(0,3).toUpperCase();
    logger.log("parentFolderTitle: "+parentFolder); 
    
    
    var d = new Date();
    var yr = d.getFullYear(); 
    
	var parentFolderName = "tops-"+parentFolder;
	value = getNextValue(parentFolderName+"-"+yr);
	
    document.setName("TOPS-"+parentFolder+"-"+yr+"-"+value);

  	logger.log("Node name: " + document.name + ", full path: " + document.getDisplayPath());
    
    document.setInheritsPermissions(false);
	document.setPermission("Contributor", document.properties["cm:creator"]);
        
    document.save();
  
} else {
  	throw ("Cannot create documents here, only TOPS folders");
  }
  
  
}

main();