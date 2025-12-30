package de.angr2301.genericllmadapter.dto.analysis;

import lombok.Data;
import java.util.Map;

@Data
public class AnalysisResponse {
    private Map<String, Object> metrics;
    private String new_role;
    private int history_length;
}
