// /admin/js/logs.js

let currentPage = 0;
let totalPages = 0;
let pageSize = 20;

function getAuthHeaders() {
  const token = localStorage.getItem("jwt_token");
  return {
    "Content-Type": "application/json",
    Accept: "application/json",
    Authorization: `Bearer ${token}`,
  };
}

function handleApiError(response) {
  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("username");
    window.location.href = "/login.html";
  }
}

// Load stats
async function loadStats() {
  try {
    const response = await fetch("/api/v1/admin/logs/stats?hours=24", {
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleApiError(response);
      return;
    }

    const result = await response.json();
    if (result.code === 1 && result.data) {
      document.getElementById("statTotal").textContent =
        result.data.totalRequests.toLocaleString();
      document.getElementById("statErrors").textContent =
        result.data.errorCount.toLocaleString();
      document.getElementById("statAvgDuration").textContent =
        result.data.avgDurationMs.toLocaleString();
    }
  } catch (error) {
    console.error("Load stats error:", error);
  }
}

// Build query params from filters
function buildQueryParams(page) {
  const params = new URLSearchParams();
  params.append("page", page);
  params.append("size", pageSize);

  const correlationId = document
    .getElementById("filterCorrelationId")
    .value.trim();
  const direction = document.getElementById("filterDirection").value;
  const endpoint = document.getElementById("filterEndpoint").value.trim();
  const httpStatus = document.getElementById("filterStatus").value;
  const externalSystems = document
    .getElementById("filterExternalSystems")
    .value.trim();
  const externalSystemsExclude = document
    .getElementById("filterExternalSystemsExclude")
    .value.trim();
  const startDate = document.getElementById("filterStartDate").value;
  const endDate = document.getElementById("filterEndDate").value;
  const errorsOnly = document.getElementById("filterErrorsOnly").checked;

  if (correlationId) params.append("correlationId", correlationId);
  if (direction) params.append("direction", direction);
  if (endpoint) params.append("endpoint", endpoint);
  if (httpStatus) params.append("httpStatus", httpStatus);
  if (externalSystems) params.append("externalSystems", externalSystems);
  if (externalSystemsExclude)
    params.append("externalSystemsExclude", externalSystemsExclude);
  if (startDate) params.append("startDate", new Date(startDate).toISOString());
  if (endDate) params.append("endDate", new Date(endDate).toISOString());
  if (errorsOnly) params.append("errorsOnly", "true");

  return params.toString();
}

// Load logs
async function loadLogs(page = 0) {
  currentPage = page;
  const tbody = document.getElementById("logsBody");
  tbody.innerHTML = `
        <tr>
            <td colspan="8" class="loading-cell">
                <div class="loading-spinner"></div>
                <span>Yuklanmoqda...</span>
            </td>
        </tr>
    `;

  try {
    const queryParams = buildQueryParams(page);
    const response = await fetch(`/api/v1/admin/logs?${queryParams}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleApiError(response);
      return;
    }

    const result = await response.json();

    if (result.code === 1 && result.data) {
      const pageData = result.data;
      totalPages = pageData.totalPages;
      displayLogs(pageData.content);
      displayPagination(pageData);
      document.getElementById("resultValue").textContent =
        pageData.totalElements.toLocaleString();
    }
  } catch (error) {
    console.error("Load logs error:", error);
    tbody.innerHTML = `
            <tr>
                <td colspan="8" class="loading-cell">
                    <p style="color: #dc2626;">Xatolik yuz berdi</p>
                </td>
            </tr>
        `;
  }
}

// Display logs in table
function displayLogs(logs) {
  const tbody = document.getElementById("logsBody");

  if (!logs || logs.length === 0) {
    tbody.innerHTML = `
            <tr>
                <td colspan="8" class="loading-cell">
                    <p>Loglar topilmadi</p>
                </td>
            </tr>
        `;
    return;
  }

  tbody.innerHTML = logs
    .map(
      (log) => `
        <tr>
            <td>${formatDateTime(log.createdAt)}</td>
            <td>
                <span class="direction-badge ${log.direction.toLowerCase()}">
                    ${log.direction}
                </span>
            </td>
            <td><strong>${log.httpMethod || "-"}</strong></td>
            <td class="endpoint-cell" title="${escapeHtml(log.endpoint || "")}">
                ${escapeHtml(log.endpoint || "-")}
            </td>
            <td>
                <span class="status-badge ${getStatusClass(log.httpStatus)}">
                    ${log.httpStatus || "-"}
                </span>
            </td>
            <td class="duration-cell ${log.durationMs > 3000 ? "slow" : ""}">
                ${log.durationMs != null ? log.durationMs.toLocaleString() : "-"}
            </td>
            <td title="${escapeHtml(log.externalSystem || "")}">
                ${escapeHtml(truncate(log.externalSystem, 15)) || "-"}
            </td>
            <td>
                <button onclick="showLogDetail(${log.logId})" class="btn-icon" title="Tafsilotlar">
                    <img src="/img/eye-open.png" alt="View" />
                </button>
            </td>
        </tr>
    `,
    )
    .join("");
}

// Pagination display
function displayPagination(pageData) {
  const container = document.getElementById("pagination");
  const { number, totalPages, totalElements, first, last } = pageData;

  if (totalPages <= 1) {
    container.innerHTML = "";
    return;
  }

  let html = "";

  // Previous button
  html += `<button onclick="loadLogs(${number - 1})" ${first ? "disabled" : ""}>&#8592; Oldingi</button>`;

  // Page numbers
  const startPage = Math.max(0, number - 2);
  const endPage = Math.min(totalPages - 1, number + 2);

  if (startPage > 0) {
    html += `<button onclick="loadLogs(0)">1</button>`;
    if (startPage > 1) html += `<span class="page-info">...</span>`;
  }

  for (let i = startPage; i <= endPage; i++) {
    html += `<button onclick="loadLogs(${i})" class="${i === number ? "active" : ""}">${i + 1}</button>`;
  }

  if (endPage < totalPages - 1) {
    if (endPage < totalPages - 2) html += `<span class="page-info">...</span>`;
    html += `<button onclick="loadLogs(${totalPages - 1})">${totalPages}</button>`;
  }

  // Next button
  html += `<button onclick="loadLogs(${number + 1})" ${last ? "disabled" : ""}>Keyingi &#8594;</button>`;

  container.innerHTML = html;
}

// Show log detail
async function showLogDetail(logId) {
  try {
    const response = await fetch(`/api/v1/admin/logs/${logId}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleApiError(response);
      return;
    }

    const result = await response.json();

    if (result.code === 1 && result.data) {
      displayLogDetail(result.data);
    }
  } catch (error) {
    console.error("Load log detail error:", error);
    alert("Xatolik: " + error.message);
  }
}

function displayLogDetail(log) {
  const content = document.getElementById("logDetailContent");

  content.innerHTML = `
        <div class="log-detail-section">
            <h4>Umumiy ma'lumot</h4>
            <div class="log-detail-grid">
                <div class="log-detail-item">
                    <label>Log ID</label>
                    <span>${log.logId}</span>
                </div>
                <div class="log-detail-item">
                    <label>Correlation ID</label>
                    <span style="font-family: monospace; font-size: 12px;">${log.correlationId || "-"}</span>
                </div>
                <div class="log-detail-item">
                    <label>Yo'nalish</label>
                    <span class="direction-badge ${log.direction.toLowerCase()}">${log.direction}</span>
                </div>
                <div class="log-detail-item">
                    <label>HTTP Status</label>
                    <span class="status-badge ${getStatusClass(log.httpStatus)}">${log.httpStatus || "-"}</span>
                </div>
                <div class="log-detail-item">
                    <label>Vaqt (ms)</label>
                    <span>${log.durationMs != null ? log.durationMs.toLocaleString() : "-"}</span>
                </div>
                <div class="log-detail-item">
                    <label>Tashqi tizim</label>
                    <span>${escapeHtml(log.externalSystem) || "-"}</span>
                </div>
                <div class="log-detail-item">
                    <label>Remote IP</label>
                    <span>${escapeHtml(log.remoteIp) || "-"}</span>
                </div>
                <div class="log-detail-item">
                    <label>User ID</label>
                    <span>${escapeHtml(log.userId) || "-"}</span>
                </div>
            </div>
        </div>

        <div class="log-detail-section">
            <h4>Vaqt</h4>
            <div class="log-detail-grid">
                <div class="log-detail-item">
                    <label>Boshlangan</label>
                    <span>${formatDateTimeFull(log.startedAt)}</span>
                </div>
                <div class="log-detail-item">
                    <label>Tugagan</label>
                    <span>${formatDateTimeFull(log.finishedAt)}</span>
                </div>
                <div class="log-detail-item">
                    <label>Yaratilgan</label>
                    <span>${formatDateTimeFull(log.createdAt)}</span>
                </div>
            </div>
        </div>

        <div class="log-detail-section">
            <h4>So'rov (Request)</h4>
            <div class="log-detail-grid">
                <div class="log-detail-item">
                    <label>Method</label>
                    <span><strong>${log.httpMethod || "-"}</strong></span>
                </div>
                <div class="log-detail-item">
                    <label>Endpoint</label>
                    <span style="word-break: break-all;">${escapeHtml(log.endpoint) || "-"}</span>
                </div>
            </div>
            ${
              log.queryParams
                ? `
                <div class="log-detail-item" style="margin-top: 12px;">
                    <label>Query Params</label>
                    <span style="word-break: break-all;">${escapeHtml(log.queryParams)}</span>
                </div>
            `
                : ""
            }
            ${
              log.requestBody
                ? `
                <div style="margin-top: 12px;">
                    <label style="font-size: 12px; color: #737373;">Request Body</label>
                    <pre class="log-body-content">${formatJson(log.requestBody)}</pre>
                </div>
            `
                : ""
            }
        </div>

        <div class="log-detail-section">
            <h4>Javob (Response)</h4>
            ${
              log.responseBody
                ? `
                <pre class="log-body-content">${formatJson(log.responseBody)}</pre>
            `
                : '<p style="color: #737373;">Mavjud emas</p>'
            }
        </div>

        ${
          log.errorMessage
            ? `
            <div class="log-detail-section">
                <h4 style="color: #dc2626;">Xato</h4>
                <pre class="log-body-content" style="background: #fee2e2; border-color: #fecaca;">${escapeHtml(log.errorMessage)}</pre>
            </div>
        `
            : ""
        }
    `;

  document.getElementById("logDetailModal").style.display = "block";
}

function closeLogDetailModal() {
  document.getElementById("logDetailModal").style.display = "none";
}

// Reset filters
function resetFilters() {
  document.getElementById("filterCorrelationId").value = "";
  document.getElementById("filterDirection").value = "";
  document.getElementById("filterEndpoint").value = "";
  document.getElementById("filterStatus").value = "";
  document.getElementById("filterExternalSystem").value = "";
  document.getElementById("filterStartDate").value = "";
  document.getElementById("filterEndDate").value = "";
  document.getElementById("filterErrorsOnly").checked = false;
  loadLogs();
}

// Utility functions
function formatDateTime(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);

  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();

  return `${hours}:${minutes}:${seconds} ${day}-${month}-${year}`;
}

function formatDateTimeFull(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("uz-UZ", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function getStatusClass(status) {
  if (!status) return "";
  if (status >= 200 && status < 300) return "success";
  if (status >= 400 && status < 500) return "warning";
  if (status >= 500) return "error";
  return "";
}

function escapeHtml(text) {
  if (!text) return "";
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

function truncate(str, maxLen) {
  if (!str) return "";
  return str.length > maxLen ? str.substring(0, maxLen) + "..." : str;
}

function formatJson(str) {
  if (!str) return "";
  try {
    const parsed = JSON.parse(str);
    return escapeHtml(JSON.stringify(parsed, null, 2));
  } catch {
    return escapeHtml(str);
  }
}

function logout() {
  if (confirm("Chiqishni xohlaysizmi?")) {
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("username");
    window.location.href = "/login.html";
  }
}

// Modal close handlers
window.onclick = function (event) {
  if (event.target.classList.contains("modal")) {
    event.target.style.display = "none";
  }
};

document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    document
      .querySelectorAll(".modal")
      .forEach((m) => (m.style.display = "none"));
  }
});

// Enter key to search
document.addEventListener("DOMContentLoaded", () => {
  // Project logs filters
  const filterInputs = document.querySelectorAll(
    ".filters-grid input, .filters-grid select",
  );
  filterInputs.forEach((input) => {
    input.addEventListener("keypress", (e) => {
      if (e.key === "Enter") {
        // Check if this is a system log filter or project log filter
        if (
          input.id === "systemLogStartDate" ||
          input.id === "systemLogEndDate" ||
          input.id === "systemLogSearchText"
        ) {
          loadSystemLogs();
        } else {
          loadLogs();
        }
      }
    });
  });
});

let currentLogSource = "project";

function reloadCurrentLogs() {
  if (currentLogSource === "project") {
    loadLogs(currentPage);
  } else {
    loadSystemLogs();
  }
}

async function loadSystemLogFiles() {
  const response = await fetch("/api/v1/admin/logs/system/files", {
    headers: getAuthHeaders(),
  });

  const result = await response.json();
  const select = document.getElementById("systemLogFileSelect");

  select.innerHTML = "";

  result.data.forEach((file) => {
    const option = document.createElement("option");
    option.value = file.path;
    option.textContent = file.path;
    select.appendChild(option);
  });

  loadSystemLogs();
}

async function loadSystemLogs() {
  const select = document.getElementById("systemLogFileSelect");
  if (!select.value) return;

  const file = select.value;
  const lines = document.getElementById("systemLogLines").value;
  const startDate = document.getElementById("systemLogStartDate").value;
  const endDate = document.getElementById("systemLogEndDate").value;
  const searchText = document.getElementById("systemLogSearchText").value.trim();

  // Show loading state
  document.getElementById("systemLogViewer").textContent = "Yuklanmoqda...";
  document.getElementById("systemLogResultValue").textContent = "0";

  const response = await fetch(
    `/api/v1/admin/logs/system/latest?lines=${lines}&file=${encodeURIComponent(file)}`,
    { headers: getAuthHeaders() },
  );

  const result = await response.json();
  let logs = result.data.logs;

  // Apply client-side filtering
  let filteredLogs = logs;

  // Filter by date range
  if (startDate || endDate) {
    filteredLogs = filterLogsByDateRange(filteredLogs, startDate, endDate);
  }

  // Filter by search text
  if (searchText) {
    filteredLogs = filterLogsByText(filteredLogs, searchText);
  }

  // Display filtered logs
  document.getElementById("systemLogViewer").textContent =
    filteredLogs.length > 0
      ? filteredLogs.join("\n")
      : "Hech qanday log topilmadi";

  // Update result count
  document.getElementById("systemLogResultValue").textContent =
    filteredLogs.length.toLocaleString();
}

// Filter logs by date range
function filterLogsByDateRange(logs, startDate, endDate) {
  if (!startDate && !endDate) return logs;

  const start = startDate ? new Date(startDate) : null;
  const end = endDate ? new Date(endDate) : null;

  return logs.filter((line) => {
    // Try to extract date from log line (common formats)
    // Format 1: ISO 8601 - 2024-01-09T10:30:45
    // Format 2: Standard - 2024-01-09 10:30:45
    // Format 3: DD-MM-YYYY HH:MM:SS
    const isoMatch = line.match(/(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})/);
    const standardMatch = line.match(/(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})/);
    const ddmmMatch = line.match(/(\d{2}[-./]\d{2}[-./]\d{4}\s+\d{2}:\d{2}:\d{2})/);

    let logDate = null;

    if (isoMatch) {
      logDate = new Date(isoMatch[1]);
    } else if (standardMatch) {
      logDate = new Date(standardMatch[1]);
    } else if (ddmmMatch) {
      // Convert DD-MM-YYYY to YYYY-MM-DD
      const parts = ddmmMatch[1].split(/[-./\s]/);
      if (parts.length >= 6) {
        logDate = new Date(
          `${parts[2]}-${parts[1]}-${parts[0]} ${parts[3]}:${parts[4]}:${parts[5]}`,
        );
      }
    }

    if (!logDate || isNaN(logDate.getTime())) {
      return true; // Keep lines without recognizable dates
    }

    if (start && logDate < start) return false;
    if (end && logDate > end) return false;

    return true;
  });
}

// Filter logs by text search (case-insensitive)
function filterLogsByText(logs, searchText) {
  if (!searchText) return logs;

  const searchLower = searchText.toLowerCase();

  return logs.filter((line) => {
    return line.toLowerCase().includes(searchLower);
  });
}

// Reset system log filters
function resetSystemLogFilters() {
  document.getElementById("systemLogStartDate").value = "";
  document.getElementById("systemLogEndDate").value = "";
  document.getElementById("systemLogSearchText").value = "";
  loadSystemLogs();
}

async function downloadSystemLog() {
  const file = document.getElementById("systemLogFileSelect").value;

  try {
    const response = await fetch(
      `/api/v1/admin/logs/system/download?file=${encodeURIComponent(file)}`,
      {
        headers: getAuthHeaders(),
      },
    );

    if (!response.ok) {
      handleApiError(response);
      throw new Error("Failed to download log file");
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = file;
    document.body.appendChild(a);
    a.click();

    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    alert("Yuklab olishda xatolik");
    console.error(error);
  }
}

function setLogSource(source) {
  if (currentLogSource === source) return;

  currentLogSource = source;

  // Toggle buttons
  document
    .getElementById("btnProjectLogs")
    .classList.toggle("active", source === "project");

  document
    .getElementById("btnSystemLogs")
    .classList.toggle("active", source === "system");

  // Sections
  document.getElementById("projectLogsSection").style.display =
    source === "project" ? "block" : "none";

  document.getElementById("systemLogsSection").style.display =
    source === "system" ? "block" : "none";

  // Load data
  if (source === "project") {
    loadLogs(0);
  } else {
    loadSystemLogFiles();
  }
}
