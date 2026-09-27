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

export function formatDateShort(value) {
  if (!value) return "-";
  try {
    const dateStr = String(value).slice(0, 10);
    return new Intl.DateTimeFormat("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
    }).format(new Date(`${dateStr}T00:00:00`));
  } catch {
    return value;
  }
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
