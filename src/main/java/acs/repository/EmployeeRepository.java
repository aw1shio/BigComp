package acs.repository;

import acs.domain.Employee;

import java.util.Optional;

public interface EmployeeRepository {

    Optional<Employee> findById(String employeeId);

    Optional<Employee> findByBadgeId(String badgeId);

    void save(Employee employee);
}
