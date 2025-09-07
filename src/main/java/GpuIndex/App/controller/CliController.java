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

    // HELP SYSTEM
    @ShellMethod(key = "fullhelp", value = "Show full help with examples")
    public String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(70)).append("\n");
        sb.append("                GPU INDEXER - FULL GUIDE\n");
        sb.append("=".repeat(70)).append("\n\n");

        sb.append("[SEARCH AND QUERY]\n");
        sb.append("   search <terms> [-a]         Search GPUs (-a to auto add)\n");
        sb.append("   gpu show <number>           Show details by result number\n");
        sb.append("   gpu compare <gpu1> <gpu2>   Compare two GPUs\n");
        sb.append("   results                    Show last search results\n\n");

        sb.append("[LIST MANAGEMENT]\n");
        sb.append("   list new <name>             Create new list\n");
        sb.append("   gpu add <number|name>       Add GPU to list\n");
        sb.append("   list status                 Current list status\n");
        sb.append("   list show                   Show list with details\n");
        sb.append("   list export [-f json|xlsx]  Export list\n");
        sb.append("   gpu remove -i <number>      Remove GPU by number\n");
        sb.append("   gpu remove -n <name>        Remove GPU by name\n");
        sb.append("   gpu remove -m <numbers>     Remove multiple GPUs\n");
        sb.append("   list clear                 Clear current list\n");
        sb.append("   list all                   Show all saved lists\n");
        sb.append("   list switch <name>          Switch to existing list\n");
        sb.append("   list remove <name>          Delete list permanently\n");
        sb.append("   list rename <old> <new>     Rename list\n");
        sb.append("   list load                  Reload lists from files\n\n");

        sb.append("[HELP SYSTEM]\n");
        sb.append("   help                      Full help\n");
        sb.append("   suggest                   Contextual suggestions\n");
        sb.append("   status                    System status\n");
        sb.append("   exit                      Exit\n\n");

        sb.append("[PRACTICAL EXAMPLES]\n");
        sb.append("   search \"rtx 4060\"          Search GPUs\n");
        sb.append("   gpu show 1                  Show details of first result\n");
        sb.append("   gpu add 1                   Add first result to list\n");
        sb.append("   list new \"My Comparison\"  Create list\n");
        sb.append("   list export -f xlsx         Export as Excel\n");
        sb.append("   list switch \"Gaming\"       Switch list\n");
        sb.append("   list all                   Show all lists\n\n");

        sb.append("[QUICK TIPS]\n");
        sb.append("   - Use numbers instead of full names\n");
        sb.append("   - Use 'suggest' if unsure what to do\n");
        sb.append("   - Quotes are optional for simple terms\n");
        sb.append("   - Lists are saved automatically\n");

        sb.append("\n").append("=".repeat(70)).append("\n");
        return sb.toString();
    }

    @ShellMethod(key = "search", value = "Search GPUs in database")
    public String searchGpus(@ShellOption(arity = Integer.MAX_VALUE) String[] queryParts) {
        String query = String.join(" ", queryParts);
        userSession.setLastQuery(query);

        List<Map<String, String>> searchResults = dbService.searchGpuResults(query);
        userSession.setLastSearchResults(searchResults);

        if (searchResults.isEmpty()) {
            return "[ERROR] No results found for: " + query;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[RESULTS FOR] '").append(query).append("'\n");
        sb.append("[Total found] ").append(searchResults.size()).append(" GPUs\n");
        sb.append("-".repeat(60)).append("\n");

        for (int i = 0; i < searchResults.size(); i++) {
            String gpuName = searchResults.get(i).get("title");
            String displayName = gpuName.length() > 50 ? gpuName.substring(0, 47) + "..." : gpuName;
            sb.append(String.format("%2d. %s\n", i + 1, displayName));
        }

        // TOO GENERAL SEARCH DETECTION
        if (searchResults.size() >= 30) {
            sb.append("\n[WARNING] TOO GENERAL SEARCH\n");
            sb.append("-".repeat(60)).append("\n");
            sb.append(getSearchSuggestions(query));
        }

        sb.append("\nUse 'gpu show <number>' to view details.");
        sb.append("\nUse 'gpu add <full name>' to add to list.");

        return sb.toString();
    }

    private String getSearchSuggestions(String query) {
        String lowerQuery = query.toLowerCase();
        StringBuilder suggestions = new StringBuilder();

        if (lowerQuery.contains("radeon") || lowerQuery.contains("amd")) {
            suggestions.append("Try:\n");
            suggestions.append("  search radeon rx\n");
            suggestions.append("  search radeon 6000\n");
            suggestions.append("  search radeon 7000\n");
            suggestions.append("  search radeon rx 6700\n");
            suggestions.append("  search amd rx 7600\n");
        } else if (lowerQuery.contains("geforce") || lowerQuery.contains("nvidia")) {
            suggestions.append("Try:\n");
            suggestions.append("  search geforce rtx\n");
            suggestions.append("  search rtx 30\n");
            suggestions.append("  search rtx 40\n");
            suggestions.append("  search rtx 3060\n");
            suggestions.append("  search gtx 1660\n");
        } else if (lowerQuery.equals("rtx")) {
            suggestions.append("Try:\n");
            suggestions.append("  search rtx 3060\n");
            suggestions.append("  search rtx 4070\n");
            suggestions.append("  search rtx 3080\n");
            suggestions.append("  search rtx 4060\n");
        } else if (lowerQuery.equals("gtx")) {
            suggestions.append("Try:\n");
            suggestions.append("  search gtx 1660\n");
            suggestions.append("  search gtx 1060\n");
            suggestions.append("  search gtx 1070\n");
            suggestions.append("  search gtx 1650\n");
        } else {
            suggestions.append("Suggestions to refine your search:\n");
            suggestions.append("  Add model: '").append(query).append(" 3060'\n");
            suggestions.append("  Specify series: '").append(query).append(" 6000'\n");
            suggestions.append("  Include memory: '").append(query).append(" 8gb'\n");
            suggestions.append("  Use more specific terms\n");
        }

        return suggestions.toString();
    }

    @ShellMethod(key = "results", value = "Show last search results")
    public String showLastResults() {
        List<Map<String, String>> lastSearchResults = userSession.getLastSearchResults();

        if (lastSearchResults.isEmpty()) {
            return "[INFO] No recent search results.\n" +
                    "Use 'search <terms>' to search GPUs\n" +
                    "Example: 'search nvidia rtx'";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[LAST SEARCH] '").append(userSession.getLastQuery()).append("'\n");
        sb.append("-".repeat(60)).append("\n");

        for (int i = 0; i < lastSearchResults.size(); i++) {
            String gpuName = lastSearchResults.get(i).get("title");
            String displayName = gpuName.length() > 50 ? gpuName.substring(0, 47) + "..." : gpuName;
            sb.append(String.format("%2d. %s\n", i + 1, displayName));
        }

        sb.append("\n[WHAT YOU CAN DO NOW]\n");
        sb.append("  'gpu show 1' - Show details of first GPU\n");
        sb.append("  'gpu add 1' - Add first GPU to your list\n");
        sb.append("  'gpu add \"Exact Name\"' - Add specific GPU\n");
        sb.append("  'search \"new terms\"' - Perform another search\n");

        return sb.toString();
    }

    // GPU DETAILS SYSTEM
    @ShellMethod(key = "gpu show", value = "Show GPU details by search index")
    public String gpuShow(@ShellOption int index) {
        List<Map<String, String>> lastSearchResults = userSession.getLastSearchResults();

        if (lastSearchResults.isEmpty()) {
            return "[ERROR] No recent searches. Use 'search <query>' first.";
        }

        if (index < 1 || index > lastSearchResults.size()) {
            return String.format("[ERROR] Invalid index. Must be between 1 and %d.", lastSearchResults.size());
        }

        String gpuName = lastSearchResults.get(index - 1).get("title");

        try {
            Gpu gpu = dbService.getGpuDetails(gpuName);
            return formatGpuDetails(gpu);
        } catch (IOException e) {
            return "[ERROR] Error getting details for: '" + gpuName + "'";
        }
    }

    // COMPARISON SYSTEM
    @ShellMethod(key = "gpu compare", value = "Compare two GPUs")
    public String gpuCompare(
            @ShellOption(value = {"gpu1"}, arity = Integer.MAX_VALUE) String[] gpu1Parts,
            @ShellOption(value = {"gpu2"}, arity = Integer.MAX_VALUE) String[] gpu2Parts) {

        String gpu1 = String.join(" ", gpu1Parts);
        String gpu2 = String.join(" ", gpu2Parts);

        return comparisonService.compareGpus(gpu1, gpu2);
    }

    // ADVANCED LIST SYSTEM
    @ShellMethod(key = "list new", value = "Create new list")
    public String listNew(@ShellOption(arity = Integer.MAX_VALUE) String[] listNameParts) {
        String listName = String.join(" ", listNameParts);
        return listManager.createList(listName);
    }

    @ShellMethod(key = "gpu add", value = "Add GPU to current list")
    public String listAdd(@ShellOption(arity = Integer.MAX_VALUE) String[] gpuQueryParts) {
        String gpuQuery = String.join(" ", gpuQueryParts);
        try {
            return listManager.addToCurrentList(gpuQuery);
        } catch (IOException e) {
            return "[ERROR] Error: " + e.getMessage();
        }
    }

    @ShellMethod(key = "list status", value = "Current list status")
    public String listStatus() {
        String status = listManager.listStatus();

        if (status.contains("Active list: None")) {
            return status + "\nUse 'list all' to see saved lists";
        } else {
            return status + "\nThis list is saved automatically";
        }
    }

    @ShellMethod(key = "list show", value = "Show detailed list content")
    public String listShow() {
        return listManager.showListDetails();
    }

    @ShellMethod(key = "list export", value = "Export list to file")
    public String listExport(
            @ShellOption(value = {"-f", "--format"}, defaultValue = "json") String format,
            @ShellOption(value = {"-o", "--output"}, defaultValue = ShellOption.NULL) String outputFile) {

        try {
            return listManager.saveList(format, outputFile);
        } catch (IOException e) {
            return "[ERROR] Error exporting: " + e.getMessage();
        }
    }

    @ShellMethod(key = "gpu remove", value = "Remove GPU from current list")
public String listRemove(
        @ShellOption(value = {"-i", "--index"}, defaultValue = ShellOption.NULL) Integer index,
        @ShellOption(value = {"-n", "--name"}, arity = Integer.MAX_VALUE, defaultValue = ShellOption.NULL) String[] gpuNameParts,
        @ShellOption(value = {"-m", "--multiple"}, arity = Integer.MAX_VALUE, defaultValue = ShellOption.NULL) Integer[] indices) {

    int methodCount = 0;
    if (index != null) methodCount++;
    if (gpuNameParts != null) methodCount++;
    if (indices != null && indices.length > 0) methodCount++;

    // MENSAJE ESPECÍFICO CUANDO NO SE PROPORCIONAN ARGUMENTOS
    if (methodCount == 0) {
        return "[ERROR] Missing arguments for 'gpu remove' command\n" +
               "-".repeat(50) + "\n" +
               "You must specify HOW to remove the GPU:\n\n" +
               "OPTIONS:\n" +
               "  -i <number>     Remove by position number in list\n" +
               "  -n <name>       Remove by GPU name (partial match)\n" +
               "  -m <numbers>    Remove multiple GPUs by positions\n\n" +
               "EXAMPLES:\n" +
               "  gpu remove -i 1                    (Remove first GPU)\n" +
               "  gpu remove -n \"RTX 4060\"           (Remove by name)\n" +
               "  gpu remove -m 1 3 5                (Remove positions 1, 3, and 5)\n\n" +
               "[TIP] Use 'list show' to see GPU positions in your list";
    }

    if (methodCount > 1) {
        return "[ERROR] Use only ONE option: -i <index> OR -n <name> OR -m <indices>";
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

    return "[ERROR] Invalid option";
}

    @ShellMethod(key = "list clear", value = "Clear current list")
    public String listClear() {
        listManager.clearCurrentList();
        return "[SUCCESS] Current list cleared.";
    }

    @ShellMethod(key = "list all", value = "Show all saved lists")
    public String listAll() {
        return listManager.listAllLists();
    }

    @ShellMethod(key = "list switch", value = "Switch to an existing list")
    public String listSwitch(@ShellOption(arity = Integer.MAX_VALUE) String[] listNameParts) {
        String listName = String.join(" ", listNameParts);
        return listManager.switchList(listName);
    }

    @ShellMethod(key = "list delete", value = "Permanently delete a list")
    public String listDelete(@ShellOption(arity = Integer.MAX_VALUE) String[] listNameParts) {
        String listName = String.join(" ", listNameParts);
        return listManager.deleteList(listName);
    }

    @ShellMethod(key = "list rename", value = "Rename a list")
    public String listRename(
            @ShellOption(value = {"old"}, arity = Integer.MAX_VALUE) String[] oldNameParts,
            @ShellOption(value = {"new"}, arity = Integer.MAX_VALUE) String[] newNameParts) {

        String oldName = String.join(" ", oldNameParts);
        String newName = String.join(" ", newNameParts);
        return listManager.renameList(oldName, newName);
    }

    @ShellMethod(key = "list load", value = "Load lists from files")
    public String listLoad() {
        return "[SUCCESS] Lists loaded from files. Use 'list all' to view.";
    }

    // SYSTEM AND CONFIGURATION
    @ShellMethod(key = "status", value = "System status")
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("[SYSTEM STATUS]\n");
        sb.append("-".repeat(50)).append("\n");
        sb.append("Last search: '").append(userSession.getLastQuery().isEmpty() ? "None" : userSession.getLastQuery()).append("'\n");
        sb.append("Saved results: ").append(userSession.getSearchResultsSize()).append("\n");

        // List status
        String listStatus = listManager.listStatus();
        if (listStatus.contains("Active list: none")) {
            sb.append("Active list: None\n");
        } else {
            sb.append(listStatus).append("\n");
        }

        try {
            int dbCount = dbService.searchGpuResults("").size();
            sb.append("Database: ").append(dbCount).append(" GPUs\n");
        } catch (Exception e) {
            sb.append("Database: Access error\n");
        }

        sb.append("-".repeat(50)).append("\n");
        return sb.toString();
    }

    @ShellMethod(key = "config clear", value = "Clear configuration")
    public String configClear() {
        userSession.clear();
        return "[SUCCESS] Configuration cleared. Ready for new session.";
    }

    @ShellMethod(key = "exitnow", value = "Exit the system")
    public void exit() {
        System.out.println("Exiting the system... Goodbye!");
        System.exit(0);
    }

    // HELPER METHODS
    private String formatGpuDetails(Gpu gpu) {
        StringBuilder sb = new StringBuilder();
        sb.append("DETAILS FOR: ").append(gpu.getName()).append("\n");
        sb.append("=".repeat(60)).append("\n");

        sb.append(String.format("%-20s %s\n", "Manufacturer:", gpu.getManufacturer()));
        sb.append(String.format("%-20s %s\n", "Architecture:", gpu.getArchitecture()));
        sb.append(String.format("%-20s %s\n", "Release Date:", gpu.getReleaseDate()));

        sb.append("\n[PERFORMANCE]\n");
        sb.append(String.format("%-20s %.2f GFLOPs\n", "FP32:", Optional.ofNullable(gpu.getFp32()).orElse(0.0)));

        sb.append("\n[GRAPHICS]\n");
        sb.append(String.format("%-20s %d MHz\n", "Base Clock:", Optional.ofNullable(gpu.getBaseClock()).orElse(0)));
        sb.append(String.format("%-20s %d MHz\n", "Boost Clock:", Optional.ofNullable(gpu.getBoostClock()).orElse(0)));

        sb.append("\n[MEMORY]\n");
        sb.append(String.format("%-20s %.1f GB\n", "Size:", Optional.ofNullable(gpu.getMemorySize()).orElse(0.0)));
        sb.append(String.format("%-20s %s\n", "Type:", Optional.ofNullable(gpu.getMemoryType()).orElse("N/A")));
        sb.append(String.format("%-20s %d bits\n", "Bus:", Optional.ofNullable(gpu.getMemoryBus()).orElse(0)));
        sb.append(String.format("%-20s %.1f GB/s\n", "Bandwidth:", Optional.ofNullable(gpu.getBandwidth()).orElse(0.0)));

        sb.append("\n[SPECIFICATIONS]\n");
        sb.append(String.format("%-20s %s\n", "Shading Units:", Optional.ofNullable(gpu.getShadingUnits()).map(Object::toString).orElse("N/A")));
        sb.append(String.format("%-20s %s\n", "TDP:", Optional.ofNullable(gpu.getTdp()).orElse("N/A")));
        sb.append(String.format("%-20s %s\n", "Suggested PSU:", Optional.ofNullable(gpu.getSuggestedPsu()).orElse("N/A")));

        sb.append("=".repeat(60)).append("\n");
        sb.append("Use 'list add \"").append(gpu.getName()).append("\"' to add to list");

        return sb.toString();
    }
}
