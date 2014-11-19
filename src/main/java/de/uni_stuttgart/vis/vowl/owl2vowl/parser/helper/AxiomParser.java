/*
 * AxiomParser.java
 *
 */

package de.uni_stuttgart.vis.vowl.owl2vowl.parser.helper;

import de.uni_stuttgart.vis.vowl.owl2vowl.model.Constants;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.containerElements.DisjointUnion;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.nodes.BaseNode;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.GeneralParser;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 *
 */
public class AxiomParser extends GeneralParser {
	public void parseAxioms(OWLEntity entity) {
		Map<String, Map<String, List<OWLAxiom>>> mapping = mapData.getEntityToAxiom();
		Map<String, List<OWLAxiom>> axioms = new HashMap<String, List<OWLAxiom>>();

		for (OWLAxiom currentAxiom : entity.getReferencingAxioms(ontology)) {
			List<OWLAxiom> items = axioms.get(currentAxiom.getAxiomType().toString());

			if (items == null) {
				items = new ArrayList<OWLAxiom>();
			}

			items.add(currentAxiom);
			axioms.put(currentAxiom.getAxiomType().toString(), items);
		}

		mapping.put(entity.getIRI().toString(), axioms);
	}

	public Set<OWLClassExpression> getDisjoints(OWLEntity entity) {
		Map<String, Map<String, List<OWLAxiom>>> mapping = mapData.getEntityToAxiom();
		Map<String, List<OWLAxiom>> i = mapping.get(entity.getIRI().toString());

		List<OWLAxiom> j = i.get(Constants.AXIOM_DISJOINT);

		Set<OWLClassExpression> disjoints = new HashSet<OWLClassExpression>();

		if (j != null) {
			for (OWLAxiom currentAxiom : j) {
				disjoints.addAll(currentAxiom.getNestedClassExpressions());
			}
		}

		return disjoints;
	}

	public Set<DisjointUnion> getDisjointUnions(OWLEntity entity) {
		Map<String, Map<String, List<OWLAxiom>>> mapping = mapData.getEntityToAxiom();
		Map<String, List<OWLAxiom>> i = mapping.get(entity.getIRI().toString());

		List<OWLAxiom> j = i.get(Constants.AXIOM_DISJOINTUNION);
		Set<DisjointUnion> elements = new HashSet<DisjointUnion>();

		if (j != null) {
			for (OWLAxiom currentAxiom : j) {
				OWLDisjointUnionAxiom theAxiom = (OWLDisjointUnionAxiom) currentAxiom;
				BaseNode base = mapData.getMergedMap().get(theAxiom.getOWLClass().getIRI().toString());
				Set<BaseNode> disjoints = new HashSet<BaseNode>();

				for (OWLClass test : theAxiom.getOWLDisjointClassesAxiom().getClassesInSignature()) {
					disjoints.add(mapData.getMergedMap().get(test.getIRI().toString()));
				}

				DisjointUnion disjointUnion = new DisjointUnion(base, disjoints);

				if (!mapData.getDisjointUnions().contains(disjointUnion)) {
					mapData.getDisjointUnions().add(disjointUnion);
					elements.add(disjointUnion);
				}
			}
		}

		return elements;
	}

	/**
	 * Searches for an union class of the property. If already exists take it an return else create
	 * a new node. If no union found return null.
	 *
	 * @param property The property to search in.
	 */
	public Set<OWLClass> search(OWLEntity property, String axiom) {
		for (OWLAxiom currentAxiom : property.getReferencingAxioms(ontology)) {

			for (OWLClassExpression nestExpr : currentAxiom.getNestedClassExpressions()) {
				if (nestExpr.getClassExpressionType().toString().equals(axiom)) {
					return nestExpr.getClassesInSignature();
				}
			}
		}

		return null;
	}

	public List<Set<OWLClass>> searchInEquivalents(OWLEntity entity, String axiom) {
		List<Set<OWLClass>> listOfNested = new ArrayList<Set<OWLClass>>();

		for (OWLAxiom currentAxiom : entity.getReferencingAxioms(ontology)) {
			// TODO if directly axiom is a logical axiom.

			for (OWLClassExpression nestExpr : currentAxiom.getNestedClassExpressions()) {
				if (nestExpr.getClassExpressionType().toString().equals(axiom)) {
					listOfNested.add(nestExpr.getClassesInSignature());
				}
			}
		}

		return listOfNested;
	}

	public Set<OWLClass> search(OWLEntity property, String axiom, boolean direction) {
		List<String> searchString = new ArrayList<String>();

		if (direction) {
			searchString.add(Constants.AXIOM_OBJ_PROP_DOMAIN);
			searchString.add(Constants.AXIOM_DATA_PROP_DOMAIN);
		} else {
			searchString.add(Constants.AXIOM_OBJ_PROP_RANGE);
			searchString.add(Constants.AXIOM_DATA_PROP_RANGE);
		}

		for (OWLAxiom currentAxiom : property.getReferencingAxioms(ontology)) {
			if (!searchString.contains(currentAxiom.getAxiomType().toString())) {
				continue;
			}

			for (OWLClassExpression nestExpr : currentAxiom.getNestedClassExpressions()) {
				if (nestExpr.getClassExpressionType().toString().equals(axiom)) {
					return nestExpr.getClassesInSignature();
				}
			}
		}

		return null;
	}
}