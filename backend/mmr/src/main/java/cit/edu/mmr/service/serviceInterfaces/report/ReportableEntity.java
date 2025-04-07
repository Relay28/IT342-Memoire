package cit.edu.mmr.service.serviceInterfaces.report;

public interface ReportableEntity {
    Long getId();
    String getEntityType();
    void validate() throws IllegalArgumentException;
    // Add any common methods needed for reporting
}