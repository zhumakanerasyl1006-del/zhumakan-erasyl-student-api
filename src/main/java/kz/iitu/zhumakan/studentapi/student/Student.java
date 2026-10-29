package kz.iitu.zhumakan.studentapi.student;

import jakarta.persistence.*;

@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(
        name = "uk_students_student_id", columnNames = "student_id"))
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "group_name", nullable = false, length = 40)
    private String groupName;

    @Column(name = "student_id", nullable = false, length = 30)
    private String studentId;

    protected Student() { }

    public Student(String name, String groupName, String studentId) {
        update(name, groupName, studentId);
    }

    public void update(String name, String groupName, String studentId) {
        this.name = name;
        this.groupName = groupName;
        this.studentId = studentId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getGroupName() { return groupName; }
    public String getStudentId() { return studentId; }
}
