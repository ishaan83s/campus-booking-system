import React from "react";

export const Select = React.forwardRef(function Select(
  { className = "", children, ...props },
  ref
) {
  return (
    <select
      ref={ref}
      className={`select ${className}`.trim()}
      {...props}
    >
      {children}
    </select>
  );
});
