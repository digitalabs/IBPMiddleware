package org.generationcp.middleware.api.breedingmethod;

import java.util.List;

public interface BreedingMethodService {

	List<MethodClassDTO> getMethodClasses();

	BreedingMethodDTO getBreedingMethod(Integer breedingMethodDbId);

	List<BreedingMethodDTO> getBreedingMethods(String programUUID, boolean favorites);
}
