package capstoneDesign.carbonTracker.apartment.controller;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListResponse;
import capstoneDesign.carbonTracker.apartment.service.AptEnergyService;
import capstoneDesign.carbonTracker.apartment.service.AptListService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value= "/api")
@RestController
public class ApartmentController {

    private final AptListService apartmentService;

    private final AptEnergyService aptEnergyService;

    @ApiOperation(value="대구시 공동주택 단지 목록제공 서비스 요청", notes="대구 시도 코드에 대한 아파트 단지 정보 반환 - 아마 없앨 예정")
    @PostMapping("/aptList")
    public String aptListApi(@RequestBody AptListRequest aptListRequest) throws Exception {
        return apartmentService.aptLists(aptListRequest);
    }

    @ApiOperation(value="대구시 전체 공동주택 단지 목록 조회 요청", notes="대구 모든 공동주택 단지 정보 반환")
    @GetMapping("/aptListAll")
    public List<AptListResponse> aptListApi() throws Exception {
        return apartmentService.aptListAll();
    }

    @ApiOperation(value="대구시 전체 공동주택 단지 목록 업데이트", notes="대구 모든 공동주택 단지 정보 업데이트")
    @GetMapping("/aptListUpdate")
    public String aptListUpdateApi() throws Exception {
        return apartmentService.aptListUpdate();
    }

    @ApiOperation(value="대구시 공동주택 에너지 사용 정보 요청", notes="특정 단지 에너지 사용 정보 반환")
    @PostMapping("/aptEnergy")
    public String aptEnergyApi(@RequestBody AptEnergyRequest aptEnergyRequest) throws Exception {
        return aptEnergyService.aptEnergy(aptEnergyRequest);
    }

    @ApiOperation(value="대구시 특정 단지 기간별 공동주택 에너지 사용 정보 요청", notes="특정 단지 기간별 에너지 사용 정보 반환 (기간은 아직 입력받지 않음, 현재 202001 ~ 202112까지 반환)")
    @PostMapping("/aptEnergyAll")
    public String aptEnergyAllApi(@RequestBody AptEnergyRequest aptEnergyRequest) throws Exception {
        return aptEnergyService.aptEnergyAll(aptEnergyRequest);
    }
}
