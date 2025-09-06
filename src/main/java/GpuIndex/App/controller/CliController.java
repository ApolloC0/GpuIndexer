package GpuIndex.App.controller;

import GpuIndex.App.model.Gpu;
import GpuIndex.App.service.AdvancedListManager;
import GpuIndex.App.service.DbService;
import GpuIndex.App.service.GpuComparisonService;
import GpuIndex.App.service.GpuFileService;
import GpuIndex.App.session.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ShellComponent
public class CliController {

    @Autowired
    private DbService dbService;
    @Autowired
    private GpuFileService gpuFileService;
    @Autowired
    private GpuComparisonService comparisonService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private UserSession userSession;
    @Autowired
    private AdvancedListManager listManager;

    private static final int MAX_LIST_SIZE = 10;

    // 🆘 SISTEMA DE AYUDA
    @ShellMethod(key = "help!", value = "Mostrar ayuda completa con ejemplos")
    public String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n🎮").append("═".repeat(70)).append("🎮\n");
        sb.append("                    GPU INDEXER - GUÍA COMPLETA\n");
        sb.append("🎮").append("═".repeat(70)).append("🎮\n\n");

        sb.append("🔍 ").append("BÚSQUEDA Y CONSULTA:").append("\n");
        sb.append("   search <términos> [-a]           Buscar GPUs (-a para añadir automáticamente)\n");
        sb.append("   gpu show <número>                Ver detalles por número de resultado\n");
        sb.append("   gpu compare <gpu1> <gpu2>        Comparar dos GPUs\n");
        sb.append("   results                          Ver última búsqueda\n\n");

        sb.append("📋 ").append("GESTIÓN DE LISTAS:").append("\n");
        sb.append("   list new <nombre>                Crear nueva lista\n");
        sb.append("   gpu add <número|nombre>          Añadir GPU a lista\n");
        sb.append("   list status                      Estado de lista actual\n");
        sb.append("   list show                        Mostrar lista con detalles\n");
        sb.append("   list export [-f json|xlsx]       Exportar lista\n");
        sb.append("   gpu remove -i <número>           Remover GPU de la lista según número\n");
        sb.append("   gpu remove -n <nombre>           Remover GPU de la lista por nombre\n");
        sb.append("   gpu remove -m <números>          Remover múltiples GPUs\n");
        sb.append("   list clear                       Limpiar lista actual\n");
        sb.append("   list all                         Mostrar TODAS las listas guardadas\n");
        sb.append("   list switch <nombre>             Cambiar a lista existente\n");
        sb.append("   list remove <nombre>             Eliminar lista permanentemente\n");
        sb.append("   list rename <viejo> <nuevo>      Renombrar lista\n");
        sb.append("   list load                        Recargar listas desde archivos\n\n");

        sb.append("💡 ").append("SISTEMA DE AYUDA:").append("\n");
        sb.append("   help                             Esta ayuda completa\n");
        sb.append("   suggest                          Sugerencias contextuales\n");
        sb.append("   status                           Estado del sistema\n");
        sb.append("   exit                             Salir\n\n");

        sb.append("📖 ").append("EJEMPLOS PRÁCTICOS:").append("\n");
        sb.append("   search \"rtx 4060\"               Buscar GPUs\n");
        sb.append("   gpu show 1                        Ver detalles del primer resultado\n");
        sb.append("   gpu add 1                         Añadir primer resultado a lista\n");
        sb.append("   list new \"Mi Comparativa\"       Crear lista\n");
        sb.append("   list export -f xlsx               Exportar como Excel\n");
        sb.append("   list switch \"Gaming\"            Cambiar a lista existente\n");
        sb.append("   list all                          Ver todas las listas\n\n");

        sb.append("💎 ").append("TIPS RÁPIDOS:").append("\n");
        sb.append("   • Usa números en lugar de nombres completos\n");
        sb.append("   • Usa 'suggest' si no sabes qué hacer\n");
        sb.append("   • Las comillas son opcionales para términos simples\n");
        sb.append("   • Las listas se guardan automáticamente\n");

