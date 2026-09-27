export function formatDate(value) {
  if (!value) return "-";
  try {
    const dateStr = String(value).slice(0, 10);
    return new Intl.DateTimeFormat("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
      year: "numeric",
    }).format(new Date(`${dateStr}T00:00:00`));
  } catch {
    return value;
  }
}

export function toLocalDateString(value) {
  if (!value) return "";
  const d = value instanceof Date ? value : new Date(value);
  if (isNaN(d.getTime())) return "";
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function formatTime(value) {
  if (!value) return "-";
  try {
    return new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      minute: "2-digit",
    }).format(new Date(`1970-01-01T${value}`));
  } catch {
    return value;
  }
}
