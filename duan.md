# 📊 PHÂN TÍCH CHI TIẾT HỆ THỐNG QUẢN LÝ TUYỂN SINH

## 1️⃣ TỔNG QUAN DỰ ÁN

### **Mục đích:**
Hệ thống **quản lý tuyển sinh** cho một trường đại học (Singapore Global University). Xử lý quy trình:
- Nhập/quản lý dữ liệu thí sinh
- Xử lý điểm thi từ nhiều phương thức (THPT, DGNL, VSAT)
- Áp dụng quy tắc tuyển sinh (bang quy đổi, điểm cộng)
- Chọn lọc và thông báo kết quả tuyển sinh

### **Loại hệ thống:**
📱 **Desktop Application** (Java Swing)  
🗄️ **Database-Driven** (MySQL)  
🏗️ **3-Layer Architecture** (GUI → Business → Data Access)

### **Công nghệ hiện tại:**
```
┌─────────────────────────────────────┐
│ Presentation: Java Swing (Desktop)  │
├─────────────────────────────────────┤
│ Business Logic: Service Classes     │
├─────────────────────────────────────┤
│ Data Access: DAO + Hibernate ORM    │
├─────────────────────────────────────┤
│ Database: MySQL 8.0                 │
├─────────────────────────────────────┤
│ Build: Maven                        │
│ JDK: Java 21                        │
└─────────────────────────────────────┘
```

---

## 2️⃣ CẤU TRÚC THƯ MỤC & KIẾN TRÚC

### **Cấu trúc Package:**

