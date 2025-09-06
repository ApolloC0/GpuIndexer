package GpuIndex.App.repository;

import GpuIndex.App.model.Gpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GpuRepository extends JpaRepository<Gpu, Long> {

    List<Gpu> findByNameContainingIgnoreCase(String name);

    @Query("SELECT g FROM Gpu g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Gpu> searchByName(@Param("query") String query);

    boolean existsByName(String name);
}