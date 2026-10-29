package kz.iitu.zhumakan.studentapi.dto;

import kz.iitu.zhumakan.studentapi.student.Student;

public record StudentResponse(Long id, String name, String groupName, String studentId) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(student.getId(), student.getName(),
                student.getGroupName(), student.getStudentId());
    }
}
