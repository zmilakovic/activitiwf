package com.reda.wf;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.apache.log4j.Logger;


public class PostProcessRejection extends REDAExecutionJavaDelegate {

	private static Logger LOGGER = Logger.getLogger(PostProcessRejection.class);

	@Override
	public void execute(DelegateExecution exec) throws Exception {

		final DelegateExecution execution = exec;
		
		String activityId = execution.getCurrentActivityId(); 
		LOGGER.debug("activityId = " + activityId);
		String activityName = execution.getCurrentActivityName(); 
		LOGGER.debug("activityName = " + activityName);

		//		LOGGER.debug("bpm_package = " + execution.getVariable("bpm_package"));			
		//		LOGGER.debug("bpm_context = " + execution.getVariable("bpm_context"));

		final String wfHistory = (String) exec.getVariable(REDAWfModel.WF_HISTORY_COMMENT);

//		getServiceRegistry().getCategoryService().


		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
			@Override
			public Boolean doWork() throws Exception {

				NodeService nodeService = getNodeServiceInternal();
				SearchService searchService = getServiceRegistry().getSearchService();
				PermissionService permissionService = getServiceRegistry().getPermissionService();
				
				//getting documents attached to wf
				ActivitiScriptNode scriptNode = (ActivitiScriptNode)execution.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
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

						nodeService.setProperty(parentFolder, REDAContentModel.PROP_TOPS_STATUS_FOLDER, REDAContentModel.TOPS_STATUS_REJECTED);
						LOGGER.debug("Set status to REJECTED to parent folder "+nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

						nodeService.setProperty(nodeRef, REDAContentModel.PROP_TOPS_STATUS, REDAContentModel.TOPS_STATUS_REJECTED);
						LOGGER.debug("Set status to REJECTED to "+nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
						
						String name = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
						name = name.replace("(" + REDAContentModel.TOPS_STATUS_UNDER_APPROVAL.toUpperCase()+ ")", "(" + REDAContentModel.TOPS_STATUS_REJECTED.toUpperCase()+ ")");
						nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
						LOGGER.debug("New name set to "+name);

					} else {
						LOGGER.debug("No TOPS aspect for document "+nodeRef);
					}
					
					//add category Rejected
					StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
					ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/cm:generalclassifiable/cm:TOPS_x0020_Status/cm:Rejected\"");
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
					
					nodeService.setProperty(nodeRef, REDAContentModel.PROP_WF_HISTORY, wfHistory);
					LOGGER.debug("Set wfHistory to "+wfHistory);
					
					//change permissions
					//allow TOPS team to Contribute
					//revoke Write permissions to owner and make it Contributor
					
					//get parent folder
					ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(nodeRef);
				    NodeRef parentFolder = childAssociationRef.getParentRef();
				    
				    
				    
				    PermissionsUtils.setPermissionsForREJECTED(nodeRef, parentFolder, nodeService, permissionService, getServiceRegistry().getPersonService());
					
					LOGGER.debug("Email preparation");
					//send email
					String topsNumber = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, REDAContentModel.PROP_TOPS_NUMBER));
					String topsStatus = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, REDAContentModel.PROP_TOPS_STATUS));
					String docName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
					String docId = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_UUID));
				    String creatorName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
					
					NodeRef creatorNode = getServiceRegistry().getPersonService().getPerson(creatorName);
					LOGGER.debug("creator node "+creatorNode);
					String emailAddress = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(creatorNode, ContentModel.PROP_EMAIL));
					LOGGER.debug("Email "+emailAddress);
					
					sendEmail(docId, docName, topsNumber, topsStatus, emailAddress);
					

				}

				return true;
			}
		}, 
		PermissionsUtils.ADMIN_USERNAME);

		// always call super to execute common logic
		super.execute(execution);


	}
	
	
	/**
	 * Example of sending email using Alfresco OOTB action
	 */
	private void sendEmail (String docId, String docName, String topsNumber, String topsStatus, String emailAddress) {
		
		LOGGER.debug("ENTER");
		LOGGER.debug(topsNumber + ", " + topsStatus + ", " + emailAddress);
		
		ActionService actionService = getServiceRegistry().getActionService();
		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, topsNumber + " has been "+topsStatus);       
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO, emailAddress);
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "redatops@redachem.com");
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "");
 
		// Define Model
		String templatePATH = "PATH:\"/app:company_home/app:dictionary/app:email_templates/cm:REDA/cm:notify_status.html.ftl\"";
		ResultSet resultSet = getServiceRegistry().getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), SearchService.LANGUAGE_LUCENE, templatePATH);
		if (resultSet.length()==0){
			LOGGER.error("Template "+ templatePATH+" not found.");
			return;
		}
		NodeRef template = resultSet.getNodeRef(0);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
		// Define parameters for the model (set fields in the ftl like : args.workflowTitle)
		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
		templateArgs.put("TOPSNumber",topsNumber);
		templateArgs.put("TOPSStatus", topsStatus);
		templateArgs.put("docName", docName);
		templateArgs.put("docId", docId);
		Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
		templateModel.put("args",(Serializable)templateArgs);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,(Serializable)templateModel);
 
		actionService.executeAction(mailAction, null);
		
		LOGGER.debug("LEAVE");
	}


}
