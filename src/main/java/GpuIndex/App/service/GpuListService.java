package GpuIndex.App.service;

import GpuIndex.App.model.Gpu;
import GpuIndex.App.model.GpuList;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GpuListService {

    @Autowired
    private DbService dbService;

    private final List<GpuList.GpuSummary> currentList = new ArrayList<>();
    private final int MAX_LIST_SIZE = 20;

    public String addGpuToList(String gpuName) throws IOException {
        if (currentList.size() >= MAX_LIST_SIZE) {
            return "This list has reached the maximum of " + MAX_LIST_SIZE + " GPUs. " +
                    "Please export this list or remove some Gpu before adding more.";
        }

        Gpu gpu = dbService.getGpuDetails(gpuName);
        GpuList.GpuSummary summary = new GpuList.GpuSummary();

        summary.setName(gpu.getName());
        summary.setShadingUnits(gpu.getShadingUnits());
        summary.setTdp(gpu.getTdp());
        summary.setMemorySize(gpu.getMemorySize());
        summary.setMemoryType(gpu.getMemoryType());
        summary.setMemoryBus(gpu.getMemoryBus());
        summary.setBandwidth(gpu.getBandwidth());
        summary.setFp32(gpu.getFp32());
        summary.setBaseClock(gpu.getBaseClock());
        summary.setBoostClock(gpu.getBoostClock());

        currentList.add(summary);

        return "GPU '" + gpuName + "' added to the list. " +
                "Total actual: " + currentList.size() + "/" + MAX_LIST_SIZE;
    }

    public String createList(String listName, String format) throws IOException {
        if (currentList.isEmpty()) {
            return "This list is empty. add some GPU first.";
        }

        GpuList gpuList = new GpuList();
        gpuList.setListName(listName);
        gpuList.setGpus(new ArrayList<>(currentList));

        // Crear nombre de archivo seguro
        String safeFileName = listName.replaceAll("[^a-zA-Z0-9]", "_");

        if (format.equalsIgnoreCase("xlsx")) {
            String fileName = createExcelFile(safeFileName, gpuList);
            currentList.clear();
            return "Excel file '" + listName + "' succesfully created. " +
                    "File: " + fileName + " (" + gpuList.getGpus().size() + " GPUs)";
        } else {
            String fileName = createJsonFile(safeFileName, gpuList);
            currentList.clear();
            return "JSON list '" + listName + "' succesfully created. " +
                    "File: " + fileName + " (" + gpuList.getGpus().size() + " GPUs)";
        }
    }

    private String createJsonFile(String safeFileName, GpuList gpuList) throws IOException {
        String fileName = safeFileName + ".json";
        File outputFile = new File(fileName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, gpuList);

        return fileName;
    }

    private String createExcelFile(String safeFileName, GpuList gpuList) throws IOException {
        String fileName = safeFileName + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("GPUs List");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Name", "Shading Units", "TDP (W)",
                    "VRAM(GB)", "Memory Type", "Memory bus(bits)",
                    "Bandwith(GB/s)", "Performance FP32 (GFLOPs)",
                    "Base clock (MHz)", "Boost clock (MHz)"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            for (GpuList.GpuSummary gpu : gpuList.getGpus()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(gpu.getName());
                row.createCell(1).setCellValue(gpu.getShadingUnits() != null ? gpu.getShadingUnits() : 0);
                row.createCell(2).setCellValue(gpu.getTdp() != null ? gpu.getTdp() : "N/A");
                row.createCell(3).setCellValue(gpu.getMemorySize() != null ? gpu.getMemorySize() : 0);
                row.createCell(4).setCellValue(gpu.getMemoryType() != null ? gpu.getMemoryType() : "N/A");
                row.createCell(5).setCellValue(gpu.getMemoryBus() != null ? gpu.getMemoryBus() : 0);
                row.createCell(6).setCellValue(gpu.getBandwidth() != null ? gpu.getBandwidth() : 0);
                row.createCell(7).setCellValue(gpu.getFp32() != null ? gpu.getFp32() : 0);
                row.createCell(8).setCellValue(gpu.getBaseClock() != null ? gpu.getBaseClock() : 0);
                row.createCell(9).setCellValue(gpu.getBoostClock() != null ? gpu.getBoostClock() : 0);
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            }
        }

        return fileName;
    }

    public String getListStatus() {
        return "Current list: " + currentList.size() + "/" + MAX_LIST_SIZE + " GPUs";
    }

    public void clearCurrentList() {
        currentList.clear();
    }

    public boolean canAddMore() {
        return currentList.size() < MAX_LIST_SIZE;
    }

    public int getRemainingSlots() {
        return MAX_LIST_SIZE - currentList.size();
    }
}
