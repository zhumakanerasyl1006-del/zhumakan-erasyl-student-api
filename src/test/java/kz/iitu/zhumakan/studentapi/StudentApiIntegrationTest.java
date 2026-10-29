package kz.iitu.zhumakan.studentapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.iitu.zhumakan.studentapi.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired StudentRepository repository;
    final String valid = """
            {"name":"Zhumakan Erasyl","groupName":"IT2-2302","studentId":"37380"}
            """;

    @BeforeEach
    void cleanDatabase() { repository.deleteAllInBatch(); }

    @Test
    void fullCrudPersistsAndDeletes() throws Exception {
        long id = create();
        mvc.perform(get("/api/students/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zhumakan Erasyl"));
        mvc.perform(get("/api/students")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        String update = """
                {"name":"Updated Student","groupName":"IT2-2303","studentId":"37381"}
                """;
        mvc.perform(put("/api/students/" + id).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Student"));
        assertThat(repository.findById(id).orElseThrow().getStudentId()).isEqualTo("37381");
        mvc.perform(delete("/api/students/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/students/" + id)).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void duplicateStudentIdReturns409AndKeepsOriginal() throws Exception {
        create();
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isConflict());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void duplicateUpdateRollsBack() throws Exception {
        long first = create();
        String secondJson = valid.replace("37380", "37381");
        String body = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long second = mapper.readTree(body).get("id").asLong();
        mvc.perform(put("/api/students/" + second).contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isConflict());
        assertThat(repository.findById(first).orElseThrow().getStudentId()).isEqualTo("37380");
        assertThat(repository.findById(second).orElseThrow().getStudentId()).isEqualTo("37381");
    }

    @Test
    void invalidStudentIsNotPersisted() throws Exception {
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void absentStudentReturns404ForAllIdOperations() throws Exception {
        mvc.perform(get("/api/students/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/api/students/999999").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/students/999999")).andExpect(status().isNotFound());
    }

    private long create() throws Exception {
        String body = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }
}
