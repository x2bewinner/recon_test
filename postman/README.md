# Postman 測試集合

本目錄包含用於測試 Recon API 的 Postman Collection 和環境配置。

## 📦 檔案說明

- **Recon_API.postman_collection.json** - Postman Collection，包含所有 API 測試案例
- **Recon_API.postman_environment.json** - Postman 環境配置檔案
- **README.md** - 本說明文件

## 🚀 快速開始

### 1. 導入 Postman Collection

1. 打開 Postman
2. 點擊左上角的 **Import** 按鈕
3. 選擇 `Recon_API.postman_collection.json` 檔案
4. 點擊 **Import** 完成導入

### 2. 導入環境配置

1. 在 Postman 中點擊右上角的 **Environments** 圖標
2. 點擊 **Import** 按鈕
3. 選擇 `Recon_API.postman_environment.json` 檔案
4. 點擊 **Import** 完成導入
5. 在環境下拉選單中選擇 **Recon API - Local**

### 3. 配置環境變數

確保環境變數 `base_url` 設置為您的應用程式地址：

- **本地開發**: `http://localhost:8080`
- **測試環境**: `http://your-test-server:8080`
- **生產環境**: `http://your-prod-server:8080`

## 📋 測試案例列表

Collection 包含以下 10 個測試案例：

### 正常流程測試

1. **基本請求** - 測試基本的審計註冊請求（單筆交易、單一條目）
2. **多條目交易** - 測試一筆交易包含多個條目的場景
3. **批次處理** - 測試批次處理多筆交易的場景

### 特殊場景測試

4. **設備重啟場景** - 測試設備重啟後序列號重置的處理
   - 先發送 seqNum=1 和 seqNum=2 的交易
   - 然後發送設備重啟後的 seqNum=1 交易
   - 驗證系統正確累計計數

5. **跨日期交易** - 測試跨日期未完成交易的處理
   - 業務日期是昨天，但今天發送
   - 驗證交易累計到正確的業務日期

### 錯誤處理測試

6. **驗證錯誤 - 缺少必填欄位** - 測試缺少必填欄位的驗證
7. **驗證錯誤 - 空交易列表** - 測試空交易列表的驗證
8. **驗證錯誤 - 空條目列表** - 測試空條目列表的驗證
9. **無效 JSON** - 測試無效 JSON 格式的錯誤處理
10. **缺少 Content-Type 標頭** - 測試缺少 Content-Type 的錯誤處理

## 🧪 執行測試

### 執行單個請求

1. 在 Postman 中選擇要測試的請求
2. 確保已選擇正確的環境（Recon API - Local）
3. 點擊 **Send** 按鈕
4. 查看響應結果和測試結果

### 執行整個 Collection

1. 在 Postman 中點擊 Collection 名稱旁邊的 **...** 按鈕
2. 選擇 **Run collection**
3. 在 Runner 視窗中選擇要執行的請求
4. 點擊 **Run Recon API - Audit Register** 按鈕
5. 查看測試結果摘要

### 使用 Newman 執行（CI/CD）

```bash
# 安裝 Newman
npm install -g newman

# 執行 Collection
newman run postman/Recon_API.postman_collection.json \
  -e postman/Recon_API.postman_environment.json \
  --reporters cli,html \
  --reporter-html-export postman/report.html
```

## 📊 測試驗證

每個請求都包含自動化測試腳本，會驗證：

- HTTP 狀態碼
- 響應碼（SUCCESS、VALIDATION_ERROR 等）
- 響應訊息
- 錯誤列表（如有）

## 🔧 自訂測試

### 修改環境變數

1. 在 Postman 中選擇環境
2. 編輯 `base_url` 變數
3. 保存更改

### 添加新的測試案例

1. 在 Collection 中點擊 **...** 按鈕
2. 選擇 **Add Request**
3. 配置請求（方法、URL、Headers、Body）
4. 在 **Tests** 標籤中添加測試腳本
5. 保存請求

### 使用動態資料

Collection 包含預請求腳本，可以設置動態日期等變數。您可以在請求中使用這些變數：

```json
{
  "businessDate": "{{yesterday}}",
  "transactionDateTime": "{{current_datetime}}"
}
```

## 📝 注意事項

1. **確保應用程式正在運行** - 在執行測試前，確保應用程式已啟動並運行在配置的端口上
2. **資料庫狀態** - 某些測試（如設備重啟場景）會依賴資料庫中的現有資料
3. **測試順序** - 設備重啟場景測試包含預請求腳本，會先發送前置請求
4. **環境變數** - 根據實際環境修改 `base_url` 變數

## 🔗 相關資源

- [Postman 官方文檔](https://learning.postman.com/)
- [Newman 文檔](https://github.com/postmanlabs/newman)
- [API 使用指南](../README.md#api-使用指南)
- [測試指南](../TEST_GUIDE.md)

## 📧 問題回報

如有任何問題或建議，請聯繫開發團隊。

