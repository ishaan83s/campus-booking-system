import React, { useState, useEffect, useCallback } from "react";
import { getMyBookings, cancelBooking, leaveWaitlist } from "../../api/student";
import { formatDate, formatTime } from "../../utils/formatters";
import { PageHeading } from "../../components/common/PageHeading";
import { Card, CardHeader, CardTitle, CardContent } from "../../components/ui/Card";
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "../../components/ui/Table";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { Empty } from "../../components/ui/Empty";
import { Calendar, Clock, AlertTriangle, CheckCircle, XCircle } from "lucide-react";

export function StudentBookings({ token, notify }) {
  const [history, setHistory] = useState({ bookings: [], waitlistEntries: [] });
  const [loading, setLoading] = useState(true);
  const [confirmDialog, setConfirmDialog] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const refresh = useCallback(() => {
    setLoading(true);
    getMyBookings(token)
      .then((data) => setHistory(data))
      .catch((e) => notify(e.message, "error"))
      .finally(() => setLoading(false));
  }, [token, notify]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function handleCancelBooking(bookingId) {
    setActionLoading(true);
    try {
      await cancelBooking(token, bookingId);
      notify("Your booking has been cancelled.", "success");
      setConfirmDialog(null);
      refresh();
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleLeaveWaitlist(waitlistId) {
    setActionLoading(true);
    try {
      await leaveWaitlist(token, waitlistId);
      notify("You have been removed from the waitlist queue.", "success");
      setConfirmDialog(null);
      refresh();
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <section>
      <PageHeading
        eyebrow="STUDENT PORTAL"
        title="Your bookings & waitlists."
        subtitle="Manage upcoming appointments, view meeting history, and track queue positions."
      />

      <div className="two-column">
        {/* Confirmed Appointments & History */}
        <Card>
          <CardHeader>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <Calendar size={18} color="var(--primary)" aria-hidden="true" />
              <CardTitle>Confirmed & History</CardTitle>
            </div>
            <span className="badge badge-info">{history.bookings.length}</span>
          </CardHeader>

          <CardContent>
            {loading ? (
              <div style={{ padding: 24, textAlign: "center", color: "var(--text-muted)" }}>
                Loading bookings…
              </div>
            ) : history.bookings.length === 0 ? (
              <Empty
                icon={Calendar}
                title="No bookings yet"
                text="You have not scheduled any office hour sessions yet."
              />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Professor</TableHead>
                    <TableHead>When</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead style={{ textAlign: "right" }}>Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {history.bookings.map((booking) => (
                    <TableRow key={booking.bookingId}>
                      <TableCell>
                        <strong style={{ display: "block" }}>{booking.professorName}</strong>
                      </TableCell>
                      <TableCell>
                        <div>{formatDate(booking.slotDate)}</div>
                        <div className="muted" style={{ fontSize: 13 }}>
                          {formatTime(booking.startTime)} – {formatTime(booking.endTime)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge value={booking.status} />
                      </TableCell>
                      <TableCell style={{ textAlign: "right" }}>
                        {booking.status === "BOOKED" && (
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() =>
                              setConfirmDialog({
                                title: "Cancel this booking?",
                                message: `Are you sure you want to cancel your appointment with ${
                                  booking.professorName
                                } on ${formatDate(booking.slotDate)} (${formatTime(
                                  booking.startTime
                                )} – ${formatTime(
                                  booking.endTime
                                )})? If other students are waitlisted, your place will automatically be given to the next student in line.`,
                                confirmText: "Yes, cancel appointment",
                                confirmVariant: "danger",
                                onConfirm: () => handleCancelBooking(booking.bookingId),
                              })
                            }
                          >
                            Cancel
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        {/* Waitlist Queue */}
        <Card>
          <CardHeader>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <Clock size={18} color="var(--warning-text)" aria-hidden="true" />
              <CardTitle>Waitlist Queue</CardTitle>
            </div>
            <span className="badge badge-warning">{history.waitlistEntries.length}</span>
          </CardHeader>

          <CardContent>
            {loading ? (
              <div style={{ padding: 24, textAlign: "center", color: "var(--text-muted)" }}>
                Loading waitlist positions…
              </div>
            ) : history.waitlistEntries.length === 0 ? (
              <Empty
                icon={Clock}
                title="No active waitlists"
                text="You are not currently in line for any full slots."
              />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Slot</TableHead>
                    <TableHead>Position</TableHead>
                    <TableHead>Joined</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead style={{ textAlign: "right" }}>Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {history.waitlistEntries.map((entry) => (
                    <TableRow key={entry.waitlistId}>
                      <TableCell>
                        <strong>Slot #{entry.slotId}</strong>
                      </TableCell>
                      <TableCell>
                        <span style={{ fontWeight: 600, color: "var(--warning-text)" }}>
                          #{entry.position} in line
                        </span>
                      </TableCell>
                      <TableCell>{formatDate(entry.createdAt)}</TableCell>
                      <TableCell>
                        <Badge value={entry.status} />
                      </TableCell>
                      <TableCell style={{ textAlign: "right" }}>
                        {entry.status === "WAITING" && (
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() =>
                              setConfirmDialog({
                                title: "Leave waitlist queue?",
                                message: `This will forfeit your position (#${entry.position}) in line for Slot #${entry.slotId}. If you rejoin later, you will be placed at the end of the line.`,
                                confirmText: "Yes, leave waitlist",
                                confirmVariant: "danger",
                                onConfirm: () => handleLeaveWaitlist(entry.waitlistId),
                              })
                            }
                          >
                            Leave
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

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
