# BigComp
```text
access-control-system/
├── README.md
├── pom.xml
├── .gitignore
├── database
│   ├── application.properties
│   └── bigcomp_db.sql
└── src/
    ├── main/
    │   └── java/
    │       └── acs/
    │           ├── domain/          # 领域模型（纯数据类）
    │           │   ├── AccessDecision.java
    │           │   ├── AccessRequest.java
    │           │   ├── AccessResult.java
    │           │   ├── Badge.java
    │           │   ├── BadgeStatus.java
    │           │   ├── Employee.java
    │           │   ├── Group.java
    │           │   ├── LogEntry.java
    │           │   ├── ReasonCode.java
    │           │   ├── Resource.java
    │           │   ├── ResourceState.java
    │           │   └── ResourceType.java
    │           ├── service/         # 业务逻辑接口 & 实现
    │           │   ├── AccessControlServce.java
    │           │   ├── AdminService.java
    │           │   └── LogQueryService.java
    │           ├── repository/      # 数据访问层（DAO / Repo）所有权限判断都在这里
    │           │   ├── EmployeeRepository
    │           │   ├── ResourceRepository
    │           │   └── LogRepository
    │           │   
    │           ├── log/             # 日志相关（LogEntry, LogService）不关心 UI / 业务，只负责存取数据
    │           │   ├── LogService
    │           │   └── LogFormatter（可选）
    │           ├── ui/              # UI / 控制层（Swing / JavaFX / Console）UI 调Service
    │           │   ├── MainApp
    │           │   ├── AdminPanel
    │           │   └── ScanPanel 
    │           ├── exception/       # 自定义异常
    │           │   ├── InvalidBadgeException
    │           │   └── AccessDeniedException
    │           └── util/            # 工具类
    │               ├── TimeUtil
    │               └── ConfigLoader
    └── test/
        └── java/
            └── acs/
                └── service/

```