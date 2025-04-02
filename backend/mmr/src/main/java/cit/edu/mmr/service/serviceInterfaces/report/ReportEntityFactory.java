package cit.edu.mmr.service.serviceInterfaces.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReportEntityFactory {
    private final Map<String, ReportableEntity> handlers;

    @Autowired
    public ReportEntityFactory(List<ReportableEntity> handlerList) {
        handlers = handlerList.stream()
                .collect(Collectors.toMap(ReportableEntity::getEntityType, handler -> handler));
    }

    public ReportableEntity getHandler(String entityType) {
        ReportableEntity handler = handlers.get(entityType);
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for entity type: " + entityType);
        }
        return handler;
    }
}