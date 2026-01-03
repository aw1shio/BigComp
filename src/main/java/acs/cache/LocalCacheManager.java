package acs.cache;

import acs.domain.Badge;
import acs.domain.Employee;
import acs.domain.Group;
import acs.domain.Resource;
import acs.domain.LogEntry;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import acs.repository.AccessLogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class LocalCacheManager {

    // 缓存存储结构
    private final Map<String, Badge> badgeCache = new HashMap<>();
    private final Map<String, Employee> employeeCache = new HashMap<>();
    private final Map<String, Group> groupCache = new HashMap<>();
    private final Map<String, Resource> resourceCache = new HashMap<>();
    private final List<LogEntry> logCache = new ArrayList<>();  // 日志缓存（有序列表）


    // 依赖的Repository
    private final BadgeRepository badgeRepository;
    private final EmployeeRepository employeeRepository;
    private final GroupRepository groupRepository;
    private final ResourceRepository resourceRepository;
    // 注入日志Repository
    private final AccessLogRepository accessLogRepository;

    public LocalCacheManager(BadgeRepository badgeRepository,
                            EmployeeRepository employeeRepository,
                            GroupRepository groupRepository,
                            ResourceRepository resourceRepository,
                            AccessLogRepository accessLogRepository) {
        this.badgeRepository = badgeRepository;
        this.employeeRepository = employeeRepository;
        this.groupRepository = groupRepository;
        this.resourceRepository = resourceRepository;
        this.accessLogRepository = accessLogRepository; // 初始化日志Repository
    }

    // 初始化缓存，应用启动时执行
    @PostConstruct
    public void initCache() {
        loadBadges();
        loadEmployees();
        loadGroups();
        loadResources();
        loadLogs(); 
        // 日志输出
        System.out.println("缓存初始化完成 - 徽章数: " + badgeCache.size() 
            + ", 员工数: " + employeeCache.size()
            + ", 组数: " + groupCache.size()
            + ", 资源数: " + resourceCache.size()
            + ", 日志数: " + logCache.size());
    }

    // 从数据库加载所有徽章到缓存
    private void loadBadges() {
        badgeCache.clear();
        badgeRepository.findAll().forEach(badge -> badgeCache.put(badge.getBadgeId(), badge));
    }

    // 从数据库加载所有员工到缓存
    private void loadEmployees() {
        employeeCache.clear();
        employeeRepository.findAll().forEach(employee -> employeeCache.put(employee.getEmployeeId(), employee));
    }

    // 从数据库加载所有组到缓存
    private void loadGroups() {
        groupCache.clear();
        groupRepository.findAll().forEach(group -> groupCache.put(group.getGroupId(), group));
    }

    // 从数据库加载所有资源到缓存
    private void loadResources() {
        resourceCache.clear();
        resourceRepository.findAll().forEach(resource -> resourceCache.put(resource.getResourceId(), resource));
    }

    // 从数据库加载所有日志到本地缓存
    private void loadLogs() {
        logCache.clear();
        // 从数据库查询所有日志，按创建时间排序后存入缓存
        List<LogEntry> allLogs = accessLogRepository.findAll();
        List<LogEntry> sortedLogs = allLogs.stream()
                .sorted(Comparator.comparing(LogEntry::getTimestamp))  // 按时间升序（从早到晚）
                .collect(Collectors.toList());
        logCache.addAll(sortedLogs);
    }

    // 缓存操作方法
    public Badge getBadge(String badgeId) {
        return badgeCache.get(badgeId);
    }

    public Employee getEmployee(String employeeId) {
        return employeeCache.get(employeeId);
    }

    public Group getGroup(String groupId) {
        return groupCache.get(groupId);
    }

    public Resource getResource(String resourceId) {
        return resourceCache.get(resourceId);
    }

    // 获取有序日志列表（返回不可修改集合，防止外部篡改顺序）
    public List<LogEntry> getLogs() {
        return Collections.unmodifiableList(logCache);
    }

    // 更新缓存中的徽章
    public void updateBadge(Badge badge) {
        badgeCache.put(badge.getBadgeId(), badge);
    }

    // 更新缓存中的员工
    public void updateEmployee(Employee employee) {
        employeeCache.put(employee.getEmployeeId(), employee);
    }

    // 更新缓存中的组
    public void updateGroup(Group group) {
        groupCache.put(group.getGroupId(), group);
    }

    // 更新缓存中的资源
    public void updateResource(Resource resource) {
        resourceCache.put(resource.getResourceId(), resource);
    }

    // 更新日志缓存（新增或修改日志后重新排序）
    public void updateLog(LogEntry log) {
        // 先移除旧记录（若存在）
        logCache.removeIf(existingLog -> existingLog.getId().equals(log.getId()));
        // 添加新记录并重新排序
        logCache.add(log);
        logCache.sort(Comparator.comparing(LogEntry::getTimestamp));  // 保持有序
    }

    // 从缓存中删除徽章
    public void removeBadge(String badgeId) {
        badgeCache.remove(badgeId);
    }

    // 从缓存中删除员工
    public void removeEmployee(String employeeId) {
        employeeCache.remove(employeeId);
    }

    // 从缓存中删除组
    public void removeGroup(String groupId) {
        groupCache.remove(groupId);
    }

    // 从缓存中删除资源
    public void removeResource(String resourceId) {
        resourceCache.remove(resourceId);
    }

    // 从缓存中删除日志
    public void removeLog(Long logId) {
        logCache.removeIf(log -> log.getId().equals(logId));
    }

    // 清理缓存中过期的日志（7天前）
    public int clearExpiredLogs(LocalDateTime sevenDaysAgo) {
        // 记录清理前的数量
        int initialSize = logCache.size();
        // 删除所有时间在7天前的日志
        logCache.removeIf(log -> log.getTimestamp().isBefore(sevenDaysAgo));
        // 返回删除的数量
        return initialSize - logCache.size();
    }

    // 强制刷新所有缓存（从数据库重新加载）
    public void refreshAllCache() {
        loadBadges();
        loadEmployees();
        loadGroups();
        loadResources();
        loadLogs();
    }
}