package fr.eliess.dto;

public class CourseWithCountDTO {

    private Long id;
    private String title;
    private Long studentCount;

    public CourseWithCountDTO(Long id, String title, Long studentCount) {
        this.id = id;
        this.title = title;
        this.studentCount = studentCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Long studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return "CourseWithCountDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", studentCount=" + studentCount +
                '}';
    }
}
