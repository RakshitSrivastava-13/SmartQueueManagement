package com.hospital.queue.config;

import com.hospital.queue.entity.*;
import com.hospital.queue.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner initData() {
        return args -> {
            // Only initialize if no data exists
            if (departmentRepository.count() > 0) {
                log.info("Data already exists, skipping initialization");
                return;
            }

            log.info("Initializing sample data...");

            // Create Departments
            Department opd = createDepartment("General Medicine", "OPD", "General OPD and Primary Care", 1, "101-110");
            Department cardiology = createDepartment("Cardiology", "CARD", "Heart and Cardiovascular Care", 2, "201-205");
            Department orthopedics = createDepartment("Orthopedics", "ORTH", "Bone and Joint Care", 2, "206-210");
            Department pediatrics = createDepartment("Pediatrics", "PED", "Child Healthcare", 1, "111-115");
            Department gynecology = createDepartment("Gynecology", "GYN", "Women Health Care", 3, "301-305");
            Department dermatology = createDepartment("Dermatology", "DERM", "Skin Care", 1, "116-118");
            Department ent = createDepartment("ENT", "ENT", "Ear Nose Throat", 2, "211-213");
            Department emergency = createDepartment("Emergency", "EMER", "Emergency Services", 0, "E01-E05");

            // Create Doctors
            createDoctor("DOC001", "Rajesh", "Sharma", "General Physician", "MBBS, MD", opd, "101", 10, 60);
            createDoctor("DOC002", "Priya", "Patel", "Cardiologist", "MBBS, DM Cardiology", cardiology, "201", 15, 40);
            createDoctor("DOC003", "Amit", "Kumar", "Orthopedic Surgeon", "MBBS, MS Ortho", orthopedics, "206", 15, 35);
            createDoctor("DOC004", "Sneha", "Reddy", "Pediatrician", "MBBS, MD Pediatrics", pediatrics, "111", 12, 50);
            createDoctor("DOC005", "Kavita", "Singh", "Gynecologist", "MBBS, MS OBG", gynecology, "301", 15, 40);
            createDoctor("DOC006", "Suresh", "Nair", "General Physician", "MBBS, MD", opd, "102", 10, 60);
            createDoctor("DOC007", "Meera", "Iyer", "Dermatologist", "MBBS, MD Dermatology", dermatology, "116", 12, 45);
            createDoctor("DOC008", "Vikram", "Joshi", "ENT Specialist", "MBBS, MS ENT", ent, "211", 15, 40);
            createDoctor("DOC009", "Arun", "Menon", "Emergency Physician", "MBBS, MD Emergency", emergency, "E01", 10, 100);

            log.info("Sample data initialization completed!");
        };
    }

    private Department createDepartment(String name, String code, String description, int floor, String rooms) {
        Department dept = Department.builder()
                .name(name)
                .code(code)
                .description(description)
                .floorNumber(floor)
                .roomNumbers(rooms)
                .isActive(true)
                .build();
        return departmentRepository.save(dept);
    }

    private Doctor createDoctor(String empId, String firstName, String lastName, String specialization,
                                String qualification, Department department, String room,
                                int consultTime, int maxPatients) {
        Doctor doctor = Doctor.builder()
                .employeeId(empId)
                .firstName(firstName)
                .lastName(lastName)
                .specialization(specialization)
                .qualification(qualification)
                .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@hospital.com")
                .phone("987654" + empId.substring(3))
                .department(department)
                .roomNumber(room)
                .consultationDurationMinutes(consultTime)
                .maxPatientsPerDay(maxPatients)
                .isAvailable(true)
                .isActive(true)
                .build();
        return doctorRepository.save(doctor);
    }
}
