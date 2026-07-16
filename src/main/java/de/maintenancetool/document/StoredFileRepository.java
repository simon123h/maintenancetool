package de.maintenancetool.document;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository für {@link StoredFile}. */
@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}
