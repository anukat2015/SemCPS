/**
 * @Copyright EIS University of Bonn
 */

package util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

/**
 * The aim of this class is to load the RDF configuration file to this program
 * containing all the input data
 * 
 * @class ConfigManager
 * @Version = 1.0
 * @Date 4/21/2016
 * @author Irlan
 **/

public class ConfigManager {

	private static ConfigManager manager;
	static Properties prop;
	private static RDFNode literal;
	private static RDFNode predicate;
	private static ArrayList<RDFNode> literals, predicates;
	private static Model model;

	public final static String HET_NAMESPACE = "http://vocab.cs.uni-bonn.de/het#";
	public final static String URI_NAMESPACE = "http://uri4uri.net/vocab.html/#";
	public final static String ONTO_NAMESPACE = "http://www.semanticweb.org/ontologies/2008/11/"
			+ "OntologySecurity.owl#";
	public final static String STO_NAMESPACE = "https://w3id.org/i40/sto#";

	/**
	 * Get the instance of manager
	 * 
	 * @return manager
	 */
	public static ConfigManager getInstance() {

		if (manager == null) {
			manager = new ConfigManager();
		}
		return manager;
	}

	/**
	 * This method load the Configuration file parameters
	 */
	public static Properties loadConfig() {
		prop = new Properties();
		String dir = System.getProperty("user.dir");
		File configFile = new File(dir + "/config.ttl");

		if (configFile.isFile() == false) {
			System.out.println("Please especify the configuration file" + "(config.ttl)");
			System.exit(0);
		}

		if (configFile.length() == 0) {
			System.out.println("The configuration file (config.ttl) is empty");
			System.exit(0);
		}

		model = ModelFactory.createDefaultModel();
		InputStream inputStream = FileManager.get().open(configFile.getPath());
		model.read(new InputStreamReader(inputStream), null, "TURTLE");
		// parses an InputStream assuming RDF in Turtle format

		literals = new ArrayList<RDFNode>();
		predicates = new ArrayList<RDFNode>();

		StmtIterator iterator = model.listStatements();

		while (iterator.hasNext()) {

			Statement stmt = iterator.nextStatement();

			predicate = stmt.getPredicate();
			predicates.add(predicate);

			literal = stmt.getLiteral();
			literals.add(literal);

		}

		for (int i = 0; i < predicates.size(); ++i) {
			for (int j = 0; j < literals.size(); ++j) {
				String key = predicates.get(j).toString();
				String value = literals.get(j).toString();
				prop.setProperty(key, value);
			}
		}

		return prop;
	}

	/**
	 * Get the general file path where all the files are located
	 * 
	 * @return
	 */
	public static String getFilePath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "path");
		return filePath;
	}
	
	/**
	 * Get the general file path where all the files are located
	 * 
	 * @return
	 */
	public static String getOntoURIPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "URI");
		return filePath;
	}

	/**
	 * Get the general file path where all the test data files are located
	 * 
	 * @return
	 */

	public static boolean createDataPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "path");
		boolean dir = new File(filePath + "PSL/test/Precision").mkdirs();
		dir = new File(filePath + "PSL/train/").mkdirs();
		return dir;
	}

	public static String getTestDataPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "testDataPath");
		return filePath;
	}

	/**
	 * Reads the configuration regarding the existence of a training set or not
	 * @return true or false
	 */
	public static String getExecutionMethod() {
		String filePath = loadConfig().getProperty(ONTO_NAMESPACE + "Training");
		return filePath;
	}
	
	/**
	 * Reads the configuration to check whether the ontological predicates 
	 * will be used or not
	 * @return true or false
	 */
	public static String getOntoPredicates() {
		String filePath = loadConfig().getProperty(ONTO_NAMESPACE + "ontoPredicates");
		return filePath;
	}

	/**
	 * Get the general file path where all the test data files are located
	 * 
	 * @return
	 */
	public static String getTrainDataPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "trainDataPath");
		return filePath;
	}

	public static String getStandard() {
		String standard = loadConfig().getProperty(STO_NAMESPACE + "Standard");
		return standard;
	}

}
