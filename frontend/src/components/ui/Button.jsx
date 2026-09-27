import React from "react";
import { Slot } from "@radix-ui/react-slot";
import { Loader2 } from "lucide-react";

export const Button = React.forwardRef(function Button(
  {
    className = "",
    variant = "primary",
    size = "md",
    asChild = false,
    loading = false,
    disabled = false,
    children,
    ...props
  },
  ref
) {
  const Comp = asChild ? Slot : "button";
  const variantClass = `btn-${variant}`;
  const sizeClass = size !== "md" ? `btn-${size}` : "";

  return (
    <Comp
      ref={ref}
      className={`btn ${variantClass} ${sizeClass} ${className}`.trim()}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <>
          <Loader2 className="animate-spin" size={16} aria-hidden="true" />
          <span>{children}</span>
        </>
      ) : (
        children
      )}
    </Comp>
  );
});
