package com.rently.rently.shared.mappers;

public interface ResponseMapper<Entity, Response> {
    Response toResponse(Entity entity);
}
