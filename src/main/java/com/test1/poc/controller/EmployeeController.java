package com.test1.poc.controller;

import com.test1.poc.entity.Employee;
import com.test1.poc.service.EmployeeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService service;


    public EmployeeController(EmployeeService service) {
        this.service = service;
    }


    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Employee> getAllEmployees() {

        return service.findAll();
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {

        return service.findById(id);
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {

        return service.createEmployee(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {

        return service.updateEmployee(id, employee);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {

        service.deleteEmployee(id);
    }

    @GetMapping("/by-date")
    public Collection<Employee> findByHireDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return service.findByHireDate(date);
    }

    @GetMapping("/by-email-and-name")
    public Collection<Employee> findByEmailAndLastName(@RequestParam String email, @RequestParam String lastName) {

        return service.findByEmailAndLastName(email, lastName);
    }

    @GetMapping("/by-department")
    public Collection<Employee> findByDepartment_DepartmentName(@RequestParam String departmentName) {

        return service.findByDepartment_DepartmentName(departmentName);
    }
}