package capstoneDesign.carbonTracker.apartment.controller;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import capstoneDesign.carbonTracker.apartment.service.AptEnergyService;
import capstoneDesign.carbonTracker.apartment.service.AptListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value= "/api")
@RestController
public class ApartmentController {

    private final AptListService apartmentService;

    private final AptEnergyService aptEnergyService;

    @PostMapping("/aptList")
    public String aptListApi(@RequestBody AptListRequest aptListRequest) throws Exception {
        log.info("공동주택 단지 목록제공 서비스 요청");
        return apartmentService.aptLists(aptListRequest);
    }

    @GetMapping("/aptListAll")
    public String aptListApi() throws Exception {
        log.info("대구시 전체 공동주택 단지 목록 조회 요청");
        return apartmentService.aptListAll();
    }

    @PostMapping("/aptEnergy")
    public String aptEnergyApi(@RequestBody AptEnergyRequest aptEnergyRequest) throws Exception {
        log.info("공동주택 에너지 사용 정보 요청");
        return aptEnergyService.aptEnergy(aptEnergyRequest);
    }

    @PostMapping("/aptEnergyAll")
    public String aptEnergyAllApi(@RequestBody AptEnergyRequest aptEnergyRequest) throws Exception {
        log.info("특정 단지의 기간별 공동주택 에너지 사용 정보 요청");
        return aptEnergyService.aptEnergyAll(aptEnergyRequest);
    }
}
