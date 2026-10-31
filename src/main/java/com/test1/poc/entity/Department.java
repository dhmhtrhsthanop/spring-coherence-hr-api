package com.test1.poc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "DEPARTMENTS")
public class Department implements Serializable {

    @Id
    @Column(name = "DEPARTMENT_ID")
    private Long departmentId;

    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    @ManyToOne
    @JoinColumn(name = "MANAGER_ID")
    private Employee manager;

    @OneToMany(
            mappedBy = "department",
            cascade = CascadeType.ALL, //aytomath apothikeush-diagrafh twn empl toy depart
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JsonManagedReference //apofugh infinite recursion
    private List<Employee> employees;

}
