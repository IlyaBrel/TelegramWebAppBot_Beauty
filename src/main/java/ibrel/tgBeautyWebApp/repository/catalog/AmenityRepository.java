package ibrel.tgBeautyWebApp.repository.catalog;

import ibrel.tgBeautyWebApp.model.master.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}