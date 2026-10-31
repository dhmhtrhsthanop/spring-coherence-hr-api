package com.test1.poc.service;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.test1.poc.controller.CoherenceController;
import com.test1.poc.entity.Department;
import com.test1.poc.entity.Employee;
import com.test1.poc.repository.DepartmentRepository;
import com.test1.poc.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private final DepartmentRepository repository;
    private final EmployeeRepository employeeRepository;

    private final NamedCache<Long, Department> departmentCache;

    private final SwitchingService coherenceSwitch;

    public DepartmentService(DepartmentRepository repository, EmployeeRepository employeeRepository, SwitchingService coherenceSwitch) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;

        this.coherenceSwitch = coherenceSwitch;
        this.departmentCache = CacheFactory.getCache("Departments");
    }

    public List<Department> findAll() {

        List<Department> departments = repository.findAll();

        if (!departments.isEmpty() & coherenceSwitch.isEnabled()) {
            Map<Long, Department> batchToCache = departments.stream()
                    .collect(Collectors.toMap(Department::getDepartmentId, dep -> dep));
            departmentCache.putAll(batchToCache);
        }

        return departments;
    }

    public Department findById(Long id) {

        if (coherenceSwitch.isEnabled()) {
            Department cachedDepartment = departmentCache.get(id);
            if (cachedDepartment != null) {
                System.out.println("=== cache ===");
                return cachedDepartment;
            }
        }

        Department department = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        if (coherenceSwitch.isEnabled()) {
            departmentCache.put(id, department);
        }

        return department;
    }

    @Transactional
    public Department createDepartment(Department department) {

        Employee manager = null;

        if (department.getManager() != null) {

            manager = employeeRepository.findById(department.getManager().getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Manager not found"));

            // If given manager already manages another department
            if (manager.getDepartment() != null) {

                Department departmentOld = manager.getDepartment();

                // Updating old dep relation
                if (departmentOld.getManager() != null && departmentOld.getManager().getEmployeeId().equals(manager.getEmployeeId())){
                    departmentOld.setManager(null);
                    departmentOld = repository.save(departmentOld);
                }

            }

            department.setManager(manager);
        }

        Department savedDepartment = repository.save(department);

        if (manager != null) {
            manager.setDepartment(savedDepartment);
            manager = employeeRepository.save(manager);
        }

        if (coherenceSwitch.isEnabled()) {
            departmentCache.put(savedDepartment.getDepartmentId(), savedDepartment);
        }

        return savedDepartment;
    }

    @Transactional
    public Department updateDepartment(Long id, Department updatedDepartment) {

        Department department = findById(id);
        Employee manager = null;

        department.setDepartmentName(updatedDepartment.getDepartmentName());

        if (updatedDepartment.getManager() != null) {

            manager = employeeRepository.findById(updatedDepartment.getManager().getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Manager not found"));

            if (manager.getDepartment() != null) {

                Department departmentOld = manager.getDepartment();

                if (departmentOld.getManager() != null && departmentOld.getManager().getEmployeeId().equals(manager.getEmployeeId())){
                    departmentOld.setManager(null);
                    departmentOld = repository.save(departmentOld);
                }

            }

            // If given dep has an old manager - Updating old managers relation
            if (department.getManager() != null && !department.getManager().getEmployeeId().equals(manager.getEmployeeId())) {
                department.getManager().setDepartment(null);
            }

            department.setManager(manager);
        }

        Department savedDepartment = repository.save(department);

        if (manager != null) {
            manager.setDepartment(savedDepartment);
            manager = employeeRepository.save(manager);
        }

        if (coherenceSwitch.isEnabled()) {
            departmentCache.put(savedDepartment.getDepartmentId(), savedDepartment);
        }

        return savedDepartment;
    }

    public void deleteDepartment(Long id) {

        Department department = findById(id);

        repository.deleteById(id);

        if (coherenceSwitch.isEnabled()) {
            departmentCache.remove(id);
        }
    }

}