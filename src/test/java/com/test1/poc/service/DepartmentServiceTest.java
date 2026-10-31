package com.test1.poc.service;

import com.test1.poc.entity.Department;
import com.test1.poc.entity.Employee;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void findAll_shouldReturnAllDepartments() {

        // Given
        List<Department> departments = List.of(
                new Department(),
                new Department()
        );
                                //return value for a mocked method
        when(repository.findAll()).thenReturn(departments);

        // When
        List<Department> result = departmentService.findAll();

        // Then     //given | realservice -> mockedrepo -> given
        assertEquals(departments, result);
        verify(repository).findAll();
    }

    @Test
    void findById_shouldReturnDepartment_whenDepartmentExists() {

        // Given
        Long id = 1L;
        Department department = new Department();
                                                //to create a non-empty depart
        when(repository.findById(id)).thenReturn(Optional.of(department));

        // When
        Department result = departmentService.findById(id);

        // Then
        assertEquals(department, result);
        verify(repository).findById(id);
    }

    @Test
    void findById_shouldThrowException_whenDepartmentDoesNotExist() {

        // Given
        Long id = 999L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows( EntityNotFoundException.class, () -> departmentService.findById(id) );

        // Then
        verify(repository).findById(id);
    }

    @Test
    void createDepartment_shouldSaveDepartment_whenManagerIsNull() {

        // Given
        Department department = new Department();

        when(repository.save(department)).thenReturn(department);

        // When
        Department result = departmentService.createDepartment(department);

        // Then
        assertEquals(department, result);
        verify(repository).save(department);
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void createDepartment_shouldThrowException_whenManagerDoesNotExist() {

        // Given
        Department department = new Department();

        Long id = 999L;
        Employee manager = new Employee();
        manager.setEmployeeId(id);

        department.setManager(manager);

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows( EntityNotFoundException.class, () -> departmentService.createDepartment(department) );

        // Then
        verify(employeeRepository).findById(id);
        verify(repository, never()).save(department);
    }

    @Test
    void createDepartment_shouldSaveDepartment_whenManagerHasNoDepartment() {

        // Given
        Department department = new Department();

        Long id = 1L;
        Employee manager = new Employee();
        manager.setEmployeeId(id);

        department.setManager(manager);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(manager));

        when(repository.save(department)).thenReturn(department);

        // When
        Department result = departmentService.createDepartment(department);

        // Then
        assertEquals(department, result);

        assertEquals(manager, department.getManager());
        assertEquals(department, manager.getDepartment());

        verify(employeeRepository).findById(id);
        verify(repository).save(department);
    }

    @Test
    void createDepartment_shouldSaveDepartment_whenManagerHasDepartment() {

        // Given
        Department oldDepartment = new Department();

        Long id = 1L;

        Employee manager = new Employee();
        manager.setEmployeeId(id);

        oldDepartment.setManager(manager);
        manager.setDepartment(oldDepartment);

        Department newDepartment = new Department();
        newDepartment.setManager(manager);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(manager));

        when(repository.save(newDepartment)).thenReturn(newDepartment);

        // When
        Department result = departmentService.createDepartment(newDepartment);

        // Then
        assertEquals(newDepartment, result);

        assertNull(oldDepartment.getManager());

        assertEquals(manager, newDepartment.getManager());
        assertEquals(newDepartment, manager.getDepartment());

        verify(employeeRepository).findById(id);
        verify(repository).save(newDepartment);
    }

    @Test
    void updateDepartment_shouldThrowException_whenDepartmentDoesNotExists() {

        // Given
        Long id = 999L;
        Department updatedDepartment = new Department();

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows( EntityNotFoundException.class, () -> departmentService.updateDepartment(id, updatedDepartment));

        // Then
        verify(repository).findById(id);
        verify(repository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_shouldUpdateDepartmentAndSave() {

        // Given
        Long id = 1L;
        Department existingDepartment = new Department();
        existingDepartment.setDepartmentId(id);
        existingDepartment.setDepartmentName("Old Name");

        Department updatedDepartment = new Department();
        updatedDepartment.setDepartmentName("New Name");

        when(repository.findById(id)).thenReturn(Optional.of(existingDepartment));

        when(repository.save(existingDepartment)).thenReturn(existingDepartment);

        // When
        Department result = departmentService.updateDepartment(id, updatedDepartment);

        // Then
        assertEquals("New Name", result.getDepartmentName());

        verify(repository).findById(id);
        verify(repository).save(existingDepartment);
    }

    @Test
    void updateDepartment_shouldUpdateManager() {

        // Given
        Long id = 1L;
        Long managerId = 10L;

        Department existingDepartment = new Department();

        Department updatedDepartment = new Department();

        Employee newManager = new Employee();
        newManager.setEmployeeId(managerId);

        updatedDepartment.setManager(newManager);

        when(repository.findById(id)).thenReturn(Optional.of(existingDepartment));

        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(newManager));

        when(repository.save(existingDepartment)).thenReturn(existingDepartment);

        // When
        Department result = departmentService.updateDepartment(id, updatedDepartment);

        // Then
        assertEquals(newManager, result.getManager());
        assertEquals(existingDepartment, newManager.getDepartment());

        verify(repository).findById(id);
        verify(employeeRepository).findById(managerId);
        verify(repository).save(existingDepartment);
    }

    @Test
    void updateDepartment_shouldUpdateManager_whenManagerAlreadyHasDepartment() {

        // Given
        Long id = 1L;

        Department existingDepartment = new Department();

        Department oldDepartment = new Department();

        Employee newManager = new Employee();
        newManager.setEmployeeId(100L);

        oldDepartment.setManager(newManager);
        newManager.setDepartment(oldDepartment);

        Department updatedDepartment = new Department();
        updatedDepartment.setManager(newManager);

        when(repository.findById(id)).thenReturn(Optional.of(existingDepartment));

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(newManager));

        when(repository.save(existingDepartment)).thenReturn(existingDepartment);

        // When
        Department result = departmentService.updateDepartment(id, updatedDepartment);

        // Then
        assertNull(oldDepartment.getManager());

        assertEquals(newManager, result.getManager());

        assertEquals(existingDepartment, newManager.getDepartment());

        verify(repository).findById(id);
        verify(employeeRepository).findById(100L);
        verify(repository).save(existingDepartment);
    }

    @Test
    void updateDepartment_shouldThrowException_whenManagerDoesNotExists() {

        // Given
        Long id = 1L;
        Department existingDepartment = new Department();
        Department updatedDepartment = new Department();

        Long managerId = 999L;
        Employee manager = new Employee();
        manager.setEmployeeId(managerId);

        updatedDepartment.setManager(manager);

        when(employeeRepository.findById(managerId)).thenReturn(Optional.empty());

        when(repository.findById(id)).thenReturn(Optional.of(existingDepartment));

        // When
        assertThrows( EntityNotFoundException.class, () -> departmentService.updateDepartment(id, updatedDepartment) );

        // Then
        verify(employeeRepository).findById(managerId);
        verify(repository).findById(id);
        verify(repository, never()).save(existingDepartment);
    }

    @Test
    void updateDepartment_whenDepartmentAlreadyHasAManager() {

        // Given
        Long id = 1L;
        Department existingDepartment = new Department();
        Department updatedDepartment = new Department();

        Long managerId = 10L;
        Employee oldManager = new Employee();
        oldManager.setEmployeeId(managerId);
        existingDepartment.setManager(oldManager);

        Long newManagerId = 100L;
        Employee newManager = new Employee();
        newManager.setEmployeeId(newManagerId);
        updatedDepartment.setManager(newManager);

        when(employeeRepository.findById(newManagerId)).thenReturn(Optional.of(newManager));

        when(repository.findById(id)).thenReturn(Optional.of(existingDepartment));

        when(repository.save(existingDepartment)).thenReturn(existingDepartment);

        // When
        Department result = departmentService.updateDepartment(id, updatedDepartment);

        // Then
        assertNull(oldManager.getDepartment());

        assertEquals(newManager, result.getManager());

        assertEquals(existingDepartment, newManager.getDepartment());

        verify(repository).findById(id);
        verify(employeeRepository).findById(100L);
        verify(repository).save(existingDepartment);
    }

    @Test
    void deleteDepartment_shouldDeleteDepartment_whenDepartmentExists() {

        // Given
        Long id = 1L;

        Department department = new Department();
        department.setDepartmentId(id);

        when(repository.findById(id)).thenReturn(Optional.of(department));

        // When
        departmentService.deleteDepartment(id);

        // Then
        verify(repository).findById(id);
        verify(repository).delete(department);
    }

    @Test
    void deleteDepartment_shouldThrowException_whenDepartmentDoesNotExist() {

        // Given
        Long id = 999L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows( EntityNotFoundException.class, () -> departmentService.deleteDepartment(id));

        // Then
        verify(repository).findById(id);
        verify(repository, never()).delete(any(Department.class));
    }
}
