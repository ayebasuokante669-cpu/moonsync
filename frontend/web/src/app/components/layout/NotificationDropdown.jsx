import { useState, useRef, useEffect } from "react";
import { Check, AlertCircle, Shield, Clock, X } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { toast } from "sonner";
import { useNavigate } from "react-router";

const API_URL = import.meta.env.VITE_API_URL || "https://moonsync-production.up.railway.app";


export function NotificationDropdown({ isOpen, onClose, notifications, setNotifications }) {
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const unreadCount = notifications.filter((n) => !n.read).length;

  // ✅ FETCH FROM API (SAFE FALLBACK)
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const token = localStorage.getItem("token") || "test";

        const res = await fetch(`${API_URL}/admin/notifications`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!res.ok) throw new Error();

        const data = await res.json();

        // map backend → frontend format safely
        const formatted = data.map((n, i) => ({
          id: n.id || i,
          type: "info",
          title: n.title,
          message: n.message || n.info,
          time: "just now",
          read: false,
          actionable: false,
        }));

        setNotifications(formatted);
      } catch (err) {
        console.error("Notification fetch failed");
        setNotifications([]);
      }
    };

    fetchNotifications();
  }, [setNotifications]);

  // Auto cleanup
  useEffect(() => {
    const cleanOld = () => {
      setNotifications((current) =>
        current.filter((n) => !n.time.includes("day") && !n.time.includes("week"))
      );
    };

    cleanOld();
    const interval = setInterval(cleanOld, 1000 * 60 * 60);
    return () => clearInterval(interval);
  }, [setNotifications]);

  // Close on outside click
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        onClose();
      }
    }

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [isOpen, onClose]);

  const markAsRead = (id) => {
    setNotifications(notifications.map((n) =>
      n.id === id ? { ...n, read: true } : n
    ));
  };

  const markAllAsRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, read: true })));
    toast.success("All notifications marked as read");
  };

  const clearAllNotifications = () => {
    setNotifications([]);
    toast.success("All notifications cleared");
    onClose();
  };

  const dismissNotification = (e, id) => {
    e.stopPropagation();
    setNotifications(notifications.filter((n) => n.id !== id));
  };

  const handleAction = (notification) => {
    markAsRead(notification.id);
    onClose();
    if (notification.route) {
      navigate(notification.route);
    }
  };

  const handleViewAll = () => {
    onClose();
    navigate("/archive");
  };

  const getIcon = (type) => {
    switch (type) {
      case "alert":
        return <AlertCircle className="h-4 w-4 text-[var(--color-error)]" />;
      case "warning":
        return <Shield className="h-4 w-4 text-[var(--color-warning)]" />;
      case "success":
        return <Check className="h-4 w-4 text-[var(--color-success)]" />;
      default:
        return <AlertCircle className="h-4 w-4 text-[var(--color-primary)]" />;
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case "alert":
        return "bg-[var(--color-error-light)]";
      case "warning":
        return "bg-[var(--color-warning-light)]";
      case "success":
        return "bg-[var(--color-success-light)]";
      default:
        return "bg-[var(--color-primary-light)]";
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          ref={dropdownRef}
          className="absolute right-0 top-full mt-2 w-96 max-w-[calc(100vw-2rem)] rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-2xl z-50 overflow-hidden"
          initial={{ opacity: 0, y: -10, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: -10, scale: 0.95 }}
          transition={{ type: "spring", damping: 25, stiffness: 300 }}
        >

          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-[var(--color-border)] bg-[var(--color-secondary-bg)]">
            <div>
              <h3 className="font-semibold text-foreground">Notifications</h3>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-0.5">
                {unreadCount} unread
              </p>
            </div>
            {unreadCount > 0 && (
              <div className="flex items-center gap-3">
                <button onClick={markAllAsRead} className="text-xs font-medium text-[var(--color-primary)] hover:underline">
                  Mark all read
                </button>
                <button onClick={clearAllNotifications} className="text-xs font-medium text-[var(--color-muted-foreground)] hover:text-foreground hover:underline">
                  Clear all
                </button>
              </div>
            )}
          </div>

          {/* List */}
          <div className="max-h-[400px] overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="py-12 text-center">No notifications</div>
            ) : (
              notifications.map((notification) => (
                <div key={notification.id} className="p-3 border-b">
                  <div className="flex gap-3">
                    <div className={`w-8 h-8 flex items-center justify-center rounded ${getTypeColor(notification.type)}`}>
                      {getIcon(notification.type)}
                    </div>
                    <div>
                      <p className="text-sm font-medium">{notification.title}</p>
                      <p className="text-xs">{notification.message}</p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Footer */}
          <div className="p-3 border-t">
            <button onClick={handleViewAll} className="w-full text-xs text-[var(--color-primary)]">
              View All Notifications
            </button>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}