package de.maintenancetool.error;

public class MaintenanceException extends RuntimeException {
  public MaintenanceException(String message) {
    super(message);
  }

  public MaintenanceException(String message, Throwable cause) {
    super(message, cause);
  }
}
