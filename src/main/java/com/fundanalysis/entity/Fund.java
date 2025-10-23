package com.fundanalysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "funds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FundType type;

    @Column(nullable = false)
    private String manager;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "fund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FundNetValue> netValues = new ArrayList<>();

    public Fund(String code, String name, FundType type, String manager, String description) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.manager = manager;
        this.description = description;
    }
}
