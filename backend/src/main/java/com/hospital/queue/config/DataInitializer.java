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
    private final DomainRepository domainRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner initData() {
        return args -> {
            // Only initialize if no data exists
            if (domainRepository.count() > 0 || departmentRepository.count() > 0) {
                log.info("Data already exists, skipping initialization");
                return;
            }

            log.info("Initializing sample data...");

            // Create Domains
            Domain hospitalDomain = createDomain("Hospital", "Healthcare services and medical consultations", "🏥");
            Domain bankDomain = createDomain("Bank", "Banking and financial services", "🏦");
            Domain medicalStoreDomain = createDomain("Medical Store", "Pharmacy and medicine dispensing", "💊");

            // Create Hospital Departments
            Department opd = createDepartment("General Medicine", "OPD", "General OPD and Primary Care", 1, "101-110", hospitalDomain);
            Department cardiology = createDepartment("Cardiology", "CARD", "Heart and Cardiovascular Care", 2, "201-205", hospitalDomain);
            Department orthopedics = createDepartment("Orthopedics", "ORTH", "Bone and Joint Care", 2, "206-210", hospitalDomain);
            Department pediatrics = createDepartment("Pediatrics", "PED", "Child Healthcare", 1, "111-115", hospitalDomain);
            Department gynecology = createDepartment("Gynecology", "GYN", "Women Health Care", 3, "301-305", hospitalDomain);
            Department dermatology = createDepartment("Dermatology", "DERM", "Skin Care", 1, "116-118", hospitalDomain);
            Department ent = createDepartment("ENT", "ENT", "Ear Nose Throat", 2, "211-213", hospitalDomain);
            Department emergency = createDepartment("Emergency", "EMER", "Emergency Services", 0, "E01-E05", hospitalDomain);

            // Create Bank Departments
            Department accountOpening = createDepartment("Account Opening", "ACC", "New account services", 1, "Counter 1-5", bankDomain);
            Department loanServices = createDepartment("Loan Services", "LOAN", "Personal and home loans", 1, "Counter 6-10", bankDomain);
            Department cashServices = createDepartment("Cash Deposits/Withdrawals", "CASH", "Cash transactions", 1, "Counter 11-15", bankDomain);
            Department premiumBanking = createDepartment("Premium Banking", "PREM", "Premium customer services", 2, "Cabin 1-3", bankDomain);

            // Create Medical Store Departments
            Department prescriptionCounter = createDepartment("Prescription Medicines", "PRESC", "Prescription-based medicines", 1, "Counter A", medicalStoreDomain);
            Department otcCounter = createDepartment("Over-the-Counter", "OTC", "OTC medicines and supplements", 1, "Counter B", medicalStoreDomain);
            Department surgicalItems = createDepartment("Surgical Items", "SURG", "Medical equipment and surgical items", 1, "Counter C", medicalStoreDomain);

            // Create Hospital Doctors
            createDoctor("DOC001", "Rajesh", "Sharma", "General Physician", "MBBS, MD", opd, "101", 10, 60);
            createDoctor("DOC002", "Priya", "Patel", "Cardiologist", "MBBS, DM Cardiology", cardiology, "201", 15, 40);
            createDoctor("DOC003", "Amit", "Kumar", "Orthopedic Surgeon", "MBBS, MS Ortho", orthopedics, "206", 15, 35);
            createDoctor("DOC004", "Sneha", "Reddy", "Pediatrician", "MBBS, MD Pediatrics", pediatrics, "111", 12, 50);
            createDoctor("DOC005", "Kavita", "Singh", "Gynecologist", "MBBS, MS OBG", gynecology, "301", 15, 40);
            createDoctor("DOC006", "Suresh", "Nair", "General Physician", "MBBS, MD", opd, "102", 10, 60);
            createDoctor("DOC007", "Meera", "Iyer", "Dermatologist", "MBBS, MD Dermatology", dermatology, "116", 12, 45);
            createDoctor("DOC008", "Vikram", "Joshi", "ENT Specialist", "MBBS, MS ENT", ent, "211", 15, 40);
            createDoctor("DOC009", "Arun", "Menon", "Emergency Physician", "MBBS, MD Emergency", emergency, "E01", 10, 100);

            // Create Bank Staff (using Doctor entity as service provider)
            createDoctor("BNK001", "Ramesh", "Gupta", "Account Manager", "MBA Finance", accountOpening, "Counter 1", 15, 40);
            createDoctor("BNK002", "Anita", "Desai", "Loan Officer", "MBA Finance", loanServices, "Counter 7", 20, 30);
            createDoctor("BNK003", "Sunil", "Mehta", "Cashier", "B.Com", cashServices, "Counter 11", 5, 80);
            createDoctor("BNK004", "Pooja", "Rao", "Relationship Manager", "MBA", premiumBanking, "Cabin 1", 25, 20);

            // Create Medical Store Staff
            createDoctor("PHR001", "Deepak", "Shah", "Pharmacist", "B.Pharm", prescriptionCounter, "Counter A", 5, 100);
            createDoctor("PHR002", "Nisha", "Verma", "Pharmacist", "B.Pharm", otcCounter, "Counter B", 5, 100);
            createDoctor("PHR003", "Karan", "Chopra", "Medical Equipment Specialist", "B.Pharm", surgicalItems, "Counter C", 10, 50);

            log.info("Sample data initialization completed!");
        };
    }

    private Domain createDomain(String name, String description, String icon) {
        Domain domain = new Domain();
        domain.setName(name);
        domain.setDescription(description);
        domain.setIcon(icon);
        domain.setActive(true);
        return domainRepository.save(domain);
    }

    private Department createDepartment(String name, String code, String description, int floor, String rooms, Domain domain) {
        Department dept = Department.builder()
                .name(name)
                .code(code)
                .description(description)
                .floorNumber(floor)
                .roomNumbers(rooms)
                .domain(domain)
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
