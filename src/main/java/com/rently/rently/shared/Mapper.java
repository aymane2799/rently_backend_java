package com.rently.rently.shared;


public interface Mapper<Entity, Response, CreateRequest, UpdateRequest> {
    public Entity toEntity(final CreateRequest request);
    void patchEntity(Entity entity, UpdateRequest request);
    public Response toResponse(final Entity entity);
}
