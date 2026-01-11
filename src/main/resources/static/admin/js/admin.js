// Store all users for filtering
let allUsers = [];
let isEditMode = false;
let currentUsername = "";

// Helper functions
function getAuthHeaders() {
  const token = localStorage.getItem("jwt_token");
  return {
    "Content-Type": "application/json",
    Accept: "application/json",
    Authorization: `Bearer ${token}`,
  };
}

function handleApiError(response, defaultMessage) {
  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("username");
    window.location.href = "/login.html";
    return;
  }
  throw new Error(defaultMessage);
}

function setButtonLoading(buttonId, isLoading, originalText) {
  const button = document.getElementById(buttonId);
  if (isLoading) {
    button.disabled = true;
    button.dataset.originalText = button.innerHTML;
    button.innerHTML = '<span class="loading-spinner"></span>';
  } else {
    button.disabled = false;
    button.innerHTML = originalText || button.dataset.originalText;
  }
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

// Theme toggle
function toggleTheme() {
  const current = document.body.getAttribute("data-theme") || "light";
  const newTheme = current === "light" ? "dark" : "light";
  document.body.setAttribute("data-theme", newTheme);
  localStorage.setItem("theme", newTheme);
}

// Load users
async function loadUsers() {
  const refreshBtn = document.getElementById("refreshBtn");
  const refreshIcon = document.getElementById("refreshIcon");
  const originalHtml = refreshBtn.innerHTML;

  refreshIcon.classList.add("spinning-icon");
  refreshBtn.disabled = true;

  try {
    const response = await fetch("/api/v1/admin/users", {
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleApiError(response, "Foydalanuvchilarni yuklashda xatolik");
      return;
    }

    const result = await response.json();

    if (result.code === 1 && result.data) {
      allUsers = result.data;
      filterUsers(); // Apply current search filter

      refreshIcon.classList.remove("spinning-icon");
      refreshIcon.src = "/img/check.png";

      setTimeout(() => {
        refreshBtn.innerHTML = originalHtml;
        refreshBtn.disabled = false;
      }, 1000);
    } else {
      alert("Foydalanuvchilar topilmadi");
      refreshIcon.classList.remove("spinning-icon");
      refreshBtn.disabled = false;
    }
  } catch (error) {
    console.error("Load users error:", error);
    alert("Xatolik: " + error.message);
    refreshIcon.classList.remove("spinning-icon");
    refreshBtn.disabled = false;
  }
}

// Search/filter users
function filterUsers() {
  const searchTerm = document.getElementById("searchInput").value.toLowerCase();

  const filtered = allUsers.filter(
    (user) =>
      user.username.toLowerCase().includes(searchTerm) ||
      user.name.toLowerCase().includes(searchTerm) ||
      (user.addInfo && user.addInfo.toLowerCase().includes(searchTerm)),
  );

  displayUsers(filtered);
}

// Display users
function displayUsers(users) {
  const tbody = document.getElementById("usersBody");

  if (!users || users.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" class="empty-cell">
          <img src="/img/empty.png" alt="Empty" style="width: 48px; opacity: 0.3; margin-bottom: 8px;" />
          <p>Foydalanuvchilar topilmadi</p>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = users
    .map(
      (user) => `
        <tr>
            <td>${user.id}</td>
            <td><strong>${escapeHtml(user.username)}</strong></td>
            <td>${escapeHtml(user.name)}</td>
            <td class="text-muted">${escapeHtml(user.addInfo) || "-"}</td>
            <td class="actions-cell">
                <button onclick="showUserPermissions('${escapeHtml(user.username)}')" class="btn-icon" title="Huquqlar">
                    <img src="/img/permission.png" alt="Permissions" />
                </button>
                <button onclick="openEditModal(${user.id}, '${escapeHtml(user.username)}', '${escapeHtml(user.name)}', '${escapeHtml(user.addInfo || "")}')" class="btn-icon" title="Tahrirlash">
                    <img src="/img/edit.png" alt="Edit" />
                </button>
            </td>
        </tr>
    `,
    )
    .join("");
}

// Create user functions
function openCreateModal() {
  document.getElementById("createUserForm").reset();
  document.getElementById("createModal").style.display = "block";
  setTimeout(() => document.getElementById("newUsername").focus(), 100);
}

function closeCreateModal() {
  document.getElementById("createModal").style.display = "none";
}

document
  .getElementById("createUserForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();
    await createUser();
  });

async function createUser() {
  const username = document.getElementById("newUsername").value.trim();
  const name = document.getElementById("newName").value.trim();
  const password = document.getElementById("newPassword").value;
  const confirmPassword = document.getElementById("confirmPassword").value;
  const addInfo = document.getElementById("addInfo").value.trim();

  if (!username || !name || !password || !confirmPassword) {
    alert("Iltimos barcha majburiy maydonlarni to'ldiring");
    return;
  }

  if (password !== confirmPassword) {
    alert("Parollar mos kelmadi!");
    return;
  }

  if (password.length < 6) {
    alert("Parol kamida 6 ta belgidan iborat bo'lishi kerak");
    return;
  }

  setButtonLoading("createBtn", true);

  try {
    const checkResponse = await fetch(
      `/api/v1/admin/users/exists?username=${encodeURIComponent(username)}`,
      { headers: getAuthHeaders() },
    );

    if (checkResponse.ok) {
      const result = await checkResponse.json();
      if (result.code === 1 && result.data === true) {
        alert("Bu login allaqachon mavjud");
        setButtonLoading("createBtn", false);
        return;
      }
    }
  } catch (error) {
    console.error("Username check error:", error);
  }

  try {
    const response = await fetch("/api/v1/admin/users/register", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        username,
        name,
        password,
        confirmPassword,
        addInfo: addInfo || null,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      alert(errorData.message || "Foydalanuvchi yaratishda xatolik");
      setButtonLoading("createBtn", false);
      return;
    }

    const result = await response.json();

    if (result.code === 1) {
      alert(result.message || "Foydalanuvchi muvaffaqiyatli yaratildi");
      closeCreateModal();
      loadUsers();
    } else {
      alert(result.message || "Xatolik yuz berdi");
    }
  } catch (error) {
    console.error("Create user error:", error);
    alert("Xatolik: " + error.message);
  } finally {
    setButtonLoading("createBtn", false);
  }
}

// Edit user functions
function openEditModal(id, username, name, addInfo) {
  document.getElementById("editUserId").value = id;
  document.getElementById("editUsername").value = username;
  document.getElementById("editName").value = name;
  document.getElementById("editAddInfo").value = addInfo;
  document.getElementById("editModal").style.display = "block";
  setTimeout(() => document.getElementById("editUsername").focus(), 100);
}

function closeEditModal() {
  document.getElementById("editModal").style.display = "none";
}

document
  .getElementById("editUserForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();
    await editUser();
  });

async function editUser() {
  const id = document.getElementById("editUserId").value;
  const username = document.getElementById("editUsername").value.trim();
  const name = document.getElementById("editName").value.trim();
  const addInfo = document.getElementById("editAddInfo").value.trim();

  if (!username || !name) {
    alert("Iltimos barcha majburiy maydonlarni to'ldiring");
    return;
  }

  setButtonLoading("saveBtn", true);

  try {
    const response = await fetch("/api/v1/admin/users", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        id: parseInt(id),
        username,
        name,
        addInfo: addInfo || null,
      }),
    });

    if (!response.ok) {
      handleApiError(response, "Foydalanuvchini yangilashda xatolik");
      setButtonLoading("saveBtn", false);
      return;
    }

    const result = await response.json();

    if (result.code === 1) {
      alert("Foydalanuvchi muvaffaqiyatli yangilandi");
      closeEditModal();
      loadUsers();
    } else {
      alert(result.message || "Xatolik yuz berdi");
    }
  } catch (error) {
    console.error("Edit user error:", error);
    alert("Xatolik: " + error.message);
  } finally {
    setButtonLoading("saveBtn", false);
  }
}

