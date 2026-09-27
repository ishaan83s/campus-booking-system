import React, { useState, useEffect } from "react";
import { getAdminReports, forceCancelBooking } from "../../api/admin";
import { PageHeading } from "../../components/common/PageHeading";
import { Card, CardHeader, CardTitle, CardContent } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import {
  Users,
  GraduationCap,
  Briefcase,
  CalendarCheck,
  Clock,
  Percent,
  AlertTriangle,
} from "lucide-react";

export function AdminOverview({ token, notify }) {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [bookingId, setBookingId] = useState("");
  const [confirmDialog, setConfirmDialog] = useState(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    getAdminReports(token)
      .then((data) => setReport(data))
      .catch((e) => notify(e.message, "error"))
      .finally(() => setLoading(false));
  }, [token, notify]);

  async function executeForceCancel() {
    setCancelling(true);
    try {
      await forceCancelBooking(token, bookingId);
      notify(`Booking #${bookingId} was successfully force-cancelled.`, "success");
      setBookingId("");
      setConfirmDialog(null);
      // Refresh report
      getAdminReports(token).then(setReport).catch(() => {});
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setCancelling(false);
    }
  }

  function handleForceCancelSubmit(e) {
    e.preventDefault();
    if (!bookingId) return;

    setConfirmDialog({
      title: `Force-cancel Booking #${bookingId}?`,
      message: `This administrative override will immediately terminate Booking #${bookingId}, revoke the student's reservation, and promote the next eligible student from the waitlist queue.`,
      confirmText: "Yes, force cancel",
      confirmVariant: "danger",
      onConfirm: executeForceCancel,
    });
  }

  const statCards = report
    ? [
        { label: "Total Users", value: report.totalUsers, icon: Users, color: "var(--primary)" },
        { label: "Professors", value: report.totalProfessors, icon: Briefcase, color: "var(--accent)" },
        { label: "Students", value: report.totalStudents, icon: GraduationCap, color: "var(--info-text)" },
        { label: "Active Bookings", value: report.totalActiveBookings, icon: CalendarCheck, color: "var(--success-text)" },
        { label: "In Waitlist Queue", value: report.totalWaitlisted, icon: Clock, color: "var(--warning-text)" },
        {
          label: "Slot Utilization",
          value: `${report.slotUtilizationPercent.toFixed(1)}%`,
          icon: Percent,
          color: "var(--text-primary)",
        },
      ]
    : [];

  return (
    <section>
      <PageHeading
        eyebrow="ADMINISTRATION"
        title="System overview & metrics."
        subtitle="Platform-wide capacity utilization, user volume, and administrative overrides."
      />

      {loading ? (
        <div style={{ padding: 40, textAlign: "center", color: "var(--text-muted)" }}>
          Loading platform metrics…
        </div>
      ) : (
        <div className="stat-grid">
          {statCards.map(({ label, value, icon: Icon, color }) => (
            <div className="stat-card" key={label}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <span>{label}</span>
                <Icon size={18} color={color} aria-hidden="true" />
              </div>
              <strong style={{ color }}>{value}</strong>
            </div>
          ))}
        </div>
      )}

      {/* Force-cancel Booking Override Card */}
      <Card style={{ marginTop: 24 }}>
        <CardHeader>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <AlertTriangle size={18} color="var(--danger-text)" aria-hidden="true" />
            <CardTitle>Administrative Override: Force-Cancel Booking</CardTitle>
          </div>
        </CardHeader>

        <CardContent>
          <p className="muted" style={{ fontSize: 14, marginBottom: 16 }}>
            Directly cancel any active booking by its unique identifier. This triggers automatic waitlist promotion for any queued students.
          </p>

          <form
            onSubmit={handleForceCancelSubmit}
            className="admin-override-form"
          >
            <Input
              type="number"
              min="1"
              value={bookingId}
              onChange={(e) => setBookingId(e.target.value)}
              placeholder="Enter Booking ID (e.g. 42)"
              required
              aria-label="Target Booking ID"
            />
            <Button type="submit" variant="danger">
              Cancel Booking
            </Button>
          </form>
        </CardContent>
      </Card>

      <ConfirmDialog
        isOpen={Boolean(confirmDialog)}
        title={confirmDialog?.title}
        message={confirmDialog?.message}
        confirmText={confirmDialog?.confirmText}
        confirmVariant={confirmDialog?.confirmVariant}
        loading={cancelling}
        onCancel={() => setConfirmDialog(null)}
        onConfirm={confirmDialog?.onConfirm}
      />
    </section>
  );
}
