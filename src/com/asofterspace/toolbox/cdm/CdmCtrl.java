/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.utils.NoOpProgressIndicator;
import com.asofterspace.toolbox.utils.Pair;
import com.asofterspace.toolbox.utils.ProgressIndicator;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CdmCtrl {

	public static final String CDM_NAMESPACE_MIDDLE = "ConfigurationTracking/";

	public static final String DEFAULT_NAMESPACE = "DefaultNamespace";

	public static final String MCM_PATH_DELIMITER = ".";

	private static final List<String> KNOWN_CDM_VERSIONS = Arrays.asList(
		"1.14.1",
		"1.14.0",
		"1.14.0b",
		"1.13",
		"1.13.0bd1",
		"1.12.1",
		"1.12",
		"1.11.3"
	);

	private static final List<String> KNOWN_CDM_PREFIXES = Arrays.asList(
		"http://www.esa.int/egscc/", // 1.14.1
		"http://www.esa.int/egscc/", // 1.14.0
		"http://www.esa.int/dme/", // 1.14.0b
		"http://www.esa.int/", // 1.13
		"http://www.esa.int/", // 1.13.0bd1
		"http://www.esa.int/dme/", // 1.12.1
		"http://www.scopeset.de/", // 1.12
		"http://www.scopeset.de/" // 1.11.3
	);

	private static final String TMPL_JUST_ROOT = "Just Root Element";
	private static final String TMPL_JUST_ROOT_SHORT = "just_root";
	private static final String TMPL_ROOT_ROUTE_SAP = "Root Element with Default Route and SAP";
	private static final String TMPL_ROOT_ROUTE_SAP_SHORT = "root_route_sap";
	private static final String TMPL_ROOT_ROUTE_SAP_EX_TYPE = "Root Element with Route, SAP and Example Data Type";
	private static final String TMPL_ROOT_ROUTE_SAP_EX_TYPE_SHORT = "root_route_sap_ex_type";

	static final String CI_MCM = "configurationcontrol:McmCI";
	static final String CI_SCRIPT = "configurationcontrol:ScriptCI";
	static final String CI_SCRIPT_TO_ACTIVITY = "configurationcontrol:Script2ActivityMapperCI";
	static final String CI_PROCEDURE = "configurationcontrol:ProcedureCI";
	static final String CI_PROCEDURE_TO_ACTIVITY = "configurationcontrol:Procedure2McmMapperCI"; // from 1.13.0bd1 onwards
	static final String CI_PROCEDURE_TO_ACTIVITY_OLD = "configurationcontrol:Procedure2ActivityMapperCI"; // until 1.12.1

	private static final List<String> CDM_TEMPLATES = Arrays.asList(
		TMPL_JUST_ROOT,
		TMPL_ROOT_ROUTE_SAP,
		TMPL_ROOT_ROUTE_SAP_EX_TYPE
	);

	private static final List<String> CDM_TEMPLATES_SHORT = Arrays.asList(
		TMPL_JUST_ROOT_SHORT,
		TMPL_ROOT_ROUTE_SAP_SHORT,
		TMPL_ROOT_ROUTE_SAP_EX_TYPE_SHORT
	);

	// has a CDM been loaded, like, at all?
	private boolean cdmLoaded = false;

	// all of the loaded CDM files (intended more for internal-ish use)
	private Set<CdmFile> fileList = new HashSet<>();
	private Map<String, Set<CdmFile>> ciMap = new HashMap<>();

	// our model of the CDM (intended more for external-ish use) - only initialized on reloadModel()!

	// map of ALL CdmNodes, including the ones in the other lists
	private Map<String, CdmNode> xmiIdMap;

	// just some lists of special elements
	private Set<CdmMonitoringControlElement> mces;
	private Set<CdmActivity> activities;
	private Set<CdmScript> scripts;
	private Set<CdmScript2Activity> scriptToActivityMappings;
	private Set<CdmProcedure> procedures;
	private Set<CdmProcedure2Activity> procedureToActivityMappings;

	// keep a list of mcm tree roots, as in a misconfigured CDM there could be none or several - so we need to be able to express that!
	private Set<CdmMonitoringControlElement> mcmTreeRoots;

	private Directory lastLoadedDirectory;

	private CdmFile lastFailedCdmFile;

	// counters that can be used by the individual CDM files, but that count across the whole CDM
	private int argumentCounter;
	private int containerCounter;
	private int fieldCounter;
	private List<String> mceAspectNames;


	private void initCdmCtrl() {

		fileList = new HashSet<>();
		ciMap = new HashMap<>();

		resetUniqueCounters();
	}

	private void initFullModel() {

		xmiIdMap = new HashMap<>();

		mces = new HashSet<>();
		activities = new HashSet<>();
		scripts = new HashSet<>();
		scriptToActivityMappings = new HashSet<>();
		procedures = new HashSet<>();
		procedureToActivityMappings = new HashSet<>();

		mcmTreeRoots = new HashSet<>();
	}

	// call this on a different thread please, as it can take forever
	// (and the updating of the progress bar only works if this is on a different thread!)
	public void loadCdmDirectory(Directory cdmDir, ProgressIndicator progress) throws AttemptingEmfException, CdmLoadingException {

		loadCdmDirectoryFaster(cdmDir, progress);

		// reload the model once, after all the CDM files have been loaded
		reloadModel();
	}

	// loads a CDM directory, but does NOT refresh the stored model - meaning that the files are only really
	// available individually, but no overall model has been created
	// in most cases, do not USE this - however, when you are a tiny little CLI, and you e.g. know you are
	// only going to convert the CDM to a different version (which works on a file-by-file-basis), and will
	// afterwards exit (so will never use the full model, including a full MCM tree etc.), then you can call
	// this one here :)
	public void loadCdmDirectoryFaster(Directory cdmDir, ProgressIndicator progress) throws AttemptingEmfException, CdmLoadingException {

		cdmLoaded = false;

		initCdmCtrl();

		List<File> cdmFiles = cdmDir.getAllFiles(true);

		double i = 0;
		double len = cdmFiles.size();

		try {
			for (File cdmFile : cdmFiles) {
				if (cdmFile.getFilename().toLowerCase().endsWith(".cdm")) {
					loadCdmFileInternally(cdmFile);
					i++;
					progress.setProgress(i / len);
				}
			}

			if (i <= 0) {
				throw new CdmLoadingException("The directory " + cdmDir.getDirname() + " does not seem to contain any .cdm files at all.");
			}

			lastLoadedDirectory = cdmDir;

			cdmLoaded = true;

		} finally {
			progress.done();
		}
	}

	/**
	 * Loads just one CDM file and reloads all the models, pretending that our entire CDM was just this one file
	 */
	public void loadJustOneCdmFile(File cdmFile) throws AttemptingEmfException, CdmLoadingException {

		cdmLoaded = false;

		initCdmCtrl();

		if (cdmFile.getFilename().toLowerCase().endsWith(".cdm")) {
			loadCdmFileInternally(cdmFile);
		} else {
			throw new CdmLoadingException("The file " + cdmFile.getFilename() + " does not seem to be a .cdm file.");
		}

		lastLoadedDirectory = cdmFile.getParentDirectory();

		cdmLoaded = true;

		// reload the model once, after all the CDM file has been loaded
		reloadModel();
	}

	/**
	 * Loads another CDM file, after already having loaded an entire directory
	 * (e.g. because we added a new file and want to load it now)
	 */
	public CdmFile loadAnotherCdmFile(File cdmFile) throws AttemptingEmfException, CdmLoadingException {

		CdmFile result = loadCdmFileInternally(cdmFile);

		// as this function was called from - gasp! - the outside world, we have to reload the model now...
		// at least for this one file ;)
		reloadAnotherModel(result);

		return result;
	}

	private CdmFile loadCdmFileInternally(File cdmFile) throws AttemptingEmfException, CdmLoadingException {

		CdmFile result = null;

		try {
			result = new CdmFile(cdmFile, this);

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		String there_was_a_problem = "There was a problem while loading the CDM file " + cdmFile.getLocalFilename() + ".";

		if (result == null) {
			throw new CdmLoadingException(there_was_a_problem);
		}

		switch (result.getMode()) {

			case XML_LOADED:
			case EMF_LOADED:
				// all is good!;
				break;

			case EMF_UNSUPPORTED:
				lastFailedCdmFile = result;
				throw new AttemptingEmfException("The CDM file " + cdmFile.getLocalFilename() + " is an EMF binary file, which is not yet fully supported.\nThis particular file sadly could not be parsed.\nPlease only use CDM files in XML format.");

			case NONE_LOADED:
			default:
				lastFailedCdmFile = result;
				throw new CdmLoadingException(there_was_a_problem);
		}

		fileList.add(result);

		String ciType = result.getCiType();

		Set<CdmFile> thisCiMap = ciMap.get(ciType);

		if (thisCiMap == null) {
			thisCiMap = new HashSet<>();
			thisCiMap.add(result);
			ciMap.put(ciType, thisCiMap);
		} else {
			thisCiMap.add(result);
		}

		return result;
	}

	/**
	 * Reload the entire internal model of the CDM
	 * Call this at a later stage if you loaded the CDM in a fast way
	 * and later on realize that you actually need to fully load it
	 */
	public void reloadModel() {

		initFullModel();

		// load the model for each file individually
		for (CdmFile cdmFile : fileList) {
			cdmFile.addContentsToCdmCtrl();
		}

		// load the entire merged model (e.g. building the entire MCM tree across all files)
		reloadMergedModel();
	}

	/**
	 * Reload the internal model of the CDM for one particular file, in addition to all others that are already there
	 */
	private void reloadAnotherModel(CdmFile cdmFile) {

		cdmFile.addContentsToCdmCtrl();

		// load the entire merged model (e.g. building the entire MCM tree across all files)
		reloadMergedModel();
	}

	/**
	 * Adds one node to our internal model - called from cdmFile, after we call it in addContentsToCdmCtrl
	 */
	void addToModel(CdmFileBase parentFile, XmlElement xmEl) {

		CdmNode cdmNode = getByXmlElement(parentFile, xmEl);

		addToModel(cdmNode);
	}

	/**
	 * Adds one node to our internal model
	 */
	void addToModel(CdmNode cdmNode) {

		if (cdmNode instanceof CdmScript) {
			scripts.add((CdmScript) cdmNode);
		}

		if (cdmNode instanceof CdmScript2Activity) {
			scriptToActivityMappings.add((CdmScript2Activity) cdmNode);
		}

		if (cdmNode instanceof CdmProcedure) {
			procedures.add((CdmProcedure) cdmNode);
		}

		if (cdmNode instanceof CdmProcedure2Activity) {
			procedureToActivityMappings.add((CdmProcedure2Activity) cdmNode);
		}

		if (cdmNode instanceof CdmMonitoringControlElement) {
			mces.add((CdmMonitoringControlElement) cdmNode);
		}

		if (cdmNode instanceof CdmActivity) {
			activities.add((CdmActivity) cdmNode);
		}

		// in any case, add this node to the map of all nodes
		xmiIdMap.put(cdmNode.getId(), cdmNode);
	}

	void removeFromModel(CdmNode cdmNode) {

		if (cdmNode instanceof CdmScript) {
			scripts.remove((CdmScript) cdmNode);
		}

		if (cdmNode instanceof CdmScript2Activity) {
			scriptToActivityMappings.remove((CdmScript2Activity) cdmNode);
		}

		if (cdmNode instanceof CdmProcedure) {
			procedures.remove((CdmProcedure) cdmNode);
		}

		if (cdmNode instanceof CdmProcedure2Activity) {
			procedureToActivityMappings.remove((CdmProcedure2Activity) cdmNode);
		}

		if (cdmNode instanceof CdmMonitoringControlElement) {
			mces.remove((CdmMonitoringControlElement) cdmNode);
		}

		if (cdmNode instanceof CdmActivity) {
			activities.remove((CdmActivity) cdmNode);
		}

		// in any case, add this node to the map of all nodes
		xmiIdMap.remove(cdmNode.getId());
	}

	private void reloadMergedModel() {

		// figure out what the MCM Tree Roots might be
		reloadTreeRoots();
	}

	private void reloadTreeRoots() {

		// first get a list of all MCEs
		Set<CdmMonitoringControlElement> leftOverMces = new HashSet<>(mces);

		// set all MCEs to being roots
		for (CdmMonitoringControlElement mce : leftOverMces) {
			mce.setContainingElement(null);
		}

		// now let each MCE find a path towards its children
		// (each MCE if it is root gets its children fast from our internal id map and tells them that they are not root anymore,
		// and they tell their children - unless they already know - and update their own internal link up such that they know
		// who is their daddy)
		for (CdmMonitoringControlElement mce : leftOverMces) {
			mce.initSubTreeFromHere();
		}

		// finally, iterate overall MCEs once more and ask if they still are root or not - there should be exactly one left ;)
		for (CdmMonitoringControlElement mce : leftOverMces) {
			if (mce.isRoot()) {
				mcmTreeRoots.add(mce);
			}
		}
	}

	public boolean hasCdmBeenLoaded() {
		return cdmLoaded;
	}

	/**
	 * Save all currently opened files - the ones that have been deleted (so far just set an internal flag)
	 * will delete their contents from the disk
	 */
	public void save() {

		for (CdmFile cdmFile : fileList) {
			cdmFile.save();
		}
	}

	/**
	 * Save all currently opened files to the new location
	 */
	public void saveTo(Directory newLocation) {

		for (CdmFile cdmFile : fileList) {
			cdmFile.saveTo(lastLoadedDirectory.traverseFileTo(cdmFile, newLocation));
		}

		lastLoadedDirectory = newLocation;
	}

	public Directory getLastLoadedDirectory() {
		return lastLoadedDirectory;
	}

	/**
	 * Convert all loaded CDM files to the given version and prefix - if either is null,
	 * keep the current one of the first file (so e.g. if the CDM contains two files,
	 * one in version 1.12 and one in version 1.12.1, and version is given as null,
	 * then both will be converted to 1.12, so at least they are all consistent!)
	 */
	public void convertTo(String toVersion, String toPrefix) {

		if (toVersion == null) {
			toVersion = getCdmVersion();
		}

		if (toPrefix == null) {
			toPrefix = getCdmVersionPrefix();
		}

		// reset the unique counters so that all the files can use them
		resetUniqueCounters();

		for (CdmFile cdmFile : fileList) {
			cdmFile.convertTo(toVersion, toPrefix);
		}

		// reset the unique counters again so they can be garbage collected
		resetUniqueCounters();
	}

	private void resetUniqueCounters() {

		argumentCounter = 0;
		containerCounter = 0;
		fieldCounter = 0;

		mceAspectNames = new ArrayList<>();
	}

	public int getArgumentCounter() {
		return argumentCounter++;
	}

	public int getContainerCounter() {
		return containerCounter++;
	}

	public int getFieldCounter() {
		return fieldCounter++;
	}

	// return an editable (!) list - the consumer is supposed to be able to change it, and changes are reflected in here!
	public List<String> getKnownMceAspectNames() {
		return mceAspectNames;
	}

	/**
	 * Get the CDM version of one CDM file at random - as they should all have the same version,
	 * we would like to receive the correct one no matter which one is being used ;)
	 */
	public String getCdmVersion() {

		if (fileList.size() <= 0) {
			return "";
		}

		return fileList.iterator().next().getCdmVersion();
	}

	public String getCdmVersionPrefix() {

		if (fileList.size() <= 0) {
			return "";
		}

		return fileList.iterator().next().getCdmVersionPrefix();
	}

	public static String getCompatWithEGSCCstr(String version, String prefix) {

		final String NOT_A_RELEASE = "(not included in any official EGS-CC release)";

		switch (version) {
			case "1.11.3":
				if ("http://www.scopeset.de/".equals(prefix)) {
					return "DR16, IR1, IR1h1, IR1h2";
				}
				return NOT_A_RELEASE;
			case "1.12":
				if ("http://www.scopeset.de/".equals(prefix)) {
					return "IR2";
				}
				return NOT_A_RELEASE;
			case "1.12.1":
				if ("http://www.esa.int/dme/".equals(prefix)) {
					return "IR2d1, IR2d2";
				}
				return NOT_A_RELEASE;
			case "1.13.0bd1":
				if ("http://www.esa.int/".equals(prefix)) {
					return "IR3, IR3p1, IR3p2";
				}
				return NOT_A_RELEASE;
			case "1.13":
				return NOT_A_RELEASE;
			case "1.14.0b":
				return NOT_A_RELEASE;
			case "1.14.0":
				if ("http://www.esa.int/egscc/".equals(prefix)) {
					return "IR4, IR4d0, IR4d0p1, IR4d0p2, IR4d0p3, IR4d0p4";
				}
				return NOT_A_RELEASE;
			case "1.14.1":
				if ("http://www.esa.int/egscc/".equals(prefix)) {
					return "IR4d1";
				}
				return NOT_A_RELEASE;
		}

		return "(unknown)";
	}

	public static String getCompatWithMCDEstr(String version, String prefix) {

		switch (version) {
			case "1.12.1":
				if ("http://www.esa.int/dme/".equals(prefix)) {
					return "0.15.2 and 0.18.0";
				}
				break;
			case "1.13.0bd1":
				if ("http://www.esa.int/".equals(prefix)) {
					return "0.18.4";
				}
				break;
			case "1.14.0b":
				if ("http://www.esa.int/dme/".equals(prefix)) {
					return "0.18.5";
				}
				break;
			case "1.14.0":
				if ("http://www.esa.int/egscc/".equals(prefix)) {
					return "0.18.7";
				}
				break;
			case "1.14.1":
				if ("http://www.esa.int/egscc/".equals(prefix)) {
					return "0.18.8";
				}
				break;
		}

		return "(unknown)";
	}

	/**
	 * The highest CDM version that is known to this controller
	 */
	public static String getHighestKnownCdmVersion() {
		return KNOWN_CDM_VERSIONS.get(0);
	}

	/**
	 * A list of all known CDM versions
	 * Please do not modify this list directly but copy it instead if using it!
	 */
	public static List<String> getKnownCdmVersions() {
		return KNOWN_CDM_VERSIONS;
	}

	/**
	 * A list of prefixes corresponding to the CDM versions given back by
	 * getKnownCdmVersions()
	 * Please do not modify this list directly but copy it instead if using it!
	 */
	public static List<String> getKnownCdmPrefixes() {
		return KNOWN_CDM_PREFIXES;
	}

	/**
	 * Given a CDM version (which is allowed to be null), return the corresponding
	 * prefix if it is known (or null if it is not)
	 */
	public static String getPrefixForVersion(String toVersion) {

		if (toVersion == null) {
			return null;
		}

		int i = 0;

		for (String ver : KNOWN_CDM_VERSIONS) {
			if (ver.equals(toVersion)) {
				return KNOWN_CDM_PREFIXES.get(i);
			}
			i++;
		}

		return null;
	}

	public Set<CdmFile> getCIs(String ciName) {

		if (!cdmLoaded) {
			return new HashSet<>();
		}

		Set<CdmFile> result = ciMap.get(ciName);

		if (result == null) {
			return new HashSet<>();
		}

		return result;
	}

	public Set<CdmMonitoringControlElement> getMonitoringControlElements() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return mces;
	}

	public Set<CdmScript> getScripts() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return scripts;
	}

	public Set<CdmScript2Activity> getScriptToActivityMappings() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return scriptToActivityMappings;
	}

	public Set<CdmProcedure> getProcedures() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return procedures;
	}

	public Set<CdmProcedure2Activity> getProcedureToActivityMappings() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return procedureToActivityMappings;
	}

	public Set<CdmActivity> getActivities() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return activities;
	}

	/**
	 * Get the list of CDM files that have been loaded
	 */
	public Set<CdmFile> getCdmFiles() {
		if (!cdmLoaded) {
			return new HashSet<>();
		}
		return fileList;
	}

	/**
	 * Check if the CDM as a whole is valid;
	 * returns 0 if it is valid, and the amount of problems
	 * encountered if it is not;
	 * in the case of it not being valid, the List<String>
	 * that has been passed in will be filled with more detailed
	 * explanations about why it is not valid
	 */
	public int checkValidity(List<String> outProblemsFound) {

		// innocent unless proven otherwise
		int verdict = 0;

		// TODO :: also check not just the main version of the file, but all the other versions in the xmlns thingenses?

		// validate that all CDM files are using the same CDM version
		// btw., we do NOT need to check that the version prefixes are all the same,
		// as we check that all versions are the same, and for each file we check
		// that the prefix agrees with the version... so it follows that, if we do
		// not complain, then the prefixes are all the same too :)

		Set<CdmFile> cdmFiles = getCdmFiles();
		Set<String> cdmVersionsFound = new HashSet<>();

		for (CdmFile file : cdmFiles) {
			String curVersion = file.getCdmVersion();
			String curVersionPrefix = file.getCdmVersionPrefix();
			if (curVersion == null) {
				verdict++;
				outProblemsFound.add(file.getLocalFilename() + " has no CDM version identifier!");
			} else {
				if (curVersionPrefix == null) {
					verdict++;
					outProblemsFound.add(file.getLocalFilename() + " has no CDM version prefix!");
				} else {
					// also check that the prefixes agree with the versions
					String shouldBePrefix = getPrefixForVersion(curVersion);
					if (shouldBePrefix != null) {
						if (!curVersionPrefix.equals(shouldBePrefix)) {
							verdict++;
							outProblemsFound.add(file.getLocalFilename() + " has the CDM version " + curVersion + " with prefix " + curVersionPrefix + ", but this version is known to use the prefix " + shouldBePrefix + "!");
						}
					}
				}
			}
			cdmVersionsFound.add(curVersion);
		}

		// TODO :: also ensure that the qudv versions are correct - sadly, the qudv prefixes are not even
		// aligned with the CDM prefixes, e.g. in 1.14.0 prefix is esa/egscc, but qudv prefix is esa/dme
		// (or wait, was that just because we did a manual conversion wrong? re-check if this is the case!),
		// while in 1.12, qudv prefix is scopeset... (in both, qudv version is 1.5)
		// confirmed good example: xmlns:configurationcontrol="http://www.esa.int/dme/ConfigurationTracking/1.14.0b" xmlns:qudv.blocks_extModel="http://www.esa.int/dme/core/qudv/blocks/1.5" xmlns:qudv.conceptualmodel_extModel="http://www.esa.int/dme/core/qudv/conceptualmodel/1.5"

		// oh no, we have different CDM versions!
		if (cdmVersionsFound.size() > 1) {
			verdict++;
			outProblemsFound.add("CIs with multiple CDM versions have been mixed together!");
			StringBuilder foundVersions = new StringBuilder();
			foundVersions.append("Found CDM versions: ");
			String sep = "";
			for (String cdmVersionFound : cdmVersionsFound) {
				foundVersions.append(sep);
				sep = ", ";
				if (cdmVersionFound == null) {
					foundVersions.append("(none)");
				} else {
					foundVersions.append(cdmVersionFound);
				}
			}
			outProblemsFound.add(foundVersions.toString());
		}

		// check that there is exactly one root node of the merged MCM tree (so no more or less than one MCE that is not
		// listed in other MCEs as subElement)
		if (getMonitoringControlElements().size() < 1) {
			verdict++;
			outProblemsFound.add("The MCM tree does not seem to contain any nodes at all!");
		} else if (getAllMcmTreeRoots().size() < 1) {
			verdict++;
			outProblemsFound.add("The MCM tree seems to not contain a root node, meaning that the intended root node was attached as child of another node!");
		} else if (getAllMcmTreeRoots().size() > 1) {
			verdict++;
			outProblemsFound.add("The MCM tree seems to have " + getAllMcmTreeRoots().size() + " root nodes, which means that there are " + getAllMcmTreeRoots().size() + " times as many root nodes as are allowed! ;)");
		}

		// check everything that can be easier checked by looking at each CDM file individually
		for (CdmFile file : cdmFiles) {
			verdict += file.checkValidity(outProblemsFound);
		}

		return verdict;
	}

	public CdmActivity addActivity(String newActivityName, String newActivityAlias, CdmMonitoringControlElement mceContainingThis) {

		// TODO :: ensure that the name is not yet taken

		CdmActivity createdActivity = mceContainingThis.addActivity(newActivityName, newActivityAlias);

		// append the resulting new mapping to the internal list of mappings of the CdmCtrl
		activities.add(createdActivity);

		return createdActivity;
	}

	public CdmScript2Activity addScriptToActivityMapping(CdmScript script, CdmActivity activity) {

		// first of all, get all script to activity mapping CIs
		Set<CdmFile> scriptToActivityMapperCis = getCIs(CI_SCRIPT_TO_ACTIVITY);

		// if there are none, create a new one
		if (scriptToActivityMapperCis.size() < 1) {
			if (!addScriptToActivityCI()) {
				return null;
			}
			scriptToActivityMapperCis = getCIs(CI_SCRIPT_TO_ACTIVITY);
		}

		// add a new script to activity mapping to the largest script to activity mapping CI that comes into our hands
		int largestFoundHas = -1;
		CdmFile scriptToActivityMapperCI = null;
		for (CdmFile curCI : scriptToActivityMapperCis) {
			int nowFoundHas = curCI.getRoughSize();
			if (nowFoundHas > largestFoundHas) {
				scriptToActivityMapperCI = curCI;
				largestFoundHas = nowFoundHas;
			}
		}

		// TODO :: make this name configurable?
		String mappingBaseName = script.getName() + "_mapping";
		String mappingName = mappingBaseName;

		// ensure that the name is not yet taken
		Set<CdmScript2Activity> existingMappers = getScriptToActivityMappings();

		boolean doContinue = true;
		int i = 1;

		while (doContinue) {
			doContinue = false;
			for (CdmScript2Activity existingMapper : existingMappers) {
				if (mappingName.equals(existingMapper.getName())) {
					doContinue = true;
				}
			}
			if (doContinue) {
				i++;
				mappingName = mappingBaseName + i;
			}
		}

		// do not just use the filename, but keep track of the relative paths - here, the relative path
		// of the script relative to the script to activity mapper CI
		String scriptFile = scriptToActivityMapperCI.getParentDirectory().getRelativePath(script.getParentFile());
		// we want a path with forward slashes, even under Windows, as the CDM is always written Linux-y
		scriptFile = IoUtils.osPathStrToLinuxPathStr(scriptFile);
		String scriptId = script.getId();

		// do not just use the filename, but keep track of the relative paths - here, the relative path
		// of the activity relative to the script to activity mapper CI
		String activityFile = scriptToActivityMapperCI.getParentDirectory().getRelativePath(activity.getParentFile());
		// we want a path with forward slashes, even under Windows, as the CDM is always written Linux-y
		activityFile = IoUtils.osPathStrToLinuxPathStr(activityFile);
		String activityId = activity.getId();

		CdmScript2Activity createdMapping = scriptToActivityMapperCI.addScript2Activity(
			mappingName,
			scriptFile,
			scriptId,
			activityFile,
			activityId
		);

		// no need to update our internal lists, as that is already done by the addScript2Activity function

		return createdMapping;
	}

	public String getXMLNS() {
		return "xmlns:configurationcontrol=\"" + getCdmVersionPrefix() + CDM_NAMESPACE_MIDDLE + getCdmVersion() + "\"";
	}

	/**
	 * Tries to add a new script to activity CI
	 * Returns true if successful, false otherwise
	 */
	public boolean addScriptToActivityCI() {

		String newCiBaseName = "ScriptToActivity";
		String newCiName = newCiBaseName;
		int i = 1;
		File newFileLocation;

		while (true) {
			newFileLocation = new File(getLastLoadedDirectory(), "Resource_" + newCiName + ".cdm");

			// check that the newCiName (+ .cdm) is not already the file name of some other CDM file!
			if (!newFileLocation.exists()) {
				break;
			}

			i++;

			// try Resource_ScriptToActivity2.cdm, Resource_ScriptToActivity3.cdm, ...
			newCiName = newCiBaseName + i;
		}

		// add a script CI with one script with exactly this name - but do not save it on the hard disk just yet
		String newCiContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<" + CI_SCRIPT_TO_ACTIVITY + " xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" " + getXMLNS() + " xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\" name=\"" + newCiName + "\" onlineRevisionIdentifier=\"0\">\n" +
			"</" + CI_SCRIPT_TO_ACTIVITY + ">";

		SimpleFile tmpCi = new SimpleFile("tmpfile.tmp");
		tmpCi.setContent(newCiContent);
		tmpCi.save();

		try {
			CdmFile newCdmFile = loadAnotherCdmFile(tmpCi);

			Set<CdmFile> scriptToActivityMapperCis = getCIs(CI_SCRIPT_TO_ACTIVITY);

			if (scriptToActivityMapperCis.size() != 1) {
				return false;
			}

			newCdmFile.setFilelocation(newFileLocation);

			tmpCi.delete();

			// add the new script to activity mapper CI to the Manifest file
			// TODO

		} catch (AttemptingEmfException | CdmLoadingException e2) {
			return false;
		}

		return true;
	}

	private static String createExternalVersionLabel() {
		return "externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\"";
	}

	/**
	 * Get a list of all templates that are supported for creating a new CDM
	 */
	public static List<String> getTemplates() {
		return CDM_TEMPLATES;
	}

	/**
	 * Get a list of all templates that are supported for creating a new CDM,
	 * written in their short forms
	 */
	public static List<String> getTemplatesShort() {
		return CDM_TEMPLATES_SHORT;
	}

	/**
	 * Creates a new CDM at the indicated path and immediately opens it.
	 * Returns true if it all worked, or false otherwise.
	 */
	@SuppressWarnings("fallthrough")
	public boolean createNewCdm(String cdmPath, String version, String versionPrefix, String template) throws AttemptingEmfException, CdmLoadingException {

		if ("".equals(cdmPath)) {
			throw new CdmLoadingException("Please enter a CDM path to create the new CDM files!");
		}

		if ("".equals(version)) {
			throw new CdmLoadingException("Please enter a CDM version to create the new CDM files!");
		}

		if ("".equals(versionPrefix)) {
			throw new CdmLoadingException("Please enter a CDM version prefix to create the new CDM files!");
		}

		Directory cdmDir = new Directory(cdmPath);

		// if the new directory does not yet exist, then we have to create it...
		if (!cdmDir.exists()) {
			cdmDir.create();
		}

		// complain if the directory is not empty
		Boolean isEmpty = cdmDir.isEmpty();
		if ((isEmpty == null) || !isEmpty) {
			throw new CdmLoadingException("The specified directory is not empty - please create the new CDM in an empty directory!");
		}

		String newCiName;
		String mcmRootDefinitionUuid;
		String resourceMcmContent;
		SimpleFile mcmCi;

		// btw., all of our templates are written for version 1.14.0... so we convert them later on in this function to whatever version is actually required ^^
		String templateVersion = "1.14.0";
		String templateVersionPrefix = "http://www.esa.int/egscc/";

		switch (template) {

			case TMPL_JUST_ROOT:
			case TMPL_JUST_ROOT_SHORT:

				// create just the ResourceMcm.cdm file in XML format with one root node (mcmRoot)
				newCiName = "Mcm";
				mcmRootDefinitionUuid = UuidEncoderDecoder.generateEcoreUUID();
				resourceMcmContent =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<configurationcontrol:McmCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:checkandcondition=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/CheckAndCondition/" + templateVersion + "\" xmlns:configurationcontrol=\"" + templateVersionPrefix + CDM_NAMESPACE_MIDDLE + templateVersion + "\" xmlns:mcmchecks=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/MCMChecks/" + templateVersion + "\" xmlns:mcmimplementationitems=\"" + templateVersionPrefix + "MonitoringControl/MCMImplementationItems/" + templateVersion + "\" xmlns:monitoringcontrolcommon=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/" + templateVersion + "\" xmlns:monitoringcontrolmodel=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/" + templateVersion + "\" xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" " + createExternalVersionLabel() + " onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
					"  <monitoringControlElement xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" name=\"mcmRoot\" subElements=\"\" definition=\"" + mcmRootDefinitionUuid + "\">\n" +
					"  </monitoringControlElement>\n" +
					"  <monitoringControlElementDefinition xmi:id=\"" + mcmRootDefinitionUuid + "\" name=\"mcmRoot_Definition\" subElements=\"\">\n" +
					"  </monitoringControlElementDefinition>\n" +
					"</configurationcontrol:McmCI>";

				mcmCi = new SimpleFile(cdmDir, "Resource_" + newCiName + ".cdm");
				mcmCi.setContent(resourceMcmContent);
				mcmCi.save();

				// also create the Manifest file
				// TODO

				break;

			case TMPL_ROOT_ROUTE_SAP_EX_TYPE:
			case TMPL_ROOT_ROUTE_SAP_EX_TYPE_SHORT:

				newCiName = "DataTypes";
				String displayFormatUuid = UuidEncoderDecoder.generateEcoreUUID();
				resourceMcmContent =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<configurationcontrol:DataTypesCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:configurationcontrol=\"" + templateVersionPrefix + CDM_NAMESPACE_MIDDLE + templateVersion + "\" xmlns:monitoringcontrolcommon=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/" + templateVersion + "\" xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" " + createExternalVersionLabel() + " onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
					"  <abstractDataType xsi:type=\"monitoringcontrolcommon:SignedInteger\" xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" name=\"INT32\" bitLength=\"32\" signedIntegerDisplayFormat=\"" + displayFormatUuid + "\"/>\n" +
					"  <abstractDataDisplayFormat xsi:type=\"monitoringcontrolcommon:SignedIntegerDisplayFormat\" xmi:id=\"" + displayFormatUuid + "\" name=\"INT32Format\" format=\"decimal\"/>\n" +
					"</configurationcontrol:DataTypesCI>\n";

				mcmCi = new SimpleFile(cdmDir, "Resource_" + newCiName + ".cdm");
				mcmCi.setContent(resourceMcmContent);
				mcmCi.save();

				// also add this to the Manifest file
				// TODO

				// INTENTIONALLY FALL THROUGH TO THE NEXT CASE - as the McmCI is the same! ^^

			case TMPL_ROOT_ROUTE_SAP:
			case TMPL_ROOT_ROUTE_SAP_SHORT:

				// create just the ResourceMcm.cdm file in XML format with one root node (mcmRoot)
				newCiName = "Mcm";
				String routeUuid = UuidEncoderDecoder.generateEcoreUUID();
				String routeTypeUuid = UuidEncoderDecoder.generateEcoreUUID();
				String sapUuid = UuidEncoderDecoder.generateEcoreUUID();
				mcmRootDefinitionUuid = UuidEncoderDecoder.generateEcoreUUID();
				String routeDefinitionUuid = UuidEncoderDecoder.generateEcoreUUID();
				String routeTypeDefinitionUuid = UuidEncoderDecoder.generateEcoreUUID();
				String sapDefinitionUuid = UuidEncoderDecoder.generateEcoreUUID();
				resourceMcmContent =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<configurationcontrol:McmCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:checkandcondition=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/CheckAndCondition/" + templateVersion + "\" xmlns:configurationcontrol=\"" + templateVersionPrefix + CDM_NAMESPACE_MIDDLE + templateVersion + "\" xmlns:mcmchecks=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/MCMChecks/" + templateVersion + "\" xmlns:mcmimplementationitems=\"" + templateVersionPrefix + "MonitoringControl/MCMImplementationItems/" + templateVersion + "\" xmlns:monitoringcontrolcommon=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/" + templateVersion + "\" xmlns:monitoringcontrolmodel=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/" + templateVersion + "\" xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" " + createExternalVersionLabel() + " onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
					"  <monitoringControlElement xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" name=\"mcmRoot\" subElements=\"\" defaultRoute=\"" + routeUuid + "\" definition=\"" + mcmRootDefinitionUuid + "\" defaultServiceAccessPoint=\"" + sapUuid + "\">\n" +
					"	<monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:Route\" xmi:id=\"" + routeUuid + "\" name=\"DefaultRoute\" baseElement=\"" + routeDefinitionUuid + "\" hasPredictedValue=\"false\" routeName=\"DefaultRoute\" routeID=\"1\" routeType=\"" + routeTypeUuid + "\"/>\n" +
					"	<monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:RouteType\" xmi:id=\"" + routeTypeUuid + "\" name=\"DefaultRouteType\" baseElement=\"" + routeTypeDefinitionUuid + "\" hasPredictedValue=\"false\" routeIDType=\"1\"/>\n" +
					"	<monitoringControlElementAspects xsi:type=\"mcmimplementationitems:ServiceAccessPoint\" xmi:id=\"" + sapUuid + "\" name=\"13\" baseElement=\"" + sapDefinitionUuid + "\" hasPredictedValue=\"false\" validRoutes=\"" + routeUuid + "\"/>\n" +
					"  </monitoringControlElement>\n" +
					"  <monitoringControlElementDefinition xmi:id=\"" + mcmRootDefinitionUuid + "\" name=\"mcmRoot_Definition\" subElements=\"\">\n" +
					"	<monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:Route\" xmi:id=\"" + routeDefinitionUuid + "\" name=\"DefaultRoute\" hasPredictedValue=\"false\" routeName=\"DefaultRouteDef\" routeID=\"1\" routeType=\"" + routeTypeDefinitionUuid + "\"/>\n" +
					"	<monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:RouteType\" xmi:id=\"" + routeTypeDefinitionUuid + "\" name=\"DefaultRouteTypeDef\" hasPredictedValue=\"false\" routeIDType=\"1\"/>\n" +
					"	<monitoringControlElementAspects xsi:type=\"mcmimplementationitems:ServiceAccessPoint\" xmi:id=\"" + sapDefinitionUuid + "\" name=\"13\" hasPredictedValue=\"false\" validRoutes=\"" + routeDefinitionUuid + "\" />\n" +
					"  </monitoringControlElementDefinition>\n" +
					"</configurationcontrol:McmCI>";

				mcmCi = new SimpleFile(cdmDir, "Resource_" + newCiName + ".cdm");
				mcmCi.setContent(resourceMcmContent);
				mcmCi.save();

				// also create the Manifest file
				// TODO

				break;

			default:
				throw new CdmLoadingException("The template '" + template + "' that you selected does not seem to be available - oops!");
		}

		// immediately open the newly created CDM using the CdmCtrl, just as if the open dialog had been called
		// (as we are just opening one, two short files, this should take less than a second and displaying
		// a progress bar would only confuse everyone!)
		ProgressIndicator noProgress = new NoOpProgressIndicator();
		loadCdmDirectory(cdmDir, noProgress);

		// actually convert our templates to the current version
		for (CdmFile cdmFile : fileList) {
			cdmFile.convertTo(version, versionPrefix);
		}

		// aaand finally save immediately - after the conversion
		save();

		return true;
	}

	/**
	 * This is the quick-access version of getAllMcmTreeRoots() - here assuming that there is exactly one to be returned
	 */
	public CdmMonitoringControlElement getMcmTreeRoot() {

		if (mcmTreeRoots == null) {
			return null;
		}

		if (mcmTreeRoots.size() > 0) {
			return mcmTreeRoots.iterator().next();
		}

		return null;
	}

	/**
	 * Get all individual unconnected MCM tree roots that have bee found... the result should have exactly
	 * one element, otherwise something is very wrong with our lovely CDM ;)
	 */
	public Set<CdmMonitoringControlElement> getAllMcmTreeRoots() {

		if (mcmTreeRoots == null) {
			return new HashSet<>();
		}

		return mcmTreeRoots;
	}

	/**
	 * This is the way - THE ONLY WAY - to get CdmNodes, ANYWHERE.
	 * You don't construct them or whatever - you use this CdmCtrl
	 * as factory to get them.
	 * The reason why: It keeps track of whether the xml element
	 * was already converted to a CdmNode or not, and if it was,
	 * you get that existing CdmNode and not a new one!
	 * TODO :: also take care that other elements, such as CdmProcedure
	 * etc., never get created except once (e.g. in here) and elsewise
	 * just get gotten...
	 */
	public CdmNode getByXmlElement(CdmFileBase parentFile, XmlElement xmEl) {

		XmlElement currentExt = xmEl.getExtendingObject();

		if (currentExt instanceof CdmNode) {
			return (CdmNode) currentExt;
		}

		CdmNode newNode = new CdmNode(parentFile, xmEl, this);

		xmEl.setExtendingObject(newNode);

		return newNode;
	}

	/**
	 * This is the quick-access version of findByUuid - where findByUuid really searches everywhere,
	 * this here just looks up the correct node in the internal map; assuming all IDs are indeed unique,
	 * as they should be, this here also works and is much faster - so use this function for internal access!
	 */
	public CdmNode getByUuid(String ecoreUuid) {

		if (ecoreUuid == null) {
			return null;
		}

		ecoreUuid = UuidEncoderDecoder.getIdFromEcoreLink(ecoreUuid);

		return xmiIdMap.get(ecoreUuid);
	}

	/**
	 * In the interest of speed when calling this function, you have to ensure that the UUID
	 * is also an Ecore one! No passing Java UUIDs to this function, you! :P
	 */
	public List<CdmNode> findByUuid(String ecoreUuid) {

		ecoreUuid = UuidEncoderDecoder.getIdFromEcoreLink(ecoreUuid);

		List<CdmNode> result = new ArrayList<>();

		for (CdmFile cdmFile : fileList) {
			cdmFile.findByUuid(ecoreUuid, result);
		}

		return result;
	}

	public List<CdmNode> findByName(String name) {

		List<CdmNode> result = new ArrayList<>();

		for (CdmFile cdmFile : fileList) {
			cdmFile.findByName(name, result);
		}

		return result;
	}

	/**
	 * Works for MCEs, but also parameters, activities, etc.
	 * This should only every return one result in the list, but in very very silly,
	 * very malformed CDMs might also return several ones, which have the same path
	 * but are different elements... lol
	 * The path here is a full path - no regexes or nonsense like that!
	 */
	public List<CdmNode> findByPath(String path) {

		List<CdmNode> result = new ArrayList<>();

		if (path == null) {
			return result;
		}

		if (path.equals("")) {
			return result;
		}

		String[] paths = path.split("\\.");

		Set<CdmMonitoringControlElement> roots = getAllMcmTreeRoots();

		// iterate over all roots that we are aware of ...
		for (CdmMonitoringControlElement root : roots) {

			CdmMonitoringControlElement curEl = root;

			// ... and, first of all, drop the lead if the root is already wrong ...
			if (!paths[0].equals(curEl.getName())) {
				continue;
			}

			// ... and if the path was just this (just the one root element), return this element ...
			if (paths.length == 1) {
				result.add(root);
				continue;
			}

			// ... and then check all the MCEs from the second path element to the second-last ...
			for (int pathNum = 1; pathNum < paths.length - 1; pathNum++) {
				curEl = curEl.getSubElementByName(paths[pathNum]);
				if (curEl == null) {
					break;
				}
			}
			if (curEl == null) {
				continue;
			}

			// ... and finally check the last path element, which could also be an MCE,
			// or just an aspect!
			String lastPath = paths[paths.length - 1];
			CdmMonitoringControlElement finalMCE = curEl.getSubElementByName(lastPath);
			if (finalMCE != null) {
				result.add(finalMCE);
			}
			CdmNode finalAspect = curEl.getAspectByName(lastPath);
			if (finalAspect != null) {
				result.add(finalAspect);
			}
		}

		return result;
	}

	public List<CdmNode> findByType(String type) {

		List<CdmNode> result = new ArrayList<>();

		for (CdmFile cdmFile : fileList) {
			cdmFile.findByType(type, result);
		}

		return result;
	}

	public List<CdmNode> findByXmlTag(String xmlTag) {

		List<CdmNode> result = new ArrayList<>();

		for (CdmFile cdmFile : fileList) {
			cdmFile.findByXmlTag(xmlTag, result);
		}

		return result;
	}

	// this one is right, the other one is left
	public List<String> findDifferencesFrom(CdmCtrl otherCdm) {

		List<String> result = new ArrayList<>();

		List<CdmFile> onlyLeft = new ArrayList<>(otherCdm.getCdmFiles());
		List<CdmFile> onlyRight = new ArrayList<>(getCdmFiles());
		List<Pair<CdmFile, CdmFile>> both = new ArrayList<>();

		for (CdmFile file : getCdmFiles()) {
			String relativePath = file.getPathRelativeToCdmRoot();

			for (CdmFile otherFile : otherCdm.getCdmFiles()) {
				if (relativePath.equals(otherFile.getPathRelativeToCdmRoot())) {
					both.add(new Pair<>(otherFile, file));
					onlyLeft.remove(otherFile);
					onlyRight.remove(file);
					break;
				}
			}
		}

		if (onlyLeft.size() > 0) {
			if (onlyLeft.size() == 1) {
				result.add("This file is only contained in the left CDM:");
			} else {
				result.add("These files are only contained in the left CDM:");
			}
			for (CdmFile onlyLeftFile : onlyLeft) {
				result.add("  " + onlyLeftFile.getPathRelativeToCdmRoot());
			}
		}

		if (onlyRight.size() > 0) {
			if (onlyRight.size() == 1) {
				result.add("This file is only contained in the right CDM:");
			} else {
				result.add("These files are only contained in the right CDM:");
			}
			for (CdmFile onlyRightFile : onlyRight) {
				result.add("  " + onlyRightFile.getPathRelativeToCdmRoot());
			}
		}

		for (Pair<CdmFile, CdmFile> filePair : both) {
			result.addAll(filePair.getRight().findDifferencesFrom(filePair.getLeft()));
		}

		return result;
	}

	public void debugPrintAll() {

		Set<CdmFile> files = getCdmFiles();

		if (files.size() < 1) {
			if (lastFailedCdmFile != null) {
				lastFailedCdmFile.print();
			}
		} else {
			for (CdmFile cdmFile : files) {
				cdmFile.print();
			}
		}
	}

}