function togglePasswordWithImage(fieldId, button) {
  const field = document.getElementById(fieldId);
  const img = button.querySelector("img");

  if (field.type === "password") {
    field.type = "text";
    img.src = "/img/eye-closed.png";
  } else {
    field.type = "password";
    img.src = "/img/eye-open.png";
  }
}

// Permissions functions (combined view/edit)
async function showUserPermissions(username) {
  currentUsername = username;
  isEditMode = false;

  try {
    const [userPermsResponse, allPermsResponse] = await Promise.all([
      fetch(`/api/v1/admin/users/permissions/${username}`, {
        headers: getAuthHeaders(),
      }),
      fetch("/api/v1/admin/permissions", {
        headers: getAuthHeaders(),
      }),
    ]);

    if (!userPermsResponse.ok || !allPermsResponse.ok) {
      throw new Error("Failed to load permissions");
    }

    const userPermsResult = await userPermsResponse.json();
    const allPermsResult = await allPermsResponse.json();

    if (userPermsResult.code === 1 && allPermsResult.code === 1) {
      displayPermissions(
        username,
        userPermsResult.data || [],
        allPermsResult.data || [],
      );
    }
  } catch (error) {
    console.error("Load permissions error:", error);
    alert("Xatolik: " + error.message);
  }
}

