package com.rently.rently.shared.mappers;

public interface PatchMapper<Entity, PatchRequest, Context> {
    Entity patchEntity(Entity entity, PatchRequest request, Context ctx);
}