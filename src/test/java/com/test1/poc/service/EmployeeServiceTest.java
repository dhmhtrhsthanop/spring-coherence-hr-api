package com.test1.poc.service;

import com.test1.poc.entity.Department;
import com.test1.poc.entity.Employee;
import com.test1.poc.entity.Job;
import com.test1.poc.repository.DepartmentRepository;
import com.test1.poc.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void findAll_shouldReturnAllEmployees() {

        // Given
        List<Employee> employees = List.of(
                new Employee(),
                new Employee()
        );
                                //return value for a mocked method
        when(repository.findAll()).thenReturn(employees);

        // When
        List<Employee> result = employeeService.findAll();

        // Then     //given | realservice -> mockedrepo -> given
        assertEquals(employees, result);
        verify(repository).findAll();
    }

    @Test
    void findById_shouldReturnEmployee_whenEmployeeExists() {

        // Given
        Long id = 1L;
        Employee employee = new Employee();
                                                //to create a non-empty employee
        when(repository.findById(id)).thenReturn(Optional.of(employee));

        // When
        Employee result = employeeService.findById(id);

        // Then
        assertEquals(employee, result);
        verify(repository).findById(id);
    }

    @Test
    void findById_shouldThrowException_whenEmployeeDoesNotExist() {

        // Given
        Long id = 999L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows( EntityNotFoundException.class, () -> employeeService.findById(id) );

        // Then
        verify(repository).findById(id);
    }

    @Test
    void createEmployee_shouldSaveEmployee() {

        // Given
        Employee employee = new Employee();

        when(repository.save(employee)).thenReturn(employee);

        // When
        Employee result = employeeService.createEmployee(employee);

        // Then
        assertEquals(employee, result);
        verify(repository).save(employee);
    }

    @Test
    void updateEmployee_shouldUpdateEmployeeAndSave() {

        // Given
        Long id = 1L;

        Employee existingEmployee = new Employee();
        existingEmployee.setEmployeeId(id);
        existingEmployee.setFirstName("Steven");
        existingEmployee.setLastName("King");
        existingEmployee.setEmail("SKING");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setFirstName("William");
        updatedEmployee.setLastName("Gietz");
        updatedEmployee.setEmail("WGEITZ");

        when(repository.findById(id)).thenReturn(Optional.of(existingEmployee));

        when(repository.save(existingEmployee)).thenReturn(existingEmployee);

        // When
        Employee result = employeeService.updateEmployee(id, updatedEmployee);

        // Then
        assertEquals("William", result.getFirstName());
        assertEquals("Gietz", result.getLastName());
        assertEquals("WGEITZ", result.getEmail());

        verify(repository).findById(id);
        verify(repository).save(existingEmployee);
    }

    @Test
    void updateEmployee_shouldUpdateRelations_whenRelationsAreProvided() {

        // Given
        Long id = 1L;

        Employee existingEmployee = new Employee();

        Employee updatedEmployee = new Employee();

        Department newDepartment = new Department();
        Job newJob = new Job();
        Employee newManager = new Employee();
            //updatedemp with new relations
        updatedEmployee.setDepartment(newDepartment);
        updatedEmployee.setJob(newJob);
        updatedEmployee.setManager(newManager);

        when(repository.findById(id)).thenReturn(Optional.of(existingEmployee));

        when(repository.save(existingEmployee)).thenReturn(existingEmployee);

        // When
        Employee result = employeeService.updateEmployee(id, updatedEmployee);

        // Then
        assertEquals(newDepartment, result.getDepartment());
        assertEquals(newJob, result.getJob());
        assertEquals(newManager, result.getManager());

        verify(repository).save(existingEmployee);
    }

    @Test
    void updateEmployee_shouldKeepExistingRelations_whenUpdatedRelationsAreNull() {

        // Given
        Long id = 1L;

        Employee existingEmployee = new Employee();

        Department oldDepartment = new Department();
        Job oldJob = new Job();
        Employee oldManager = new Employee();
        //existingemp with relations
        existingEmployee.setDepartment(oldDepartment);
        existingEmployee.setJob(oldJob);
        existingEmployee.setManager(oldManager);

        Employee updatedEmployee = new Employee();

        when(repository.findById(id)).thenReturn(Optional.of(existingEmployee));

        when(repository.save(existingEmployee)).thenReturn(existingEmployee);

        // When
        Employee result = employeeService.updateEmployee(id, updatedEmployee);

        // Then
        assertEquals(oldDepartment, result.getDepartment());
        assertEquals(oldJob, result.getJob());
        assertEquals(oldManager, result.getManager());

        verify(repository).save(existingEmployee);
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmployeeDoesNotExist() {

        // Given
        Long id = 999L;
        Employee updatedEmployee = new Employee();

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows( EntityNotFoundException.class, () -> employeeService.updateEmployee(id, updatedEmployee));

        // Then
        verify(repository).findById(id);
        verify(repository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenEmployeeHasNoDepartment() {

        // Given
        Long id = 1L;

        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setDepartment(null);

        when(repository.findById(id)).thenReturn(Optional.of(employee));

        // When
        employeeService.deleteEmployee(id);

        // Then
        verify(repository).findById(id);
        verify(repository).delete(employee);

        verifyNoInteractions(departmentRepository);
    }

    @Test
    void deleteEmployee_shouldRemoveEmployeeAsDepartmentManager_beforeDeleting() {

        // Given
        Long employeeId = 1L;
        Long departmentId = 10L;

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);

        Department department = new Department();
        department.setDepartmentId(departmentId);
        department.setManager(employee);

        employee.setDepartment(department);

        when(repository.findById(employeeId)).thenReturn(Optional.of(employee));

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        // When
        employeeService.deleteEmployee(employeeId);

        // Then
        assertNull(department.getManager());

        verify(repository).findById(employeeId);
        verify(departmentRepository).findById(departmentId);
        verify(repository).delete(employee);
    }

    @Test
    void deleteEmployee_shouldKeepDepartmentManager_whenEmployeeIsNotManager() {

        // Given
        Long employeeId = 1L;
        Long departmentId = 10L;
        Long managerId = 2L;

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);

        Employee manager = new Employee();
        manager.setEmployeeId(managerId);

        Department department = new Department();
        department.setDepartmentId(departmentId);
        department.setManager(manager);

        employee.setDepartment(department);

        when(repository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(department));

        // When
        employeeService.deleteEmployee(employeeId);

        // Then
        assertEquals(manager, department.getManager());

        verify(departmentRepository).findById(departmentId);
        verify(repository).delete(employee);
    }

    @Test
    void deleteEmployee_shouldThrowException_whenDepartmentDoesNotExist() {

        // Given
        Long employeeId = 1L;
        Long departmentId = 10L;

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);

        Department departmentReference = new Department();
        departmentReference.setDepartmentId(departmentId);

        employee.setDepartment(departmentReference);

        when(repository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.empty());

        // When
        assertThrows(
                EntityNotFoundException.class,
                () -> employeeService.deleteEmployee(employeeId)
        );

        // Then
        verify(departmentRepository).findById(departmentId);
        verify(repository, never()).delete(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldThrowException_whenEmployeeDoesNotExist() {

        // Given
        Long id = 999L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> employeeService.deleteEmployee(id)
        );

        verify(repository).findById(id);
        verify(repository, never()).delete(any(Employee.class));
    }
}
