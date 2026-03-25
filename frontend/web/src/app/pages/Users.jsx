import { useState, useEffect } from "react";
import { Search, MoreVertical, X, Ban, Clock, Key, Bell, ShieldCheck, UserCheck } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { ActionButton } from "../components/admin/ActionButton";
import { FilterBar } from "../components/admin/FilterBar";
import { MedicalPersonnelModal } from "../components/modals/MedicalPersonnelModal";
import { useNavigate } from "react-router";
import { motion } from "motion/react";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, DialogClose } from "../components/ui/dialog";

const API_URL = import.meta.env.VITE_API_URL || "https://moonsync-production.up.railway.app";

export function Users() {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [isMedicalModalOpen, setIsMedicalModalOpen] = useState(false);

  const [actionModal, setActionModal] = useState({ isOpen: false, type: null, user: null });
  const [notificationMessage, setNotificationMessage] = useState("");

  const [filters, setFilters] = useState({ status: "", role: "" });

  const navigate = useNavigate();
  const token = localStorage.getItem("token") || "test";

  // =========================
  // FETCH USERS
  // =========================
  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const res = await fetch(`${API_URL}/admin/users`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        const data = await res.json();

        const formatted = (Array.isArray(data) ? data : data.data || []).map((u) => ({
          id: u.id,
          name: u.name,
          email: u.email,
          status: u.status || "active",
          registered: u.created_at || "-",
          lastActive: u.last_active || "-",
          isMedical: u.is_medical || false,
          avatar: u.name?.[0] || "U",
        }));

        setUsers(formatted);
      } catch {
        console.error("Failed to fetch users");
      }
    };

    fetchUsers();
  }, []);

  // =========================
  // FILTER
  // =========================
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus = filters.status ? user.status === filters.status : true;
    const matchesRole = filters.role
      ? filters.role === "Medical Personnel"
        ? user.isMedical
        : !user.isMedical
      : true;

    return matchesSearch && matchesStatus && matchesRole;
  });

  // =========================
  // ACTIONS
  // =========================
  const executeAction = async () => {
    const { type, user } = actionModal;

    try {
      if (type === "notify") {
        if (!notificationMessage.trim()) return;

        await fetch(`${API_URL}/admin/notifications`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            title: "Admin Message",
            message: notificationMessage,
            user_id: user.id,
          }),
        });

        toast.success("Notification sent");
      }

      if (type === "warn") {
        if (!notificationMessage.trim()) return;

        await fetch(
          `${API_URL}/admin/users/${user.id}/warn?message=${encodeURIComponent(notificationMessage)}`,
          {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        toast.warning("User warned");
      }

      if (type === "reset") {
        await fetch(`${API_URL}/admin/users/${user.id}/reset-password`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        });

        toast.success("Password reset sent");
      }

      if (type === "suspend") {
        await fetch(`${API_URL}/admin/users/${user.id}/suspend`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        });

        toast.warning("User suspended");
      }

      if (type === "ban") {
        await fetch(`${API_URL}/admin/users/${user.id}/ban`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        });

        toast.error("User banned");
      }
    } catch {
      toast.error("Action failed");
    }

    setActionModal({ isOpen: false, type: null, user: null });
    setNotificationMessage("");
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between">
        <h1 className="text-2xl font-semibold">User Management</h1>
        <p>{users.length}</p>
      </div>

      <FilterBar
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        filters={[
          {
            label: "Status",
            value: filters.status,
            onChange: (val) => setFilters(f => ({ ...f, status: val })),
            options: ["active", "suspended", "banned"],
          },
        ]}
      />

      {filteredUsers.map((user) => (
        <div key={user.id} onClick={() => setSelectedUser(user)}>
          {user.name} - {user.email}
        </div>
      ))}

      {/* DRAWER */}
      {selectedUser && (
        <div>
          <ActionButton onClick={() => setActionModal({ isOpen: true, type: "notify", user: selectedUser })}>
            Notify
          </ActionButton>

          <ActionButton onClick={() => setActionModal({ isOpen: true, type: "warn", user: selectedUser })}>
            Warn User
          </ActionButton>

          <ActionButton onClick={() => setActionModal({ isOpen: true, type: "reset", user: selectedUser })}>
            Reset Password
          </ActionButton>

          <ActionButton onClick={() => setActionModal({ isOpen: true, type: "suspend", user: selectedUser })}>
            Suspend
          </ActionButton>

          <ActionButton onClick={() => setActionModal({ isOpen: true, type: "ban", user: selectedUser })}>
            Ban
          </ActionButton>
        </div>
      )}

      {/* MODAL */}
      <Dialog open={actionModal.isOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {actionModal.type === "notify" && "Send Notification"}
              {actionModal.type === "warn" && "Warn User"}
              {actionModal.type === "reset" && "Reset Password"}
              {actionModal.type === "suspend" && "Suspend Account"}
              {actionModal.type === "ban" && "Ban User"}
            </DialogTitle>

            <DialogDescription>
              {actionModal.user?.name}
            </DialogDescription>
          </DialogHeader>

          {(actionModal.type === "notify" || actionModal.type === "warn") && (
            <textarea
              value={notificationMessage}
              onChange={(e) => setNotificationMessage(e.target.value)}
              placeholder="Message..."
              className="w-full border p-2"
            />
          )}

          <DialogFooter>
            <DialogClose asChild>
              <ActionButton>Cancel</ActionButton>
            </DialogClose>
            <ActionButton onClick={executeAction}>
              Confirm
            </ActionButton>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}