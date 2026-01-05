# 測試指南

本文件說明如何執行和維護應用程式的測試案例。

## 📋 目錄

- [測試結構](#測試結構)
- [執行測試](#執行測試)
- [測試案例說明](#測試案例說明)
- [測試資料](#測試資料)
- [測試場景](#測試場景)

## 🏗️ 測試結構

```
src/test/
├── java/com/financial/recon/
│   ├── controller/
│   │   └── AuditRegisterControllerTest.java    # Controller 層整合測試
│   ├── service/
│   │   └── AuditRegisterServiceTest.java       # Service 層單元測試
│   └── util/
│       └── TestDataBuilder.java                # 測試資料構建器
└── resources/
    ├── application-test.yml                    # 測試配置檔案
    └── test-data/                              # 測試資料 JSON 檔案
        ├── basic-request.json
        ├── multi-entry-request.json
        ├── batch-request.json
        ├── device-restart-request.json
        ├── cross-date-request.json
        ├── invalid-request-missing-fields.json
        └── empty-entries-request.json
```

## 🚀 執行測試

### 執行所有測試

```bash
mvn test
```

### 執行特定測試類

```bash
# 執行 Service 層測試
mvn test -Dtest=AuditRegisterServiceTest

# 執行 Controller 層測試
mvn test -Dtest=AuditRegisterControllerTest
```

### 執行特定測試方法

```bash
mvn test -Dtest=AuditRegisterServiceTest#testProcessBasicTransaction
```

### 在 IDE 中執行

- **IntelliJ IDEA**: 右鍵點擊測試類或測試方法，選擇 "Run 'TestName'"
- **Eclipse**: 右鍵點擊測試類或測試方法，選擇 "Run As" > "JUnit Test"

### 查看測試覆蓋率

```bash
# 使用 JaCoCo 插件（需要先配置）
mvn clean test jacoco:report
```

## 📝 測試案例說明

### Service 層測試 (AuditRegisterServiceTest)

#### 1. 基本交易處理測試
- **測試方法**: `testProcessBasicTransaction()`
- **目的**: 驗證基本交易處理流程
- **驗證點**: 
  - 響應碼為 SUCCESS
  - 資料已保存到資料庫
  - MirrorAr 和 MirrorArDetail 記錄已創建

#### 2. 設備累計摘要測試
- **測試方法**: 
  - `testDeviceSummaryFirstRequest()` - 第一次請求
  - `testDeviceSummaryAccumulation()` - 累計測試
- **目的**: 驗證設備摘要的創建和累計邏輯
- **驗證點**:
  - 第一次請求創建新摘要
  - 後續請求累計到現有摘要
  - 計數和金額正確累計

#### 3. 設備重啟處理測試
- **測試方法**: `testDeviceRestartHandling()`
- **目的**: 驗證設備重啟後計數重置的處理
- **場景**: 
  1. 第一次請求 (seqNum=1): 計數=10
  2. 第二次請求 (seqNum=2): 計數=15
  3. 設備重啟後 (seqNum=1): 計數=5
- **驗證點**: 累計計數應為 30 (10+15+5)，而不是 15

#### 4. 跨日期交易測試
- **測試方法**: `testCrossDateTransaction()`
- **目的**: 驗證跨日期未完成交易的處理
- **場景**: 業務日期是昨天，但今天發送
- **驗證點**: 摘要應創建在業務日期，而不是發送日期

#### 5. 多條目交易測試
- **測試方法**: `testMultiEntryTransaction()`
- **目的**: 驗證一筆交易包含多個條目的處理
- **驗證點**: 每個條目都應創建獨立的摘要記錄

#### 6. 批次處理測試
- **測試方法**: `testBatchProcessing()`
- **目的**: 驗證批次處理多筆交易
- **驗證點**: 所有交易都應成功處理

#### 7. 不同設備獨立摘要測試
- **測試方法**: `testDifferentDevicesIndependentSummary()`
- **目的**: 驗證不同設備的摘要相互獨立
- **驗證點**: 每個設備的摘要應獨立累計

#### 8. 不同業務日期獨立摘要測試
- **測試方法**: `testDifferentBusinessDatesIndependentSummary()`
- **目的**: 驗證不同業務日期的摘要相互獨立
- **驗證點**: 每個業務日期的摘要應獨立累計

### Controller 層測試 (AuditRegisterControllerTest)

#### 1. 基本 API 請求測試
- **測試方法**: `testBasicApiRequest()`
- **目的**: 驗證基本 API 請求處理
- **驗證點**:
  - HTTP 狀態碼為 200
  - 響應碼為 SUCCESS
  - 響應格式正確

#### 2. 驗證錯誤測試
- **測試方法**: 
  - `testValidationError()` - 缺少必填欄位
  - `testEmptyTransactionList()` - 空交易列表
  - `testEmptyEntriesList()` - 空條目列表
- **目的**: 驗證請求驗證邏輯
- **驗證點**:
  - HTTP 狀態碼為 400
  - 響應碼為 VALIDATION_ERROR
  - 錯誤訊息包含驗證失敗的欄位

#### 3. 批次處理測試
- **測試方法**: `testBatchProcessing()`
- **目的**: 驗證批次處理 API
- **驗證點**: 所有交易都應成功處理

#### 4. 設備重啟場景測試
- **測試方法**: `testDeviceRestartScenario()`
- **目的**: 驗證設備重啟場景的 API 處理
- **驗證點**: 即使序列號重置，也應成功處理

#### 5. 跨日期交易測試
- **測試方法**: `testCrossDateTransaction()`
- **目的**: 驗證跨日期交易的 API 處理
- **驗證點**: 應成功處理跨日期交易

#### 6. 錯誤處理測試
- **測試方法**: 
  - `testInvalidJson()` - 無效 JSON
  - `testMissingContentType()` - 缺少 Content-Type
- **目的**: 驗證錯誤處理邏輯
- **驗證點**: 應返回適當的錯誤響應

## 📊 測試資料

### JSON 測試資料檔案

測試資料檔案位於 `src/test/resources/test-data/` 目錄：

1. **basic-request.json**: 基本請求範例
2. **multi-entry-request.json**: 多條目交易請求
3. **batch-request.json**: 批次處理請求（3 筆交易）
4. **device-restart-request.json**: 設備重啟場景請求
5. **cross-date-request.json**: 跨日期交易請求
6. **invalid-request-missing-fields.json**: 缺少必填欄位的無效請求
7. **empty-entries-request.json**: 空條目列表請求

### 使用測試資料構建器

`TestDataBuilder` 類提供了多種方法來創建測試資料：

```java
// 創建基本請求
AuditRegisterRequest request = TestDataBuilder.createBasicRequest();

// 創建設備重啟請求
AuditRegisterRequest request = TestDataBuilder.createDeviceRestartRequest();

// 創建跨日期請求
AuditRegisterRequest request = TestDataBuilder.createCrossDateRequest();

// 創建批次請求
AuditRegisterRequest request = TestDataBuilder.createBatchRequest(5);

// 創建多條目請求
AuditRegisterRequest request = TestDataBuilder.createMultiEntryRequest();

// 創建特定設備和日期的請求
AuditRegisterRequest request = TestDataBuilder.createRequestForDeviceAndDate(
    "DEVICE-001", LocalDate.now(), 1);
```

## 🎯 測試場景

### 場景 1: 正常交易處理

**描述**: 設備發送正常的審計註冊請求

**測試步驟**:
1. 創建基本請求
2. 發送請求
3. 驗證響應為 SUCCESS
4. 驗證資料已保存到資料庫

**預期結果**: 
- 響應碼: SUCCESS
- MirrorAr 記錄已創建
- MirrorArDetail 記錄已創建
- DeviceAuditRegisterSummary 記錄已創建

### 場景 2: 設備重啟處理

**描述**: 設備重啟後序列號重置，需要正確累計

**測試步驟**:
1. 發送第一次請求 (seqNum=1, count=10)
2. 發送第二次請求 (seqNum=2, count=15)
3. 發送設備重啟後的請求 (seqNum=1, count=5)
4. 驗證累計值

**預期結果**: 
- 累計計數: 30 (10+15+5)
- 累計金額: 正確累計
- 系統檢測到設備重啟

### 場景 3: 跨日期交易處理

**描述**: 業務日期是昨天，但今天發送

**測試步驟**:
1. 創建業務日期為昨天的請求
2. 發送請求
3. 驗證摘要創建在業務日期

**預期結果**: 
- 摘要記錄的業務日期為昨天
- 不創建今天的摘要記錄

### 場景 4: 批次處理

**描述**: 一次處理多筆交易

**測試步驟**:
1. 創建包含多筆交易的請求
2. 發送請求
3. 驗證所有交易都成功處理

**預期結果**: 
- 響應碼: SUCCESS
- 所有交易都成功處理
- 每筆交易都創建對應的記錄

### 場景 5: 驗證失敗

**描述**: 請求缺少必填欄位

**測試步驟**:
1. 創建缺少必填欄位的請求
2. 發送請求
3. 驗證驗證錯誤

**預期結果**: 
- HTTP 狀態碼: 400
- 響應碼: VALIDATION_ERROR
- 錯誤訊息包含驗證失敗的欄位

## 🔧 測試配置

### 測試資料庫

測試使用 H2 內存資料庫，配置在 `application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=Oracle
    username: sa
    password: 
```

### 測試特性

- 使用 `@ActiveProfiles("test")` 啟用測試配置
- 使用 `@Transactional` 確保測試後清理資料
- 使用 `@BeforeEach` 在每個測試前清理資料

## 📈 測試覆蓋率目標

建議的測試覆蓋率目標：

- **Service 層**: ≥ 90%
- **Controller 層**: ≥ 85%
- **整體覆蓋率**: ≥ 80%

## 🐛 除錯測試

### 查看測試日誌

在 `application-test.yml` 中調整日誌級別：

```yaml
logging:
  level:
    com.financial.recon: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

### 使用 H2 控制台

測試執行時可以訪問 H2 控制台查看資料：

```
http://localhost:8080/h2-console
```

連接資訊：
- JDBC URL: `jdbc:h2:mem:testdb`
- 用戶名: `sa`
- 密碼: (空)

## 📝 新增測試案例

### 步驟

1. 在對應的測試類中添加測試方法
2. 使用 `@Test` 和 `@DisplayName` 註解
3. 使用 `TestDataBuilder` 創建測試資料
4. 執行測試並驗證結果
5. 如有需要，添加新的測試資料 JSON 檔案

### 範例

```java
@Test
@DisplayName("測試新功能 - 應該符合預期行為")
void testNewFeature() {
    // Given
    AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
    
    // When
    AuditRegisterResponse response = auditRegisterService.processAuditRegister(
        request, "TEST-ID");
    
    // Then
    assertEquals("SUCCESS", response.getResponseCode());
    // 更多驗證...
}
```

## ✅ 測試檢查清單

在提交代碼前，確保：

- [ ] 所有測試都通過
- [ ] 新增功能有對應的測試
- [ ] 測試覆蓋率符合目標
- [ ] 測試資料已更新（如有需要）
- [ ] 測試文檔已更新（如有需要）

---

如有任何問題，請聯繫開發團隊。

