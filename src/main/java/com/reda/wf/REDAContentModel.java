package com.reda.wf;

import org.alfresco.service.namespace.QName;

public class REDAContentModel {

	public static final QName PROP_TOPS_NUMBER = QName.createQName("http://www.redachem.com/model/content/1.0", "topsNumber");
	public static final QName PROP_GPPO_NUMBER = QName.createQName("http://www.redachem.com/model/content/1.0", "GPPONumber");
	
	public static final QName PROP_WF_HISTORY = QName.createQName("http://www.redachem.com/model/content/1.0", "historyComments");
	
	public static final QName PROP_TOPS_STATUS = QName.createQName("http://www.redachem.com/model/content/1.0", "topsStatus");
	public static final QName PROP_TOPS_STATUS_FOLDER = QName.createQName("http://www.redachem.com/model/content/1.0", "topsStatusFolder");
	public static final QName PROP_TOPS_APPROVED_BY = QName.createQName("http://www.redachem.com/model/content/1.0", "approvedBy");
	public static final QName PROP_TOPS_APPROVED_DATE = QName.createQName("http://www.redachem.com/model/content/1.0", "approvedDate");
	public static final QName PROP_TOPS_SALES_MANAGERS = QName.createQName("http://www.redachem.com/model/content/1.0", "salesManagers");
	
	public static final QName TOPS_ASPECT_QNAME = QName.createQName("http://www.redachem.com/model/content/1.0", "tops");
    
	public static String TOPS_STATUS_DRAFT =  "Draft";
	public static String TOPS_STATUS_PROCESSED =  "Processed";
	public static String TOPS_STATUS_APPROVED =  "Approved";
	public static String TOPS_STATUS_REJECTED =  "Rejected";
	public static String TOPS_STATUS_UNDER_APPROVAL =  "Under Review";
	
	public static String INVALID_PROCESSOR = "INVALID_USER_PROCESSOR";
	
	public static String GROUP_REDA_TOPS_APPROVER = "GROUP_REDA_TOPS_APPROVER";
	public static String GROUP_REDA_TOPS_PROCESSORS = "GROUP_REDA_TOPS_PROCESSORS";
	public static String GROUP_REDA_TOPS_EXECUTIVES = "GROUP_REDA_TOPS_EXECUTIVES";
	
	public static String PERM_READ = "Read";
	public static String PERM_CONTRIBUTOR = "Contributor";
	public static String PERM_COLLABORATOR = "Collaborator";
	
}
