var poTemplate = companyhome.childByNamePath("/data dictionary/space templates/PO KluberLubrication Template");

var targetFolder = bpm_package.children[0].getParent().getParent().childByNamePath("/Supplier PO");

//create po from template
var supplierPO = poTemplate.copy(targetFolder);
if (supplierPO != null)
{
	execution.setVariable('supplierPO', supplierPO);
	// remove Template from the name
	supplierPO.name = "Supplier PO " + supplierPO.name.replace("Template", "").trim();
	supplierPO.properties["cm:title"] = "";
	supplierPO.save();
	//add to the wf package
	bpm_package.addNode(supplierPO);
}

//rename workflow title
var newWfDescription = "Please prepare Supplier PO for " + execution.getVariable('rdwf_poCaseName') ;
execution.setVariable( 'bpm_workflowDescription',   newWfDescription  );