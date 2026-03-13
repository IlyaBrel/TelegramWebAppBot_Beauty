package ibrel.tgBeautyWebApp.repository.catalog;

import ibrel.tgBeautyWebApp.model.master.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);
    List<City> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}