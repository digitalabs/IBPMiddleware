
package org.generationcp.middleware.domain.ontology;

public enum TermRelationshipId {

	// CV Term Relationship
	HAS_METHOD(1210), HAS_PROPERTY(1200), HAS_SCALE(1220), HAS_TYPE(1105), HAS_VALUE(1190), IS_A(1225);

	private final int id;

	private TermRelationshipId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public static TermRelationshipId getById(int id) {
		for (TermRelationshipId term : TermRelationshipId.values()) {
			if (term.getId() == id) {
				return term;
			}
		}
		return null;
	}
}
