package com.emergency.platform.config;

import com.emergency.platform.entity.Ambulance;
import com.emergency.platform.entity.Bed;
import com.emergency.platform.entity.BedType;
import com.emergency.platform.entity.Doctor;
import com.emergency.platform.entity.Driver;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.Role;
import com.emergency.platform.entity.Specialization;
import com.emergency.platform.entity.AppUser;
import com.emergency.platform.repository.AmbulanceRepository;
import com.emergency.platform.repository.AppUserRepository;
import com.emergency.platform.repository.BedRepository;
import com.emergency.platform.repository.DoctorRepository;
import com.emergency.platform.repository.DriverRepository;
import com.emergency.platform.repository.HospitalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedPlatformData(HospitalRepository hospitalRepository,
                                       DoctorRepository doctorRepository,
                                       DriverRepository driverRepository,
                                       AmbulanceRepository ambulanceRepository,
                                       BedRepository bedRepository,
                                       AppUserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            if (hospitalRepository.count() > 0) {
                return;
            }

            Hospital cityHeart = hospital("City Heart Emergency Institute", "Bengaluru", "MG Road Command Zone", "heart.ops@example.com", "+919900000101", 160, 47, 8, Set.of(Specialization.CARDIOLOGY, Specialization.ICU, Specialization.EMERGENCY_MEDICINE));
            Hospital neuroCare = hospital("NeuroCare Trauma Center", "Bengaluru", "Indiranagar Rapid Response Block", "neuro.ops@example.com", "+919900000102", 120, 31, 6, Set.of(Specialization.NEUROLOGY, Specialization.TRAUMA_CENTER, Specialization.ICU));
            Hospital metroBurn = hospital("Metro Burn and Critical Care", "Bengaluru", "Whitefield Emergency Corridor", "burn.ops@example.com", "+919900000103", 90, 22, 5, Set.of(Specialization.BURN_UNIT, Specialization.EMERGENCY_MEDICINE, Specialization.ICU));
            Hospital highway = hospital("Highway Trauma Response Hospital", "Bengaluru", "Outer Ring Road Dispatch Sector", "trauma.ops@example.com", "+919900000104", 140, 39, 9, Set.of(Specialization.TRAUMA_CENTER, Specialization.EMERGENCY_MEDICINE, Specialization.GENERAL_MEDICINE));
            Hospital apex = hospital("Apex Multi-Specialty Emergency Center", "Bengaluru", "Electronic City Medical Grid", "apex.ops@example.com", "+919900000105", 180, 52, 10, Set.of(Specialization.CARDIOLOGY, Specialization.NEUROLOGY, Specialization.PEDIATRICS, Specialization.EMERGENCY_MEDICINE));
            Hospital north = hospital("Northside ICU Network Hospital", "Bengaluru", "Hebbal Critical Care Hub", "north.ops@example.com", "+919900000106", 110, 28, 5, Set.of(Specialization.ICU, Specialization.GENERAL_MEDICINE, Specialization.EMERGENCY_MEDICINE));
            hospitalRepository.saveAll(Set.of(cityHeart, neuroCare, metroBurn, highway, apex, north));

            seedHospitalTeam(cityHeart, "cardio", Specialization.CARDIOLOGY, doctorRepository, driverRepository, ambulanceRepository, bedRepository);
            seedHospitalTeam(neuroCare, "neuro", Specialization.NEUROLOGY, doctorRepository, driverRepository, ambulanceRepository, bedRepository);
            seedHospitalTeam(metroBurn, "burn", Specialization.BURN_UNIT, doctorRepository, driverRepository, ambulanceRepository, bedRepository);
            seedHospitalTeam(highway, "trauma", Specialization.TRAUMA_CENTER, doctorRepository, driverRepository, ambulanceRepository, bedRepository);
            seedHospitalTeam(apex, "apex", Specialization.EMERGENCY_MEDICINE, doctorRepository, driverRepository, ambulanceRepository, bedRepository);
            seedHospitalTeam(north, "icu", Specialization.ICU, doctorRepository, driverRepository, ambulanceRepository, bedRepository);

            userRepository.save(user("Admin Operator", "admin@ops.local", "password", Role.ADMIN, null, passwordEncoder));
            userRepository.save(user("Doctor Console", "doctor@ops.local", "password", Role.DOCTOR, cityHeart, passwordEncoder));
            userRepository.save(user("Driver Console", "driver@ops.local", "password", Role.DRIVER, cityHeart, passwordEncoder));

            java.util.List.of(cityHeart, neuroCare, metroBurn, highway, apex, north).forEach(h -> {
                userRepository.save(user(h.getName() + " Admin", h.getContactEmail(), "password", Role.HOSPITAL, h, passwordEncoder));
            });
        };
    }

    private Hospital hospital(String name, String city, String address, String email, String phone, int totalBeds, int availableBeds, int ambulances, Set<Specialization> specializations) {
        Hospital hospital = new Hospital();
        hospital.setName(name);
        hospital.setCity(city);
        hospital.setAddress(address);
        hospital.setContactEmail(email);
        hospital.setContactPhone(phone);
        hospital.setTotalBeds(totalBeds);
        hospital.setAvailableBeds(availableBeds);
        hospital.setAvailableAmbulances(ambulances);
        hospital.setSpecializations(specializations);
        return hospital;
    }

    private void seedHospitalTeam(Hospital hospital,
                                  String key,
                                  Specialization primarySpecialization,
                                  DoctorRepository doctorRepository,
                                  DriverRepository driverRepository,
                                  AmbulanceRepository ambulanceRepository,
                                  BedRepository bedRepository) {
        Doctor primary = doctor("Dr. " + title(key) + " Lead", key + ".doctor@example.com", "+91980000" + suffix(key, "11"), primarySpecialization, hospital);
        Doctor emergency = doctor("Dr. " + title(key) + " Response", key + ".response@example.com", "+91980000" + suffix(key, "12"), Specialization.EMERGENCY_MEDICINE, hospital);
        doctorRepository.save(primary);
        doctorRepository.save(emergency);

        Driver driver = new Driver();
        driver.setName(title(key) + " Ambulance Driver");
        driver.setEmail(key + ".driver@example.com");
        driver.setPhone("+91970000" + suffix(key, "21"));
        driverRepository.save(driver);

        Ambulance ambulance = new Ambulance();
        ambulance.setCallSign("AMB-" + key.toUpperCase());
        ambulance.setDriver(driver);
        ambulance.setHospital(hospital);
        ambulanceRepository.save(ambulance);

        for (int i = 1; i <= 4; i++) {
            Bed bed = new Bed();
            bed.setHospital(hospital);
            bed.setBedCode(key.toUpperCase() + "-ER-" + i);
            bed.setBedType(i == 1 ? BedType.ICU : BedType.EMERGENCY);
            bedRepository.save(bed);
        }
    }

    private Doctor doctor(String name, String email, String phone, Specialization specialization, Hospital hospital) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setSpecialization(specialization);
        doctor.setHospital(hospital);
        return doctor;
    }

    private AppUser user(String name, String email, String password, Role role, Hospital hospital, PasswordEncoder passwordEncoder) {
        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setHospital(hospital);
        return user;
    }

    private String title(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private String suffix(String key, String fallback) {
        return String.valueOf(Math.abs(key.hashCode())).substring(0, 2) + fallback;
    }
}