function displayPermissions(username, userPermissions, allPermissions) {
  const modal = document.getElementById("permissionsModal");
  document.getElementById("permissionsUsername").textContent = username;

  const container = document.getElementById("permissionsList");
  const userPermCodes = userPermissions.map((p) => p.code);
  const grouped = groupPermissionsByCategory(allPermissions);

  let html = "";

  for (const [category, perms] of Object.entries(grouped)) {
    const checkedCount = perms.filter((p) =>
      userPermCodes.includes(p.code),
    ).length;
    const totalCount = perms.length;

    html += `
      <div class="permission-group">
        <div class="category-header ${category.toLowerCase()}" onclick="toggleCategory('${category}')">
          <span class="category-icon"></span>
          <h4>${getCategoryLabel(category)}</h4>
          <span class="permission-count">${checkedCount}/${totalCount}</span>
          <span class="collapse-icon" id="collapse-${category}">▼</span>
        </div>
        <div class="permission-items collapsed" id="items-${category}">
        ${perms
          .map(
            (perm) => `
          <label class="permission-item">
            <input
              type="checkbox"
              value="${perm.code}"
              ${userPermCodes.includes(perm.code) ? "checked" : ""}
              data-id="${perm.id}"
              disabled
            />
            <div class="permission-info">
              <strong>${perm.name}</strong>
              ${perm.code.endsWith("_*") ? '<span class="wildcard-badge">⭐</span>' : ""}
              ${perm.addInfo ? `<p class="perm-desc">${perm.addInfo}</p>` : ""}
              ${perm.endPoints?.length ? `<code class="perm-endpoints">${perm.endPoints.join(", ")}</code>` : ""}
            </div>
          </label>
        `,
          )
          .join("")}
        </div>
      </div>
    `;
  }

  container.innerHTML = html;
  modal.style.display = "block";

  // Reset edit mode UI
  document.getElementById("editToggleBtn").innerHTML =
    '<img src="/img/edit.png" class="btn-icon-img" />';
  document.getElementById("savePermissionsBtn").style.display = "none";
}

function toggleCategory(category) {
  const itemsContainer = document.getElementById(`items-${category}`);
  const collapseIcon = document.getElementById(`collapse-${category}`);

  if (itemsContainer.classList.contains("collapsed")) {
    itemsContainer.classList.remove("collapsed");
    collapseIcon.textContent = "▲";
  } else {
    itemsContainer.classList.add("collapsed");
    collapseIcon.textContent = "▼";
  }
}

