package industryStandard;

import java.io.FileNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author Irlan Grangel
 *
 *         Represents the AutomationML Standard as an RDF graph
 */
public class AML extends IndustryStandards {

	public AML(Model model, int newNumber) {
		super(model, newNumber);
	}

	public AML() {
		// TODO Auto-generated constructor stub
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public void setNumber(int newNumber) {
		this.number = newNumber;
	}

	/**
	 * Automation ML part for data population
	 * 
	 * @throws FileNotFoundException
	 */
	public void addsDataforAML() throws FileNotFoundException {

		StmtIterator iterator = model.listStatements();
		// RefSemantic part starts here
		while (iterator.hasNext()) {

			Statement stmt = iterator.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
			object = stmt.getObject();

			if (number == 3) {

			} // all subjects are added according to ontology e.g aml
			else {
				addSubjectURI(subject, "", number, "hasDocument");
			}

			hasAttributeName();
			addDomainRange();
			addHasType();
			hasAttributeValue();
			addGenericObject("hasExternalReference", "refBaseClassPath");
			addGenericObject("hasInternalLink", "hasRefPartnerSideB");
			addGenericObject("hasInternalLink", "hasRefPartnerSideA");
			hasAttribute();
			addGenericObject("hasRefSemantic", "hasCorrespondingAttributePath");
			eClassCheck();
			hasIdentifier();
		}

	}

	/**
	 * This function checks if the attribute type is of eclass. Adds notation
	 * :remove marking it as literal value.
	 */
	private void eClassCheck() {
		if (object.isLiteral()) {
			if (checkEclass(object)) {
				StmtIterator stmts = model.listStatements(subject.asResource(), null,
						(RDFNode) null);
				while (stmts.hasNext()) {
					Statement stmte = stmts.nextStatement();

					if (stmte.getPredicate().asNode().getLocalName().equals("hasAttributeValue")) {

						if (object.asLiteral().getLexicalForm()
								.equals("eClassClassificationClass")) {
							addSubjectURI(subject,
									":remove" + stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEClassClassificationClass");
						}

						if (object.asLiteral().getLexicalForm().equals("eClassVersion")) {
							addSubjectURI(subject,
									":remove" + stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEclassVersion");
						}

						if (object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
							addSubjectURI(subject,
									":remove" + stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEclassIRDI");
						}

					}
				}
			}
		}
	}

	/**
	 * This function populates all data for Elements with ID.
	 */
	private void hasIdentifier() {
		// RefSemantic part starts here

		if (predicate.asNode().getLocalName().equals("identifier")) {
			// gets the literal ID value and add it to hasID
			addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
					"has" + getType(subject) + "ID");
		}
	}

	/**
	 * This function populates all data with Attribute.
	 */
	private void hasAttribute() {
		if (predicate.asNode().getLocalName().equals("hasAttribute")) {
			// gets all classes which hasAttribute relation
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					"has" + getType(subject));
		}
	}

	/**
	 * This function populates all data with Attribute Value.
	 */

	private void hasAttributeValue() {
		if (predicate.asNode().getLocalName().equals("hasAttributeValue")) {
			if (!checkEclass(object)) {
				addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
						predicate.asNode().getLocalName());
			}
		}

	}

	/**
	 * This function populates with type of Element.
	 */

	private void addHasType() {
		if (predicate.asNode().getLocalName().equals("type")) {
			if (number != 3)
				addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
						"has" + predicate.asNode().getLocalName());
		}

	}

	/**
	 * This function adds Domain and Rage.
	 */

	private void addDomainRange() {
		// Adds domain and range
		if (predicate.asNode().getLocalName().equals("domain")
				|| predicate.asNode().getLocalName().equals("range")) {

			addSubjectURI(subject, ":" + object.asNode().getLocalName(), 1,
					"has" + predicate.asNode().getLocalName());
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), 2,
					"has" + predicate.asNode().getLocalName());
			addSubjectURI(subject, "", 1, "hasDocument");
			addSubjectURI(subject, "", 2, "hasDocument");
			addSubjectURI(object, "", 1, "hasDocument");
			addSubjectURI(object, "", 2, "hasDocument");
		}
	}

	/**
	 * This function populates all data with AttributeName.
	 */

	private void hasAttributeName() {
		if (predicate.asNode().getLocalName().equals("hasAttributeName")) {
			if (!checkEclass(object)) {
				if (!getType(subject).equals("Attribute")) {
					addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
							"has" + getType(subject)
									+ predicate.asNode().getLocalName().replace("has", ""));
				} else {
					addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
							predicate.asNode().getLocalName());
				}
			}
		}
	}

	/**
	 * checks if its elcass then ignore it because we only need values with
	 * object which can be unique.
	 * 
	 * @param object
	 * @return
	 */
	boolean checkEclass(RDFNode object) {
		if (object.asLiteral().getLexicalForm().equals("eClassClassificationClass")
				|| object.asLiteral().getLexicalForm().equals("eClassVersion")
				|| object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
			return true;
		}
		return false;
	}

}
