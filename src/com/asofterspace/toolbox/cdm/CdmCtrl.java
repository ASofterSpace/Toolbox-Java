package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
import com.asofterspace.toolbox.io.XmlMode;
import com.asofterspace.toolbox.utils.NoOpProgressIndicator;
import com.asofterspace.toolbox.utils.ProgressIndicator;
import com.asofterspace.toolbox.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* // TAKE OUT EMF DEPENDENCIES
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.Resource;
*/ // TAKE OUT EMF DEPENDENCIES

public class CdmCtrl {

	public static final String CDM_NAMESPACE_MIDDLE = "ConfigurationTracking/";
	// public static final String ASS_CDM_NAMESPACE_ROOT = "http://www.asofterspace.com/";
	// public static final String ASS_CDM_NAMESPACE = ASS_CDM_NAMESPACE_ROOT + "ConfigurationTracking/";

	public static final String DEFAULT_NAMESPACE = "DefaultNamespace";

	public static final String MCM_PATH_DELIMITER = ".";

	private static final List<String> KNOWN_CDM_VERSIONS = Arrays.asList(
		"1.14.0",
		"1.14.0b",
		"1.13.0bd1",
		"1.12.1",
		"1.12"
	);

	private static final List<String> KNOWN_CDM_PREFIXES = Arrays.asList(
		"http://www.esa.int/egscc/",
		"http://www.esa.int/dme/",
		"http://www.esa.int/",
		"http://www.esa.int/dme/",
		"http://www.scopeset.de/"
	);

	// a reasonable default CDM prefix to be used in case a user-supplied CDM version is not known
	public static final String REASONABLE_DEFAULT_CDM_PREFIX = "http://www.esa.int/dme/";

	private static final String TMPL_JUST_ROOT = "Just Root Element";
	private static final String TMPL_JUST_ROOT_SHORT = "just_root";
	private static final String TMPL_ROOT_ROUTE_SAP = "Root Element with Default Route and SAP";
	private static final String TMPL_ROOT_ROUTE_SAP_SHORT = "root_route_sap";
	private static final String TMPL_ROOT_ROUTE_SAP_EX_TYPE = "Root Element with Route, SAP and Example Data Type";
	private static final String TMPL_ROOT_ROUTE_SAP_EX_TYPE_SHORT = "root_route_sap_ex_type";

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
	private static boolean cdmLoaded = false;

	// all of the loaded CDM files (intended more for internal-ish use)
	private static List<CdmFile> fileList = new ArrayList<>();

	// our model of the CDM (intended more for external-ish use)
	private static List<CdmMonitoringControlElement> mces;
	private static List<CdmScript> scripts;
	private static List<CdmFile> scriptToActivityMappingCIs;
	private static List<CdmScript2Activity> scriptToActivityMappings;
	private static List<CdmActivity> activities;

	private static Directory lastLoadedDirectory;


	// call this on a different thread please, as it can take forever
	// (and the updating of the progress bar only works if this is on a different thread!)
	public static void loadCdmDirectory(Directory cdmDir, ProgressIndicator progress) throws AttemptingEmfException, CdmLoadingException {

		cdmLoaded = false;

		fileList = new ArrayList<>();
		mces = new ArrayList<>();
		scripts = new ArrayList<>();
		scriptToActivityMappingCIs = new ArrayList<>();
		scriptToActivityMappings = new ArrayList<>();
		activities = new ArrayList<>();

		List<File> cdmFiles = cdmDir.getAllFiles(true);

		double i = 0;
		double len = cdmFiles.size();

		try {

			if (len <= 0) {
				throw new CdmLoadingException("The directory " + cdmDir.getDirname() + " does not seem to contain any .cdm files at all.");
			}

			for (File cdmFile : cdmFiles) {
				if (cdmFile.getFilename().endsWith(".cdm")) {
					loadAnotherCdmFile(cdmFile);
					i++;
					progress.setProgress(i / len);
				}
			}

			lastLoadedDirectory = cdmDir;

			// reload the model once, after all the CDM files have been loaded
			reloadModel();

			cdmLoaded = true;

		} finally {
			progress.done();
		}
	}

