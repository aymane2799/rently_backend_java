package com.rently.rently.shared;

import java.util.List;

public interface CRUDService<
        CreateRequest,
        PatchRequest,
        Response,
        ID>
{
    List<Response> getAll();
    Response get(ID id);
    Response create(CreateRequest request);
    void update(ID id, PatchRequest request);
    void delete(ID id);
}
