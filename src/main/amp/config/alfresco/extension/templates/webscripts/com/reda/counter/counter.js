var countersFolder;

function getCounter(name) {
	var companyhome = search.luceneSearch("PATH:\"/app:company_home\"")[0];
	countersFolder = companyhome.childByNamePath("/Counters");
	if (countersFolder == null) {
		countersFolder = companyhome.createFolder("Counters");
	}
	return countersFolder.childByNamePath("/" + name);
}

