

function main() {

  if(document.hasAspect("rd:tops")) {
  
	var value;
    var parentFolderName = document.parent.name;
  	
  	var parentFolderTitle = document.parent.properties["cm:title"];

  	document.setName(parentFolderName+"-sheet (DRAFT)");
    document.properties["rd:topsNumber"] = parentFolderName;
    document.properties["cm:title"] = parentFolderTitle;
    logger.log("parentFolderName = "+parentFolderName);
    logger.log("parentFolderTitle = "+parentFolderTitle);
    
    
    
  } else if(document.typeShort.equals("cm:folder")) {
        throw ("Cannot create folders here, only documents");
  }
  

//do this for every document
  logger.log("parentFolderName = "+document.parent.properties["cm:name"]);
  var topsStatus = document.parent.properties["rd:topsStatusFolder"];
  logger.debug("topsStatusFolder = "+topsStatus);

  var creator = document.properties["cm:creator"];
  logger.log("Creator = "+creator);
  
  if("Draft".equals(topsStatus)) {
	  document.parent.setPermission("Contributor", creator);
	  document.save();
	  //add Draft categories
//	  var nodeRef = search.luceneSearch("Draft")[0];
//	  var categories = new Array(1);
//	  categories.push(nodeRef);
//	  document.properties["cm:categories"] = categories;
//	  document.save();
  }
  
  //add ownable aspect
  props = new Array(1);
  props["cm:owner"] = creator;
  document.addAspect("cm:ownable", props);
  
}

main();