	public static CdmFile loadCdmFile(File cdmFile) throws AttemptingEmfException, CdmLoadingException {

		CdmFile result = loadAnotherCdmFile(cdmFile);

		// as this function was called from - gasp! - the outside world, we have to reload the model now...
		// at least for this one file ;)
		reloadModel(result);

		return result;
	}

	private static CdmFile loadAnotherCdmFile(File cdmFile) throws AttemptingEmfException, CdmLoadingException {

		CdmFile result = loadCdmFileViaXML(cdmFile);

		switch (result.getMode()) {

			case XML_LOADED:
				// all is good!;
				break;

			case EMF_LOADED:
				throw new AttemptingEmfException("The CDM file " + cdmFile.getLocalFilename() + " is an EMF binary file, which is not yet supported.\nPlease only use CDM files in XML format.");

			case NONE_LOADED:
			default:
				throw new CdmLoadingException("There was a problem while loading the CDM file " + cdmFile.getLocalFilename() + ".");
		}

		// TODO - also get the EMF stuff to work ;)
		// loadCdmFileViaEMF(cdmFile);

		fileList.add(result);

		return result;
	}

	private static CdmFile loadCdmFileViaXML(File cdmFile) {

		try {
			CdmFile cdm = new CdmFile(cdmFile);

			return cdm;

		} catch (Exception e) {
			System.err.println(e);
		}

		return null;
	}

	private static void loadCdmFileViaEMF(File cdmFile) {

/* // TAKE OUT EMF DEPENDENCIES
		// TODO - load the CDM File using EMF: https://www.eclipse.org/modeling/emf/
		// you can get EMF from here: http://www.eclipse.org/modeling/emf/downloads/
		// TODO - add CDM namespaces... we need some .ecore files or somesuch?
		// do this similar to: EPackage.Registry.INSTANCE.put("schemas.xmlsoap.org/wsdl/", "file:/C:/workspace/Trans/bin/metamodels/WSDL.ecore");
		// or similar to: Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
		// also see: http://www.vogella.com/tutorials/EclipseEMF/article.html (so apparently we need .ecore and genmodel files, but genmodel can be created from ecore)
		// >> as all of this is rather cumbersome, maybe go for plain XML for now after all...

		System.out.println(cdmFile); // debug
		java.net.URI cdmURI = cdmFile.getURI();
		System.out.println(cdmURI); // debug

		// try to read an XML CDM...
		XMIResource xResource = new XMIResourceImpl(URI.createURI(cdmURI.toString()));
		try {
			xResource.load(null);
			System.out.println(xResource.getContents().get(0)); // debug
		} catch (IOException ex) {
			// ... there was an exception! Must be binary then...
			System.out.println(ex); // debug
			Resource bResource = new BinaryResourceImpl(URI.createURI(cdmURI.toString()));
			try {
				bResource.load(null);
				System.out.println(bResource.getContents().get(0)); // debug
			} catch (IOException eb) {
				// ... oh wow; not binary either. Is this a CDM encoded in Morse code?
				System.out.println(eb); // debug
			}
		}
*/ // TAKE OUT EMF DEPENDENCIES
	}

	/**
	 * Reload the entire internal model of the CDM
	 */
	private static void reloadModel() {
		for (CdmFile cdmFile : fileList) {
			reloadModel(cdmFile);
		}
	}

	/**
	 * Reload the internal model of the CDM for one particular file
	 */
	private static void reloadModel(CdmFile cdmFile) {

		switch (cdmFile.getCiType()) {

			case "configurationcontrol:ScriptCI":
				scripts.addAll(cdmFile.getScripts());
				break;

			case "configurationcontrol:Script2ActivityMapperCI":
				scriptToActivityMappingCIs.add(cdmFile);
				scriptToActivityMappings.addAll(cdmFile.getScript2Activities());
				break;

			case "configurationcontrol:McmCI":
				activities.addAll(cdmFile.getActivities());
				mces.addAll(cdmFile.getMonitoringControlElements());
				break;
		}
	}

