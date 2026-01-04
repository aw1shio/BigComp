#BigComp
```
access-control-system
├─ pom.xml
├─ README.md
└─ src
   ├─ main
   │  ├─ java
   │  │  └─ acs
   │  │     ├─ AccessControlApplication.java
   │  │     ├─ cache
   │  │     │  └─ LocalCacheManager.java
   │  │     ├─ domain
   │  │     │  ├─ AccessDecision.java
   │  │     │  ├─ AccessRequest.java
   │  │     │  ├─ AccessResult.java
   │  │     │  ├─ Badge.java
   │  │     │  ├─ BadgeStatus.java
   │  │     │  ├─ Employee.java
   │  │     │  ├─ Group.java
   │  │     │  ├─ LogEntry.java
   │  │     │  ├─ ReasonCode.java
   │  │     │  ├─ Resource.java
   │  │     │  ├─ ResourceState.java
   │  │     │  └─ ResourceType.java
   │  │     ├─ log
   │  │     │  ├─ impl
   │  │     │  │  └─ LogServiceImpl.java
   │  │     │  └─ LogService.java
   │  │     ├─ repository
   │  │     │  ├─ AccessLogRepository.java
   │  │     │  ├─ BadgeRepository.java
   │  │     │  ├─ EmployeeRepository.java
   │  │     │  ├─ GroupRepository.java
   │  │     │  └─ ResourceRepository.java
   │  │     ├─ service
   │  │     │  ├─ AccessControlService.java
   │  │     │  ├─ AdminService.java
   │  │     │  ├─ impl
   │  │     │  │  ├─ AccessControlServiceImpl.java
   │  │     │  │  ├─ AdminServiceImpl.java
   │  │     │  │  └─ LogQueryServiceImpl.java
   │  │     │  ├─ LogCleanupService.java
   │  │     │  └─ LogQueryService.java
   │  │     └─ ui
   │  │        ├─ AdminPanel.java
   │  │        ├─ hello.java
   │  │        ├─ MainApp.java
   │  │        └─ ScanPanel.java
   │  └─ resources
   │     ├─ application.properties
   │     └─ db
   │        └─ access_control_db.sql
   └─ test
      ├─ java
      │  └─ acs
      │     ├─ cache
      │     │  └─ LocalCacheManagerIntegrationTest.java
      │     └─ service
      │        └─ impl
      │           ├─ AccessControlServiceImplTest.java
      │           ├─ AdminServiceImplTest.java
      │           └─ LogQueryServiceImplTest.java
      └─ resources
         ├─ application-test.properties
         └─ db
            └─ init-test-data.sql

```