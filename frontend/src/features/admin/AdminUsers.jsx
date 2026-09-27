import React, { useState, useEffect, useCallback, useMemo } from "react";
import { getAdminUsers, toggleUserActive } from "../../api/admin";
import { PageHeading } from "../../components/common/PageHeading";
import { Card, CardHeader, CardTitle, CardContent } from "../../components/ui/Card";
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "../../components/ui/Table";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { Empty } from "../../components/ui/Empty";
import { Users, Search, ShieldCheck, ShieldAlert } from "lucide-react";

export function AdminUsers({ token, notify }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [confirmDialog, setConfirmDialog] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const refresh = useCallback(() => {
    setLoading(true);
    getAdminUsers(token)
      .then((data) => setUsers(data ?? []))
      .catch((e) => notify(e.message, "error"))
      .finally(() => setLoading(false));
  }, [token, notify]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function handleToggleUser(user, action) {
    setActionLoading(true);
    try {
      await toggleUserActive(token, user.id, action);
      notify(`User ${user.fullName} has been ${action}d.`, "success");
      setConfirmDialog(null);
      refresh();
    } catch (e) {
      notify(e.message, "error");
    } finally {
      setActionLoading(false);
    }
  }

  function handleActionClick(user) {
    if (user.isActive) {
      setConfirmDialog({
        title: `Deactivate ${user.fullName}?`,
        message: `This will suspend access for ${user.fullName} (${user.email}). They will immediately be prevented from signing in, booking slots, or managing office hours until reactivated.`,
        confirmText: "Yes, deactivate account",
        confirmVariant: "danger",
        onConfirm: () => handleToggleUser(user, "deactivate"),
      });
    } else {
      handleToggleUser(user, "activate");
    }
  }

  const filteredUsers = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return users.filter((u) => {
      const matchesSearch =
        !q ||
        (u.fullName || "").toLowerCase().includes(q) ||
        (u.email || "").toLowerCase().includes(q);
      const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [users, searchQuery, roleFilter]);

  return (
    <section>
      <PageHeading
        eyebrow="ADMINISTRATION"
        title="User access management."
        subtitle="Review registered university faculty and students, inspect roles, and manage access."
      />

      <Card>
        <CardHeader>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <Users size={18} color="var(--primary)" aria-hidden="true" />
            <CardTitle>Registered Accounts</CardTitle>
          </div>
          <span className="badge badge-info">{users.length} Total</span>
        </CardHeader>

        <CardContent>
          {/* Search and Filters */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "minmax(0, 1fr) 180px",
              gap: 12,
              marginBottom: 16,
            }}
          >
            <div style={{ position: "relative" }}>
              <Input
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search users by name or email…"
                aria-label="Search users"
              />
            </div>

            <Select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              aria-label="Filter by role"
            >
              <option value="ALL">All Roles</option>
              <option value="STUDENT">Students</option>
              <option value="PROFESSOR">Professors</option>
              <option value="ADMIN">Admins</option>
            </Select>
          </div>

          {loading ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--text-muted)" }}>
              Loading user accounts…
            </div>
          ) : filteredUsers.length === 0 ? (
            <Empty
              icon={Users}
              title="No users found"
              text={searchQuery ? "No matching accounts for your search criteria." : "No registered users in the database."}
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Access Status</TableHead>
                  <TableHead style={{ textAlign: "right" }}>Account Action</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredUsers.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <strong style={{ display: "block" }}>{user.fullName}</strong>
                      <span className="muted" style={{ fontSize: 13 }}>
                        {user.email}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge value={user.role} />
                    </TableCell>
                    <TableCell>
                      <Badge
                        value={user.isActive ? "ACTIVE" : "INACTIVE"}
                        variant={user.isActive ? "success" : "danger"}
                      />
                    </TableCell>
                    <TableCell style={{ textAlign: "right" }}>
                      <Button
                        variant={user.isActive ? "danger" : "secondary"}
                        size="sm"
                        onClick={() => handleActionClick(user)}
                        disabled={actionLoading}
                      >
                        {user.isActive ? "Deactivate" : "Activate"}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

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
