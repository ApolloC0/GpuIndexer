package GpuIndex.App.controller;

import GpuIndex.App.model.Gpu;
import GpuIndex.App.service.DbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gpus")
public class GpuController {

    @Autowired
    private DbService dbService;

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, String>>> searchGpus(@RequestParam String query) {
        List<Map<String, String>> results = dbService.searchGpuResults(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/details/from-url")
    public ResponseEntity<Gpu> getGpuDetailsFromUrl(@RequestParam String url) {
        try {
            Gpu gpu = dbService.getGpuDetails(url);
            if (gpu != null) {
                return ResponseEntity.ok(gpu);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/details")
    public ResponseEntity<Gpu> getGpuDetails(@RequestParam(required = false) String url,
                                             @RequestParam(required = false) String name) {
        try {
            if (url != null) {
                Gpu gpu = dbService.getGpuDetails(url);
                return ResponseEntity.ok(gpu);
            } else if (name != null) {
                // Lógica para buscar por nombre (si la implementas)
                return ResponseEntity.badRequest().body(null);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