// Add function to expand all categories
function expandAllCategories() {
  document.querySelectorAll(".permission-items").forEach((items) => {
    items.classList.remove("collapsed");
  });
  document.querySelectorAll(".collapse-icon").forEach((icon) => {
    icon.textContent = "▲";
  });
}

// Add function to collapse all categories
function collapseAllCategories() {
  document.querySelectorAll(".permission-items").forEach((items) => {
    items.classList.add("collapsed");
  });
  document.querySelectorAll(".collapse-icon").forEach((icon) => {
    icon.textContent = "▼";
  });
}

function toggleEditMode() {
  isEditMode = !isEditMode;
  const checkboxes = document.querySelectorAll(
    '#permissionsList input[type="checkbox"]',
  );
  const toggleBtn = document.getElementById("editToggleBtn");
  const saveBtn = document.getElementById("savePermissionsBtn");

  checkboxes.forEach((cb) => (cb.disabled = !isEditMode));

  if (isEditMode) {
    toggleBtn.innerHTML = '<img src="/img/cancel.png" class="btn-icon-img" />';
    saveBtn.style.display = "block";
  } else {
    toggleBtn.innerHTML = '<img src="/img/edit.png" class="btn-icon-img" />';
    saveBtn.style.display = "none";
    // Reload to reset checkboxes
    showUserPermissions(currentUsername);
  }
}

async function saveUserPermissions() {
  const checkboxes = document.querySelectorAll(
    '#permissionsList input[type="checkbox"]:checked',
  );
  const selectedPermissions = Array.from(checkboxes).map((cb) => ({
    id: parseInt(cb.dataset.id),
    code: cb.value,
  }));

  setButtonLoading("savePermissionsBtn", true);

  try {
    const response = await fetch("/api/v1/admin/users/permissions", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        username: currentUsername,
        permissions: selectedPermissions,
      }),
    });

    if (!response.ok) {
      throw new Error("Failed to save permissions");
    }

    const result = await response.json();

    if (result.code === 1) {
      alert("Huquqlar muvaffaqiyatli saqlandi");
      isEditMode = false;
      showUserPermissions(currentUsername);
    } else {
      alert(result.message || "Xatolik yuz berdi");
    }
  } catch (error) {
    console.error("Save permissions error:", error);
    alert("Xatolik: " + error.message);
  } finally {
    setButtonLoading("savePermissionsBtn", false);
  }
}

function groupPermissionsByCategory(permissions) {
  const groups = {
    ADMIN: [],
    INTERNAL: [],
    EXTERNAL: [],
    OTHER: [],
  };

  permissions.forEach((perm) => {
    if (perm.code.startsWith("ADMIN_")) {
      groups.ADMIN.push(perm);
    } else if (perm.code.startsWith("INTERNAL_")) {
      groups.INTERNAL.push(perm);
    } else if (perm.code.startsWith("EXTERNAL_")) {
      groups.EXTERNAL.push(perm);
    } else {
      groups.OTHER.push(perm);
    }
  });

  Object.keys(groups).forEach((key) => {
    if (groups[key].length === 0) {
      delete groups[key];
    }
  });

  return groups;
}

function getCategoryLabel(category) {
  const labels = {
    ADMIN: "Admin huquqlari",
    INTERNAL: "Ichki huquqlar",
    EXTERNAL: "Tashqi huquqlar",
    OTHER: "Boshqa huquqlar",
  };
  return labels[category] || category;
}

function closePermissionsModal() {
  document.getElementById("permissionsModal").style.display = "none";
  isEditMode = false;
}

function logout() {
  if (confirm("Chiqishni xohlaysizmi?")) {
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("username");
    window.location.href = "/login.html";
  }
}

// Close modals
window.onclick = function (event) {
  if (event.target.classList.contains("modal")) {
    event.target.style.display = "none";
    if (event.target.id === "permissionsModal") {
      isEditMode = false;
    }
  }
};

document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    document
      .querySelectorAll(".modal")
      .forEach((m) => (m.style.display = "none"));
    isEditMode = false;
  }
});
