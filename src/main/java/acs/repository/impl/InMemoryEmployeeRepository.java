package acs.repository.impl;

import acs.domain.Employee;
import acs.repository.EmployeeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final ConcurrentHashMap<String, Employee> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Employee> findById(String employeeId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(employeeId));
    }

    @Override
    public Optional<Employee> findByBadgeId(String badgeId) {
        if (badgeId == null) {
            return Optional.empty();
        }
        // 千量级数据，线性扫描在内存仓库中是可接受的；若未来规模更大，可加二级索引映射。
        return store.values().stream()
                .filter(e -> badgeId.equals(e.getBadgeId()))
                .findFirst();
    }

    @Override
    public void save(Employee employee) {
        if (employee == null || employee.getEmployeeId() == null) {
            return;
        }
        store.put(employee.getEmployeeId(), employee);
    }
}
