package com.test1.poc.controller;

import com.test1.poc.entity.Department;
import com.test1.poc.service.DepartmentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService service;


    public DepartmentController(DepartmentService service) {
        this.service = service;
    }


    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Department> getAllDepartments() {

        return service.findAll();
    }

    @GetMapping("/{id}")
    public Department getDepartment(@PathVariable Long id) {

        return service.findById(id);
    }

    @PostMapping
    public Department createDepartment(@RequestBody Department department) {

        return service.createDepartment(department);
    }

    @PutMapping("/{id}")
    public Department updateDepartment(@PathVariable Long id, @RequestBody Department department) {

        return service.updateDepartment(id, department);
    }

    @DeleteMapping("/{id}")
    public void deleteDepartment(@PathVariable Long id) {

        service.deleteDepartment(id);
    }

}
