package com.test1.poc.service;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.test1.poc.entity.Department;
import com.test1.poc.entity.Employee;
import com.test1.poc.repository.DepartmentRepository;
import com.test1.poc.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository repository;
    private final DepartmentRepository departmentRepository;

    private final NamedCache<Long, Employee> employeeCache;

    private final SwitchingService coherenceSwitch;

    public EmployeeService(EmployeeRepository repository, DepartmentRepository departmentRepository, SwitchingService coherenceSwitch) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;

        this.employeeCache = CacheFactory.getCache("Employees");
        this.coherenceSwitch = coherenceSwitch;
    }

    public List<Employee> findAll() {

        List<Employee> employees = repository.findAll();

        if (!employees.isEmpty() & coherenceSwitch.isEnabled()) {
            Map<Long, Employee> batchToCache = employees.stream()
                    .collect(Collectors.toMap(Employee::getEmployeeId, emp -> emp));
            employeeCache.putAll(batchToCache);
        }

        return employees;
    }

    public Employee findById(Long id) {

        if (coherenceSwitch.isEnabled()) {
            Employee cachedEmployee = employeeCache.get(id);
            if (cachedEmployee != null) {
                System.out.println("=== cache ===");
                return cachedEmployee;
            }
        }

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (coherenceSwitch.isEnabled()) {
            employeeCache.put(id, employee);
        }

        return employee;
    }

    public Employee createEmployee(Employee employee) {

        Employee savedEmployee = repository.save(employee);

        if (coherenceSwitch.isEnabled()) {
            employeeCache.put(savedEmployee.getEmployeeId(), savedEmployee);
        }

        return savedEmployee;
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {

        Employee employee = findById(id);

        // Update simple fields
        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setEmail(updatedEmployee.getEmail());
        employee.setDate(updatedEmployee.getDate());

        // Update Department relation
        if (updatedEmployee.getDepartment() != null) {
            employee.setDepartment(updatedEmployee.getDepartment());
        }

        // Update Job relation
        if (updatedEmployee.getJob() != null) {
            employee.setJob(updatedEmployee.getJob());
        }

        // Update Manager relation
        if (updatedEmployee.getManager() != null) {
            employee.setManager(updatedEmployee.getManager());
        }

        Employee savedEmployee = repository.save(employee);

        if (coherenceSwitch.isEnabled()) {
            employeeCache.put(savedEmployee.getEmployeeId(), savedEmployee);
        }

        return savedEmployee;
    }

    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = findById(id);

        // If employee is managing a department
        if (employee.getDepartment() != null) {
            Department department = departmentRepository.findById(employee.getDepartment().getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

            if (department.getManager() != null && department.getManager().getEmployeeId().equals(employee.getEmployeeId())) {
                department.setManager(null);
            }
        }

        repository.deleteById(id);

        if (coherenceSwitch.isEnabled()) {
            employeeCache.remove(id);
        }
    }

    public Collection<Employee> findByHireDate(LocalDate date) {

        return repository.findByDateGreaterThanEqual(date);

    }

    public Collection<Employee> findByEmailAndLastName(String email, String lastName) {

        return repository.findByEmailAndLastName(email, lastName);

    }

    public Collection<Employee> findByDepartment_DepartmentName(String departmentName) {

        return repository.findByDepartment_DepartmentName(departmentName);

    }

}