# BigComp
```text
access-control-system/
├── README.md
├── pom.xml
├── .gitignore
└── src/
    ├── main/
    │   ├── java/
    │   │   └── acs/
    │   │       ├── domain/          # 领域模型（纯数据类）
    │   │       │   ├── Employee.java
    │   │       │   ├── Badge.java
    │   │       │   ├── Group.java
    │   │       │   ├── Resource.java
    │   │       │   └── LogEntry.java
    │   │       ├── service/         # 业务逻辑接口 & 实现
    │   │       │   ├── AccessControlService
    │   │       │   ├── AccessControlManage
    │   │       │   └── ResourceService
    │   │       ├── repository/      # 数据访问层（DAO / Repo）所有权限判断都在这里
    │   │       │   ├── EmployeeRepository
    │   │       │   ├── ResourceRepository
    │   │       │   └── LogRepository
    │   │       │   
    │   │       ├── log/             # 日志相关（LogEntry, LogService）不关心 UI / 业务，只负责存取数据
    │   │       │   ├── LogService
    │   │       │   └── LogFormatter（可选）
    │   │       ├── ui/              # UI / 控制层（Swing / JavaFX / Console）UI 调Service
    │   │       │   ├── MainApp
    │   │       │   ├── AdminPanel
    │   │       │   └── ScanPanel 
    │   │       ├── exception/       # 自定义异常
    │   │       │   ├── InvalidBadgeException
    │   │       │   └── AccessDeniedException
    │   │       └── util/            # 工具类
    │   │           ├── TimeUtil
    │   │           └── ConfigLoader
    │   └── resources/
    │       ├── application.properties
    │       └── schema.sql
    └── test/
        └── java/
            └── acs/
                └── service/
```