package com.reda.wf;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.log4j.Logger;


public class ReviewTOPS extends REDATaskJavaDelegate {

	
	private static final long serialVersionUID = 5232647705622239470L;

	public static final String ADMIN_USER_NAME = "admin";
	
	private static Logger LOGGER = Logger.getLogger(ReviewTOPS.class);
	
	
	public void onCreate(DelegateTask task) throws Exception {
		super.onCreate(task);
	}
	
	
	public void onComplete(final DelegateTask task) throws Exception {
		try {
			
			LOGGER.debug("ENTER");
			
			String outcome = (String) task.getVariable(REDAWfModel.WF_TOPS_MGR_APPR_STATUS);
			Object addReviewer = task.getVariable(REDAWfModel.WF_ADDITIONAL_REVIEWER);
			LOGGER.debug(outcome);
			LOGGER.debug(addReviewer);
			
			
			
			//copy outcome
			REDAWfModel.copyLocalToExec(task, REDAWfModel.WF_TOPS_MGR_APPR_STATUS);
			REDAWfModel.copyLocalToExec(task, REDAWfModel.WF_ADDITIONAL_REVIEWER);
			REDAWfModel.copyLocalToExec(task, REDAWfModel.WF_SELECTED_PROCESSOR);
			
//			DelegateExecution execution = task.getExecution();
			LOGGER.debug(task.getExecution().getVariable(REDAWfModel.WF_ADDITIONAL_REVIEWER));
			
			final String userName = task.getAssignee();
			Object selectedProcessor = task.getVariable(REDAWfModel.WF_SELECTED_PROCESSOR);
			
			String useSelectedProcessor;
			if(selectedProcessor != null && !"".equals(selectedProcessor))
				useSelectedProcessor = "true";
			else 
				useSelectedProcessor = "false";
			
			task.setVariable(REDAWfModel.WF_USE_SELECTED_PROCESSOR, useSelectedProcessor);
			
			
			//if output selected is "Additional Review" and no reviewer selected, throw an exception
			if(outcome.equals(REDAWfModel.WF_TOPS_MGR_APPR_STATUS_ADDITIONAL_REVIEW) && (addReviewer == null || "".equals(addReviewer))) {
				LOGGER.debug("Error condition met");
				throw new Exception("Please select name for Additional Reviewer");
			} else if(outcome.equals(REDAWfModel.WF_TOPS_MGR_APPR_STATUS_APPROVED)) {
				AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
					@Override
					public Boolean doWork() throws Exception {
						
						AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
						
						//check if the current user belongs to TOPS_APPROVERS authority
						AuthorityService authService = getServiceRegistry().getAuthorityService();	
						Set<String> groupsForUser = authService.getAuthoritiesForUser(userName);
						if(!groupsForUser.contains(REDAContentModel.GROUP_REDA_TOPS_APPROVER)) {
							task.getExecution().setVariable(REDAWfModel.WF_TOPS_MGR_APPR_STATUS, "INVALID_VALUE");
							throw new Exception("User is not allowed to finish this task");
						}
						
						NodeService nodeService = getNodeServiceInternal();		
						LOGGER.debug("nodeService = "+nodeService);
						
						SearchService searchService = getServiceRegistry().getSearchService();		
						LOGGER.debug("searchService = "+searchService);

						//getting documents attached to wf
						ActivitiScriptNode scriptNode = (ActivitiScriptNode)task.getExecution().getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
						NodeRef packagenode = scriptNode.getNodeRef();
						List<ChildAssociationRef> childRefList  = nodeService.getChildAssocs(packagenode);

						for (ChildAssociationRef childAssocRef : childRefList ) {
							// do something with each document in the workflow package
							final NodeRef nodeRef = childAssocRef.getChildRef();
							if(nodeService.hasAspect(nodeRef, REDAContentModel.TOPS_ASPECT_QNAME)) {
								LOGGER.debug("Setting approved value of TOPS");

								//get parent folder
								ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(nodeRef);
							    NodeRef parentFolder = childAssociationRef.getParentRef();
							    LOGGER.debug("Parent folder "+parentFolder);

							    nodeService.setProperty(parentFolder, REDAContentModel.PROP_TOPS_STATUS_FOLDER, REDAContentModel.TOPS_STATUS_APPROVED);
								LOGGER.debug("Set status to APPROVED to FOLDER "+nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
							    
								nodeService.setProperty(nodeRef, REDAContentModel.PROP_TOPS_STATUS, REDAContentModel.TOPS_STATUS_APPROVED);
								LOGGER.debug("Set status to APPROVED to "+nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

								nodeService.setProperty(nodeRef, REDAContentModel.PROP_TOPS_APPROVED_DATE, new Date());
								nodeService.setProperty(nodeRef, REDAContentModel.PROP_TOPS_APPROVED_BY, userName);
								
								String name = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
								name = name.replace("(" + REDAContentModel.TOPS_STATUS_UNDER_APPROVAL.toUpperCase()+ ")", "(" + REDAContentModel.TOPS_STATUS_APPROVED.toUpperCase()+ ")");
								nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
								LOGGER.debug("New name set to "+name);
								
								//assign category
								//add category Approved
								StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
								ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/cm:generalclassifiable/cm:TOPS_x0020_Status/cm:Approved\"");
								NodeRef categoryNode = null;
								
								try	{
									if (rs.length() == 0) 	{
										throw new AlfrescoRuntimeException("Didn't find Category");
									}
									categoryNode = rs.getNodeRef(0);
								}
								finally	{
									rs.close();
								}
								
								ArrayList<NodeRef> categories = new ArrayList<NodeRef>(1);
								categories.add(categoryNode);
								nodeService.setProperty(nodeRef, ContentModel.PROP_CATEGORIES, categories);
								
							} else {
								LOGGER.debug("No TOPS aspect for document "+nodeRef);
							}
						}

						return true;
					}
				}, 
				ADMIN_USER_NAME);
			}

			LOGGER.debug("LEAVE");
			
		} catch (Exception e) {
			LOGGER.error(e);
			throw e;
		}
		
		super.onComplete(task);
	}
	

}
