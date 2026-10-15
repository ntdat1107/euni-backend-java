package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "program_structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramStructure extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_structures_id_seq")
    @SequenceGenerator(name = "program_structures_id_seq", sequenceName = "program_structures_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProgramStructure parent;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "credit_required")
    @Builder.Default
    private Integer creditRequired = 0;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramStructure> children = new ArrayList<>();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramElectiveGroup> electiveGroups = new ArrayList<>();
}