        sb.append("\n🎮").append("═".repeat(70)).append("🎮\n");
        return sb.toString();
    }

    @ShellMethod(key = "search", value = "Buscar GPUs en la base de datos")
    public String searchGpus(@ShellOption(arity = Integer.MAX_VALUE) String[] queryParts) {
        String query = String.join(" ", queryParts);
        userSession.setLastQuery(query);

        List<Map<String, String>> searchResults = dbService.searchGpuResults(query);
        userSession.setLastSearchResults(searchResults);

        if (searchResults.isEmpty()) {
            return "❌ No se encontraron resultados para: " + query;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔍 RESULTADOS PARA: '").append(query).append("'\n");
        sb.append("📊 Encontradas: ").append(searchResults.size()).append(" GPUs\n");
        sb.append("─".repeat(60)).append("\n");

        for (int i = 0; i < searchResults.size(); i++) {
            String gpuName = searchResults.get(i).get("title");
            String displayName = gpuName.length() > 50 ? gpuName.substring(0, 47) + "..." : gpuName;
            sb.append(String.format("%2d. %s\n", i + 1, displayName));
        }

        // ✅ DETECCIÓN DE BÚSQUEDAS DEMASIADO GENERALES
        if (searchResults.size() >= 30) {
            sb.append("\n⚠️  BÚSQUEDA DEMASIADO GENERAL\n");
            sb.append("──────────────────────────────────────────────────────\n");

            // Sugerencias específicas basadas en la búsqueda
            String lowerQuery = query.toLowerCase();

            if (lowerQuery.contains("radeon") || lowerQuery.contains("amd")) {
                sb.append("💡 Para AMD Radeon, prueba:\n");
                sb.append("   • 'search radeon rx'\n");
                sb.append("   • 'search radeon 6000' (serie 6000)\n");
                sb.append("   • 'search radeon 7000' (serie 7000)\n");
                sb.append("   • 'search radeon rx 6700'\n");
                sb.append("   • 'search amd rx 7600'\n");
            }
            else if (lowerQuery.contains("geforce") || lowerQuery.contains("nvidia")) {
                sb.append("💡 Para NVIDIA GeForce, prueba:\n");
                sb.append("   • 'search geforce rtx'\n");
                sb.append("   • 'search rtx 30' (serie 3000)\n");
                sb.append("   • 'search rtx 40' (serie 4000)\n");
                sb.append("   • 'search rtx 3060'\n");
                sb.append("   • 'search gtx 1660'\n");
            }
            else if (lowerQuery.equals("rtx")) {
                sb.append("💡 Para NVIDIA RTX, prueba:\n");
                sb.append("   • 'search rtx 3060'\n");
                sb.append("   • 'search rtx 4070'\n");
                sb.append("   • 'search rtx 3080'\n");
                sb.append("   • 'search rtx 4060'\n");
            }
            else if (lowerQuery.equals("gtx")) {
                sb.append("💡 Para NVIDIA GTX, prueba:\n");
                sb.append("   • 'search gtx 1660'\n");
                sb.append("   • 'search gtx 1060'\n");
                sb.append("   • 'search gtx 1070'\n");
                sb.append("   • 'search gtx 1650'\n");
            }
            else {
                sb.append("💡 Sugerencias para afinar la búsqueda:\n");
                sb.append("   • Añade el modelo: '").append(query).append(" 3060'\n");
                sb.append("   • Especifica la serie: '").append(query).append(" 6000'\n");
                sb.append("   • Incluye la memoria: '").append(query).append(" 8gb'\n");
                sb.append("   • Usa términos más específicos\n");
            }

            sb.append("──────────────────────────────────────────────────────\n");
        }

        sb.append("\n💡 Usa 'gpu show <número>' para ver detalles");
        sb.append("\n💡 Usa 'gpu add <número>' para añadir a lista");

        return sb.toString();
    }

    // Agrega este método auxiliar en CliController
    private String getSearchSuggestions(String query) {
        String lowerQuery = query.toLowerCase();
        StringBuilder suggestions = new StringBuilder();

        suggestions.append("⚠️  BÚSQUEDA DEMASIADO GENERAL\n");
        suggestions.append("──────────────────────────────────────────────────────\n");

        Map<String, List<String>> keywordSuggestions = Map.of(
                "radeon", List.of("radeon rx", "radeon 6000", "radeon 7000", "radeon rx 6700", "amd rx 7600"),
                "amd", List.of("amd radeon", "amd rx", "amd 6000", "amd 7000", "amd rx 6700"),
                "geforce", List.of("geforce rtx", "rtx 30", "rtx 40", "rtx 3060", "gtx 1660"),
                "nvidia", List.of("nvidia rtx", "nvidia gtx", "rtx 3060", "gtx 1060", "rtx 4070"),
                "rtx", List.of("rtx 3060", "rtx 4070", "rtx 3080", "rtx 4060", "rtx 3070"),
                "gtx", List.of("gtx 1660", "gtx 1060", "gtx 1070", "gtx 1650", "gtx 1050"),
                "intel", List.of("intel arc", "arc a", "intel a770", "intel a750", "arc 7")
        );

        boolean foundSuggestion = false;

        for (Map.Entry<String, List<String>> entry : keywordSuggestions.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                suggestions.append("💡 Para ").append(entry.getKey().toUpperCase()).append(", prueba:\n");
                for (String suggestion : entry.getValue()) {
                    suggestions.append("   • 'search ").append(suggestion).append("'\n");
                }
                foundSuggestion = true;
                break;
            }
        }

        if (!foundSuggestion) {
            suggestions.append("💡 Sugerencias para afinar la búsqueda:\n");
            suggestions.append("   • Añade el modelo: '").append(query).append(" 3060'\n");
            suggestions.append("   • Especifica la serie: '").append(query).append(" 6000'\n");
            suggestions.append("   • Incluye la memoria: '").append(query).append(" 8gb'\n");
            suggestions.append("   • Usa términos más específicos\n");
        }

        suggestions.append("──────────────────────────────────────────────────────\n");
        return suggestions.toString();
    }



    @ShellMethod(key = "results", value = "Mostrar resultados de la última búsqueda")
    public String showLastResults() {
        List<Map<String, String>> lastSearchResults = userSession.getLastSearchResults();

        if (lastSearchResults.isEmpty()) {
            return "📭 No hay resultados de búsqueda recientes.\n" +
                    "💡 Usa 'search <términos>' para buscar GPUs\n" +
                    "💡 Ejemplo: 'search nvidia rtx'";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 ÚLTIMA BÚSQUEDA: '").append(userSession.getLastQuery()).append("'\n");
        sb.append("─".repeat(60)).append("\n");

        for (int i = 0; i < lastSearchResults.size(); i++) {
            String gpuName = lastSearchResults.get(i).get("title");
            String displayName = gpuName.length() > 50 ? gpuName.substring(0, 47) + "..." : gpuName;
            sb.append(String.format("%2d. %s\n", i + 1, displayName));
        }

        // Sugerencias contextuales
        sb.append("\n🎯 QUÉ PUEDES HACER AHORA:\n");
        sb.append("   • 'gpu show 1' - Ver detalles de la primera GPU\n");
        sb.append("   • 'gpu add 1' - Añadir la primera GPU a tu lista\n");
        sb.append("   • 'gpu add \"Nombre Exacto\"' - Añadir GPU específica\n");
        sb.append("   • 'search \"nuevos términos\"' - Hacer otra búsqueda\n");

        return sb.toString();
    }

    // 📊 SISTEMA DE DETALLES DE GPU
    @ShellMethod(key = "gpu show", value = "Mostrar detalles de GPU por índice de búsqueda")
    public String gpuShow(@ShellOption int index) {
        List<Map<String, String>> lastSearchResults = userSession.getLastSearchResults();

        if (lastSearchResults.isEmpty()) {
            return "❌ No hay búsquedas recientes. Usa 'search <query>' primero.";
        }

        if (index < 1 || index > lastSearchResults.size()) {
            return String.format("❌ Índice inválido. Debe estar entre 1 y %d.", lastSearchResults.size());
        }

        String gpuName = lastSearchResults.get(index - 1).get("title");

        try {
            Gpu gpu = dbService.getGpuDetails(gpuName);
            return formatGpuDetails(gpu);
        } catch (IOException e) {
            return "❌ Error al obtener detalles para: '" + gpuName + "'";
        }
    }

    // ⚖️ SISTEMA DE COMPARACIÓN
    @ShellMethod(key = "gpu compare", value = "Comparar dos GPUs")
    public String gpuCompare(
            @ShellOption(value = {"gpu1"}, arity = Integer.MAX_VALUE) String[] gpu1Parts,
            @ShellOption(value = {"gpu2"}, arity = Integer.MAX_VALUE) String[] gpu2Parts) {

        String gpu1 = String.join(" ", gpu1Parts);
        String gpu2 = String.join(" ", gpu2Parts);

        return comparisonService.compareGpus(gpu1, gpu2);
    }

    // 📋 SISTEMA AVANZADO DE LISTAS
    @ShellMethod(key = "list new", value = "Crear nueva lista")
    public String listNew(@ShellOption(arity = Integer.MAX_VALUE) String[] listNameParts) {
        String listName = String.join(" ", listNameParts);
        return listManager.createList(listName);
    }

    @ShellMethod(key = "gpu add", value = "Añadir GPU a lista actual")
    public String listAdd(@ShellOption(arity = Integer.MAX_VALUE) String[] gpuQueryParts) {
        String gpuQuery = String.join(" ", gpuQueryParts);
        try {
            return listManager.addToCurrentList(gpuQuery);
        } catch (IOException e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list status", value = "Estado de lista actual")
    public String listStatus() {
        String status = listManager.listStatus();

        if (status.contains("Lista activa: Ninguna")) {
            return status + "\n💡 Usa 'list all' para ver listas guardadas";
        } else {
            return status + "\n💡 Esta lista se guarda automáticamente";
        }
    }

    @ShellMethod(key = "list show", value = "Mostrar contenido detallado de lista")
    public String listShow() {
        return listManager.showListDetails();
    }

    @ShellMethod(key = "list export", value = "Exportar lista a archivo")
    public String listExport(
            @ShellOption(value = {"-f", "--format"}, defaultValue = "json") String format,
            @ShellOption(value = {"-o", "--output"}, defaultValue = ShellOption.NULL) String outputFile) {

        try {
            return listManager.saveList(format, outputFile);
        } catch (IOException e) {
            return "❌ Error al exportar: " + e.getMessage();
        }
    }

    @ShellMethod(key = "gpu remove", value = "Remover GPU de la lista actual")
    public String listRemove(
            @ShellOption(value = {"-i", "--index"}, defaultValue = ShellOption.NULL) Integer index,
            @ShellOption(value = {"-n", "--name"}, arity = Integer.MAX_VALUE, defaultValue = ShellOption.NULL) String[] gpuNameParts,
            @ShellOption(value = {"-m", "--multiple"}, arity = Integer.MAX_VALUE, defaultValue = ShellOption.NULL) Integer[] indices) {

        int methodCount = 0;
        if (index != null) methodCount++;
        if (gpuNameParts != null) methodCount++;
        if (indices != null && indices.length > 0) methodCount++;

        if (methodCount != 1) {
            return "❌ Usa solo una opción: -i <índice> O -n <nombre> O -m <índices>";
        }

        if (index != null) {
            return listManager.removeGpuFromList(index);
        }

        if (gpuNameParts != null) {
            String gpuName = String.join(" ", gpuNameParts);
            return listManager.removeGpuFromList(gpuName);
        }

        if (indices != null && indices.length > 0) {
            int[] primitiveIndices = Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
            return listManager.removeGpusFromList(primitiveIndices);
        }

        return "❌ Opción no válida";
    }

    @ShellMethod(key = "list clear", value = "Limpiar lista actual")
    public String listClear() {
        listManager.clearCurrentList();
        return "✅ Lista actual limpiada.";
    }

    @ShellMethod(key = "list all", value = "Mostrar todas las listas guardadas")
    public String listAll() {
        return listManager.listAllLists();
    }

    @ShellMethod(key = "list switch", value = "Cambiar a una lista existente")
    public String listSwitch(@ShellOption(arity = Integer.MAX_VALUE) String[] listNameParts) {
        String listName = String.join(" ", listNameParts);
        return listManager.switchList(listName);
    }

    @ShellMethod(key = "list delete", value = "Eliminar una lista permanentemente")
    public String listDelete(@ShellOption(arity = Integer.MAX_VALUE) String[] listNameParts) {
        String listName = String.join(" ", listNameParts);
        return listManager.deleteList(listName);
    }

    @ShellMethod(key = "list rename", value = "Renombrar una lista")
    public String listRename(
            @ShellOption(value = {"old"}, arity = Integer.MAX_VALUE) String[] oldNameParts,
            @ShellOption(value = {"new"}, arity = Integer.MAX_VALUE) String[] newNameParts) {

        String oldName = String.join(" ", oldNameParts);
        String newName = String.join(" ", newNameParts);
        return listManager.renameList(oldName, newName);
    }

    @ShellMethod(key = "list load", value = "Cargar listas desde archivos")
    public String listLoad() {
        return "✅ Listas cargadas desde archivos. Usa 'list all' para verlas.";
    }

    // ⚙️ SISTEMA Y CONFIGURACIÓN
    @ShellMethod(key = "status", value = "Estado del sistema")
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("🎮 ESTADO DEL SISTEMA\n");
        sb.append("─".repeat(50)).append("\n");
        sb.append("🔍 Última búsqueda: '").append(userSession.getLastQuery().isEmpty() ? "Ninguna" : userSession.getLastQuery()).append("'\n");
        sb.append("📊 Resultados guardados: ").append(userSession.getSearchResultsSize()).append("\n");

        // Estado de listas
        String listStatus = listManager.listStatus();
        if (listStatus.contains("No hay lista activa")) {
            sb.append("📋 Lista activa: Ninguna\n");
        } else {
            sb.append(listStatus).append("\n");
        }

        try {
            int dbCount = dbService.searchGpuResults("").size();
            sb.append("💾 Base de datos: ").append(dbCount).append(" GPUs\n");
        } catch (Exception e) {
            sb.append("💾 Base de datos: Error de acceso\n");
        }

        sb.append("─".repeat(50)).append("\n");
        return sb.toString();
    }

    @ShellMethod(key = "config clear", value = "Limpiar configuración")
    public String configClear() {
        userSession.clear();
        return "✅ Configuración limpiada. Listo para nueva sesión.";
    }

    @ShellMethod(key = "Exit!", value = "Salir del sistema")
    public void exit() {
        System.out.println("🚪 Saliendo del sistema... ¡Hasta pronto!");
        System.exit(0);
    }

    // 🛠️ MÉTODOS AUXILIARES
    private String formatGpuDetails(Gpu gpu) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 DETALLES DE: ").append(gpu.getName()).append("\n");
        sb.append("═".repeat(60)).append("\n");

        sb.append(String.format("%-20s %s\n", "🏭 Fabricante:", gpu.getManufacturer()));
        sb.append(String.format("%-20s %s\n", "🔩 Arquitectura:", gpu.getArchitecture()));
        sb.append(String.format("%-20s %s\n", "📅 Lanzamiento:", gpu.getReleaseDate()));

        sb.append("\n⚡ RENDIMIENTO:\n");
        sb.append(String.format("%-20s %.2f GFLOPs\n", "FP32:", Optional.ofNullable(gpu.getFp32()).orElse(0.0)));

        sb.append("\n🎮 GRÁFICOS:\n");
        sb.append(String.format("%-20s %d MHz\n", "Frec. Base:", Optional.ofNullable(gpu.getBaseClock()).orElse(0)));
        sb.append(String.format("%-20s %d MHz\n", "Frec. Boost:", Optional.ofNullable(gpu.getBoostClock()).orElse(0)));

        sb.append("\n💾 MEMORIA:\n");
        sb.append(String.format("%-20s %.1f GB\n", "Tamaño:", Optional.ofNullable(gpu.getMemorySize()).orElse(0.0)));
        sb.append(String.format("%-20s %s\n", "Tipo:", Optional.ofNullable(gpu.getMemoryType()).orElse("N/A")));
        sb.append(String.format("%-20s %d bits\n", "Bus:", Optional.ofNullable(gpu.getMemoryBus()).orElse(0)));
        sb.append(String.format("%-20s %.1f GB/s\n", "Ancho Banda:", Optional.ofNullable(gpu.getBandwidth()).orElse(0.0)));

        sb.append("\n⚙️ ESPECIFICACIONES:\n");
        sb.append(String.format("%-20s %s\n", "Unid. Sombreado:", Optional.ofNullable(gpu.getShadingUnits()).map(Object::toString).orElse("N/A")));
        sb.append(String.format("%-20s %s\n", "TDP:", Optional.ofNullable(gpu.getTdp()).orElse("N/A")));
        sb.append(String.format("%-20s %s\n", "PSU Sugerida:", Optional.ofNullable(gpu.getSuggestedPsu()).orElse("N/A")));

        sb.append("═".repeat(60)).append("\n");
        sb.append("💡 Usa 'list add \"").append(gpu.getName()).append("\"' para añadir a lista");

        return sb.toString();
    }
}