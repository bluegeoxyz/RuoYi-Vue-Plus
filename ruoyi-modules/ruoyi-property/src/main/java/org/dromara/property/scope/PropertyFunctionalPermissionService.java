package org.dromara.property.scope;

/**
 * Revalidates a functional permission against the current database state.
 * This complements Sa-Token's login snapshot for revocation-sensitive operations.
 */
public interface PropertyFunctionalPermissionService {

    void assertCurrent(String permission);
}
