package kz.iitu.zhumakan.studentapi.config;

import kz.iitu.zhumakan.studentapi.dto.StudentRequest;
import kz.iitu.zhumakan.studentapi.student.StudentRepository;
import kz.iitu.zhumakan.studentapi.student.StudentService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoStudentConfiguration {
    @Bean
    @ConditionalOnProperty(name = "app.seed-demo-student", havingValue = "true")
    ApplicationRunner demoStudent(StudentRepository repository, StudentService service) {
        return args -> {
            if (!repository.existsByStudentId("37380")) {
                service.create(new StudentRequest("Zhumakan Erasyl", "IT2-2302", "37380"));
            }
        };
    }
}
