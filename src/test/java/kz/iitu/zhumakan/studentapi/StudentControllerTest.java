package kz.iitu.zhumakan.studentapi;

import java.util.List;
import kz.iitu.zhumakan.studentapi.dto.*;
import kz.iitu.zhumakan.studentapi.exception.*;
import kz.iitu.zhumakan.studentapi.student.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(GlobalExceptionHandler.class)
class StudentControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean StudentService service;
    final StudentResponse response = new StudentResponse(1L, "Zhumakan Erasyl", "IT2-2302", "37380");
    final String valid = """
            {"name":"Zhumakan Erasyl","groupName":"IT2-2302","studentId":"37380"}
            """;

    @Test
    void createsWithLocation() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/students/1"))
                .andExpect(jsonPath("$.name").value("Zhumakan Erasyl"))
                .andExpect(jsonPath("$.studentId").value("37380"));
    }

    @Test
    void listsStudents() throws Exception {
        when(service.findAll()).thenReturn(List.of(response));
        mvc.perform(get("/api/students")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getsStudent() throws Exception {
        when(service.findById(1)).thenReturn(response);
        mvc.perform(get("/api/students/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("IT2-2302"));
    }

    @Test
    void updatesStudent() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(response);
        mvc.perform(put("/api/students/1").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isOk()).andExpect(jsonPath("$.studentId").value("37380"));
        verify(service).update(eq(1L), any(StudentRequest.class));
    }

    @Test
    void deletesWithNoContent() throws Exception {
        mvc.perform(delete("/api/students/1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).delete(1L);
    }

    @Test
    void missingGetReturns404() throws Exception {
        when(service.findById(999)).thenThrow(new StudentNotFoundException(999));
        mvc.perform(get("/api/students/999")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void missingUpdateReturns404() throws Exception {
        when(service.update(eq(999L), any())).thenThrow(new StudentNotFoundException(999));
        mvc.perform(put("/api/students/999").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingDeleteReturns404() throws Exception {
        doThrow(new StudentNotFoundException(999)).when(service).delete(999L);
        mvc.perform(delete("/api/students/999")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\" \",\"groupName\":\"IT2-2302\",\"studentId\":\"37380\"}",
            "{\"name\":\"Zhumakan Erasyl\",\"groupName\":\"\",\"studentId\":\"37380\"}",
            "{\"name\":\"Zhumakan Erasyl\",\"groupName\":\"IT2-2302\",\"studentId\":\"abc\"}",
            "{}"
    })
    void rejectsInvalidBodies(String body) throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isMap());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsInvalidUpdate() throws Exception {
        mvc.perform(put("/api/students/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void rejectsInvalidId(String id) throws Exception {
        mvc.perform(get("/api/students/" + id)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void duplicateReturns409WithoutDatabaseDetails() throws Exception {
        when(service.create(any())).thenThrow(new DataIntegrityViolationException("secret SQL"));
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "Student data conflicts with a database constraint; studentId must be unique"));
    }
}
