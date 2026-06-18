package com.rently.rently.location.branch;

import com.rently.rently.shared.CRUDService;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BranchService extends CRUDService<CreateBranchRequest, UpdateBranchRequest, BranchResponse, String> {
    PagedResponse<BranchResponse> getAll(Boolean active, String city, String search, Pageable pageable);
    List<BranchOptionResponse> getOptions();
}
