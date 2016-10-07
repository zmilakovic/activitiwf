function main() {
   
    var siteAdminsGroup = "GROUP_SITE_ADMINISTRATORS";
  	var isAdmin = false;

  	var currentUser = person.properties.userName;
	logger.log("currentUser = "+currentUser);
  
     var user = people.getPerson(currentUser);
  
    if(user){
        groups = people.getContainerGroups(user);
        for (var j=0; j<groups.length;j++) {
          var groupName = groups[j].properties["cm:name"];
          if(siteAdminsGroup.equals(groupName)) {
             	isAdmin = true;
             }
     	}
    } else {
      logger.log("user is null");
    }
      
    if(!isAdmin) 
      throw ("Cannot create items here, only Site Administrator are allowed to create items here");
  	
}

main();