package kz.iitu.zhumakan.studentapi;

import java.util.List;
import java.util.Optional;
import kz.iitu.zhumakan.studentapi.dto.StudentRequest;
import kz.iitu.zhumakan.studentapi.exception.StudentNotFoundException;
import kz.iitu.zhumakan.studentapi.student.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock StudentRepository repository;
    @InjectMocks StudentService service;
    Student student;
    final StudentRequest request = new StudentRequest("Zhumakan Erasyl", "IT2-2302", "37380");

    @BeforeEach
    void setUp() {
        student = new Student(request.name(), request.groupName(), request.studentId());
        ReflectionTestUtils.setField(student, "id", 1L);
    }

    @Test
    void createsStudent() {
        when(repository.saveAndFlush(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        var result = service.create(request);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Zhumakan Erasyl");
        assertThat(result.groupName()).isEqualTo("IT2-2302");
        assertThat(result.studentId()).isEqualTo("37380");
        var captor = ArgumentCaptor.forClass(Student.class);
        verify(repository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getStudentId()).isEqualTo("37380");
    }

    @Test
    void listsStudents() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(student));
        assertThat(service.findAll()).singleElement().satisfies(result ->
                assertThat(result.studentId()).isEqualTo("37380"));
    }

    @Test
    void listsEmptyRepository() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());
        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void getsStudent() {
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        assertThat(service.findById(1L).name()).isEqualTo("Zhumakan Erasyl");
    }

    @Test
    void updatesAllFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        when(repository.saveAndFlush(student)).thenReturn(student);
        var result = service.update(1L, new StudentRequest("Updated Name", "IT2-2303", "37381"));
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.groupName()).isEqualTo("IT2-2303");
        assertThat(result.studentId()).isEqualTo("37381");
        verify(repository).saveAndFlush(student);
    }

    @Test
    void deletesStudent() {
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        service.delete(1L);
        verify(repository).delete(student);
        verify(repository).flush();
    }

    @Test
    void missingGetThrowsNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(999L)).isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void missingUpdateDoesNotSave() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(999L, request)).isInstanceOf(StudentNotFoundException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void missingDeleteDoesNotDelete() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(999L)).isInstanceOf(StudentNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}