```
src/main/java/
├── QuanLyTuyenSinh.java           (Entry point - MainFrame)
├── NganhDAO.java                  (Legacy - moved to dal/dao)
├── bus/                           (Business Logic Layer - 12 services)
│   ├── AdmissionEngineService.java        [EMPTY] ❌
│   ├── AspirationService.java             [EMPTY] ❌
│   ├── AuditLogService.java               [EMPTY] ❌
│   ├── AuthenticationService.java         [EMPTY] ❌
│   ├── AuthorizationService.java          [EMPTY] ❌
│   ├── BonusPointService.java             [EMPTY] ❌
│   ├── CandidateService.java              [✅ DONE] - Full CRUD, pagination, search
│   ├── CatalogService.java                [EMPTY] ❌
│   ├── ConversionRuleService.java         [EMPTY] ❌
│   ├── ExamScoreService.java              [EMPTY] ❌
│   ├── ScoreStatisticsService.java        [EMPTY] ❌
│   └── UserService.java                   [EMPTY] ❌
│
├── dal/                           (Data Access Layer)
│   ├── DBConnection.java          (Connection checker)
│   ├── dao/                       (Data Access Objects)
│   │   ├── AdmissionDAO.java      [EMPTY] ❌
│   │   ├── AspirationDAO.java     [EMPTY] ❌
│   │   ├── AuditLogDAO.java       [EMPTY] ❌
│   │   ├── BonusPointDAO.java     [EMPTY] ❌
│   │   ├── CandidateDAO.java      [✅ DONE] - Full implementation
│   │   ├── ConversionRuleDAO.java [EMPTY] ❌
│   │   ├── ExamScoreDAO.java      [EMPTY] ❌
│   │   ├── MajorCombinationDAO.java [EMPTY] ❌
│   │   ├── MajorDAO.java          [EMPTY] ❌
│   │   ├── RolePermissionDAO.java [EMPTY] ❌
│   │   ├── ToHopMonDAO.java       [EMPTY] ❌
│   │   └── UserDAO.java           [EMPTY] ❌
│   ├── entities/                  (Hibernate Entities)
│   │   ├── UserEntity.java        [EMPTY] ❌
│   │   ├── CandidateEntity.java   [✅ DONE]
│   │   ├── ExamScoreEntity.java   [✅ DONE]
│   │   ├── AspirationEntity.java  [EMPTY] ❌
│   │   ├── RoleEntity.java        [EMPTY] ❌
│   │   ├── PermissionEntity.java  [EMPTY] ❌
│   │   ├── RolePermissionEntity.java [EMPTY] ❌
│   │   ├── UserRoleEntity.java    [EMPTY] ❌
│   │   ├── ToHopMon.java          [✅ DONE]
│   │   └── Nganh.java             [✅ DONE]
│   └── hibernate/
│       └── HibernateUtil.java     [✅ DONE] - SessionFactory management
│
├── dto/                           (Data Transfer Objects)
│   ├── CandidateDTO.java          [✅ DONE]
│   ├── ExamScoreDTO.java          [EMPTY] ❌
│   ├── AspirationDTO.java         [EMPTY] ❌
│   ├── AuditLogDTO.java           [EMPTY] ❌
│   ├── BonusPointDTO.java         [EMPTY] ❌
│   ├── AdmissionResultDTO.java    [EMPTY] ❌
│   ├── ConversionRuleDTO.java     [EMPTY] ❌
│   ├── MajorCombinationDTO.java   [EMPTY] ❌
│   ├── MajorDTO.java              [EMPTY] ❌
│   ├── RolePermissionDTO.java     [EMPTY] ❌
│   ├── ScoreStatisticDTO.java     [EMPTY] ❌
│   ├── SubjectCombinationDTO.java [EMPTY] ❌
│   └── UserDTO.java               [EMPTY] ❌
│
├── gui/                           (User Interface - Swing)
│   ├── MainFrame.java             [✅ DONE] - Main window
│   ├── LoginFrame.java            [EMPTY] ❌
│   ├── components/
│   │   ├── AppHeader.java         [✅ DONE]
│   │   └── LeftSidebar.java       [✅ DONE]
│   ├── panels/                    (11 content panels)
│   │   ├── DashboardPanel.java    [EMPTY] ❌
│   │   ├── CandidatePanel.java    [✅ DONE]
│   │   ├── UserManagementPanel.java [EMPTY] ❌
│   │   ├── AdmissionRunPanel.java [EMPTY] ❌
│   │   ├── ExamScorePanel.java    [EMPTY] ❌
│   │   ├── BonusPointPanel.java   [EMPTY] ❌
│   │   ├── AspirationPanel.java   [EMPTY] ❌
│   │   ├── ConversionRulePanel.java [EMPTY] ❌
│   │   ├── CatalogPanel.java      [EMPTY] ❌
│   │   ├── AuditLogPanel.java     [EMPTY] ❌
│   │   └── ScoreStatisticsPanel.java [EMPTY] ❌
│   ├── dialogs/
│   │   ├── CandidateFormDialog.java [✅ DONE]
│   │   └── (other dialogs)
│   └── menu/
│
├── utils/
│   ├── excel/
│   │   ├── CandidateExcelImportUtil.java
│   │   └── CandidateExcelExportUtil.java
│   └── HibernateUtils.java
│
└── test/java/
    └── daoTester.java
```

### **Kiến trúc đang sử dụng:**

✅ **3-Layer Architecture (Tầng 3)**

```
┌─────────────────────────────────────────────────┐
│  PRESENTATION LAYER (GUI - Swing)              │
│  - MainFrame, Panels, Dialogs                   │
├─────────────────────────────────────────────────┤
│  BUSINESS LOGIC LAYER (Services)                │
│  - CandidateService, ExamScoreService, etc.     │
├─────────────────────────────────────────────────┤
│  DATA ACCESS LAYER (DAO + ORM)                  │
│  - DAO classes + Hibernate Entities             │
├─────────────────────────────────────────────────┤
│  PERSISTENCE LAYER (MySQL Database)             │
│  - 11 tables                                     │
└─────────────────────────────────────────────────┘
```

**Vai trò từng layer:**

| Layer | Vai trò | Trạng thái |
|-------|--------|-----------|
| **GUI** | Hiển thị UI, nhập liệu, xử lý sự kiện người dùng | ⚠️ Bộ khung (50%) |
| **Service** | Xử lý logic nghiệp vụ, validation, tính toán | ❌ Hầu hết rỗng |
| **DAO** | Thực thi các câu query DB, mapping Entity ↔ DTO | ✅ CandidateDAO ok, còn lại rỗng |
| **Entity** | Mapping ORM Hibernate → Database tables | ⚠️ Bộ khung (40%) |

---

## 3️⃣ DATABASE & MODEL

### **11 Bảng dữ liệu hiện tại:**

