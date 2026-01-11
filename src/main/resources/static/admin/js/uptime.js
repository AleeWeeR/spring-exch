(function () {
  let serverStartTime = null;
  let uptimeIntervalId = null;

  async function fetchUptime() {
    const token = localStorage.getItem("jwt_token");
    if (!token) return;

    try {
      const response = await fetch(
        "/api/v1/admin/actuator/metrics/process.uptime",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
          },
        },
      );

      if (response.ok) {
        const data = await response.json();
        const uptimeSeconds = data.measurements[0].value;

        serverStartTime = new Date(Date.now() - uptimeSeconds * 1000);

        const startElement = document.getElementById("uptimeStart");
        if (startElement) {
          startElement.textContent = formatStartTime(serverStartTime);
          startElement.title = serverStartTime.toLocaleString("uz-UZ");
        }

        const indicator = document.getElementById("uptimeIndicator");
        const infoElement = document.querySelector(".uptime-info");

        if (indicator) {
          indicator.classList.remove("uptime-error");
        }

        // Show the uptime info when server is back
        if (infoElement) {
          infoElement.style.display = "flex";
        }

        if (!uptimeIntervalId) {
          updateUptimeDisplay();
          uptimeIntervalId = setInterval(updateUptimeDisplay, 1000);
        }
      } else {
        setErrorState();
      }
    } catch (error) {
      console.error("Failed to fetch uptime:", error);
      setErrorState();
    }
  }

  function setErrorState() {
    const indicator = document.getElementById("uptimeIndicator");
    const infoElement = document.querySelector(".uptime-info");

    if (indicator) {
      indicator.classList.add("uptime-error");
    }

    // Hide the uptime info when unavailable
    if (infoElement) {
      infoElement.style.display = "none";
    }
  }
  function updateUptimeDisplay() {
    if (!serverStartTime) return;

    const now = new Date();
    const diff = now - serverStartTime;

    const valueElement = document.getElementById("uptimeValue");
    if (valueElement) {
      valueElement.textContent = formatUptime(diff);
    }
  }


  function formatUptime(ms) {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) {
      return `${days}d ${hours % 24}h ${minutes % 60}m`;
    } else if (hours > 0) {
      return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }

  function formatStartTime(date) {
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    const timeStr = date.toLocaleTimeString("uz-UZ", {
      hour: "2-digit",
      minute: "2-digit",
    });

    if (isToday) {
      return `Bugun ${timeStr}`;
    }

    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) {
      return `Kecha ${timeStr}`;
    }

    return date.toLocaleDateString("uz-UZ", {
      day: "2-digit",
      month: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", fetchUptime);
  } else {
    fetchUptime();
  }

  setInterval(fetchUptime, 5 * 1000);
})();
