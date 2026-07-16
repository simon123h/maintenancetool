package de.maintenancetool.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends MaintenanceException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