```sql
1. xt_staff_accounts        -- Tài khoản đăng nhập (username, password, role)
2. xt_thisinhxettuyen25     -- Thí sinh (CCCD, họ tên, ngày sinh, địa chỉ, ...)
3. xt_diemthixettuyen       -- Điểm thi từ nhiều môn học
4. xt_nganh                 -- Ngành tuyển sinh (mã, tên, chỉ tiêu, ...)
5. xt_tohop_monthi          -- Tổ hợp môn thi (VD: Toán-Vật-Hóa = A00)
6. xt_nganh_tohop           -- Mapping Ngành ↔ Tổ hợp môn
7. xt_diemcongxetuyen       -- Điểm cộng ưu tiên (DTƯT, KVƯT, ...)
8. xt_bangquydoi            -- Bang quy đổi điểm (Tương đương tích lũy)
9. (Aspiration table)       -- [TODO] Bảng nguyên vọng thí sinh
10. (Audit log table)       -- [TODO] Bảng ghi nhật ký thao tác
11. (Role/Permission)       -- [TODO] Phân quyền người dùng
```

### **Quan hệ giữa các bảng:**

```
┌─────────────────────────────────────────────────┐
│           xt_staff_accounts                     │
│  (id_staff, username, password, role)           │
└────────────────┬────────────────────────────────┘
                 │
                 │ 1:N
                 ↓
┌─────────────────────────────────────────────────┐
│      xt_thisinhxettuyen25 (Candidates)          │
│  (idthisinh, cccd, soBaoDanh, họ, tên, ...)    │
└────────────┬─────────────────────────┬──────────┘
             │                         │
             │ 1:1                     │ 1:1
             ↓                         ↓
  ┌──────────────────┐    ┌────────────────────────┐
  │ xt_diemthixettuyen │  │ xt_diemcongxetuyen    │
  │ (cccd, điểm...)  │    │ (cccd, manganh, ...)  │
  └────────┬─────────┘    └────────┬───────────────┘
           │                       │
           │ FK                    │ FK
           └───────┬───────────────┘
                   ↓
         ┌──────────────────────────┐
         │    xt_nganh              │
         │ (manganh, tennganh, ...) │
         └────────┬─────────────────┘
                  │ N:N
                  ↓
         ┌──────────────────────────┐
         │ xt_tohop_monthi          │
         │ (matohop, mon1, ...)     │
         └──────────────────────────┘
```

### **Hibernate Mapping hiện tại:**

```java
// HibernateUtil.java - Các entity được register:
✅ CandidateEntity.class
✅ ExamScoreEntity.class
✅ ToHopMon.class
✅ Nganh.class
❌ UserEntity (chưa implement)
❌ RoleEntity (chưa implement)
❌ AspirationEntity (chưa implement)
```

---

## 4️⃣ LUỒNG CHỨC NĂNG HIỆN TẠI

### **Luồng 1: Quản lý thí sinh (CandidatePanel) ✅**

```
┌─────────────────────────────────────────────────────────┐
│ 1. Người dùng nhấn vào tab "Thí sinh"                   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 2. CandidatePanel renders JTable với dữ liệu thí sinh   │
│    - Tìm kiếm theo CCCD hoặc tên (live search)          │
│    - Phân trang 20 dòng/trang                           │
└─────────────────────────────────────────────────────────┘
                          ↓
              ┌───────────┴───────────┐
              ↓                       ↓
    [Import Excel]          [Xem/Sửa/Xóa]
              ↓                       ↓
    CandidateExcelImportUtil  CandidateFormDialog
              ↓                       ↓
    Parse file                Form validation
              ↓                       ↓
    Insert/Update DB          Update CandidateEntity
              ↓                       ↓
    Refresh table             Reload từ DB
```

**Chi tiết luồng:**

```
GUI: CandidatePanel.loadPage(1)
  ↓
Service: CandidateService.getCandidates(cccd, name, page)
  ↓
DAO: CandidateDAO.findCandidates(...)
  ↓
Hibernate Query:
  SELECT c FROM CandidateEntity c 
  WHERE c.cccd LIKE ? AND c.ho + c.ten LIKE ?
  ORDER BY c.id ASC
  LIMIT 20 OFFSET 0
  ↓
JOIN ExamScoreEntity ON c.cccd = s.cccd
  ↓
Convert to DTO: CandidateDTO
  ↓
Render JTable
```