	public static boolean hasCdmBeenLoaded() {
		return cdmLoaded;
	}

	/**
	 * Save all currently opened files - the ones that have been deleted (so far just set an internal flag)
	 * will delete their contents from the disk
	 */
	public static void save() {

		for (CdmFile cdmFile : fileList) {
			cdmFile.save();
		}
	}

	/**
	 * Save all currently opened files to the new location
	 */
	public static void saveTo(Directory newLocation) {

		for (CdmFile cdmFile : fileList) {
			cdmFile.saveTo(lastLoadedDirectory.traverseFileTo(cdmFile, newLocation));
		}

		lastLoadedDirectory = newLocation;
	}

	public static Directory getLastLoadedDirectory() {
		return lastLoadedDirectory;
	}

	/**
	 * Convert all loaded CDM files to the given version and prefix - if either is null,
	 * keep the current one of the first file (so e.g. if the CDM contains two files,
	 * one in version 1.12 and one in version 1.12.1, and version is given as null,
	 * then both will be converted to 1.12, so at least they are all consistent!)
	 */
	public static void convertTo(String toVersion, String toPrefix) {

		if (toVersion == null) {
			toVersion = getCdmVersion();
		}

		if (toPrefix == null) {
			toPrefix = getCdmVersionPrefix();
		}

		for (CdmFile cdmFile : fileList) {
			cdmFile.convertTo(toVersion, toPrefix);
		}
	}

	/**
	 * Get the CDM version of one CDM file at random - as they should all have the same version,
	 * we would like to receive the correct one no matter which one is being used ;)
	 */
	public static String getCdmVersion() {

		if (fileList.size() <= 0) {
			return "";
		}

		return fileList.get(0).getCdmVersion();
	}

	public static String getCdmVersionPrefix() {

		if (fileList.size() <= 0) {
			return "";
		}

		return fileList.get(0).getCdmVersionPrefix();
	}

