package kz.iitu.zhumakan.studentapi.student;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kz.iitu.zhumakan.studentapi.dto.StudentRequest;
import kz.iitu.zhumakan.studentapi.dto.StudentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/students/" + response.id())).body(response);
    }

    @GetMapping
    public List<StudentResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable @Positive long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable @Positive long id,
                                  @Valid @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
