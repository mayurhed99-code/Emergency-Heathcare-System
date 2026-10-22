package com.emergency.platform.controller.hospital;

import com.emergency.platform.dto.ResourceDto.*;
import com.emergency.platform.service.hospital.HospitalResourceService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/hospital/resources")
public class HospitalResourceController {

    private final HospitalResourceService resourceService;

    public HospitalResourceController(HospitalResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/doctors")
    public List<DoctorDto> getDoctors(Principal principal) {
        return resourceService.getDoctors(principal.getName());
    }

    @PostMapping("/doctors")
    public DoctorDto addDoctor(@RequestBody CreateDoctorRequest request, Principal principal) {
        return resourceService.addDoctor(principal.getName(), request);
    }

    @GetMapping("/drivers")
    public List<DriverDto> getDrivers(Principal principal) {
        return resourceService.getDrivers(principal.getName());
    }

    @PostMapping("/drivers")
    public DriverDto addDriver(@RequestBody CreateDriverRequest request, Principal principal) {
        return resourceService.addDriver(principal.getName(), request);
    }

    @GetMapping("/ambulances")
    public List<AmbulanceDto> getAmbulances(Principal principal) {
        return resourceService.getAmbulances(principal.getName());
    }

    @PostMapping("/ambulances")
    public AmbulanceDto addAmbulance(@RequestBody CreateAmbulanceRequest request, Principal principal) {
        return resourceService.addAmbulance(principal.getName(), request);
    }
}
