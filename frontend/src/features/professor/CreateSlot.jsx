import React, { useState } from "react";
import { createSlot } from "../../api/professor";
import { PageHeading } from "../../components/common/PageHeading";
import { Card, CardHeader, CardTitle, CardContent } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Calendar, Clock, Users, ArrowLeft } from "lucide-react";

export function CreateSlot({ token, notify, onSlotCreated, onCancel }) {
  const [form, setForm] = useState({
    slotDate: "",
    startTime: "",
    endTime: "",
    capacity: 1,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const update = (key) => (e) => {
    setError("");
    setForm({ ...form, [key]: e.target.value });
  };

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (form.startTime && form.endTime && form.startTime >= form.endTime) {
      setError("End time must be strictly after start time.");
      return;
    }

    setSaving(true);
    try {
      await createSlot(token, {
        ...form,
        capacity: Number(form.capacity),
      });
      notify("New office hour slot created successfully.", "success");
      setForm({ slotDate: "", startTime: "", endTime: "", capacity: 1 });
      onSlotCreated?.();
    } catch (err) {
      setError(err.message);
      notify(err.message, "error");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="narrow">
      <PageHeading
        eyebrow="FACULTY WORKSPACE"
        title="Add a slot."
        subtitle="Schedule a future office-hour window for student bookings."
        action={
          onCancel && (
            <Button variant="ghost" size="sm" onClick={onCancel}>
              <ArrowLeft size={16} aria-hidden="true" />
              Back to schedule
            </Button>
          )
        }
      />

      <Card>
        <CardHeader>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <Calendar size={18} color="var(--primary)" aria-hidden="true" />
            <CardTitle>Slot Details</CardTitle>
          </div>
        </CardHeader>

        <CardContent>
          <form className="form-stack" onSubmit={handleSubmit}>
            {error && (
              <div className="form-error" id="create-slot-error" role="alert">
                <span>{error}</span>
              </div>
            )}

            <div className="field">
              <label htmlFor="create-slotDate">Date</label>
              <Input
                id="create-slotDate"
                type="date"
                value={form.slotDate}
                onChange={update("slotDate")}
                required
                aria-invalid={Boolean(error)}
                aria-describedby={error ? "create-slot-error" : undefined}
              />
            </div>

            <div className="two-column">
              <div className="field">
                <label htmlFor="create-startTime">Start Time</label>
                <Input
                  id="create-startTime"
                  type="time"
                  value={form.startTime}
                  onChange={update("startTime")}
                  required
                  aria-invalid={Boolean(error)}
                  aria-describedby={error ? "create-slot-error" : undefined}
                />
              </div>

              <div className="field">
                <label htmlFor="create-endTime">End Time</label>
                <Input
                  id="create-endTime"
                  type="time"
                  value={form.endTime}
                  onChange={update("endTime")}
                  required
                  aria-invalid={Boolean(error)}
                  aria-describedby={error ? "create-slot-error" : undefined}
                />
              </div>
            </div>

            <div className="field">
              <label htmlFor="create-capacity">Capacity (Seats / Student Limit)</label>
              <Input
                id="create-capacity"
                type="number"
                min="1"
                value={form.capacity}
                onChange={update("capacity")}
                required
                aria-invalid={Boolean(error)}
                aria-describedby={error ? "create-slot-error" : undefined}
              />
              <p className="helper">
                Once booked to capacity, additional students will join the waitlist queue.
              </p>
            </div>

            <div className="button-row" style={{ marginTop: 12 }}>
              <Button type="submit" variant="primary" size="lg" loading={saving}>
                Create Office Hour Window
              </Button>
              {onCancel && (
                <Button type="button" variant="secondary" onClick={onCancel} disabled={saving}>
                  Cancel
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>
    </section>
  );
}
