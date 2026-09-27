import React, { useState, useEffect } from "react";
import { getStudentProfile, updateStudentProfile } from "../../api/student";
import { PageHeading } from "../../components/common/PageHeading";
import { Card, CardHeader, CardTitle, CardContent } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { User, CheckCircle2 } from "lucide-react";

export function StudentProfile({ token, user, notify }) {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ rollNo: "", yearOfStudy: "" });
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStudentProfile(token)
      .then((data) => {
        setProfile(data);
        setForm({
          rollNo: data.rollNo || "",
          yearOfStudy: data.yearOfStudy !== null && data.yearOfStudy !== undefined ? String(data.yearOfStudy) : "",
        });
      })
      .catch((e) => notify(e.message, "error"))
      .finally(() => setLoading(false));
  }, [token, notify]);

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    try {
      const result = await updateStudentProfile(token, {
        rollNo: form.rollNo,
        yearOfStudy: form.yearOfStudy ? Number(form.yearOfStudy) : null,
      });
      setProfile(result);
      notify("Profile updated successfully.", "success");
    } catch (err) {
      notify(err.message, "error");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="narrow">
      <PageHeading
        eyebrow="STUDENT PORTAL"
        title="Student profile."
        subtitle="Keep your institutional identifier and academic year accurate."
      />

      <Card>
        <CardHeader>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div className="avatar" style={{ width: 40, height: 40, fontSize: 16 }}>
              {profile?.fullName?.[0]?.toUpperCase() ?? "S"}
            </div>
            <div>
              <CardTitle>{profile?.fullName || "Student Account"}</CardTitle>
              <p className="muted" style={{ fontSize: 13, margin: 0 }}>
                {profile?.email || user?.email || ""}
              </p>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <form className="form-stack" onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="prof-rollNo">Roll / Student Number</label>
              <Input
                id="prof-rollNo"
                value={form.rollNo}
                onChange={(e) => setForm({ ...form, rollNo: e.target.value })}
                required
                maxLength={50}
                placeholder="e.g. CS-2024-001"
              />
            </div>

            <div className="field">
              <label htmlFor="prof-year">Year of Study (1 – 8)</label>
              <Input
                id="prof-year"
                type="number"
                min="1"
                max="8"
                value={form.yearOfStudy}
                onChange={(e) => setForm({ ...form, yearOfStudy: e.target.value })}
                placeholder="e.g. 3"
              />
              <p className="helper">Used by professors to contextualize degree progression.</p>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={saving}
              style={{ marginTop: 8 }}
            >
              Save Profile Changes
            </Button>
          </form>
        </CardContent>
      </Card>
    </section>
  );
}
