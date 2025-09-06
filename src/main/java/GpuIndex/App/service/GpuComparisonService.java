package GpuIndex.App.service;

import GpuIndex.App.model.Gpu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GpuComparisonService {

    @Autowired
    private GpuFileService gpuFileService;

    @Autowired
    private DbService dbService;

    public String compareGpus(String gpu1Name, String gpu2Name) {
        try {
            Gpu gpu1 = dbService.getGpuDetails(gpu1Name);
            Gpu gpu2 = dbService.getGpuDetails(gpu2Name);

            return generateComparisonReport(gpu1, gpu2);
        } catch (Exception e) {
            return "❌ Error: Una o ambas GPUs no se encontraron. Asegúrate de usar los nombres exactos de la búsqueda.";
        }
    }

    private String generateComparisonReport(Gpu gpu1, Gpu gpu2) {
        StringBuilder report = new StringBuilder();
        Gpu[] gpus = {gpu1, gpu2};

        report.append("══════════════════════════ COMPARACIÓN DE GPUs ══════════════════════════\n");
        report.append(String.format("%-25s %-25s %-25s\n", "ESPECIFICACIÓN",
                gpu1.getName().length() > 23 ? gpu1.getName().substring(0, 23) + ".." : gpu1.getName(),
                gpu2.getName().length() > 23 ? gpu2.getName().substring(0, 23) + ".." : gpu2.getName()));
        report.append("───────────────────────────────────────────────────────────────────────────\n");

        addSpecificationRow(report, "Arquitectura", gpus, Gpu::getArchitecture, "%s");
        addSpecificationRow(report, "Frec. Base (MHz)", gpus, Gpu::getBaseClock, "%d");
        addSpecificationRow(report, "Frec. Boost (MHz)", gpus, Gpu::getBoostClock, "%d");
        addSpecificationRow(report, "Memoria (GB)", gpus, Gpu::getMemorySize, "%.1f");
        addSpecificationRow(report, "Tipo de Memoria", gpus, Gpu::getMemoryType, "%s");
        addSpecificationRow(report, "Bus Memoria (bits)", gpus, Gpu::getMemoryBus, "%d");
        addSpecificationRow(report, "Ancho de Banda (GB/s)", gpus, Gpu::getBandwidth, "%.1f");
        addSpecificationRow(report, "Unid. Sombreado", gpus, Gpu::getShadingUnits, "%d");
        addSpecificationRow(report, "Rendimiento FP32 (GFLOPs)", gpus, Gpu::getFp32, "%.2f");
        addSpecificationRow(report, "TDP (W)", gpus, Gpu::getTdp, "%s");
        addSpecificationRow(report, "PSU Sugerida (W)", gpus, Gpu::getSuggestedPsu, "%s");
        report.append("═══════════════════════════════════════════════════════════════════════════\n");

        return report.toString();
    }

    private interface GpuValueGetter<T> {
        T getValue(Gpu gpu);
    }

    private <T> void addSpecificationRow(StringBuilder report, String specName, Gpu[] gpus,
                                         GpuValueGetter<T> getter, String format) {
        report.append(String.format("%-25s", specName));
        for (Gpu gpu : gpus) {
            if (gpu != null) {
                T value = getter.getValue(gpu);
                String formattedValue = "N/A";
                if (value != null) {
                    if (value instanceof Double || value instanceof Integer) {
                        formattedValue = String.format(format, value);
                    } else {
                        formattedValue = value.toString();
                    }
                }
                report.append(String.format(" %-24s", formattedValue));
            } else {
                report.append(String.format(" %-24s", "N/A"));
            }
        }
        report.append("\n");
    }
}