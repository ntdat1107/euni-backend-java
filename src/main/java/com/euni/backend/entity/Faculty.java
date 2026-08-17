package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "faculties_id_seq")
    @SequenceGenerator(name = "faculties_id_seq", sequenceName = "faculties_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    private String description;
}
