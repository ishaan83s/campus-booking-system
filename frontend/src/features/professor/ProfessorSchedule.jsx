import React, { useState, useEffect, useCallback } from "react";
import { getMySlots, cancelSlot, updateSlot, getSlotBookings } from "../../api/professor";
import { formatDate, formatTime } from "../../utils/formatters";
import { PageHeading } from "../../components/common/PageHeading";
import { Card, CardHeader, CardTitle, CardContent } from "../../components/ui/Card";
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "../../components/ui/Table";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "../../components/ui/Dialog";
import { Empty } from "../../components/ui/Empty";
import { Calendar, Users, Edit3, Trash2, Clock, CheckCircle } from "lucide-react";

export function ProfessorSchedule({ token, notify, onGoToCreate }) {
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [roster, setRoster] = useState(null);
  const [rosterLoading, setRosterLoading] = useState(false);
  const [editingSlot, setEditingSlot] = useState(null);
  const [editSaving, setEditSaving] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const refresh = useCallback(() => {
    setLoading(true);
    getMySlots(token)
      .then((data) => setSlots(data ?? []))
      .catch((e) => notify(e.message, "error"))
      .finally(() => setLoading(false));
  }, [token, notify]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function handleCancelSlot(slotId) {
    setActionLoading(true);
    try {
      await cancelSlot(token, slotId);
      notify("Office hour slot cancelled successfully.", "success");
      setConfirmDialog(null);
      refresh();
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleViewRoster(slotId) {
    setRosterLoading(true);
    try {
      const data = await getSlotBookings(token, slotId);
      setRoster(data);
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setRosterLoading(false);
    }
  }

  async function handleSaveEdit(e) {
    e.preventDefault();
    setEditSaving(true);
    try {
      await updateSlot(token, editingSlot.slotId, {
        slotDate: editingSlot.slotDate,
        startTime: editingSlot.startTime,
        endTime: editingSlot.endTime,
        capacity: Number(editingSlot.capacity),
      });
      notify("Slot updated successfully.", "success");
      setEditingSlot(null);
      refresh();
    } catch (err) {
      notify(err.message, "error");
    } finally {
      setEditSaving(false);
    }
  }

  return (
    <section>
      <PageHeading
        eyebrow="FACULTY WORKSPACE"
        title="Your office-hour schedule."
        subtitle="Review upcoming availability, inspect student rosters, and manage capacity windows."
        action={
          <Button variant="primary" onClick={onGoToCreate}>
            + Add New Slot
          </Button>
        }
      />

      <Card>
        <CardHeader>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <Calendar size={18} color="var(--primary)" aria-hidden="true" />
            <CardTitle>Upcoming & Scheduled Slots</CardTitle>
          </div>
          <Badge value={`${slots.length} Total`} variant="info" />
        </CardHeader>

        <CardContent>
          {loading ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--text-muted)" }}>
              Loading schedule…
            </div>
          ) : slots.length === 0 ? (
            <Empty
              icon={Calendar}
              title="No slots scheduled"
              text="You do not have any office hour windows configured yet."
              action={
                <Button variant="primary" size="sm" onClick={onGoToCreate}>
                  Create your first slot
                </Button>
              }
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Time Window</TableHead>
                  <TableHead>Capacity & Enrolled</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead style={{ textAlign: "right" }}>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {slots.map((slot) => {
                  const isCancelled = slot.status === "CANCELLED";
                  const utilization = Math.round((slot.bookedCount / slot.capacity) * 100);

                  return (
                    <TableRow key={slot.slotId}>
                      <TableCell>
                        <strong>{formatDate(slot.slotDate)}</strong>
                      </TableCell>
                      <TableCell>
                        <span className="tabular-nums">
                          {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                        </span>
                      </TableCell>
                      <TableCell>
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                          <span style={{ fontWeight: 600 }}>
                            {slot.bookedCount} / {slot.capacity}
                          </span>
                          <span className="muted" style={{ fontSize: 12 }}>
                            ({utilization}%)
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge value={slot.status} />
                      </TableCell>
                      <TableCell style={{ textAlign: "right" }}>
                        <div style={{ display: "inline-flex", gap: 8 }}>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleViewRoster(slot.slotId)}
                            title="View enrolled students and waitlist"
                          >
                            <Users size={15} aria-hidden="true" />
                            <span>Roster</span>
                          </Button>

                          {!isCancelled && (
                            <>
                              <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => setEditingSlot({ ...slot })}
                                title="Edit date, time or capacity"
                              >
                                <Edit3 size={14} aria-hidden="true" />
                                <span>Edit</span>
                              </Button>

                              <Button
                                variant="danger"
                                size="sm"
                                onClick={() =>
                                  setConfirmDialog({
                                    title: "Cancel this office hour slot?",
                                    message: `This will cancel your slot on ${formatDate(
                                      slot.slotDate
                                    )} (${formatTime(slot.startTime)} – ${formatTime(
                                      slot.endTime
                                    )}). All confirmed student reservations (${
                                      slot.bookedCount
                                    }) and waiting list entries for this slot will be cancelled.`,
                                    confirmText: "Yes, cancel slot",
                                    confirmVariant: "danger",
                                    onConfirm: () => handleCancelSlot(slot.slotId),
                                  })
                                }
                                title="Cancel slot"
                              >
                                <Trash2 size={14} aria-hidden="true" />
                                <span>Cancel</span>
                              </Button>
                            </>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Roster Modal Dialog */}
      <Dialog open={Boolean(roster)} onOpenChange={(open) => !open && setRoster(null)}>
        <DialogContent style={{ maxWidth: 560 }}>
          <DialogHeader>
            <DialogTitle>Slot Roster #{roster?.slotId}</DialogTitle>
            <DialogDescription>
              Enrolled students and waiting queue for this office hour window.
            </DialogDescription>
          </DialogHeader>

          <div style={{ display: "grid", gap: 20, margin: "16px 0" }}>
            <div>
              <h3 style={{ fontSize: 14, fontWeight: 600, color: "var(--text-secondary)", marginBottom: 8 }}>
                Confirmed Bookings ({roster?.bookings?.length ?? 0})
              </h3>
              {roster?.bookings?.length ? (
                <div style={{ display: "grid", gap: 6 }}>
                  {roster.bookings.map((item) => (
                    <div
                      key={item.bookingId}
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        padding: "8px 12px",
                        background: "var(--bg-surface-subtle)",
                        borderRadius: "var(--radius-md)",
                        border: "1px solid var(--border-subtle)",
                        fontSize: 14,
                      }}
                    >
                      <strong style={{ color: "var(--text-primary)" }}>{item.studentName}</strong>
                      <span className="muted" style={{ fontVariantNumeric: "tabular-nums" }}>
                        {item.rollNo || "No Roll #"}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="muted" style={{ fontSize: 13, margin: 0 }}>
                  No confirmed bookings for this slot.
                </p>
              )}
            </div>

            <div>
              <h3 style={{ fontSize: 14, fontWeight: 600, color: "var(--warning-text)", marginBottom: 8 }}>
                Waitlist Queue ({roster?.waitlist?.length ?? 0})
              </h3>
              {roster?.waitlist?.length ? (
                <div style={{ display: "grid", gap: 6 }}>
                  {roster.waitlist.map((item) => (
                    <div
                      key={item.waitlistId}
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        padding: "8px 12px",
                        background: "var(--warning-bg)",
                        borderRadius: "var(--radius-md)",
                        border: "1px solid var(--warning-border)",
                        fontSize: 14,
                      }}
                    >
                      <span style={{ color: "var(--warning-text)", fontWeight: 600 }}>
                        #{item.position} in line
                      </span>
                      <strong style={{ color: "var(--text-primary)" }}>{item.studentName}</strong>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="muted" style={{ fontSize: 13, margin: 0 }}>
                  No students currently waiting.
                </p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button variant="secondary" onClick={() => setRoster(null)}>
              Close Roster
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Slot Modal Dialog */}
      <Dialog open={Boolean(editingSlot)} onOpenChange={(open) => !open && setEditingSlot(null)}>
        <DialogContent style={{ maxWidth: 500 }}>
          <form onSubmit={handleSaveEdit}>
            <DialogHeader>
              <DialogTitle>Edit Slot #{editingSlot?.slotId}</DialogTitle>
              <DialogDescription>
                Modify the date, scheduled time window, or seat capacity.
              </DialogDescription>
            </DialogHeader>

            {editingSlot && (
              <div className="form-stack" style={{ margin: "16px 0" }}>
                <div className="field">
                  <label htmlFor="edit-date">Date</label>
                  <Input
                    id="edit-date"
                    type="date"
                    value={editingSlot.slotDate}
                    onChange={(e) =>
                      setEditingSlot({ ...editingSlot, slotDate: e.target.value })
                    }
                    required
                  />
                </div>

                <div className="two-column">
                  <div className="field">
                    <label htmlFor="edit-start">Start Time</label>
                    <Input
                      id="edit-start"
                      type="time"
                      value={editingSlot.startTime}
                      onChange={(e) =>
                        setEditingSlot({ ...editingSlot, startTime: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="edit-end">End Time</label>
                    <Input
                      id="edit-end"
                      type="time"
                      value={editingSlot.endTime}
                      onChange={(e) =>
                        setEditingSlot({ ...editingSlot, endTime: e.target.value })
                      }
                      required
                    />
                  </div>
                </div>

                <div className="field">
                  <label htmlFor="edit-capacity">Seat Capacity (Minimum 1)</label>
                  <Input
                    id="edit-capacity"
                    type="number"
                    min="1"
                    value={editingSlot.capacity}
                    onChange={(e) =>
                      setEditingSlot({ ...editingSlot, capacity: e.target.value })
                    }
                    required
                  />
                </div>
              </div>
            )}

            <DialogFooter>
              <Button
                type="button"
                variant="secondary"
                onClick={() => setEditingSlot(null)}
                disabled={editSaving}
              >
                Cancel
              </Button>
              <Button type="submit" variant="primary" loading={editSaving}>
                Save Changes
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Confirmation Dialog */}
      <ConfirmDialog
        isOpen={Boolean(confirmDialog)}
        title={confirmDialog?.title}
        message={confirmDialog?.message}
        confirmText={confirmDialog?.confirmText}
        confirmVariant={confirmDialog?.confirmVariant}
        loading={actionLoading}
        onCancel={() => setConfirmDialog(null)}
        onConfirm={confirmDialog?.onConfirm}
      />
    </section>
  );
}
