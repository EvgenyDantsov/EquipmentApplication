# 🧰 EquipmentApplication(update file in future)

### 🧾 Project Description
**EquipmentApplication** is a desktop JavaFX program for managing equipment in an organization.  
It helps record where each item is installed, who is responsible for it, and its current status (installed, stored, or disposed).

---

### 🎯 Project Goal
The main goal is to make equipment registration and tracking easier.

You can:
- 📍 View all equipment and its location  
- 👤 Assign responsible persons  
- 🔄 Change statuses  
- 🧾 Add, edit, or delete records  
- 🔍 Search through the list

---

### ⚙️ Technologies Used
- ☕ **Java 17+**
- 🎨 **JavaFX** — user interface
- 🗄️ **MySQL** — database
- 🧩 **DAO / DTO pattern**
- ⚙️ **Maven / Build Artifacts**

---

### 📁 Project Structure
```text
src/java/com/example/equipmentapplication/
├── config/
│ └── Config.java
│
├── dao/
│ ├── DepartmentDAO.java
│ ├── EquipmentDAO.java
│ ├── EquipmentHistoryDAO.java
│ ├── EquipmentDictionaryDAO.java
│ ├── EquipmentTypeDAO.java
│ ├── OfficeDAO.java
│ ├── SeniorDepartmentDAO.java
│ ├── UltrasoundSensorDAO.java
│ └── UltrasoundSensorDictionaryDAO.java
│
├── dto/
│ ├── Department.java
│ ├── Equipment.java
│ ├── EquipmentDictionary.java
│ ├── EquipmentHistory.java
│ ├── EquipmentType.java
│ ├── MainRecord.java
│ ├── Office.java
│ ├── SeniorDepartment.java
│ ├── UltrasoundSensor.java
│ └── UltrasoundSensorDictionary.java
│
├── util/
│ ├── AlertUtils.java
│ └── WindowUtils.java
│
├── window/
│ ├── DepartmentWindow.java
│ ├── EquipmentDictionaryWindow.java
│ ├── EquipmentHistoryWindow.java
│ ├── EquipmentTypeWindow.java
│ ├── EquipmentWindow.java
│ ├── LoadingWindow.java
│ ├── MainWindow.java
│ ├── OfficeWindow.java
│ ├── SeniorDepartmentWindow.java
│ ├── UltrasoundSensorDictionaryWindow.java
│ └── UltrasoundSensorWindow.java
│
├── DatabaseHelper.java
├── FieldValidator.java
└── HelloApplication.java

src/java/resources/
```
---

### 🧠 Database Diagram (ER Model)
```mermaid
erDiagram
    DEPARTMENT {
        int id
        string department_name
    }
    SENIORDEPARTMENT {
        int id
        string fio
    }
    OFFICE {
        int id
        string number_office
        string name_office
    }
    EQUIPMENTTYPE {
        int id
        string name
    }
    EQUIPMENTDICTIONARY {
        int id
        string name
        string model
    }
    EQUIPMENT_HISTORY {
        int id
        string status
        string action
        datetime change_date
        text details
    }
    EQUIPMENT {
        int id
        string name
        string model
        string sn_number
        string note
        enum status
    }
    ULTRASOUNDSENSORS {
        int id
        string sensor_name
        string sensor_type
        string sn_number
        string note
        int equipment_id
    }
    ULTRASOUNDSENSOR_DICTIONARY {
        int id
        string name
        string type
    }

    DEPARTMENT ||--o{ SENIORDEPARTMENT : ""
    DEPARTMENT ||--o{ OFFICE : ""
    SENIORDEPARTMENT ||--o{ EQUIPMENT_HISTORY : ""
    OFFICE ||--o{ EQUIPMENT : ""
    EQUIPMENTTYPE ||--o{ EQUIPMENT : ""
    EQUIPMENTTYPE ||--o{ EQUIPMENTDICTIONARY : ""
    EQUIPMENT ||--o{ ULTRASOUNDSENSORS : ""
    ULTRASOUNDSENSOR_DICTIONARY ||--o{ ULTRASOUNDSENSORS : ""
    EQUIPMENT ||--o{ EQUIPMENT_HISTORY : ""
```
---
🖼️ Interface Example
![screenshot.png](src/main/java/images/screenshot.png)
---
👨‍💻 Author
- Author: Evgeny Dantsov
- License: MIT
- Project type: Work-use application
