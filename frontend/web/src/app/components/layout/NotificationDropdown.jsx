import { useState, useRef, useEffect } from "react";
import { Check, AlertCircle, Shield, Clock, X } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { toast } from "sonner";
import { useNavigate } from "react-router";

export const mockNotifications = [
  {
    id: 1,
    type: "alert",
    title: "New Flagged Content",
    message: "Community post flagged for review by multiple users",
    time: "2 minutes ago",
    read: false,
    actionable: true,
    route: "/community",
  },
  {
    id: 2,
    type: "warning",
    title: "High Severity Log Entry",
    message: "Menstrual log flagged with severe pain indicators",
    time: "15 minutes ago",
    read: false,
    actionable: true,
    route: "/menstrual-logs",
  },
  {
    id: 3,
    type: "info",
    title: "New User Registration",
    message: "Dr. Sarah Johnson created an account",
    time: "1 hour ago",
    read: false,
    actionable: true,
    route: "/users",
  },
  {
    id: 4,
    type: "success",
    title: "Article Published",
    message: "Understanding Hormonal Changes article is now live",
    time: "2 hours ago",
    read: true,
    route: "/articles",
  },
  {
    id: 5,
    type: "info",
    title: "User Milestone Reached",
    message: "Total users surpassed 4,000 mark",
    time: "5 hours ago",
    read: true,
    route: "/users",
  },
];

export function NotificationDropdown({ isOpen, onClose, notifications, setNotifications }) {
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const unreadCount = notifications.filter((n) => !n.read).length;

  // Auto-cleanup notifications older than 24 hours
  useEffect(() => {
    const cleanOldNotifications = () => {
      // In a real app we would use timestamps, for this mock we will just
      // assume anything containing "day" or "week" ago in its string is > 24 hours
      // and filter them out.
      setNotifications((current) => 
        current.filter(n => !n.time.includes("day") && !n.time.includes("week"))
      );
    };
    
    cleanOldNotifications();
    
    // Set up an interval to check periodically (e.g. every hour in a real app)
    const interval = setInterval(cleanOldNotifications, 1000 * 60 * 60);
    return () => clearInterval(interval);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Close dropdown when clicking outside
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
                <button
                  onClick={markAllAsRead}
                  className="text-xs font-medium text-[var(--color-primary)] hover:underline"
                >
                  Mark all read
                </button>
                <button
                  onClick={clearAllNotifications}
                  className="text-xs font-medium text-[var(--color-muted-foreground)] hover:text-foreground hover:underline"
                >
                  Clear all
                </button>
              </div>
            )}
          </div>

          {/* Notifications List */}
          <div className="max-h-[400px] overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 px-6 text-center">
                <Check className="h-10 w-10 text-[var(--color-muted-foreground)] mb-3" />
                <p className="text-sm font-medium text-foreground mb-1">All caught up!</p>
                <p className="text-xs text-[var(--color-muted-foreground)]">
                  No new notifications
                </p>
              </div>
            ) : (
              <div className="divide-y divide-[var(--color-border)]">
                {notifications.map((notification) => (
                  <motion.div
                    key={notification.id}
                    className={`p-3 hover:bg-[var(--color-secondary-bg)] transition-colors cursor-pointer ${
                      !notification.read ? "bg-[var(--color-primary-light)]/20" : ""
                    }`}
                    onClick={() => notification.actionable && handleAction(notification)}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    whileHover={{ x: 2 }}
                  >
                    <div className="flex gap-3">
                      {/* Icon */}
                      <div className={`flex-shrink-0 w-8 h-8 rounded-lg ${getTypeColor(notification.type)} flex items-center justify-center`}>
                        {getIcon(notification.type)}
                      </div>

                      {/* Content */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2 mb-1">
                          <h4 className="text-sm font-medium text-foreground line-clamp-1">
                            {notification.title}
                          </h4>
                          <div className="flex items-center gap-2 flex-shrink-0">
                            {!notification.read && (
                              <span className="w-2 h-2 rounded-full bg-[var(--color-primary)] mt-1"></span>
                            )}
                            <button
                              onClick={(e) => dismissNotification(e, notification.id)}
                              className="text-[var(--color-muted-foreground)] hover:text-[var(--color-error)] transition-colors p-0.5 rounded-full hover:bg-[var(--color-muted)]"
                              title="Dismiss"
                            >
                              <X className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        </div>
                        <p className="text-xs text-[var(--color-muted-foreground)] mb-2 line-clamp-2">
                          {notification.message}
                        </p>
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-1 text-xs text-[var(--color-muted-foreground)]">
                            <Clock className="h-3 w-3" />
                            {notification.time}
                          </div>
                          {!notification.read && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                markAsRead(notification.id);
                              }}
                              className="text-xs font-medium text-[var(--color-primary)] hover:underline"
                            >
                              Mark read
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="p-3 border-t border-[var(--color-border)] bg-[var(--color-secondary-bg)]">
            <button
              onClick={handleViewAll}
              className="w-full px-3 py-2 rounded-lg text-xs font-medium text-[var(--color-primary)] hover:bg-[var(--color-primary-light)] transition-smooth"
            >
              View All Notifications
            </button>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
