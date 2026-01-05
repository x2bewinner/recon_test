<<<<<<< HEAD
# 財務系統 - 交易審計註冊對帳應用程式

這是一個 Spring Boot 應用程式，用於處理交易審計註冊對帳（Transaction Audit Register Reconciliation）。系統會接收設備發送的審計註冊請求，處理交易資料，並自動處理設備重啟和跨日期未完成交易的情況。

## 📋 目錄

- [系統需求](#系統需求)
- [快速開始](#快速開始)
- [安裝與配置](#安裝與配置)
- [啟動應用程式](#啟動應用程式)
- [API 使用指南](#api-使用指南)
- [功能特性](#功能特性)
- [測試](#測試)
- [常見問題](#常見問題)

## 🔧 系統需求

- **Java**: JDK 17 或更高版本
- **Maven**: 3.6+ 或更高版本
- **資料庫**: Oracle Database（建議 12c 或更高版本）
- **作業系統**: Windows、Linux 或 macOS

## 🚀 快速開始

### 1. 克隆或下載專案

```bash
cd /path/to/your/workspace
```

### 2. 建立資料庫表

在 Oracle 資料庫中執行以下 SQL 腳本建立必要的表結構：

```bash
# 連接到 Oracle 資料庫
sqlplus username/password@database

# 執行建表腳本
@src/main/resources/db/migration/create_device_ar_summary.sql
```

**注意**: 確保以下表已存在（如果不存在，需要先建立）：
- `MIRROR_AR` - 主要審計註冊表
- `MIRROR_AR_EX` - 異常審計註冊表
- `MIRROR_AR_DETAIL` - 審計註冊明細表
- `MIRROR_AR_DETAIL_EX` - 異常審計註冊明細表

### 3. 配置資料庫連接

編輯 `src/main/resources/application.yml` 檔案，設定資料庫連接資訊：

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@your_host:1521:your_service
    username: your_username
    password: your_password
```

或者使用環境變數（推薦用於生產環境）：

```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### 4. 編譯專案

```bash
mvn clean compile
```

### 5. 啟動應用程式

```bash
mvn spring-boot:run
```

應用程式將在 `http://localhost:8080` 啟動。

## 📦 安裝與配置

### 使用 Maven 打包

```bash
# 編譯並打包
mvn clean package

# 打包後會生成 target/recon-1.0.0.jar
```

### 使用 JAR 檔案執行

```bash
# 設定環境變數
export DB_USERNAME=your_username
export DB_PASSWORD=your_password

# 執行 JAR 檔案
java -jar target/recon-1.0.0.jar
```

### 配置選項

在 `application.yml` 中可以調整以下配置：

```yaml
server:
  port: 8080  # 應用程式端口

spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # 連接池最大連接數
      minimum-idle: 5        # 連接池最小空閒連接數

logging:
  level:
    com.financial.recon: DEBUG  # 日誌級別（DEBUG/INFO/WARN/ERROR）
```

## 🎯 啟動應用程式

### 方法 1: 使用 Maven

```bash
mvn spring-boot:run
```

### 方法 2: 使用 JAR 檔案

```bash
java -jar target/recon-1.0.0.jar
```

### 方法 3: 使用 IDE

在 IntelliJ IDEA 或 Eclipse 中直接執行 `ReconApplication.java` 的 `main` 方法。

### 驗證啟動成功

啟動成功後，您應該看到類似以下的日誌：

```
Started ReconApplication in X.XXX seconds
```

## 📡 API 使用指南

### 端點資訊

- **URL**: `POST /v1/ar/auditRegister`
- **Content-Type**: `application/json`
- **可選標頭**: `X-Client-Request-Identifier` (用於追蹤請求)

### 請求格式

#### 基本請求結構

```json
{
  "auditRegisterTxns": [
    {
      "transactionType": "string",
      "transactionDateTime": "2024-01-15T10:30:00+08:00",
      "deviceTypeId": "string",
      "equipmentId": "string",
      "deviceSpecialMode": "string",
      "deviceId": "string",
      "serviceId": "string",
      "beId": 1,
      "auditRegisterSeqNum": 1,
      "businessDate": "2024-01-15",
      "auditRegisterEntries": [
        {
          "arTypeIdentifier": "string",
          "cardMediaTypeId": "string",
          "count": 10,
          "value": 1000.50
        }
      ]
    }
  ]
}
```

#### 欄位說明

**AuditRegisterTransaction（交易層級）**:
- `transactionType` (必填): 交易類型，例如 "AUD001"
- `transactionDateTime` (必填): 交易日期時間，ISO 8601 格式
- `equipmentId` (必填): 設備實體 ID
- `deviceId` (必填): 設備 ID
- `beId` (必填): 業務實體 ID（整數）
- `auditRegisterSeqNum` (必填): 審計註冊序列號（整數）
- `businessDate` (必填): 業務日期，格式 "YYYY-MM-DD"
- `deviceTypeId` (可選): 設備類型 ID
- `deviceSpecialMode` (可選): 設備特殊模式
- `serviceId` (可選): 服務 ID
- `auditRegisterEntries` (必填): 審計註冊條目陣列

**AuditRegisterEntry（條目層級）**:
- `arTypeIdentifier` (必填): 審計註冊類型標識符
- `count` (必填): 交易計數（整數）
- `value` (可選): 交易金額（浮點數）
- `cardMediaTypeId` (可選): 卡片媒體類型 ID

### 實際使用範例

#### 範例 1: 單筆交易，單一條目

```bash
curl -X POST http://localhost:8080/v1/ar/auditRegister \
  -H "Content-Type: application/json" \
  -H "X-Client-Request-Identifier: CLIENT-001" \
  -d '{
    "auditRegisterTxns": [
      {
        "transactionType": "AUD001",
        "transactionDateTime": "2024-01-15T10:30:00+08:00",
        "deviceId": "DEVICE-001",
        "equipmentId": "EQ-001",
        "beId": 1,
        "auditRegisterSeqNum": 1,
        "businessDate": "2024-01-15",
        "auditRegisterEntries": [
          {
            "arTypeIdentifier": "AR-TYPE-001",
            "cardMediaTypeId": "CARD-001",
            "count": 10,
            "value": 1000.50
          }
        ]
      }
    ]
  }'
```

#### 範例 2: 單筆交易，多個條目

```bash
curl -X POST http://localhost:8080/v1/ar/auditRegister \
  -H "Content-Type: application/json" \
  -d '{
    "auditRegisterTxns": [
      {
        "transactionType": "AUD001",
        "transactionDateTime": "2024-01-15T10:30:00+08:00",
        "deviceId": "DEVICE-001",
        "equipmentId": "EQ-001",
        "beId": 1,
        "auditRegisterSeqNum": 2,
        "businessDate": "2024-01-15",
        "auditRegisterEntries": [
          {
            "arTypeIdentifier": "AR-TYPE-001",
            "cardMediaTypeId": "CARD-001",
            "count": 15,
            "value": 1500.00
          },
          {
            "arTypeIdentifier": "AR-TYPE-002",
            "cardMediaTypeId": "CARD-002",
            "count": 5,
            "value": 500.00
          }
        ]
      }
    ]
  }'
```

#### 範例 3: 批次處理多筆交易

```bash
curl -X POST http://localhost:8080/v1/ar/auditRegister \
  -H "Content-Type: application/json" \
  -d '{
    "auditRegisterTxns": [
      {
        "transactionType": "AUD001",
        "transactionDateTime": "2024-01-15T10:30:00+08:00",
        "deviceId": "DEVICE-001",
        "equipmentId": "EQ-001",
        "beId": 1,
        "auditRegisterSeqNum": 1,
        "businessDate": "2024-01-15",
        "auditRegisterEntries": [
          {
            "arTypeIdentifier": "AR-TYPE-001",
            "count": 10,
            "value": 1000.00
          }
        ]
      },
      {
        "transactionType": "AUD001",
        "transactionDateTime": "2024-01-15T11:00:00+08:00",
        "deviceId": "DEVICE-002",
        "equipmentId": "EQ-002",
        "beId": 1,
        "auditRegisterSeqNum": 1,
        "businessDate": "2024-01-15",
        "auditRegisterEntries": [
          {
            "arTypeIdentifier": "AR-TYPE-001",
            "count": 20,
            "value": 2000.00
          }
        ]
      }
    ]
  }'
```

### 響應格式

#### 成功響應

```json
{
  "responseCode": "SUCCESS",
  "responseMessage": "Successfully processed 1 transaction(s)",
  "errors": null
}
```

#### 部分成功響應（部分交易失敗）

```json
{
  "responseCode": "PARTIAL_SUCCESS",
  "responseMessage": "Processed 2 success, 1 failure(s)",
  "errors": [
    "Transaction processing failed for deviceId: DEVICE-003, seqNum: 1 - Error message here"
  ]
}
```

#### 錯誤響應

```json
{
  "responseCode": "ERROR",
  "responseMessage": "Failed to process audit register request: Error message",
  "errors": [
    "Error message here"
  ]
}
```

### HTTP 狀態碼

- `200 OK`: 所有交易處理成功
- `202 Accepted`: 部分交易處理成功或包含錯誤
- `400 Bad Request`: 請求格式錯誤或驗證失敗
- `500 Internal Server Error`: 伺服器內部錯誤

### 使用 Postman 測試

1. 建立新的 POST 請求
2. URL: `http://localhost:8080/v1/ar/auditRegister`
3. Headers:
   - `Content-Type: application/json`
   - `X-Client-Request-Identifier: TEST-001` (可選)
4. Body (選擇 raw JSON):
   ```json
   {
     "auditRegisterTxns": [
       {
         "transactionType": "AUD001",
         "transactionDateTime": "2024-01-15T10:30:00+08:00",
         "deviceId": "DEVICE-001",
         "equipmentId": "EQ-001",
         "beId": 1,
         "auditRegisterSeqNum": 1,
         "businessDate": "2024-01-15",
         "auditRegisterEntries": [
           {
             "arTypeIdentifier": "AR-TYPE-001",
             "count": 10,
             "value": 1000.00
           }
         ]
       }
     ]
   }
   ```
5. 點擊 Send

## ✨ 功能特性

### 1. 交易處理
- 接收並處理審計註冊交易記錄
- 支援批次處理多筆交易
- 自動驗證請求資料格式

### 2. 設備重啟處理
系統會自動檢測設備重啟並處理計數重置：

- **自動檢測**: 當 `auditRegisterSeqNum` 小於之前記錄的最大值時，系統會檢測到設備重啟
- **累計值計算**: 系統會使用資料庫中的累計值加上當前值，確保計數正確
- **按維度追蹤**: 按設備ID、業務實體ID、業務日期、AR類型、卡片媒體類型進行分組追蹤

**範例場景**:
```
1. 第一次請求 (seqNum=1): 計數=10, 金額=1000
   → 系統記錄：累計計數=10, 累計金額=1000

2. 第二次請求 (seqNum=2): 計數=15, 金額=1500
   → 系統記錄：累計計數=25, 累計金額=2500

3. 設備重啟後 (seqNum=1): 計數=5, 金額=500
   → 系統檢測到重啟（seqNum=1 < 之前的最大值 2）
   → 系統記錄：累計計數=30 (25+5), 累計金額=3000 (2500+500)
```

### 3. 跨日期未完成交易處理
系統會自動處理跨日期發送的未完成交易：

- **業務日期分組**: 按業務日期（businessDate）分組累計，不受發送日期影響
- **自動檢測**: 自動檢測並記錄跨日期場景（例如 10-Dec 的交易在 11-Dec 發送）
- **正確累計**: 確保未完成交易正確累計到對應的業務日期

**範例場景**:
```
1. 10-Dec-2025 09:00AM: 設備發送 Audit Register
   → 業務日期：2025-12-10
   → 累計計數：100, 累計金額：10000

2. 10-Dec-2025 10:00AM: 設備收到新交易，但未上傳就關閉

3. 11-Dec-2025 08:00AM: 設備重新開啟，發送未完成的交易
   → 業務日期：2025-12-10（重要：使用交易發生的業務日期）
   → 計數：5, 金額：500
   → 系統檢測到跨日期場景
   → 更新 10-Dec 的摘要：累計計數=105, 累計金額=10500
```

### 4. 異常處理
- 處理失敗的交易會自動儲存到異常表（`MIRROR_AR_EX` 和 `MIRROR_AR_DETAIL_EX`）
- 詳細的錯誤訊息回報
- 部分成功時仍會處理成功的交易

### 5. 資料驗證
- 使用 Bean Validation 驗證請求資料
- 必填欄位檢查
- 資料格式驗證

## ❓ 常見問題

### Q1: 如何確認應用程式已成功啟動？

A: 檢查日誌輸出，應該看到：
```
Started ReconApplication in X.XXX seconds
```
或者訪問 `http://localhost:8080`，如果應用程式正常運行，會返回 404（因為沒有根路徑），這表示應用程式已啟動。

### Q2: 資料庫連接失敗怎麼辦？

A: 檢查以下項目：
1. 確認 Oracle 資料庫服務正在運行
2. 檢查 `application.yml` 中的資料庫連接資訊是否正確
3. 確認網路連接正常
4. 檢查防火牆設定
5. 確認資料庫用戶名和密碼正確

### Q3: 如何查看詳細的日誌？

A: 在 `application.yml` 中調整日誌級別：
```yaml
logging:
  level:
    com.financial.recon: DEBUG
```

### Q4: 設備重啟後計數不正確怎麼辦？

A: 系統會自動處理設備重啟情況。如果仍有問題：
1. 檢查 `DEVICE_AR_SUMMARY` 表中是否有對應的記錄
2. 確認 `businessDate` 欄位是否正確
3. 查看應用程式日誌中的設備重啟檢測訊息

### Q5: 跨日期交易沒有正確累計怎麼辦？

A: 確保：
1. 請求中的 `businessDate` 欄位設定為交易發生的業務日期（不是發送日期）
2. 檢查 `DEVICE_AR_SUMMARY` 表中按業務日期分組的記錄
3. 查看應用程式日誌中的跨日期處理訊息

### Q6: 如何查詢處理結果？

A: 可以查詢以下資料表：
- `MIRROR_AR`: 正常處理的交易
- `MIRROR_AR_DETAIL`: 正常處理的交易明細
- `MIRROR_AR_EX`: 處理失敗的交易
- `MIRROR_AR_DETAIL_EX`: 處理失敗的交易明細
- `DEVICE_AR_SUMMARY`: 設備累計摘要（用於對帳）

### Q7: 如何處理大量交易？

A: 系統支援批次處理。建議：
1. 將多筆交易放在同一個請求的 `auditRegisterTxns` 陣列中
2. 根據實際情況調整資料庫連接池大小（在 `application.yml` 中）
3. 監控應用程式性能，必要時增加伺服器資源

### Q8: 生產環境部署建議？

A: 
1. 使用環境變數管理敏感資訊（資料庫密碼等）
2. 設定適當的日誌級別（生產環境建議使用 INFO）
3. 配置資料庫連接池大小
4. 設定應用程式監控和告警
5. 定期備份資料庫
6. 使用反向代理（如 Nginx）處理負載平衡

## 📚 專案結構

```
src/
├── main/
│   ├── java/com/financial/recon/
│   │   ├── ReconApplication.java          # Spring Boot 主應用程式
│   │   ├── controller/
│   │   │   └── AuditRegisterController.java    # REST API 控制器
│   │   ├── service/
│   │   │   └── AuditRegisterService.java       # 業務邏輯服務層
│   │   ├── repository/                          # 資料庫存取層
│   │   ├── entity/                              # 實體類
│   │   ├── dto/                                 # 資料傳輸物件
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java     # 全域異常處理
│   └── resources/
│       ├── application.yml                      # 應用程式配置
│       └── db/migration/                        # 資料庫遷移腳本
```

## 🔗 相關資源

- [Spring Boot 官方文檔](https://spring.io/projects/spring-boot)
- [Spring Data JPA 文檔](https://spring.io/projects/spring-data-jpa)
- [Oracle JDBC 驅動程式](https://www.oracle.com/database/technologies/appdev/jdbc.html)

## 🧪 測試

### 執行測試

#### 執行所有測試

```bash
mvn test
```

或使用測試腳本：

```bash
./run-tests.sh
```

#### 執行特定測試

```bash
# 執行 Service 層測試
mvn test -Dtest=AuditRegisterServiceTest

# 執行 Controller 層測試
mvn test -Dtest=AuditRegisterControllerTest

# 或使用測試腳本
./run-tests.sh --service
./run-tests.sh --controller
```

### 測試結構

專案包含以下測試：

- **Service 層測試** (`AuditRegisterServiceTest.java`): 測試業務邏輯
  - 基本交易處理
  - 設備累計摘要
  - 設備重啟處理
  - 跨日期交易處理
  - 批次處理
  - 多條目交易

- **Controller 層測試** (`AuditRegisterControllerTest.java`): 測試 API 端點
  - API 請求處理
  - 驗證錯誤處理
  - 批次處理
  - 錯誤場景

### 測試資料

測試資料位於 `src/test/resources/test-data/` 目錄，包含：

- `basic-request.json` - 基本請求範例
- `multi-entry-request.json` - 多條目交易請求
- `batch-request.json` - 批次處理請求
- `device-restart-request.json` - 設備重啟場景
- `cross-date-request.json` - 跨日期交易請求
- `invalid-request-missing-fields.json` - 無效請求範例

### 測試配置

測試使用 H2 內存資料庫，無需配置外部資料庫。測試配置檔案位於 `src/test/resources/application-test.yml`。

### 詳細測試文檔

更多測試相關資訊，請參閱 [TEST_GUIDE.md](TEST_GUIDE.md)。

## 📝 注意事項

1. **業務日期設定**: 設備發送 Audit Register 時，務必確保 `businessDate` 欄位正確設置為交易發生的業務日期，而不是發送日期
2. **序列號管理**: `auditRegisterSeqNum` 應該由設備端管理，系統會自動檢測設備重啟
3. **資料庫表**: 確保所有必要的資料庫表已建立
4. **時區設定**: 系統使用 UTC 時區儲存時間戳，請注意時區轉換
5. **批次大小**: 建議單次請求的交易數量不超過 100 筆，以確保處理效率

---

如有任何問題或建議，請聯繫開發團隊。
