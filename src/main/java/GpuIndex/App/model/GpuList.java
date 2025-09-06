package GpuIndex.App.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GpuList {
    private String listName;
    private List<GpuSummary> gpus = new ArrayList<>();

    @Data
    public static class GpuSummary {
        private String name;
        private Integer shadingUnits;
        private String tdp;
        private Double memorySize;
        private String memoryType;
        private Integer memoryBus;
        private Double bandwidth;
        private Double fp32;
        private Integer baseClock;
        private Integer boostClock;
    }
}
