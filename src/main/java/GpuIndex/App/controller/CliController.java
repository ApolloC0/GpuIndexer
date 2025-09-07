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

    private static final int MAX_LIST_SIZE = 20;

    // HELP SYSTEM
    @ShellMethod(key = "fullhelp", value = "Show full help with examples")
    public String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(70)).append("\n");
        sb.append("                    GPU INDEXER - FULL GUIDE\n");
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

            String lowerQuery = query.toLowerCase();

            if (lowerQuery.contains("radeon") || lowerQuery.contains("amd")) {
                sb.append("Try:\n");
                sb.append("  search radeon rx\n");
                sb.append("  search radeon 6000\n");
                sb.append("  search radeon 7000\n");
                sb.append("  search radeon rx 6700\n");
                sb.append("  search amd rx 7600\n");
            } else if (lowerQuery.contains("geforce") || lowerQuery.contains("nvidia")) {
                sb.append("Try:\n");
                sb.append("  search geforce rtx\n");
                sb.append("  search rtx 30\n");
                sb.append("  search rtx 40\n");
                sb.append("  search rtx 3060\n");
                sb.append("  search gtx 1660\n");
            } else if (lowerQuery.equals("rtx")) {
                sb.append("Try:\n");
                sb.append("  search rtx 3060\n");
                sb.append("  search rtx 4070\n");
                sb.append("  search rtx 3080\n");
                sb.append("  search rtx 4060\n");
            } else if (lowerQuery.equals("gtx")) {
                sb.append("Try:\n");
                sb.append("  search gtx 1660\n");
                sb.append("  search gtx 1060\n");
                sb.append("  search gtx 1070\n");
                sb.append("  search gtx 1650\n");
            } else {
                sb.append("Suggestions to refine your search:\n");
                sb.append("  Add model: '").append(query).append(" 3060'\n");
                sb.append("  Specify series: '").append(query).append(" 6000'\n");
                sb.append("  Include memory: '").append(query).append(" 8gb'\n");
                sb.append("  Use more specific terms\n");
            }

            sb.append("-".repeat(60)).append("\n");
        }

        sb.append("\nUse 'gpu show <number>' to view details.");
        sb.append("\nUse 'gpu add <number>' to add to list.");

        return sb.toString();
    }


    @ShellMethod(key = "exitnow", value = "Exit the system")
    public void exit() {
        System.out.println("Exiting the system... Goodbye!");
        System.exit(0);
    }


    private String formatGpuDetails(Gpu gpu) {
        StringBuilder sb = new StringBuilder();
        sb.append("DETAILS FOR: ").append(gpu.getName()).append("\n");
        sb.append("=".repeat(60)).append("\n");

        sb.append(String.format("%-20s %s\n", "Manufacturer:", gpu.getManufacturer()));
        sb.append(String.format("%-20s %s\n", "Architecture:", gpu.getArchitecture()));
        sb.append(String.format("%-20s %s\n", "Release Date:", gpu.getReleaseDate()));

        sb.append(String.format("%-20s %.2f GFLOPs\n", "FP32:", Optional.ofNullable(gpu.getFp32()).orElse(0.0)));
        sb.append(String.format("%-20s %d MHz\n", "Base Clock:", Optional.ofNullable(gpu.getBaseClock()).orElse(0)));
        sb.append(String.format("%-20s %d MHz\n", "Boost Clock:", Optional.ofNullable(gpu.getBoostClock()).orElse(0)));

        sb.append(String.format("%-20s %.1f GB\n", "Memory Size:", Optional.ofNullable(gpu.getMemorySize()).orElse(0.0)));
        sb.append(String.format("%-20s %s\n", "Memory Type:", Optional.ofNullable(gpu.getMemoryType()).orElse("N/A")));
        sb.append(String.format("%-20s %d bits\n", "Memory Bus:", Optional.ofNullable(gpu.getMemoryBus()).orElse(0)));
        sb.append(String.format("%-20s %.1f GB/s\n", "Bandwidth:", Optional.ofNullable(gpu.getBandwidth()).orElse(0.0)));

        sb.append(String.format("%-20s %s\n", "Shading Units:", Optional.ofNullable(gpu.getShadingUnits()).map(Object::toString).orElse("N/A")));
        sb.append(String.format("%-20s %s\n", "TDP:", Optional.ofNullable(gpu.getTdp()).orElse("N/A")));
        sb.append(String.format("%-20s %s\n", "Suggested PSU:", Optional.ofNullable(gpu.getSuggestedPsu()).orElse("N/A")));

        sb.append("=".repeat(60)).append("\n");
        sb.append("Use 'list add \"").append(gpu.getName()).append("\" to add to list.");

        return sb.toString();
    }
}
