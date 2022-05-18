package capstoneDesign.carbonTracker.apartment.repository;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AptEnergyRepository extends ElasticsearchRepository<AptEnergyResponse, String> {
    Optional<AptEnergyResponse> findByKaptCodeAndDate(String kaptCode, String date);
}
