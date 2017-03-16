
package pslApproach;

import java.text.DecimalFormat;

import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.config.*;
import edu.umd.cs.psl.database.DataStore;
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type;
import edu.umd.cs.psl.groovy.*;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.argument.type.*;
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.model.predicate.type.*;
import edu.umd.cs.psl.ui.functions.textsimilarity.*;
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;

ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("ontology-alignment")

def defaultPath = System.getProperty("java.io.tmpdir")
String dbpath = config.getString("dbpath", defaultPath + File.separator + "ontology-alignment")
DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)
PSLModel m = new PSLModel(this, data);


public void definePredicates(){
	m.add predicate: "name"        , types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

	m.add predicate: "fromDocument", types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

	m.add predicate: "Attribute"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

	m.add predicate: "InternalElements"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

	m.add predicate: "hasRefSemantic"     , types: [ArgumentType.UniqueID, ArgumentType.String];

	m.add predicate: "hasID"     , types: [ArgumentType.UniqueID, ArgumentType.String];

	m.add predicate: "hasInternalLink"     , types: [ArgumentType.UniqueID, ArgumentType.String];

	m.add predicate: "hasEClassVersion"     , types: [ArgumentType.UniqueID, ArgumentType.String];

	m.add predicate: "hasEClassClassificationClass"     , types: [ArgumentType.UniqueID, ArgumentType.String];

	m.add predicate: "hasEClassIRDI"     , types: [ArgumentType.UniqueID, ArgumentType.String];

	m.add predicate: "similar"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]l;

	m.add predicate: "similarType"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID];
}


public void defineFunctions(){
	m.add function: "similarValue"  , implementation: new MyStringSimilarity();
}

public void defineRules(){

	// Two AML Attributes are the same if its RefSemantic are the same
	m.add rule : (Attribute(A,X) & Attribute(B,Y) & hasRefSemantic(X,Z) & hasRefSemantic(Y,W) & similarValue(Z,W) &
	fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 10;

	// Two AMl Attributes are the same if they share the same ID
	m.add rule : (Attribute(A,X) & Attribute(B,Y) & hasID(A,Z) & hasID(Y,W) & similarValue(Z,W) &
	fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 5;

	// Two AMl InternalElements are the same if they share the same ID
	m.add rule : (InternalElements(A,X) & InternalElements(B,Y) & hasID(A,Z) & hasID(Y,W) & similarValue(Z,W) &
	fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 5;

	m.add rule : (hasID(X,Z) & hasID(Y,W) & similarValue(Z,W) & fromOntology(X,O1) & fromOntology(Y,O2) & (O1-O2)) >> similar(X,Y), weight : 1000;

	// Two AML Attributes are semantically the same if its eclass,IRDI and classification class are the same
	m.add rule :( Attribute(E,X) & Attribute(U,Y)  & hasEClassIRDI(X,Z) & hasEClassIRDI(Y,W) & similarValue(Z,W)
	& Attribute(E,Q) & Attribute(U,T) & hasEClassVersion(Q,M) & hasEClassVersion(T,N) & similarValue(M,N)
	& Attribute(E,D) & Attribute(U,K) & hasEClassVersion(D,O) & hasEClassVersion(K,L) & similarValue(O,L)
	& fromDocument(E,O1) & fromDocument(U,O2) & (O1-O2)) >> similar(E,U) , weight : 12;

	// Two InternalElements are the same if its InternalLink is the same
	m.add rule : (InternalElements(A,X) & InternalElements(B,Y)  & hasInternalLink(X,Z) & hasInternalLink(Y,W) &
	similarValue(Z,W) & fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 12;

	// constraints
	m.add PredicateConstraint.PartialFunctional , on : similar;
	m.add PredicateConstraint.PartialInverseFunctional , on : similar;
	m.add PredicateConstraint.Symmetric , on : similar;

	// prior
	m.add rule : ~similar(A,B), weight: 1;
}


GroundTerm classID = data.getUniqueID("class");



//////////////////////////// data setup ///////////////////////////

/* Loads data */
def dir = 'data' + java.io.File.separator + 'ontology' + java.io.File.separator;

/////////////////////////// test setup //////////////////////////////////

def testDir = dir + 'test' + java.io.File.separator;
Partition testObservations = new Partition(3);
Partition testPredictions = new Partition(4);
for (Predicate p : [fromDocument, name, Attribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, InternalElements])
{
	insert = data.getInserter(p, testObservations);
	InserterUtils.loadDelimitedData(insert, testDir + p.getName().toLowerCase() + ".txt");

}

Database testDB = data.getDatabase(testPredictions, [name, fromDocument, Attribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, InternalElements] as Set, testObservations);

populateSimilar(testDB);

/////////////////////////// test inference //////////////////////////////////
println "INFERRING...";

MPEInference inference = new MPEInference(m, testDB, config);
inference.mpeInference();
inference.close();

println "INFERENCE DONE";
def file1 = new File('data/ontology/test/similar.txt')
file1.write('')
DecimalFormat formatter = new DecimalFormat("#.##");
for (GroundAtom atom : Queries.getAllAtoms(testDB, similar)){
	println atom.toString() + ": " + formatter.format(atom.getValue());
	// only writes if its equal to 1 or u can set the threshold
	if(formatter.format(atom.getValue())>="0.5"){
		println 'matches threshold writing to similar.txt'
		file1.append('\n' + atom.toString())
	}
}


/**
 * Populates all the similar atoms between the concepts of two Documents using
 * the fromDocument predicate.
 * 
 * @param db  The database to populate. It should contain the fromDocument atoms
 */
void populateSimilar(Database db) {
	/* Collects the ontology concepts */
	Set<GroundAtom> concepts = Queries.getAllAtoms(db, fromDocument);
	Set<GroundTerm> o1 = new HashSet<GroundTerm>();
	Set<GroundTerm> o2 = new HashSet<GroundTerm>();
	for (GroundAtom atom : concepts) {
		if (atom.getArguments()[1].toString().equals("aml"))
			o1.add(atom.getArguments()[0]);
		else
			o2.add(atom.getArguments()[0]);
	}

	/* Populates manually (as opposed to using DatabasePopulator) */
	for (GroundTerm o1Concept : o1) {
		for (GroundTerm o2Concept : o2) {
			((RandomVariableAtom) db.getAtom(similar, o1Concept, o2Concept)).commitToDB();
			((RandomVariableAtom) db.getAtom(similar, o2Concept, o1Concept)).commitToDB();
		}
	}

}
