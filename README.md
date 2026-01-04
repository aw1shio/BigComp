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
   │  │     │  └─ LogService.java
   │  │     ├─ repository
   │  │     │  ├─ AccessLogRepository.java
   │  │     │  ├─ BadgeRepository.java
   │  │     │  ├─ EmployeeRepository.java
   │  │     │  ├─ GroupRepository.java
   │  │     │  └─ ResourceRepository.java
   │  │     └─ service
   │  │        ├─ AccessControlService.java
   │  │        ├─ AdminService.java
   │  │        ├─ impl
   │  │        │  └─ AdminServiceImpl.java
   │  │        └─ LogQueryService.java
   │  └─ resources
   │     ├─ application.properties
   │     └─ db
   │        └─ bigcomp_db.sql
   └─ test
      ├─ java
      │  └─ acs
      │     └─ service
      │        ├─ AccessControlServiceTest.java
      │        └─ DatabaseIntegrationTest.java
      └─ resources
         ├─ application.properties
         └─ db
            └─ init-test-data.sql

```