package capstoneDesign.carbonTracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value= "/api")
@RestController
public class Controller {

    @GetMapping("/aptList")
    public String aptListApi() {
        return "";
    }

    @GetMapping("/aptEnergy")
    public String aptEnergyApi() {
        return "";
    }
}
