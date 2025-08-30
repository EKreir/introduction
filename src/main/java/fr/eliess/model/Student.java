package fr.eliess.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "courses")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // <- cascade activé
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    /*
    Pour les relations ManyToMany.
    Crée une table de jointure entre Student et Course.
    name = "student_course" → nom de la table de jointure.
    joinColumns = @JoinColumn(name = "student_id") → colonne qui pointe vers Student.
    inverseJoinColumns = @JoinColumn(name = "course_id") → colonne qui pointe vers Course.
    */
    private Set<Course> courses = new HashSet<>();
    // initialise la collection pour éviter les NullPointerException

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", unique = true)
    private StudentProfile profile;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
/*
    public void addCourse(Course course) {
        if (courses.add(course)) {
            course.getStudents().add(this);
        }
    }
*/
}
