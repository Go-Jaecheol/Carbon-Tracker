package capstoneDesign.carbonTracker.apartment.controller;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import capstoneDesign.carbonTracker.apartment.service.ApartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value= "/api")
@RestController
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping("/aptList")
    public String aptListApi(@RequestBody AptListRequest aptListRequest) throws IOException {
        log.info("공동주택 단지 목록제공 서비스 요청");
        return apartmentService.aptLists(aptListRequest);
    }

    @PostMapping("/aptEnergy")
    public String aptEnergyApi(@RequestBody AptEnergyRequest aptEnergyRequest) throws IOException {
        log.info("공동주택 에너지 사용 정보 요청");
        return apartmentService.aptEnergy(aptEnergyRequest);
    }
}
