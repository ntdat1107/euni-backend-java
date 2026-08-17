package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "majors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Major extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "majors_id_seq")
    @SequenceGenerator(name = "majors_id_seq", sequenceName = "majors_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Program> programs;
}
