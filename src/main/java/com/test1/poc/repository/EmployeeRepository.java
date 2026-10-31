package com.test1.poc.repository;

import com.test1.poc.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.*;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Collection<Employee> findByEmailAndLastName(String email, String lastName);

    Collection<Employee> findByDateGreaterThanEqual(LocalDate date);

    Collection<Employee> findByDepartment_DepartmentName(String departmentName);

}