### **Luồng 2: Tích hợp điểm thi (ExamScoreEntity) ⚠️**

```
User → DB (xt_diemthixettuyen) → ExamScoreEntity
  ↓
[NOT IMPLEMENTED YET]
  ↓
ExamScoreService (rỗng)
  ↓
Score statistics, conversion, ranking
```

### **Luồng 3: Tuyên dương & Kết quả (AdmissionEngineService) ❌**

```
[NOT IMPLEMENTED]
  ↓
ExamScoreEntity (điểm thi)
  + CandidateEntity (ĐTƯT, KVƯT)
  + xt_diemcongxetuyen (điểm cộng)
  + xt_bangquydoi (quy đổi)
  ↓
Thuật toán lọc:
  1. Tính điểm xét tuyển = điểm thi + điểm cộng
  2. Quy đổi theo bang quy đổi
  3. Xếp hạng theo chỉ tiêu ngành
  4. Lọc ra thí sinh đủ điểm
  ↓
AdmissionResult
```

### **Luồng 4: Đăng nhập (NOT IMPLEMENTED) ❌**

```
[Chưa có LoginFrame implementation]
  ↓
xt_staff_accounts:
  - Check username/password
  - Lưu session current user
  ↓
[Chưa có authorization check]
```

---

## 5️⃣ NHỮNG CHỨC NĂNG ĐÃ CÓ

### **Đã hoàn chỉnh ✅**

| Module | Tính năng | Trạng thái |
|--------|----------|-----------|
| **Candidate** | CRUD thí sinh | ✅ Done |
| | Tìm kiếm + phân trang | ✅ Done |
| | Import/Export Excel | ✅ Done |
| | Form dialog thêm/sửa | ✅ Done |
| **UI/UX** | MainFrame layout | ✅ Done |
| | LeftSidebar menu | ✅ Done |
| | AppHeader | ✅ Done |
| | CandidatePanel display | ✅ Done |
| **Database** | Schema 11 bảng | ✅ Done |
| | Sample data (seed) | ✅ Done |
| **ORM** | Hibernate setup | ✅ Done |
| | CandidateEntity mapping | ✅ Done |
| | ExamScoreEntity mapping | ✅ Done |
| | ToHopMon, Nganh mapping | ✅ Done |

### **Đang dở dứt ⚠️**

| Module | Ghi chú |
|--------|---------|
| User Management | UI layout có nhưng không logic |
| Dashboard | Component rỗng |
| Audit Log | Bảng có nhưng service rỗng |
| Entity models | UserEntity, RoleEntity rỗng |

### **Chưa làm ❌**

| Module | Cần làm |
|--------|--------|
| **Authentication** | Login, session management |
| **Authorization** | Role-based access control (RBAC) |
| **Admission Engine** | Core algorithm tuyên dương |
| **Aspiration/Registration** | Quản lý nguyên vọng thí sinh |
| **Score Statistics** | Thống kê điểm, phân bố |
| **Conversion Rules** | Apply bang quy đổi |
| **Bonus Points** | Tính điểm ưu tiên |
| **Audit Logging** | Ghi nhật ký thao tác |
| **Reports** | In báo cáo tuyên dương |

---

## 6️⃣ NHỮNG GÌ CÒN THIẾU (So với đồ án)

### **🚨 Các vấn đề lớn:**

#### **1. Không có Authentication ❌**
```java
// LoginFrame.java - TRỐNG
// AuthService.java - TRỐNG
// Kết quả: Bất cứ ai cũng có thể dùng ứng dụng
```
**Cần:** Implement login form, password hashing, session management

#### **2. Không có Authorization (Phân quyền) ❌**
```java
// AuthorizationService.java - TRỐNG
// RoleEntity, PermissionEntity - RỖ TRỐNG
// Kết quả: Không khác biệt ADMIN vs NHAN_VIEN
```
**Cần:** RBAC - Role Based Access Control, permission checker

