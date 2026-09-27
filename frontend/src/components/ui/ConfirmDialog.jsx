import React from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "./Dialog";
import { Button } from "./Button";
import { AlertTriangle } from "lucide-react";

export function ConfirmDialog({
  isOpen,
  title,
  message,
  confirmText = "Confirm",
  confirmVariant = "danger",
  loading = false,
  onConfirm,
  onCancel,
}) {
  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onCancel()}>
      <DialogContent>
        <DialogHeader>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            {confirmVariant === "danger" && (
              <AlertTriangle size={22} color="var(--danger-text)" aria-hidden="true" style={{ flexShrink: 0 }} />
            )}
            <DialogTitle>{title}</DialogTitle>
          </div>
          <DialogDescription style={{ marginTop: 8 }}>
            {message}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button
            type="button"
            variant="secondary"
            onClick={onCancel}
            disabled={loading}
          >
            Never mind
          </Button>
          <Button
            type="button"
            variant={confirmVariant}
            onClick={onConfirm}
            loading={loading}
          >
            {confirmText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
