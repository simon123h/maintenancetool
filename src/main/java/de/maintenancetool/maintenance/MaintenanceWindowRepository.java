package de.maintenancetool.maintenance;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, UUID> {
  List<MaintenanceWindow> findByApplicationId(UUID applicationId);

  List<MaintenanceWindow> findByEmailsSentFalse();
}
