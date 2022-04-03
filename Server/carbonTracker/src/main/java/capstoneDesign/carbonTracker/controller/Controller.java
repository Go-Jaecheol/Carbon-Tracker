package capstoneDesign.carbonTracker.controller;

import capstoneDesign.carbonTracker.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.dto.AptListRequest;
import capstoneDesign.carbonTracker.service.AptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value= "/api")
@RestController
public class Controller {

    private final AptService aptService;

    @PostMapping("/aptList")
    public String aptListApi(@RequestBody AptListRequest aptListRequest) throws IOException {
        log.info("공동주택 단지 목록제공 서비스 요청");
        return aptService.aptLists(aptListRequest);
    }

    @PostMapping("/aptEnergy")
    public String aptEnergyApi(@RequestBody AptEnergyRequest aptEnergyRequest) throws IOException {
        log.info("공동주택 에너지 사용 정보");
        return aptService.aptEnergy(aptEnergyRequest);
    }
}
