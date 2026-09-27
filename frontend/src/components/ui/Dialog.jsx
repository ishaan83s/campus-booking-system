import React from "react";
import * as DialogPrimitive from "@radix-ui/react-dialog";
import { X } from "lucide-react";

export const Dialog = DialogPrimitive.Root;
export const DialogTrigger = DialogPrimitive.Trigger;
export const DialogPortal = DialogPrimitive.Portal;
export const DialogClose = DialogPrimitive.Close;

export function DialogOverlay({ className = "", ...props }) {
  return (
    <DialogPrimitive.Overlay
      className={`dialog-overlay ${className}`.trim()}
      {...props}
    />
  );
}

export function DialogContent({ className = "", children, ...props }) {
  return (
    <DialogPortal>
      <DialogOverlay />
      <DialogPrimitive.Content
        className={`dialog-content ${className}`.trim()}
        {...props}
      >
        {children}
        <DialogPrimitive.Close
          className="btn btn-ghost btn-sm"
          style={{ position: "absolute", top: 16, right: 16, padding: 6, minHeight: "auto" }}
          aria-label="Close dialog"
        >
          <X size={16} />
        </DialogPrimitive.Close>
      </DialogPrimitive.Content>
    </DialogPortal>
  );
}

export function DialogHeader({ className = "", children, ...props }) {
  return (
    <div className={`dialog-header ${className}`.trim()} {...props}>
      {children}
    </div>
  );
}

export function DialogTitle({ className = "", children, ...props }) {
  return (
    <DialogPrimitive.Title
      className={`dialog-title ${className}`.trim()}
      {...props}
    >
      {children}
    </DialogPrimitive.Title>
  );
}

export function DialogDescription({ className = "", children, ...props }) {
  return (
    <DialogPrimitive.Description
      className={`dialog-description ${className}`.trim()}
      {...props}
    >
      {children}
    </DialogPrimitive.Description>
  );
}

export function DialogFooter({ className = "", children, ...props }) {
  return (
    <div className={`dialog-footer ${className}`.trim()} {...props}>
      {children}
    </div>
  );
}
