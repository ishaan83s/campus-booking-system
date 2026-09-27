import React from "react";
import { Inbox } from "lucide-react";

export function Empty({
  icon: Icon = Inbox,
  title = "No data found",
  text,
  action,
  className = "",
}) {
  return (
    <div className={`empty-box ${className}`.trim()}>
      <Icon size={36} aria-hidden="true" />
      <p style={{ fontWeight: 500, color: "var(--text-secondary)" }}>{text || title}</p>
      {action && <div style={{ marginTop: 12 }}>{action}</div>}
    </div>
  );
}
