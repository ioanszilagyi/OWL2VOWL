/*
 * DatatypePropertyParser.java
 *
 */

package de.uni_stuttgart.vis.vowl.owl2vowl.parser;

import de.uni_stuttgart.vis.vowl.owl2vowl.model.Constants;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.edges.properties.OwlDatatypeProperty;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.nodes.BaseNode;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.nodes.classes.OwlThing;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.nodes.datatypes.BaseDatatype;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.nodes.datatypes.RdfsDatatype;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.nodes.datatypes.RdfsLiteral;
import de.uni_stuttgart.vis.vowl.owl2vowl.pipes.FormatText;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class DatatypePropertyParser extends GeneralPropertyParser {

	private Set<OWLDataProperty> data;

	public DatatypePropertyParser(Set<OWLDataProperty> data) {
		this.data = data;
	}

	protected void execute() {
		Map<String, OwlDatatypeProperty> propertyMap = mapData.getDatatypePropertyMap();
		Map<String, OWLDataProperty> owlDatatypeProperties = mapData.getOwlDatatypeProperties();
		int indexCounter = 1;

		for (OWLDataProperty currentProperty : data) {
			rdfsLabel = "";
			rdfsComment = "";
			isDeprecated = false;
			rdfsIsDefinedBy = "";
			owlVersionInfo = "";
			rdfsRange = "";
			rdfsDomain = "";
			iri = currentProperty.getIRI().toString();

			Set<OWLAnnotation> annotations = currentProperty.getAnnotations(ontology);
			parseAnnotations(annotations);

			if (rdfsLabel.isEmpty()) {
				rdfsLabel = extractNameFromIRI(iri);
			}

			rdfsDomain = retrieveDomain(currentProperty);

			rdfsRange = retrieveRange(currentProperty);

			BaseNode domainNode = findNode(rdfsDomain);

			BaseDatatype rangeNode;
			String resourceName = extractNameFromIRI(rdfsRange);
			System.out.println(resourceName);
			boolean isGeneric = false;

			if (resourceName.isEmpty()) {
				resourceName = "Literal";
				isGeneric = true;
			}

			if (rdfsRange.equals(Constants.GENERIC_LITERAL_URI)) {
				isGeneric = true;
			}

			if (isGeneric) {
				rangeNode = new RdfsLiteral();
			} else {
				rangeNode = new RdfsDatatype();
			}

			rangeNode.setName(resourceName);
			mapData.getDatatypeMap().put(rangeNode.getId(), rangeNode);

			if(domainNode == null) {
				domainNode = getDisconnectedThing();

				if(domainNode == null) {
					OwlThing newThing = new OwlThing();
					newThing.setId("thing" + mapData.getThingMap().size() + 1);
					mapData.getThingMap().put(newThing.getId(), newThing);
					domainNode = newThing;
				}
			}

			OwlDatatypeProperty property = new OwlDatatypeProperty();

			if(isImported(iri)) {
				property.getAttributes().add(Constants.PROP_ATTR_IMPORT);
			}

			if(isFuntionalDataProperty(currentProperty)) {
				property.getAttributes().add(Constants.PROP_ATTR_FUNCT);
			}

			property.setName(FormatText.cutQuote(rdfsLabel));
			property.setComment(FormatText.cutQuote(rdfsComment));
			property.setIri(iri);
			property.setDefinedBy(rdfsIsDefinedBy);
			property.setOwlVersion(owlVersionInfo);
			property.setId("datatypeProperty" + indexCounter);
			domainNode.getOutGoingEdges().add(property);
			rangeNode.getInGoingEdges().add(property);

			property.setDomain(domainNode);
			property.setRange(rangeNode);

			indexCounter++;

			owlDatatypeProperties.put(currentProperty.getIRI().toString(), currentProperty);
			propertyMap.put(property.getIri(), property);
		}
	}

	/**
	 * Checks whether the given OWLDataProperty is a functional property.
	 *
	 * @param owlDataProp the specific OWLDataProperty
	 * @return true, if the given OWLDataProperty is a functional property
	 */
	public boolean isFuntionalDataProperty(OWLDataProperty owlDataProp) {
		return !ontology.getFunctionalDataPropertyAxioms(owlDataProp).isEmpty();
	}
}