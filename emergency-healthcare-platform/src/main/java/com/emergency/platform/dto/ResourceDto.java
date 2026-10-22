package com.emergency.platform.dto;

import com.emergency.platform.entity.AmbulanceStatus;
import com.emergency.platform.entity.Specialization;

public class ResourceDto {
    public record DoctorDto(Long id, String name, String email, String phone, Specialization specialization, boolean available) {}
    
    public record DriverDto(Long id, String name, String email, String phone, String licenseNumber, boolean available, int completedTrips) {}
    
    public record AmbulanceDto(Long id, String callSign, AmbulanceStatus status, DriverDto driver) {}
    
    // Requests
    public record CreateDoctorRequest(String name, String email, String phone, Specialization specialization) {}
    public record CreateDriverRequest(String name, String email, String phone, String licenseNumber) {}
    public record CreateAmbulanceRequest(String callSign) {}
}
