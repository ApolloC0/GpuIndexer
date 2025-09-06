package GpuIndex.App.service;

import GpuIndex.App.model.Gpu;
import GpuIndex.App.repository.GpuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class GpuFileService {

    @Autowired
    private GpuRepository gpuRepository;

    @Transactional
    public void saveGpu(Gpu gpu) {
        gpuRepository.save(gpu);
    }

    @Transactional(readOnly = true)
    public List<Gpu> searchGpus(String query) {
        return gpuRepository.searchByName(query);
    }

    @Transactional(readOnly = true)
    public Optional<Gpu> getGpuByName(String name) {
        return gpuRepository.findByNameContainingIgnoreCase(name).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public boolean gpuExists(String name) {
        return gpuRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public List<Gpu> getAllGpus() {
        return gpuRepository.findAll();
    }
}