#### **3. Thiếu Admission Engine (Thuật toán lọc) ❌**
```java
// AdmissionEngineService.java - TRỐNG
// Kết quả: Không thể chạy quy trình tuyên dương
```
**Cần:** Core algorithm:
- Tính tổng điểm xét tuyển
- Quy đổi điểm
- Xếp hạng
- Lọc theo chỉ tiêu

#### **4. Không có Aspiration Management ❌**
```sql
-- Bảng nguyên vọng không tồn tại
-- Kết quả: Không biết thí sinh chọn ngành nào
```
**Cần:** Table + UI + Service để quản lý nguyên vọng

#### **5. Thiếu Security ❌**
```
❌ Password hashing (plain text)
❌ SQL injection prevention (partial)
❌ Input validation inconsistent
❌ Không audit trail
```

#### **6. Thiếu Dashboard ❌**
```java
// DashboardPanel.java - TRỐNG
// Kết quả: Không overview số liệu
```

#### **7. Thiếu Reports ❌**
```
❌ In danh sách tuyên dương
❌ Báo cáo thống kê
❌ Export kết quả
```

---

## 7️⃣ ĐỀ XUẤT CẤU TRÚC CHUẨN HÓA

### **Vấn đề hiện tại:**
1. ❌ Một số class ở sai vị trí (NganhDAO.java ở root)
2. ❌ Hầu hết service classes trống
3. ❌ Chưa có layer Controller (nếu cần)
4. ❌ Chưa có Config/Properties management

### **Cấu trúc được đề xuất:**

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── sgutuyensinh/
│   │           ├── QuanLyTuyenSinh.java (Entry point)
│   │           │
│   │           ├── config/
│   │           │   ├── AppConfig.java
│   │           │   └── DatabaseConfig.java
│   │           │
│   │           ├── gui/
│   │           │   ├── MainFrame.java
│   │           │   ├── LoginFrame.java
│   │           │   ├── components/
│   │           │   ├── panels/
│   │           │   ├── dialogs/
│   │           │   └── utils/
│   │           │
│   │           ├── service/
│   │           │   ├── auth/
│   │           │   │   ├── AuthService.java
│   │           │   │   └── AuthorizationService.java
│   │           │   ├── candidate/
│   │           │   │   ├── CandidateService.java
│   │           │   │   └── CandidateValidator.java
│   │           │   ├── admission/
│   │           │   │   ├── AdmissionEngineService.java
│   │           │   │   └── ScoringStrategy.java
│   │           │   ├── score/
│   │           │   ├── aspiration/
│   │           │   ├── audit/
│   │           │   └── common/
│   │           │       └── BaseService.java
│   │           │
│   │           ├── repository/
│   │           │   ├── CandidateRepository.java
│   │           │   ├── ExamScoreRepository.java
│   │           │   └── BaseRepository.java
│   │           │
│   │           ├── entity/
│   │           │   ├── user/
│   │           │   ├── candidate/
│   │           │   ├── score/
│   │           │   └── common/
│   │           │
│   │           ├── dto/
│   │           │   ├── request/
│   │           │   ├── response/
│   │           │   └── mapper/
│   │           │
│   │           ├── util/
│   │           │   ├── excel/
│   │           │   ├── security/
│   │           │   ├── validation/
│   │           │   └── common/
│   │           │
│   │           └── exception/
│   │               ├── AppException.java
│   │               ├── ValidationException.java
│   │               └── AuthException.java
│   │
│   └── resources/
│       ├── hibernate.cfg.xml
│       ├── app.properties
│       └── log4j.properties
│
└── test/
    ├── java/
    │   └── com/sgutuyensinh/
    │       ├── service/
    │       ├── repository/
    │       └── util/
    └── resources/
```

### **Lợi ích:**
✅ Package organization rõ ràng  
✅ Dễ bảo trì & mở rộng  
✅ Repository pattern (không inject DAO trực tiếp)  
✅ Validation layer riêng  
✅ Exception handling tập trung  

---

## 8️⃣ KẾ HOẠCH PHÁT TRIỂN TIẾP THEO

### **Ưu tiên 1 - CRITICAL (Làm trước):**

```
PHASE 1: SECURITY & AUTH (Week 1-2)
├── 1.1 Implement UserEntity + UserDAO
├── 1.2 Password hashing (BCrypt)
├── 1.3 LoginFrame GUI
├── 1.4 AuthService + session management
├── 1.5 Database update password field
└── 1.6 Test login flow

