# 🧰 EquipmentApplication(update file in future)

### 🧾 Project Description
**EquipmentApplication** is a desktop JavaFX application for managing equipment within an organization.
The application allows users to track equipment locations, responsible persons, current status, repair history, and generate QR codes for quick identification. It also integrates with a Telegram bot, allowing equipment information to be accessed directly from a mobile device.


---

### 🎯 Project Goal
TThe main goal is to simplify equipment registration, inventory management, and maintenance tracking.

You can:

- 📍 View all equipment and its current location
- 👤 Assign responsible persons
- 🔄 Change equipment status (installed, stored, written off)
- 🧾 Add, edit, and delete equipment
- 🔍 Search equipment by different parameters
- 🏷️ Generate unique QR codes for every equipment item
- 🖨️ Print QR labels for equipment
- 👁️ View QR codes directly inside the application
- 🛠️ Maintain complete repair history
- 📅 Record repair dates, malfunctions, performed work, and repair costs
- 📱 Scan QR codes using a Telegram bot
- 🚚 Move equipment between offices directly from Telegram
- 📖 View repair history through Telegram
- 📊 Generate Excel reports

---

### ⚙️ Technologies Used

- ☕ **Java 17**
- 🎨 **JavaFX**
- 🗄️ **MySQL**
- 📦 **Maven**
- 🧩 **DAO / DTO architecture**
- 📄 **Apache POI** — Excel report generation
- 🏷️ **ZXing** — QR code generation
- 🤖 **Telegram Bots API** — mobile interaction with the system
- 📡 **Jackson Databind** — JSON processing
- 📑 **JUnit 5** — testing
- 📝 **Log4j 2** — logging

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
│ ├── EquipmentDictionaryDAO.java
│ ├── EquipmentHistoryDAO.java
│ ├── EquipmentRepairDAO.java
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
│ ├── EquipmentRepair.java
│ ├── EquipmentType.java
│ ├── MainRecord.java
│ ├── Office.java
│ ├── SeniorDepartment.java
│ ├── UltrasoundSensor.java
│ └── UltrasoundSensorDictionary.java
│
├── telegramBot/
│ ├── BotStarter.java
│ └── EquipmentBot.java
│
├── util/
│ ├── AlertUtils.java
│ ├── QRCodeGenerator.java
│ ├── QRCodeViewer.java
│ └── WindowUtils.java
│
├── window/
│ ├── DepartmentWindow.java
│ ├── EquipmentDictionaryWindow.java
│ ├── EquipmentHistoryWindow.java
│ ├── EquipmentRepairWindow.java
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
    EQUIPMENT_REPAIR {
        int id
        date repair_date
        string malfunction
        string work_done
        decimal cost
    }
    EQUIPMENT {
        int id
        string name
        string model
        string sn_number
        string note
        enum status
        string qr_code
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
    EQUIPMENT ||--o{ EQUIPMENT_HISTORY : ""
    EQUIPMENT ||--o{ EQUIPMENT_REPAIR : ""
    EQUIPMENT ||--o{ ULTRASOUNDSENSORS : ""
    ULTRASOUNDSENSOR_DICTIONARY ||--o{ ULTRASOUNDSENSORS : ""
```
---
🖼️ Interface Example
![screenshot.png](src/main/java/images/screenshot.png)
---
👨‍💻 Author
- Author: Evgeny Dantsov
- License: MIT
- Project type: Work-use application
