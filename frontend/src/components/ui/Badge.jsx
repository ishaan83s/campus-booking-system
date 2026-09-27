import React from "react";

export function Badge({ value, variant, className = "" }) {
  if (!value && !variant) return null;

  let computedVariant = variant;
  if (!computedVariant) {
    const str = String(value).toUpperCase();
    if (["OPEN", "AVAILABLE", "CONFIRMED", "BOOKED", "ACTIVE"].includes(str)) {
      computedVariant = "success";
    } else if (["FULL", "WAITLIST", "FULL / WAITLIST", "WAITING", "PENDING"].includes(str)) {
      computedVariant = "warning";
    } else if (["CANCELLED", "INACTIVE", "DEACTIVATED"].includes(str)) {
      computedVariant = "danger";
    } else if (["STUDENT", "PROFESSOR", "ADMIN"].includes(str)) {
      computedVariant = "accent";
    } else {
      computedVariant = "info";
    }
  }

  return (
    <span className={`badge badge-${computedVariant} ${className}`.trim()}>
      {value}
    </span>
  );
}
