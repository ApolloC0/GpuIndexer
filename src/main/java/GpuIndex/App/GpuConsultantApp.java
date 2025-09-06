package GpuIndex.App;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpuConsultantApp {
    public static void main(String[] args) {
        try {
            System.out.println("\n");
            System.out.println(" ⠀⠀⠀⠀⠀⠀             ⣤⣄⢘⣒⣀⣀⣀⣀⠀⠀⠀");
            System.out.println(" ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀ ⣽⣿⣛⠛⢛⣿⣿⡿⠟⠂⠀");
            System.out.println(" ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀ ⣀⣀⣀⣀⡀⠀⣤⣾⣿⣿⣿⣿⣿⣿⣿⣷⣿⡆⠀");
            System.out.println(" ⠀⠀⠀⠀⠀⠀⣀⣤⣶⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠁⠀");
            System.out.println(" ⠀⠀⠀⢀⣴⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀");
            System.out.println(" ⠀⠀⣠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀");
            System.out.println(" ⠀⠀⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠜⠀⠀⠀⠀⠀⠀⠀");
            System.out.println(" ⠀⠀⠀⢿⣿⣿⣿⣿⠿⠿⣿⣿⡿⢿⣿⣿⠈⣿⣿⣿⡏⣠⡴⠀⠀⠀⠀⠀⠀⠀");
            System.out.println(" ⠀⠀⣠⣿⣿⣿⡿⢁⣴⣶⣄⠀⠀⠉⠉⠉⠀⢻⣿⡿⢰⣿⡇⠀⠀⠀⠀⠀⠀⠀");
            System.out.println(" ⠀⠀⢿⣿⠟⠋⠀⠈⠛⣿⣿⠀⠀⠀⠀⠀⠀⠸⣿⡇⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀");
            System.out.println(" ⠀⠀⢸⣿⠀⠀⠀⠀⠀⠘⠿⠆⠀⠀⠀⠀⠀⠀⣿⡇⠀⠿⠇⠀⠀⠀⠀" +

                    "GPU Index Manager By ApolloC0 (Manuel Rivas)");
            System.out.println("\n");
            System.out.println("🚀 Iniciando Indexador de GPUs...");
            System.out.println("═".repeat(50));

            SpringApplication.run(GpuConsultantApp.class, args);

        } catch (Exception e) {
            /*
             * SISTEMA DE MANEJO DE ERRORES COMENTADO
             *
             * Este bloque estaba diseñado para capturar y mostrar errores detallados
             * durante el inicio de la aplicación. Incluía:
             *
             * 1. StackTrace completo de la excepción
             * 2. Análisis de causas raíz con múltiples niveles
             * 3. Información estructurada de cada excepción en la cadena
             *
             * Se ha comentado para mantener un inicio más limpio, pero puede
             * ser reactivado para debugging en entornos de desarrollo.
             *
             * System.err.println("\n❌ ERROR AL INICIAR LA APLICACIÓN");
             * System.err.println("═".repeat(50));
             * e.printStackTrace();
             *
             * // Imprimir todas las causas
             * Throwable cause = e;
             * int level = 0;
             * while (cause != null) {
             *     System.err.println("🔍 CAUSA " + level + ": " + cause.getClass().getSimpleName());
             *     System.err.println("📄 MENSAJE: " + cause.getMessage());
             *     System.err.println("─".repeat(50));
             *     cause = cause.getCause();
             *     level++;
             * }
             */

            // Mensaje simplificado para el usuario final
            System.err.println("\n❌ Error: No se pudo iniciar la aplicación");
            System.err.println("💡 Contacta al soporte técnico si el problema persiste");
            System.exit(1);
        }
    }
}