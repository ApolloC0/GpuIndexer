package GpuIndex.App.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Gpu implements Serializable {

    @Id
    private String id;

    private String name;

    @JsonProperty("gpu_name")
    private String gpuName;
    private String manufacturer;
    private String generation;
    private String architecture;
    private String foundry;


    @JsonProperty("process_size_nm")
    private Integer processSizeNm;

    @JsonProperty("transistor_count_m")
    private Double transistorCountM;

    @JsonProperty("transistor_density_k_mm2")
    private Double transistorDensityKmm2;
    @JsonProperty("die_size_mm2")
    private Double dieSizeMm2;

    @JsonProperty("chip_package")
    private String chipPackage;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("bus_interface")
    private String busInterface;

    @JsonProperty("base_clock_mhz")
    private Integer baseClock;

    @JsonProperty("boost_clock_mhz")
    private Integer boostClock;

    @JsonProperty("memory_clock_mhz")
    private Double memoryClockMhz;

    @JsonProperty("memory_size_gb")
    private Double memorySize;

    @JsonProperty("memory_bus_bits")
    private Integer memoryBus;

    @JsonProperty("memory_type")
    private String memoryType;

    @JsonProperty("memory_bandwidth_gb_s")
    private Double bandwidth;

    @JsonProperty("shading_units")
    private Integer shadingUnits;

    @JsonProperty("texture_mapping_units")
    private Integer textureMappingUnits;

    @JsonProperty("render_output_processors")
    private Integer renderOutputProcessors;

    @JsonProperty("streaming_multiprocessors")
    private Integer streamingMultiprocessors;

    @JsonProperty("tensor_cores")
    private Integer tensorCores;

    @JsonProperty("ray_tracing_cores")
    private Integer rayTracingCores;

    @JsonProperty("l1_cache_kb")
    private Double l1CacheKb;

    @JsonProperty("l2_cache_mb")
    private Double l2CacheMb;

    @JsonProperty("thermal_design_power_w")
    private String tdp;

    @JsonProperty("board_length_mm")
    private Double boardLengthMm;

    @JsonProperty("board_width_mm")
    private Double boardWidthMm;

    @JsonProperty("board_slot_width")
    private String boardSlotWidth;

    @JsonProperty("suggested_psu_w")
    private String suggestedPsu;

    @JsonProperty("power_connectors")
    private String powerConnectors;

    @JsonProperty("display_connectors")
    private String displayConnectors;

    @JsonProperty("single_float_performance_gflop_s")
    private Double singleFloatPerformanceGflopS;

    public Double getFp32() {
        return singleFloatPerformanceGflopS;
    }

    public Object get(String fieldName) {
        try {
            return this.getClass().getDeclaredField(fieldName).get(this);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
