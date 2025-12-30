package de.angr2301.genericllmadapter.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisRequest {
    private String session_id;
    private String prompt;
    private String response;
    private String prev_role;
}
