package fr.eliess.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Relation 1 prof -> plusieurs cours
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();

    // Constructeur vide obligatoire (JPA)
    public Teacher() {
    }

    public Teacher(String name) {
        this.name = name;
    }

    // Méthode pour lier prof <-> cours
    public void addCourse(Course course) {

        courses.add(course);
        course.setTeacher(this);

    }

    public void removeCourse(Course course) {

        courses.remove(course);
        course.setTeacher(null);

    }
}
