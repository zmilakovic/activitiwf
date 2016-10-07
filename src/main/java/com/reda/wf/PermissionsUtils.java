package com.reda.wf;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.log4j.Logger;

public class PermissionsUtils {
	
	public static String ADMIN_USERNAME = "admin";
	private static Logger LOGGER = Logger.getLogger(PermissionsUtils.class);
	
	public static void setOwnerToAdminToAllDocs(NodeRef parentFolder, NodeService nodeService) throws Exception {
		List<ChildAssociationRef> filesFolderList = nodeService.getChildAssocs(parentFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);       
	    for (ChildAssociationRef file : filesFolderList) {
	        NodeRef childRef = file.getChildRef();                       
	        String fileName = (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME);
	        LOGGER.debug("Changing owner of = "+fileName+" to Admin");
	        nodeService.setProperty(childRef, ContentModel.PROP_OWNER, ADMIN_USERNAME);
	        
	        //set all creators to read only to cover documents other then TOPS sheet
			/*String creatorName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
		    LOGGER.debug("creator = "+creatorName);
			permissionService.clearPermission(parentFolder, creatorName);
		    permissionService.setPermission(parentFolder, creatorName, REDAContentModel.PERM_READ, true);
		    LOGGER.debug("creator permissions changed");*/
	    }
	}
	
	public static void setPermissionsOfSalesTeamTo(NodeRef nodeRef, NodeRef parentFolder, NodeService nodeService, PermissionService permissionService, PersonService personService, String permission) throws Exception {
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, REDAContentModel.PROP_TOPS_SALES_MANAGERS);
	    
		for (AssociationRef childAssocRefNode : assocRefs ) {
			// do something with each document in the workflow package
			final NodeRef person = childAssocRefNode.getTargetRef();
			PersonInfo personInfo = personService.getPerson(person);
			permissionService.clearPermission(parentFolder, personInfo.getUserName());
			permissionService.setPermission(parentFolder, personInfo.getUserName(), permission, true);
		}
	}
	
	
	/**
	 * Method to set permissions for Under Approval status
	 * @param nodeRef
	 * @param parentFolder
	 * @param nodeService
	 * @param permissionService
	 * @param personService
	 * @throws Exception
	 */
	public static void setPermissionsForUNDERAPPROVAL(NodeRef nodeRef, NodeRef parentFolder, NodeService nodeService, PermissionService permissionService, PersonService personService) throws Exception {
		
		//remove ownershiop
		PermissionsUtils.setOwnerToAdminToAllDocs(parentFolder, nodeService);
	    LOGGER.debug("Ownership revoked");

	    //set permissions to sales team to ReadOnly
		PermissionsUtils.setPermissionsOfSalesTeamTo(nodeRef, parentFolder, nodeService, permissionService, personService, REDAContentModel.PERM_READ);
		LOGGER.debug("Sales team permissions changed");
	    
		//set ILC team to collaborator
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_PROCESSORS, REDAContentModel.PERM_CONTRIBUTOR, true);
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_APPROVER, REDAContentModel.PERM_CONTRIBUTOR, true);
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_EXECUTIVES, REDAContentModel.PERM_READ, true);
		LOGGER.debug("ILC team permissions changed");
		
	}
	
	public static void setPermissionsForPROCESSED(NodeRef nodeRef, NodeRef parentFolder, NodeService nodeService, PermissionService permissionService, PersonService personService) throws Exception {
		
		//remove all permissions from all
		//this causes duplicate nodes in search engine for the folder and all nodes that inherit the permissions of the folder
		//So for the TOPS document the original node (APPROVED) remains in the search in addition to the updated node (PROCESSED).
		//So whenever a hit for PROCESSED is returned, both nodes come back (APPROVED) and (PROCESSED) since they share the same node id
		
		//permissionService.deletePermissions(parentFolder);
		
		//change ownership to admin
		PermissionsUtils.setOwnerToAdminToAllDocs(parentFolder, nodeService);
	    LOGGER.debug("Ownership changed to Admin to All Docs");

	    //set permissions to sales team to ReadOnly on the Folder
		PermissionsUtils.setPermissionsOfSalesTeamTo(nodeRef, parentFolder, nodeService, permissionService, personService, REDAContentModel.PERM_READ);
		LOGGER.debug("Sales team permissions changed to Read");
		
		//revoke ownership of parent folder --> do we need this?
		String creatorName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(parentFolder, ContentModel.PROP_CREATOR));
	    
		
		permissionService.clearPermission(parentFolder, creatorName);
	    permissionService.setPermission(parentFolder, creatorName, REDAContentModel.PERM_READ, true);
	    
	    permissionService.clearPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_APPROVER);
	    permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_APPROVER, REDAContentModel.PERM_READ, true);
	    
	    permissionService.clearPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_PROCESSORS);
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_PROCESSORS, REDAContentModel.PERM_READ, true);
		
		permissionService.clearPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_EXECUTIVES);
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_EXECUTIVES, REDAContentModel.PERM_READ, true);
		
		LOGGER.debug("Creator and ILC team permissions changed to Read");
	}
	
	/**
	 * Method to restore ownership to original author and grant everybody Contributor role
	 * @param nodeRef
	 * @param parentFolder
	 * @param nodeService
	 * @param permissionService
	 * @param personService
	 * @throws Exception
	 */
	public static void setPermissionsForREJECTED(NodeRef nodeRef, NodeRef parentFolder, NodeService nodeService, PermissionService permissionService, PersonService personService) throws Exception {
		
		//restore ownership
		List<ChildAssociationRef> filesFolderList = nodeService.getChildAssocs(parentFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);       
	    for (ChildAssociationRef file : filesFolderList) {
	        NodeRef childRef = file.getChildRef();                       
	        String fileName = (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME);
	        String owner = (String) nodeService.getProperty(childRef, ContentModel.PROP_OWNER);
	        String creator = (String) nodeService.getProperty(childRef, ContentModel.PROP_CREATOR);
	        if(ADMIN_USERNAME.equals(owner)) {
	        	//restore to original creator
	        	LOGGER.debug("Changing owner of = "+fileName+" to "+creator);
	 	        nodeService.setProperty(childRef, ContentModel.PROP_OWNER, creator);
	        }
	    }
	    
	    setPermissionsOfSalesTeamTo(nodeRef, parentFolder, nodeService, permissionService, personService, REDAContentModel.PERM_CONTRIBUTOR);
	    
	    String creatorName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	    permissionService.clearPermission(parentFolder, creatorName);
	    permissionService.setPermission(parentFolder, creatorName, REDAContentModel.PERM_CONTRIBUTOR, true);
	    
	    permissionService.clearPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_PROCESSORS);
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_PROCESSORS, REDAContentModel.PERM_CONTRIBUTOR, true);
		
		permissionService.clearPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_APPROVER);
		permissionService.setPermission(parentFolder, REDAContentModel.GROUP_REDA_TOPS_APPROVER, REDAContentModel.PERM_CONTRIBUTOR, true);
	}
	
}
