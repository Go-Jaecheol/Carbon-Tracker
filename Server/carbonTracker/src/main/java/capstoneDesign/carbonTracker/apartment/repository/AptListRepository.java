package capstoneDesign.carbonTracker.apartment.repository;

import capstoneDesign.carbonTracker.apartment.dto.AptListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AptListRepository extends ElasticsearchRepository<AptListResponse, String> {
    Page<AptListResponse> findAll();
    Optional<AptListResponse> findByKaptCode(String kaptCode);
}
