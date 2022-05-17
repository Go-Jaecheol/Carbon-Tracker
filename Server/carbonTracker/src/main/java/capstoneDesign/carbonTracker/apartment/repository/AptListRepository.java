package capstoneDesign.carbonTracker.apartment.repository;

import capstoneDesign.carbonTracker.apartment.dto.AptListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AptListRepository extends ElasticsearchRepository<AptListResponse, String> {
    Page<AptListResponse> findAll();
}
