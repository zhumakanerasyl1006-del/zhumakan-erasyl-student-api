package kz.iitu.zhumakan.studentapi;

import kz.iitu.zhumakan.studentapi.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.seed-demo-student=true",
        "spring.datasource.url=jdbc:h2:mem:demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("test")
class DemoStudentTest {
    @Autowired StudentRepository repository;

    @Test
    void seedsPersonalizedStudent() {
        assertThat(repository.findAll()).singleElement().satisfies(student -> {
            assertThat(student.getName()).isEqualTo("Zhumakan Erasyl");
            assertThat(student.getGroupName()).isEqualTo("IT2-2302");
            assertThat(student.getStudentId()).isEqualTo("37380");
        });
    }
}
