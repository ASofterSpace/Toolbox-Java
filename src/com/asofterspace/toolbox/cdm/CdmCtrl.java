package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.gui.ProgressDialog;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
import com.asofterspace.toolbox.io.XmlMode;
import com.asofterspace.toolbox.Utils;

import java.io.IOException;
import java.util.ArrayList;
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
	public static void loadCdmDirectory(Directory cdmDir) throws AttemptingEmfException, CdmLoadingException {

		// add a progress bar (which is especially helpful when the CDM contains no scripts, so the main view stays empty after loading a CDM!)
		ProgressDialog progress = new ProgressDialog("Loading the CDM directory...");
	
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
				throw new CdmLoadingException("The directory " + cdmDir + " does not seem to contain any .cdm files at all.");
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
	 * returns true if it is valid, and false if it is not;
	 * in the case of it not being valid, the StringBuilder
	 * that has been passed in will be filled with more detailed
	 * explanations about why it is not valid
	 */
	public static boolean isCdmValid(StringBuilder outProblemsFound) {

		// innocent unless proven otherwise
		boolean verdict = true;

		// validate that all CDM files are using the same CDM version
		List<CdmFile> cdmFiles = CdmCtrl.getCdmFiles();
		List<String> cdmVersionsFound = new ArrayList<>();

		// TODO :: also check that the version prefixes are all the same?
		for (CdmFile file : cdmFiles) {
			String curVersion = file.getCdmVersion();
			if (!cdmVersionsFound.contains(curVersion)) {
				cdmVersionsFound.add(curVersion);
			}
		}

		// oh no, we have different CDM versions!
		if (cdmVersionsFound.size() > 1) {
			verdict = false;
			outProblemsFound.append("CIs with multiple CDM versions have been mixed together!\n");
			outProblemsFound.append("Found CDM versions: ");
			String sep = "";
			for (String cdmVersionFound : cdmVersionsFound) {
				outProblemsFound.append(sep);
				sep = ", ";
				outProblemsFound.append(cdmVersionFound);
			}
		}

		// TODO :: check that all activity mappers are fully filled (e.g. no script or activity missing),
		// and that these mappings then also exist (e.g. not mapping to a CI that is not existing, etc.)

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
		List<CdmFile> scriptToActivityMapperCis = CdmCtrl.getScriptToActivityMappingCIs();

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
		List<CdmScript2Activity> existingMappers = CdmCtrl.getScriptToActivityMappings();
		
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
			newFileLocation = new File(CdmCtrl.getLastLoadedDirectory(), "Resource_" + newCiName + ".cdm");

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
			"<configurationcontrol:Script2ActivityMapperCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" " + CdmCtrl.getXMLNS() + " xmi:id=\"" + Utils.generateEcoreUUID() + "\" externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\" name=\"" + newCiName + "\" onlineRevisionIdentifier=\"0\">\n" +
			"</configurationcontrol:Script2ActivityMapperCI>";

		File tmpCi = new File("tmpfile.tmp");
		tmpCi.setContent(newCiContent);
		tmpCi.save();

		try {
			CdmFile newCdmFile = CdmCtrl.loadCdmFile(tmpCi);

			List<CdmFile> scriptToActivityMapperCis = CdmCtrl.getScriptToActivityMappingCIs();

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

}
