import { useState, useEffect } from "react";
import { Send, Users, Calendar, Smartphone, Trash } from "lucide-react";
import { ActionButton } from "../components/admin/ActionButton";

const API_URL = import.meta.env.VITE_API_URL || "https://moonsync-production.up.railway.app";

export function Notifications() {
  const [notificationData, setNotificationData] = useState({
    title: "",
    message: "",
    segment: "all",
    scheduleDate: "",
    scheduleTime: "",
  });

  const [recentNotifications, setRecentNotifications] = useState([]);
  const [loading, setLoading] = useState(false);

  const token = localStorage.getItem("token") || "test";

  // =========================
  // GET
  // =========================
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await fetch(`${API_URL}/admin/notifications`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) throw new Error();

        const data = await res.json();

        const safe = Array.isArray(data) ? data : data.data || [];

        setRecentNotifications(safe);
      } catch {
        setRecentNotifications([]);
      }
    };

    fetchNotifications();
  }, []);

  // =========================
  // POST (FIXED → QUERY PAYLOAD)
  // =========================
  const handleSend = async () => {
    try {
      setLoading(true);

      const payload = {
        title: notificationData.title,
        message: notificationData.message,
        segment: notificationData.segment,
        schedule_date: notificationData.scheduleDate || null,
        schedule_time: notificationData.scheduleTime || null,
      };

      const res = await fetch(`${API_URL}/admin/notifications`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify(payload),
      });

      if (!res.ok) throw new Error();

      const newNotif = await res.json();

      setRecentNotifications((prev) => [newNotif, ...prev]);

      setNotificationData({
        title: "",
        message: "",
        segment: "all",
        scheduleDate: "",
        scheduleTime: "",
      });

    } catch {
      console.error("Send failed");
    } finally {
      setLoading(false);
    }
  };

  // =========================
  // DELETE
  // =========================
  const handleDelete = async (id) => {
    try {
      await fetch(`${API_URL}/admin/notifications/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      setRecentNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch {
      console.error("Delete failed");
    }
  };

  return (
    <div className="space-y-6">

      <div>
        <h1 className="text-2xl font-semibold text-foreground">Notifications Manager</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* FORM */}
        <div className="lg:col-span-2 rounded-xl border bg-[var(--color-card)] p-6">
          <div className="space-y-5">

            <input
              placeholder="Title"
              value={notificationData.title}
              onChange={(e) => setNotificationData({ ...notificationData, title: e.target.value })}
              className="w-full p-3 border rounded-lg"
            />

            <textarea
              placeholder="Message"
              value={notificationData.message}
              onChange={(e) => setNotificationData({ ...notificationData, message: e.target.value })}
              className="w-full p-3 border rounded-lg"
            />

            <select
              value={notificationData.segment}
              onChange={(e) => setNotificationData({ ...notificationData, segment: e.target.value })}
              className="w-full p-3 border rounded-lg"
            >
              <option value="all">All Users</option>
              <option value="active">Active Users</option>
            </select>

            <div className="grid grid-cols-2 gap-4">
              <input
                type="date"
                value={notificationData.scheduleDate}
                onChange={(e) => setNotificationData({ ...notificationData, scheduleDate: e.target.value })}
                className="p-3 border rounded-lg"
              />
              <input
                type="time"
                value={notificationData.scheduleTime}
                onChange={(e) => setNotificationData({ ...notificationData, scheduleTime: e.target.value })}
                className="p-3 border rounded-lg"
              />
            </div>

            <ActionButton onClick={handleSend} icon={Send}>
              {loading ? "Sending..." : "Send"}
            </ActionButton>
          </div>
        </div>

        {/* PREVIEW */}
        <div className="border rounded-xl p-6">
          <h3>Preview</h3>
          <p>{notificationData.title || "Title"}</p>
          <p>{notificationData.message || "Message..."}</p>
        </div>
      </div>

      {/* LIST */}
      <div className="border rounded-xl p-6">
        <h3>Recent Notifications</h3>

        {!Array.isArray(recentNotifications) || recentNotifications.length === 0 ? (
          <p>No notifications</p>
        ) : (
          recentNotifications.map((n, i) => (
            <div key={n.id || i} className="p-3 border rounded-lg mb-2 flex justify-between">
              <div>
                <p>{n.title}</p>
                <p>{n.message || n.info}</p>
              </div>
              <button onClick={() => handleDelete(n.id)}>
                <Trash size={16} />
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}