PHASE 2: AUTHORIZATION (Week 2-3)
├── 2.1 RoleEntity + PermissionEntity implementation
├── 2.2 RoleDAO + PermissionDAO
├── 2.3 AuthorizationService (checkPermission)
├── 2.4 UI interceptor (hide/disable button theo role)
└── 2.5 Database seed roles

PHASE 3: ADMISSION ENGINE (Week 3-4)
├── 3.1 ExamScoreService fully implement
├── 3.2 ScoringStrategy (tính điểm xét tuyển)
├── 3.3 ConversionRuleService (apply bang quy đổi)
├── 3.4 BonusPointService (cộng ưu tiên)
├── 3.5 AdmissionEngineService (ranking + selection)
└── 3.6 AdmissionResultDTO + AdmissionRunPanel UI
```

### **Ưu tiên 2 - HIGH (Làm sau):**

```
PHASE 4: STUDENT ASPIRATIONS (Week 4-5)
├── 4.1 Create xt_aspiration table
├── 4.2 AspirationEntity + AspirationDAO
├── 4.3 AspirationService
└── 4.4 AspirationPanel UI

PHASE 5: AUDIT & LOGGING (Week 5)
├── 5.1 Create xt_audit_log table
├── 5.2 AuditLogEntity + AuditLogDAO
├── 5.3 AuditLogService + interceptor
└── 5.4 AuditLogPanel display logs

PHASE 6: STATISTICS & DASHBOARD (Week 6)
├── 6.1 ScoreStatisticsService
├── 6.2 Query analytics
├── 6.3 DashboardPanel charts
└── 6.4 Performance optimization
```

### **Ưu tiên 3 - MEDIUM (Nếu có thời gian):**

```
PHASE 7: ADVANCED FEATURES
├── 7.1 Report generation (PDF export)
├── 7.2 Email notifications
├── 7.3 Batch processing
├── 7.4 Data validation utilities
└── 7.5 Unit tests (JUnit 5)

PHASE 8: UI/UX POLISH
├── 8.1 Responsive layout
├── 8.2 Error messages
├── 8.3 Dark mode
└── 8.4 Keyboard shortcuts
```

---

### **Tóm tắt Dependency:**

```
┌─────────────────────────────────────────────┐
│ Phase 1: Authentication                    │
│ (MUST HAVE - không có thì không chạy được) │
└────────────┬────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────┐
│ Phase 2: Authorization                     │
│ (MUST HAVE - không có phân quyền)           │
└────────────┬────────────────────────────────┘
             ↓
┌──────────────────────┬──────────────────────┐
│ Phase 3: Admission   │ Phase 4: Aspiration  │
│ Engine              │                      │
└──────────────────────┴──────────────────────┘
             ↓
┌─────────────────────────────────────────────┐
│ Phase 5: Audit Logging                     │
└─────────────────────────────────────────────┘
```

---

## 📋 TÓM TẮT

| Khía cạnh | Trạng thái | Ghi chú |
|----------|-----------|--------|
| **Cấu trúc** | ⚠️ 70% | Cần refactor package names |
| **Database** | ✅ 100% | Schema đầy đủ, test data OK |
| **Entities** | ⚠️ 40% | 4/10 entities done |
| **Services** | ❌ 10% | 1/12 services done |
| **GUI** | ⚠️ 30% | MainFrame ok, hầu hết panels rỗng |
| **Authentication** | ❌ 0% | Không có login |
| **Authorization** | ❌ 0% | Không có phân quyền |
| **Admission Engine** | ❌ 0% | Core logic không có |
| **Testing** | ❌ 0% | Không có unit tests |

---

## 🎯 KẾT LUẬN

✅ **Điều tốt:**
- Cơ sở dữ liệu được thiết kế tốt
- Cấu trúc 3-layer phù hợp
- Module Candidate hoàn chỉnh (có thể copy pattern)
- Hibernate setup đúng cách

❌ **Điều cần cải thiện:**
- Hầu hết các service trống
- Không có authentication/authorization
- Admission engine chưa implement
- Aspiration management chưa có
- Cần refactor package structure
