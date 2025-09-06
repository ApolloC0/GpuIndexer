package GpuIndex.App.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import GpuIndex.App.model.Gpu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbService {

    private static final Logger logger = LoggerFactory.getLogger(DbService.class);
    private List<Gpu> gpuDatabase;

    @PostConstruct
    public void loadGpuDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = new ClassPathResource("gpu_database.json").getInputStream()) {
            List<Gpu> allGpus = mapper.readValue(inputStream, mapper.getTypeFactory().constructCollectionType(List.class, Gpu.class));
            logger.info("Base de datos de GPUs cargada. Total: {} GPUs.", allGpus.size());

            final LocalDate cutoffDate = LocalDate.of(2013, 1, 1);
            this.gpuDatabase = allGpus.stream()
                    .filter(gpu -> {
                        if (gpu.getReleaseDate() == null || gpu.getReleaseDate().trim().isEmpty()) {
                            return false;
                        }
                        try {
                            LocalDate releaseDate = LocalDate.parse(gpu.getReleaseDate());
                            return !releaseDate.isBefore(cutoffDate);
                        } catch (DateTimeParseException e) {
                            logger.warn("Formato de fecha inválido para '{}': {}. Excluyendo de la búsqueda.", gpu.getName(), gpu.getReleaseDate());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            logger.info("Total de GPUs después de filtrar por fecha (>= 2013): {}", gpuDatabase.size());

        } catch (IOException e) {
            logger.error("Error al cargar la base de datos de GPUs desde gpu_database.json", e);
            gpuDatabase = Collections.emptyList();
        }
    }

    private String normalizeForSearch(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    @Cacheable("gpuSearch")
    public List<Map<String, String>> searchGpuResults(String query) {
        if (gpuDatabase.isEmpty()) {
            return Collections.emptyList();
        }

        final String lowerQuery = query.toLowerCase().trim();

        // ✅ LISTA DE BÚSQUEDAS DEMASIADO GENERALES (se bloquearán)
        List<String> tooGeneralSearches = Arrays.asList(
                "radeon", "amd", "geforce", "nvidia", "rtx", "gtx",
                "intel", "graphics", "gpu", "video", "card"
        );

        // Verificar si la búsqueda es demasiado general
        for (String generalTerm : tooGeneralSearches) {
            if (lowerQuery.equals(generalTerm) ||
                    lowerQuery.equals("amd " + generalTerm) ||
                    lowerQuery.equals("nvidia " + generalTerm)) {
                return Collections.emptyList(); // ✅ Retorna lista vacía para forzar el mensaje
            }
        }

        // Búsqueda normal para términos específicos
        return gpuDatabase.stream()
                .filter(gpu -> gpu.getName().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparing(Gpu::getName))
                .limit(30)
                .map(gpu -> Map.of(
                        "title", gpu.getName(),
                        "url", "local://" + gpu.getName().toLowerCase().replace(" ", "-"),
                        "description", "Datos locales."
                ))
                .collect(Collectors.toList());
    }

    @Cacheable("gpuDetails")
    public Gpu getGpuDetails(String gpuName) throws IOException {
        return gpuDatabase.stream()
                .filter(gpu -> gpu.getName().equalsIgnoreCase(gpuName))
                .findFirst()
                .orElseThrow(() -> new IOException("GPU no encontrada en la base de datos local: " + gpuName));
    }
}