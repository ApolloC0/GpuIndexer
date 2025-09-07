package GpuIndex.App;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GpuIndexApp {
    public static void main(String[] args) {
        SpringApplication.run(GpuIndexApp.class, args);
    }

    @PostConstruct
    public void showHelp() {

            System.out.println();
            System.out.println("================================================");
            System.out.println("           GPU INDEX MANAGER");
            System.out.println("           By ApolloC0 (Manuel Rivas)");
            System.out.println("================================================");
            System.out.println();
            System.out.println("Starting GPU Indexer App...");
            System.out.println("================================================");

            System.out.println("COMMANDS HELP:");
            System.out.println(" search <terms> [-a]         Search GPUs (-a to auto add)");
            System.out.println(" gpu show <number>           Show details by result number");
            System.out.println(" gpu compare <gpu1> <gpu2>   Compare two GPUs");
            System.out.println(" results                     Show last search results");
            System.out.println(" list new <name>             Create new list");
            System.out.println(" gpu add <number|name>       Add GPU to list");
            System.out.println(" list status                 Current list status");
            System.out.println(" list show                   Show list with details");
            System.out.println(" list export [-f json|xlsx]  Export list");
            System.out.println(" gpu remove -i <number>      Remove GPU by number");
            System.out.println(" gpu remove -n <name>        Remove GPU by name");
            System.out.println(" gpu remove -m <numbers>     Remove multiple GPUs");
            System.out.println(" list clear                  Clear current list");
            System.out.println(" list all                    Show ALL saved lists");
            System.out.println(" list switch <name>          Switch to existing list");
            System.out.println(" list remove <name>          Delete list permanently");
            System.out.println(" list rename <old> <new>     Rename list");
            System.out.println(" list load                   Reload lists from files");
            System.out.println(" fullhelp                    Full help");
            System.out.println(" suggest                     Contextual suggestions");
            System.out.println(" status                      System status");
            System.out.println(" exitnow                     Exit application");
            System.out.println("================================================");
        }
    }





