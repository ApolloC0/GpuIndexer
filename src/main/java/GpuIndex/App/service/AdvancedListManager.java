package GpuIndex.App.service;

import GpuIndex.App.model.Gpu;
import GpuIndex.App.model.GpuList;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AdvancedListManager {

    @Autowired
    private DbService dbService;
    
    private final Map<String, List<GpuList.GpuSummary>> lists = new HashMap<>();
    private String currentListName;
    private final int MAX_LIST_SIZE = 20;
    private static final String LISTS_DIRECTORY = "saved_lists";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(LISTS_DIRECTORY));
            loadSavedLists();
        } catch (IOException e) {
            System.err.println("❌ Error creando directorio de listas: " + e.getMessage());
        }
    }

    // 📋 MÉTODOS DE PERSISTENCIA
    private void loadSavedLists() {
        try {
            File listsDir = new File(LISTS_DIRECTORY);
            if (listsDir.exists() && listsDir.isDirectory()) {
                File[] jsonFiles = listsDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (jsonFiles != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    for (File file : jsonFiles) {
                        try {
                            GpuList gpuList = mapper.readValue(file, GpuList.class);
                            String listName = file.getName().replace(".json", "");
                            lists.put(listName.toLowerCase(), gpuList.getGpus());
                        } catch (IOException e) {
                            System.err.println(" Error cargando lista: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error inicializando listas guardadas: " + e.getMessage());
        }
    }

    private void saveListToFile(String listName) {
        try {
            List<GpuList.GpuSummary> list = lists.get(listName.toLowerCase());
            if (list != null) {
                GpuList gpuList = new GpuList();
                gpuList.setListName(listName);
                gpuList.setGpus(list);
                
                ObjectMapper mapper = new ObjectMapper();
                File outputFile = new File(LISTS_DIRECTORY + "/" + listName + ".json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, gpuList);
            }
        } catch (IOException e) {
            System.err.println("Error guardando lista: " + e.getMessage());
        }
    }

    private void deleteListFile(String listName) {
        try {
            File file = new File(LISTS_DIRECTORY + "/" + listName + ".json");
            if (file.exists()) {
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            System.err.println(" Error eliminando lista: " + e.getMessage());
        }
    }

    // 📋 MÉTODOS PRINCIPALES
    public String createList(String listName) {
        if (lists.containsKey(listName.toLowerCase())) {
            return " La lista '" + listName + "' ya existe.";
        }
        
        lists.put(listName.toLowerCase(), new ArrayList<>());
        currentListName = listName;
        saveListToFile(listName);
        return "✅ Lista '" + listName + "' creada y seleccionada.";
    }

    public String addToCurrentList(String gpuQuery) throws IOException {
        if (currentListName == null) {
            return " No hay lista seleccionada. Usa 'list new <nombre>' primero.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        if (currentList.size() >= MAX_LIST_SIZE) {
            return " Lista llena (" + MAX_LIST_SIZE + " GPUs). Usa 'list export' primero.";
        }

        Gpu gpu;
        try {
            gpu = dbService.getGpuDetails(gpuQuery);
        } catch (IOException e) {
            var results = dbService.searchGpuResults(gpuQuery);
            if (results.isEmpty()) {
                return " GPU no encontrada: '" + gpuQuery + "'";
            }
            gpu = dbService.getGpuDetails(results.get(0).get("title"));
        }

        GpuList.GpuSummary summary = createSummary(gpu);
        currentList.add(summary);
        saveListToFile(currentListName);
        
        return "✅ '" + gpu.getName() + "' añadida a '" + currentListName + "' " +
               "(" + currentList.size() + "/" + MAX_LIST_SIZE + ")";
    }

    public String removeGpuFromList(int index) {
        if (currentListName == null) {
            return " No hay lista seleccionada. Usa 'list new <nombre>' primero.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        if (currentList.isEmpty()) {
            return " La lista '" + currentListName + "' está vacía.";
        }
        
        if (index < 1 || index > currentList.size()) {
            return String.format(" Índice inválido. Rango: 1-%d", currentList.size());
        }
        
        GpuList.GpuSummary removedGpu = currentList.remove(index - 1);
        saveListToFile(currentListName);
        return String.format("✅ GPU '%s' removida de la lista '%s'", 
                            removedGpu.getName(), currentListName);
    }

    public String removeGpuFromList(String gpuName) {
        if (currentListName == null) {
            return " No hay lista seleccionada. Usa 'list new <nombre>' primero.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        if (currentList.isEmpty()) {
            return " La lista '" + currentListName + "' está vacía.";
        }
        
        Optional<GpuList.GpuSummary> foundGpu = currentList.stream()
            .filter(gpu -> gpu.getName().toLowerCase().contains(gpuName.toLowerCase()))
            .findFirst();
        
        if (foundGpu.isPresent()) {
            currentList.remove(foundGpu.get());
            saveListToFile(currentListName);
            return String.format("✅ GPU '%s' removida de la lista '%s'", 
                                foundGpu.get().getName(), currentListName);
        }
        
        return String.format(" GPU '%s' no encontrada en la lista '%s'",
                            gpuName, currentListName);
    }

    public String removeGpusFromList(int[] indices) {
        if (currentListName == null) {
            return " No hay lista seleccionada. Usa 'list new <nombre>' primero.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        if (currentList.isEmpty()) {
            return " La lista '" + currentListName + "' está vacía.";
        }
        
        for (int index : indices) {
            if (index < 1 || index > currentList.size()) {
                return String.format(" Índice %d inválido. Rango: 1-%d", index, currentList.size());
            }
        }
        
        Arrays.sort(indices);
        List<String> removedGpus = new ArrayList<>();
        
        for (int i = indices.length - 1; i >= 0; i--) {
            int actualIndex = indices[i] - 1;
            removedGpus.add(currentList.get(actualIndex).getName());
            currentList.remove(actualIndex);
        }
        
        saveListToFile(currentListName);
        return String.format("✅ %d GPUs removidas: %s", 
                            indices.length, String.join(", ", removedGpus));
    }

    public String saveList(String format, String customFileName) throws IOException {
        if (currentListName == null) {
            return " No hay lista seleccionada.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        if (currentList.isEmpty()) {
            return " La lista está vacía.";
        }

        GpuList gpuList = new GpuList();
        gpuList.setListName(currentListName);
        gpuList.setGpus(new ArrayList<>(currentList));
        
        String fileName = customFileName != null ? customFileName : currentListName.replaceAll("[^a-zA-Z0-9]", "_");
        
        if (format.equalsIgnoreCase("xlsx")) {
            String xlsxFile = createExcelFile(fileName, gpuList);
            return "✅ Excel exported to: " + xlsxFile;
        } else {
            String jsonFile = createJsonFile(fileName, gpuList);
            return "✅ JSON exported to: " + jsonFile;
        }
    }

    public String switchList(String listName) {
        if (!lists.containsKey(listName.toLowerCase())) {
            return "List '" + listName + "' not found. Use 'list new <name>' first.";
        }
        
        currentListName = listName;
        return "✅ List name changed to: '" + listName + "' (" +
               lists.get(listName.toLowerCase()).size() + " GPUs)";
    }

    public String deleteList(String listName) {
        if (!lists.containsKey(listName.toLowerCase())) {
            return "List '" + listName + "' not found.";
        }
        
        lists.remove(listName.toLowerCase());
        deleteListFile(listName);
        
        if (listName.equals(currentListName)) {
            currentListName = null;
        }
        
        return "✅ List '" + listName + "' deleted.";
    }

    public String renameList(String oldName, String newName) {
        if (!lists.containsKey(oldName.toLowerCase())) {
            return "List '" + oldName + "' not found.";
        }
        
        if (lists.containsKey(newName.toLowerCase())) {
            return newName + "' already exist.";
        }
        
        List<GpuList.GpuSummary> list = lists.remove(oldName.toLowerCase());
        lists.put(newName.toLowerCase(), list);
        
        deleteListFile(oldName);
        saveListToFile(newName);
        
        if (oldName.equals(currentListName)) {
            currentListName = newName;
        }
        
        return "✅ List renamed: '" + oldName + "' → '" + newName + "'";
    }

    public String listAllLists() {
        if (lists.isEmpty()) {
            return "there are no lists. Use 'list new <name>'.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("SAVED LISTS:\n");
        sb.append("═".repeat(50)).append("\n");
        
        List<String> sortedNames = new ArrayList<>(lists.keySet());
        Collections.sort(sortedNames);
        
        for (String name : sortedNames) {
            String indicator = name.equalsIgnoreCase(currentListName) ? "👉 " : "   ";
            int gpuCount = lists.get(name).size();
            sb.append(String.format("%s%s: %d GPU%s\n", 
                indicator, name, gpuCount, gpuCount != 1 ? "s" : ""));
        }
        
        sb.append("\n💡 Use 'list switch <name>' to switch active list");
        sb.append("\n💡 Use 'list delete <name>' to delete a list");
        
        return sb.toString();
    }

    public String listStatus() {
        if (currentListName == null) {
            return "there are no lists. Use 'list new <name>'.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        StringBuilder sb = new StringBuilder();
        sb.append("LIST: ").append(currentListName).append("\n");
        sb.append("GPUs: ").append(currentList.size()).append("/").append(MAX_LIST_SIZE).append("\n");
        sb.append("➖➖➖➖➖➖➖➖➖➖➖➖➖\n");
        
        if (currentList.isEmpty()) {
            sb.append("The list is empty\n");
        } else {
            for (int i = 0; i < currentList.size(); i++) {
                GpuList.GpuSummary gpu = currentList.get(i);
                sb.append(i + 1).append(". ").append(gpu.getName());
                
                if (gpu.getFp32() != null) {
                    sb.append(" (").append(String.format("%.1f", gpu.getFp32())).append(" GFLOPs)");
                }
                if (gpu.getMemorySize() != null) {
                    sb.append(" [").append(gpu.getMemorySize()).append("GB]");
                }
                sb.append("\n");
            }
        }
        
        sb.append("\n💡 Use 'list show' to see full list with details");
        sb.append("\n💡 Use 'list export' to save the list to a file");
        
        return sb.toString();
    }

    public String showListDetails() {
        if (currentListName == null) {
            return "There are no active lists. Use 'list new <name>'.";
        }
        
        List<GpuList.GpuSummary> currentList = lists.get(currentListName.toLowerCase());
        if (currentList.isEmpty()) {
            return "List '" + currentListName + "' its empty";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("LIST: ").append(currentListName).append(" (").append(currentList.size()).append(" GPUs)\n");
        sb.append("═".repeat(80)).append("\n");
        
        for (int i = 0; i < currentList.size(); i++) {
            GpuList.GpuSummary gpu = currentList.get(i);
            sb.append(String.format("%d. %s\n", i + 1, gpu.getName()));
            sb.append(String.format("  +  Performance(FP32): %.1f GFLOPs\n", gpu.getFp32() != null ? gpu.getFp32() : 0.0));
            sb.append(String.format("  +  VRAM: %.1f GB %s\n", gpu.getMemorySize() != null ? gpu.getMemorySize() : 0.0, gpu.getMemoryType() != null ? gpu.getMemoryType() : ""));
            sb.append(String.format("  +  TDP: %s\n", gpu.getTdp() != null ? gpu.getTdp() : "N/A"));
            sb.append(String.format("  +  🔩Shading Units: %s\n", gpu.getShadingUnits() != null ? gpu.getShadingUnits() : "N/A"));
            sb.append(String.format("  +  Clock speed(Base/Boost): %d/%d MHz\n\n",
                gpu.getBaseClock() != null ? gpu.getBaseClock() : 0, 
                gpu.getBoostClock() != null ? gpu.getBoostClock() : 0));
        }
        
        sb.append("💡 Use 'list remove -i <número>' to remove a GPU from list");
        sb.append("\n💡 Use 'list export' to export a list to a file");
        return sb.toString();
    }

    public void clearCurrentList() {
        if (currentListName != null) {
            lists.get(currentListName.toLowerCase()).clear();
            saveListToFile(currentListName);
        }
    }

    public boolean canAddMore() {
        return currentListName != null && 
               lists.get(currentListName.toLowerCase()).size() < MAX_LIST_SIZE;
    }

    public boolean hasCurrentList() {
        return currentListName != null;
    }

    public int getRemainingSlots() {
        return currentListName != null ? 
               MAX_LIST_SIZE - lists.get(currentListName.toLowerCase()).size() : 0;
    }

    // 🛠️ MÉTODOS AUXILIARES
    private GpuList.GpuSummary createSummary(Gpu gpu) {
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
        return summary;
    }

    private String createJsonFile(String fileName, GpuList gpuList) throws IOException {
        String fullFileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fullFileName), gpuList);
        return fullFileName;
    }

    private String createExcelFile(String fileName, GpuList gpuList) throws IOException {
        String fullFileName = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("GPUs Comparison");
            
            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            
            CellStyle gpuNameStyle = workbook.createCellStyle();
            Font gpuNameFont = workbook.createFont();
            gpuNameFont.setBold(true);
            gpuNameStyle.setFont(gpuNameFont);
            gpuNameStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            gpuNameStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            gpuNameStyle.setBorderBottom(BorderStyle.THIN);

            // Encabezados en primera fila (horizontal)
            String[] headers = {
                "GPU Name", "Shading Units", "TDP(W)", "VRAM(GB)",
                "Memory Type", "Memory Bus(bits)", "Bandwidth(GB/s)",
                "FP32(GFLOPs)", "Base Clock(MHz)", "Boost Clock(MHz)"
            };
            
            // Crear fila de encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos - CADA GPU EN SU PROPIA FILA
            int rowNum = 1;
            for (GpuList.GpuSummary gpu : gpuList.getGpus()) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Columna 0: Nombre de GPU
                Cell nameCell = dataRow.createCell(0);
                nameCell.setCellValue(gpu.getName());
                nameCell.setCellStyle(gpuNameStyle);
                
                // Columna 1: Unidades de Sombreado
                Cell shadingCell = dataRow.createCell(1);
                shadingCell.setCellValue(gpu.getShadingUnits() != null ? gpu.getShadingUnits() : 0);
                shadingCell.setCellStyle(dataStyle);
                
                // Columna 2: TDP
                Cell tdpCell = dataRow.createCell(2);
                tdpCell.setCellValue(gpu.getTdp() != null ? gpu.getTdp() : "N/A");
                tdpCell.setCellStyle(dataStyle);
                
                // Columna 3: Memoria(GB)
                Cell memoryCell = dataRow.createCell(3);
                memoryCell.setCellValue(gpu.getMemorySize() != null ? gpu.getMemorySize() : 0);
                memoryCell.setCellStyle(dataStyle);
                
                // Columna 4: Tipo de Memoria
                Cell memTypeCell = dataRow.createCell(4);
                memTypeCell.setCellValue(gpu.getMemoryType() != null ? gpu.getMemoryType() : "N/A");
                memTypeCell.setCellStyle(dataStyle);
                
                // Columna 5: Bus de Memoria
                Cell busCell = dataRow.createCell(5);
                busCell.setCellValue(gpu.getMemoryBus() != null ? gpu.getMemoryBus() : 0);
                busCell.setCellStyle(dataStyle);
                
                // Columna 6: Ancho de Banda
                Cell bandwidthCell = dataRow.createCell(6);
                bandwidthCell.setCellValue(gpu.getBandwidth() != null ? gpu.getBandwidth() : 0);
                bandwidthCell.setCellStyle(dataStyle);
                
                // Columna 7: FP32 Performance
                Cell fp32Cell = dataRow.createCell(7);
                fp32Cell.setCellValue(gpu.getFp32() != null ? gpu.getFp32() : 0);
                fp32Cell.setCellStyle(dataStyle);
                
                // Columna 8: Base Clock
                Cell baseClockCell = dataRow.createCell(8);
                baseClockCell.setCellValue(gpu.getBaseClock() != null ? gpu.getBaseClock() : 0);
                baseClockCell.setCellStyle(dataStyle);
                
                // Columna 9: Boost Clock
                Cell boostClockCell = dataRow.createCell(9);
                boostClockCell.setCellValue(gpu.getBoostClock() != null ? gpu.getBoostClock() : 0);
                boostClockCell.setCellStyle(dataStyle);
            }
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Congelar la fila de encabezados
            sheet.createFreezePane(0, 1);
            
            // Guardar archivo
            try (FileOutputStream outputStream = new FileOutputStream(fullFileName)) {
                workbook.write(outputStream);
            }
        }
        
        return fullFileName;
    }
}