	public static String getCompatWithEGSCCstr(String version, String prefix) {

		final String NOT_A_RELEASE = "(not included in any official EGS-CC release)";

		switch (version) {
			case "1.13.0bd1":
				if ("http://www.esa.int/".equals(prefix)) {
					return "IR3, IR3p1, IR3p2";
				}
				return NOT_A_RELEASE;
			case "1.13.0":
				return NOT_A_RELEASE;
			case "1.14.0b":
				return NOT_A_RELEASE;
			case "1.14.0":
				if ("http://www.esa.int/egscc/".equals(prefix)) {
					return "IR4";
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

	public static List<CdmMonitoringControlElement> getMonitoringControlElements() {
		if (!cdmLoaded) {
			return new ArrayList<>();
		}
		return mces;
	}

	public static List<CdmScript> getScripts() {
		if (!cdmLoaded) {
			return new ArrayList<>();
		}
		return scripts;
	}

	public static List<CdmFile> getScriptToActivityMappingCIs() {
		if (!cdmLoaded) {
			return new ArrayList<>();
		}
		return scriptToActivityMappingCIs;
	}

	public static List<CdmScript2Activity> getScriptToActivityMappings() {
		if (!cdmLoaded) {
			return new ArrayList<>();
		}
		return scriptToActivityMappings;
	}

	public static List<CdmActivity> getActivities() {
		if (!cdmLoaded) {
			return new ArrayList<>();
		}
		return activities;
	}

	/**
	 * Get the list of CDM files that have been loaded
	 */
	public static List<CdmFile> getCdmFiles() {
		if (!cdmLoaded) {
			return new ArrayList<>();
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
	public static int checkValidity(List<String> outProblemsFound) {

		// innocent unless proven otherwise
		int verdict = 0;

		// TODO :: also check not just the main version of the file, but all the other versions in the xmlns thingenses?

		// validate that all CDM files are using the same CDM version
		// btw., we do NOT need to check that the version prefixes are all the same,
		// as we check that all versions are the same, and for each file we check
		// that the prefix agrees with the version... so it follows that, if we do
		// not complain, then the prefixes are all the same too :)

		List<CdmFile> cdmFiles = getCdmFiles();
		List<String> cdmVersionsFound = new ArrayList<>();

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
			if (!cdmVersionsFound.contains(curVersion)) {
				cdmVersionsFound.add(curVersion);
			}
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

		// TODO :: check that in version 1.14.0, all arguments have names and arg values have names and values!
		// (and eng args have eng values rather than raw values...)

		// TODO :: check that all activity mappers are fully filled (e.g. no script or activity missing)

		// TODO :: check that all CIs have at least one child

		// TODO :: check that all references actually lead to somewhere

		// TODO :: check that there is exactly one root node of the merged MCM tree (so no more or less than one MCE that is not
		// listed in other MCEs as subElement)

		// TODO :: check that every MCE has an MCE definition

		return verdict;
	}

	public static CdmActivity addActivity(String newActivityName, String newActivityAlias, CdmMonitoringControlElement mceContainingThis) {

		// TODO :: ensure that the name is not yet taken

		CdmActivity createdActivity = mceContainingThis.addActivity(newActivityName, newActivityAlias);

		// append the resulting new mapping to the internal list of mappings of the CdmCtrl
		activities.add(createdActivity);

		return createdActivity;
	}

	public static CdmScript2Activity addScriptToActivityMapping(CdmScript script, CdmActivity activity) {

		// first of all, get all script to activity mapping CIs
		List<CdmFile> scriptToActivityMapperCis = getScriptToActivityMappingCIs();

		// if there are none, create a new one
		if (scriptToActivityMapperCis.size() < 1) {
			if (!addScriptToActivityCI()) {
				return null;
			}
		}

		// add a new script to activity mapping to the largest script to activity mapping CI that comes into our hands
		int largestFoundHas = -1;
		CdmFile scriptToActivityMapperCI = scriptToActivityMapperCis.get(0);
		for (CdmFile curCI : scriptToActivityMapperCis) {
			int nowFoundHas = curCI.getScript2Activities().size();
			if (nowFoundHas > largestFoundHas) {
				scriptToActivityMapperCI = curCI;
				largestFoundHas = nowFoundHas;
			}
		}

		// TODO :: make this name configurable?
		String mappingBaseName = script.getName() + "_mapping";
		String mappingName = mappingBaseName;

		// ensure that the name is not yet taken
		List<CdmScript2Activity> existingMappers = getScriptToActivityMappings();

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
		String scriptFile = scriptToActivityMapperCI.getParentDirectory().getRelativePath(script.getParent());
		// we want a path with forward slashes, even under Windows, as the CDM is always written Linux-y
		scriptFile = IoUtils.osPathStrToLinuxPathStr(scriptFile);
		String scriptId = script.getId();

		// do not just use the filename, but keep track of the relative paths - here, the relative path
		// of the activity relative to the script to activity mapper CI
		String activityFile = scriptToActivityMapperCI.getParentDirectory().getRelativePath(activity.getParent());
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

		// append the resulting new mapping to the internal list of mappings of the CdmCtrl
		scriptToActivityMappings.add(createdMapping);

		return createdMapping;
	}

	public static String getXMLNS() {
		return "xmlns:configurationcontrol=\"" + getCdmVersionPrefix() + CDM_NAMESPACE_MIDDLE + getCdmVersion() + "\"";
	}

	/**
	 * Tries to add a new script to activity CI
	 * Returns true if successful, false otherwise
	 */
	public static boolean addScriptToActivityCI() {

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
			"<configurationcontrol:Script2ActivityMapperCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" " + getXMLNS() + " xmi:id=\"" + Utils.generateEcoreUUID() + "\" externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\" name=\"" + newCiName + "\" onlineRevisionIdentifier=\"0\">\n" +
			"</configurationcontrol:Script2ActivityMapperCI>";

		File tmpCi = new File("tmpfile.tmp");
		tmpCi.setContent(newCiContent);
		tmpCi.save();

		try {
			CdmFile newCdmFile = loadCdmFile(tmpCi);

			List<CdmFile> scriptToActivityMapperCis = getScriptToActivityMappingCIs();

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
	public static boolean createNewCdm(String cdmPath, String version, String versionPrefix, String template) throws AttemptingEmfException, CdmLoadingException {

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
		File mcmCi;

		// btw., all of our templates are written for version 1.14.0... so we convert them later on in this function to whatever version is actually required ^^
		String templateVersion = "1.14.0";
		String templateVersionPrefix = "http://www.esa.int/egscc/";

		switch (template) {

			case TMPL_JUST_ROOT:
			case TMPL_JUST_ROOT_SHORT:

				// create just the ResourceMcm.cdm file in XML format with one root node (mcmRoot)
				newCiName = "Mcm";
				mcmRootDefinitionUuid = Utils.generateEcoreUUID();
				resourceMcmContent =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<configurationcontrol:McmCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:checkandcondition=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/CheckAndCondition/" + templateVersion + "\" xmlns:configurationcontrol=\"" + templateVersionPrefix + CDM_NAMESPACE_MIDDLE + templateVersion + "\" xmlns:mcmchecks=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/MCMChecks/" + templateVersion + "\" xmlns:mcmimplementationitems=\"" + templateVersionPrefix + "MonitoringControl/MCMImplementationItems/" + templateVersion + "\" xmlns:monitoringcontrolcommon=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/" + templateVersion + "\" xmlns:monitoringcontrolmodel=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/" + templateVersion + "\" xmi:id=\"" + Utils.generateEcoreUUID() + "\" " + createExternalVersionLabel() + " onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
					"  <monitoringControlElement xmi:id=\"" + Utils.generateEcoreUUID() + "\" name=\"mcmRoot\" subElements=\"\" definition=\"" + mcmRootDefinitionUuid + "\">\n" +
					"  </monitoringControlElement>\n" +
					"  <monitoringControlElementDefinition xmi:id=\"" + mcmRootDefinitionUuid + "\" name=\"mcmRoot_Definition\" subElements=\"\">\n" +
					"  </monitoringControlElementDefinition>\n" +
					"</configurationcontrol:McmCI>";

				mcmCi = new File(cdmDir, "Resource_" + newCiName + ".cdm");
				mcmCi.setContent(resourceMcmContent);
				mcmCi.save();

				// also create the Manifest file
				// TODO

				break;

			case TMPL_ROOT_ROUTE_SAP_EX_TYPE:
			case TMPL_ROOT_ROUTE_SAP_EX_TYPE_SHORT:

				newCiName = "DataTypes";
				String displayFormatUuid = Utils.generateEcoreUUID();
				resourceMcmContent =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<configurationcontrol:DataTypesCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:configurationcontrol=\"" + templateVersionPrefix + CDM_NAMESPACE_MIDDLE + templateVersion + "\" xmlns:monitoringcontrolcommon=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/" + templateVersion + "\" xmi:id=\"" + Utils.generateEcoreUUID() + "\" " + createExternalVersionLabel() + " onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
					"  <abstractDataType xsi:type=\"monitoringcontrolcommon:SignedInteger\" xmi:id=\"" + Utils.generateEcoreUUID() + "\" name=\"INT32\" bitLength=\"32\" signedIntegerDisplayFormat=\"" + displayFormatUuid + "\"/>\n" +
					"  <abstractDataDisplayFormat xsi:type=\"monitoringcontrolcommon:SignedIntegerDisplayFormat\" xmi:id=\"" + displayFormatUuid + "\" name=\"INT32Format\" format=\"decimal\"/>\n" +
					"</configurationcontrol:DataTypesCI>\n";

				mcmCi = new File(cdmDir, "Resource_" + newCiName + ".cdm");
				mcmCi.setContent(resourceMcmContent);
				mcmCi.save();

				// also add this to the Manifest file
				// TODO

				// INTENTIONALLY FALL THROUGH TO THE NEXT CASE - as the McmCI is the same! ^^

			case TMPL_ROOT_ROUTE_SAP:
			case TMPL_ROOT_ROUTE_SAP_SHORT:

				// create just the ResourceMcm.cdm file in XML format with one root node (mcmRoot)
				newCiName = "Mcm";
				String routeUuid = Utils.generateEcoreUUID();
				String routeTypeUuid = Utils.generateEcoreUUID();
				String sapUuid = Utils.generateEcoreUUID();
				mcmRootDefinitionUuid = Utils.generateEcoreUUID();
				String routeDefinitionUuid = Utils.generateEcoreUUID();
				String routeTypeDefinitionUuid = Utils.generateEcoreUUID();
				String sapDefinitionUuid = Utils.generateEcoreUUID();
				resourceMcmContent =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<configurationcontrol:McmCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:checkandcondition=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/CheckAndCondition/" + templateVersion + "\" xmlns:configurationcontrol=\"" + templateVersionPrefix + CDM_NAMESPACE_MIDDLE + templateVersion + "\" xmlns:mcmchecks=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/MCMChecks/" + templateVersion + "\" xmlns:mcmimplementationitems=\"" + templateVersionPrefix + "MonitoringControl/MCMImplementationItems/" + templateVersion + "\" xmlns:monitoringcontrolcommon=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlCommon/" + templateVersion + "\" xmlns:monitoringcontrolmodel=\"" + templateVersionPrefix + "MonitoringControl/MonitoringControlModel/" + templateVersion + "\" xmi:id=\"" + Utils.generateEcoreUUID() + "\" " + createExternalVersionLabel() + " onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
					"  <monitoringControlElement xmi:id=\"" + Utils.generateEcoreUUID() + "\" name=\"mcmRoot\" subElements=\"\" defaultRoute=\"" + routeUuid + "\" definition=\"" + mcmRootDefinitionUuid + "\" defaultServiceAccessPoint=\"" + sapUuid + "\">\n" +
					"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:Route\" xmi:id=\"" + routeUuid + "\" name=\"DefaultRoute\" baseElement=\"" + routeDefinitionUuid + "\" hasPredictedValue=\"false\" routeName=\"DefaultRoute\" routeID=\"1\" routeType=\"" + routeTypeUuid + "\"/>\n" +
					"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:RouteType\" xmi:id=\"" + routeTypeUuid + "\" name=\"DefaultRouteType\" baseElement=\"" + routeTypeDefinitionUuid + "\" hasPredictedValue=\"false\" routeIDType=\"1\"/>\n" +
					"    <monitoringControlElementAspects xsi:type=\"mcmimplementationitems:ServiceAccessPoint\" xmi:id=\"" + sapUuid + "\" name=\"13\" baseElement=\"" + sapDefinitionUuid + "\" hasPredictedValue=\"false\" validRoutes=\"" + routeUuid + "\"/>\n" +
					"  </monitoringControlElement>\n" +
					"  <monitoringControlElementDefinition xmi:id=\"" + mcmRootDefinitionUuid + "\" name=\"mcmRoot_Definition\" subElements=\"\">\n" +
					"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:Route\" xmi:id=\"" + routeDefinitionUuid + "\" name=\"DefaultRoute\" hasPredictedValue=\"false\" routeName=\"DefaultRouteDef\" routeID=\"1\" routeType=\"" + routeTypeDefinitionUuid + "\"/>\n" +
					"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:RouteType\" xmi:id=\"" + routeTypeDefinitionUuid + "\" name=\"DefaultRouteTypeDef\" hasPredictedValue=\"false\" routeIDType=\"1\"/>\n" +
					"    <monitoringControlElementAspects xsi:type=\"mcmimplementationitems:ServiceAccessPoint\" xmi:id=\"" + sapDefinitionUuid + "\" name=\"13\" hasPredictedValue=\"false\" validRoutes=\"" + routeDefinitionUuid + "\" />\n" +
					"  </monitoringControlElementDefinition>\n" +
					"</configurationcontrol:McmCI>";

				mcmCi = new File(cdmDir, "Resource_" + newCiName + ".cdm");
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

}
