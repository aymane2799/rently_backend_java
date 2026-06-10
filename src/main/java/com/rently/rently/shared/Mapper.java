package com.rently.rently.shared;

import com.rently.rently.catalog.entities.CarModel;

public interface Mapper<Entity , Request, Response> {
    public Entity toEntity(final Request request);
    public Response toResponse(final Entity entity);
}
