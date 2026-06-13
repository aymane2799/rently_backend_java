package com.rently.rently.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStatusResponse {
    private boolean ready;
    private String url;
}
