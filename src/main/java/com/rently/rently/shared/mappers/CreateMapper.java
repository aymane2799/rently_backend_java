package com.rently.rently.shared.mappers;

public interface CreateMapper<Entity, CreateRequest, Context> {
    Entity toEntity(CreateRequest request, Context ctx);
}
