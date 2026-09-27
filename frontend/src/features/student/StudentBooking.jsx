import React, { useState, useEffect, useCallback, useRef, useMemo } from "react";
import { getProfessors, getProfessorSlots, bookSlot } from "../../api/student";
import { formatDate, formatTime, toLocalDateString } from "../../utils/formatters";
import { PageHeading } from "../../components/common/PageHeading";
import { Card } from "../../components/ui/Card";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Select } from "../../components/ui/Select";
import { Textarea } from "../../components/ui/Textarea";
import { Empty } from "../../components/ui/Empty";
import { DayPicker } from "react-day-picker";
import { Calendar, Clock, User, CheckCircle2, AlertCircle, Sparkles } from "lucide-react";

export function StudentBooking({ token, notify }) {
  const [professors, setProfessors] = useState([]);
  const [selectedProfessorId, setSelectedProfessorId] = useState("");
  const [slots, setSlots] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [reason, setReason] = useState("");
  const [bookingLoading, setBookingLoading] = useState(false);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const sideCardRef = useRef(null);

  // Load professors
  useEffect(() => {
    getProfessors(token, 0, 50)
      .then((page) => {
        const rows = page.content ?? [];
        setProfessors(rows);
        if (rows[0]?.professorId) {
          setSelectedProfessorId(String(rows[0].professorId));
        }
      })
      .catch((e) => notify(e.message, "error"));
  }, [token, notify]);

  // Load slots for selected professor
  const loadSlots = useCallback(() => {
    if (!selectedProfessorId) return;
    setLoadingSlots(true);
    getProfessorSlots(token, selectedProfessorId)
      .then((data) => {
        setSlots(data.slots ?? []);
      })
      .catch((e) => notify(e.message, "error"))
      .finally(() => setLoadingSlots(false));
  }, [selectedProfessorId, token, notify]);

  useEffect(() => {
    loadSlots();
    setSelectedSlot(null);
  }, [loadSlots]);

  // Extract set of dates that have slots
  const datesWithSlots = useMemo(() => {
    const dates = new Set();
    slots.forEach((s) => {
      if (s.slotDate) dates.add(s.slotDate.slice(0, 10));
    });
    return dates;
  }, [slots]);

  // Filter slots by selectedDate if one is picked
  const filteredSlots = useMemo(() => {
    if (!selectedDate) return slots;
    const dateStr = toLocalDateString(selectedDate);
    return slots.filter((s) => s.slotDate && s.slotDate.slice(0, 10) === dateStr);
  }, [slots, selectedDate]);

  const chosenProfessor = professors.find(
    (item) => String(item.professorId) === selectedProfessorId
  );

  function handleSelectSlot(slot) {
    setSelectedSlot(slot);
    if (typeof window !== "undefined" && window.innerWidth <= 900) {
      setTimeout(() => {
        sideCardRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
      }, 60);
    }
  }

  async function handleBook() {
    if (!selectedSlot) return;
    setBookingLoading(true);
    try {
      const result = await bookSlot(token, selectedSlot.slotId);
      if (result.bookingId) {
        notify("Booking confirmed! Your appointment is scheduled.", "success");
      } else {
        notify(`You have joined the waitlist at position #${result.position}.`, "success");
      }
      setSelectedSlot(null);
      setReason("");
      loadSlots();
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setBookingLoading(false);
    }
  }

  return (
    <section>
      <PageHeading
        eyebrow="STUDENT PORTAL"
        title="Book office hours."
        subtitle="Select your professor, check real-time availability, and reserve your session."
      />

      <div className="page-grid">
        <div>
          {/* Professor Selector */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="field">
              <label htmlFor="prof-select">
                <span style={{ display: "inline-flex", alignItems: "center", gap: 6 }}>
                  <User size={15} aria-hidden="true" />
                  Select Professor
                </span>
              </label>
              <Select
                id="prof-select"
                value={selectedProfessorId}
                onChange={(e) => {
                  setSelectedProfessorId(e.target.value);
                  setSelectedSlot(null);
                  setSelectedDate(null);
                }}
              >
                {professors.map((item) => (
                  <option key={item.professorId} value={item.professorId}>
                    {item.fullName} — {item.department}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          {/* Cal.com style Date Picker & Availability Filter */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
              <h2 style={{ fontSize: "1rem", display: "flex", alignItems: "center", gap: 6 }}>
                <Calendar size={16} color="var(--primary)" aria-hidden="true" />
                Filter by Date
              </h2>
              {selectedDate && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setSelectedDate(null)}
                >
                  Show all dates
                </Button>
              )}
            </div>

            <div style={{ display: "flex", justifyContent: "center", padding: "8px 0" }}>
              <DayPicker
                mode="single"
                selected={selectedDate}
                onSelect={(date) => {
                  setSelectedDate(date);
                  setSelectedSlot(null);
                }}
                modifiers={{
                  hasSlots: (date) => {
                    const str = toLocalDateString(date);
                    return datesWithSlots.has(str);
                  },
                }}
                modifiersStyles={{
                  hasSlots: { fontWeight: 700, textDecoration: "underline" },
                }}
              />
            </div>
          </div>

          {/* Slot List */}
          <div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
              <h2 style={{ fontSize: "1.1rem", margin: 0 }}>
                {selectedDate
                  ? `Available Slots on ${formatDate(toLocalDateString(selectedDate))}`
                  : "All Upcoming Slots"}
              </h2>
              <span className="muted" style={{ fontSize: 13 }}>
                {filteredSlots.length} {filteredSlots.length === 1 ? "window" : "windows"}
              </span>
            </div>

            {loadingSlots ? (
              <Card>
                <div style={{ padding: 24, textAlign: "center", color: "var(--text-muted)" }}>
                  Loading available time slots…
                </div>
              </Card>
            ) : filteredSlots.length === 0 ? (
              <Card>
                <Empty
                  icon={Clock}
                  title="No office hour slots available"
                  text={
                    selectedDate
                      ? "No slots scheduled for this particular date. Select another date on the calendar above."
                      : "This professor has no future office hours scheduled at this time."
                  }
                />
              </Card>
            ) : (
              <div className="slot-list">
                {filteredSlots.map((slot) => {
                  const isSelectable = slot.status === "OPEN" || slot.status === "FULL";
                  const isFull = slot.status === "FULL";
                  const isSelected = selectedSlot?.slotId === slot.slotId;
                  const spacesLeft = slot.capacity - slot.bookedCount;

                  return (
                    <button
                      key={slot.slotId}
                      type="button"
                      className={`slot-row ${isSelected ? "selected" : ""} ${
                        isFull ? "slot-row-full" : ""
                      }`}
                      disabled={!isSelectable}
                      onClick={() => handleSelectSlot(slot)}
                      aria-label={`${formatDate(slot.slotDate)} from ${formatTime(
                        slot.startTime
                      )} to ${formatTime(slot.endTime)}. ${
                        isFull ? "Full, waitlist available" : `${spacesLeft} spaces remaining`
                      }`}
                    >
                      <div className="slot-row-left">
                        <strong>{formatDate(slot.slotDate)}</strong>
                        <span>
                          {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                        </span>
                      </div>

                      <div className="slot-row-right">
                        <small>
                          {isFull ? (
                            <span style={{ color: "var(--warning-text)", fontWeight: 600 }}>
                              0 spaces left (Waitlist queue)
                            </span>
                          ) : (
                            `${spacesLeft} ${spacesLeft === 1 ? "space" : "spaces"} left`
                          )}
                        </small>
                        <Badge
                          value={isFull ? "FULL / WAITLIST" : slot.status}
                          variant={isFull ? "warning" : "success"}
                        />
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Selected Slot Side Panel */}
        <aside
          ref={sideCardRef}
          className={`side-card ${selectedSlot ? "side-card-active" : ""}`}
        >
          <span className="eyebrow" style={{ color: selectedSlot?.status === "FULL" ? "var(--warning-text)" : "var(--primary)" }}>
            {selectedSlot?.status === "FULL" ? "WAITLIST RESERVATION" : "SELECTED OFFICE HOUR"}
          </span>

          {selectedSlot ? (
            <div>
              <h2 style={{ fontSize: "1.35rem", margin: "8px 0 4px" }}>
                {formatDate(selectedSlot.slotDate)}
              </h2>
              <p className="slot-time" style={{ fontSize: 16, color: "var(--text-primary)", marginBottom: 4 }}>
                {formatTime(selectedSlot.startTime)} – {formatTime(selectedSlot.endTime)}
              </p>
              <p className="muted" style={{ marginBottom: 16 }}>
                with {chosenProfessor?.fullName} ({chosenProfessor?.department})
              </p>

              {selectedSlot.status === "FULL" ? (
                <div className="waitlist-banner">
                  <strong>This window is currently full.</strong>
                  <p style={{ margin: 0 }}>
                    Confirming will place you in the automated waitlist queue. If a confirmed student cancels, the system will automatically promote your position.
                  </p>
                </div>
              ) : (
                <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "10px 12px", background: "var(--success-bg)", border: "1px solid var(--success-border)", borderRadius: "var(--radius-md)", color: "var(--success-text)", fontSize: 13, marginBottom: 16 }}>
                  <CheckCircle2 size={16} aria-hidden="true" />
                  <span>Immediate confirmation available</span>
                </div>
              )}

              <div className="field" style={{ marginBottom: 16 }}>
                <label htmlFor="visit-reason">Reason for meeting (optional)</label>
                <Textarea
                  id="visit-reason"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="What topics or questions would you like to discuss?…"
                  maxLength={500}
                />
                <p className="helper">
                  Note: Stored in your active session notes.
                </p>
              </div>

              <Button
                variant={selectedSlot.status === "FULL" ? "waitlist" : "primary"}
                size="lg"
                className="btn-w100"
                onClick={handleBook}
                loading={bookingLoading}
              >
                {selectedSlot.status === "FULL" ? "Join Waitlist Queue" : "Confirm Appointment"}
              </Button>
            </div>
          ) : (
            <div style={{ padding: "20px 0" }}>
              <Empty
                icon={Sparkles}
                title="Select a slot"
                text="Choose an available office hour window on the left to review details and confirm your reservation."
              />
            </div>
          )}
        </aside>
      </div>
    </section>
  );
}
