package com.rently.rently.shared.mappers;

public interface PatchMapper<Entity, PatchRequest, Context> {
    void patchEntity(Entity entity, PatchRequest request, Context ctx);
}