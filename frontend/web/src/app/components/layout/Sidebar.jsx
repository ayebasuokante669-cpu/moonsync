import { Link, useLocation } from "react-router";
import {
  LayoutDashboard,
  Users,
  MessageSquare,
  MessageSquareWarning,
  FileText,
  BookOpen,
  Bell,
  BarChart3,
  Settings,
  Info,
  ChevronLeft,
  ChevronRight,
  X,
  UserCircle,
  ShieldCheck,
  Archive
} from "lucide-react";
import logoImg from "../../../assets/moonsync-logo.png";

const navItems = [
  { path: "/", label: "Dashboard", icon: LayoutDashboard },
  { path: "/users", label: "Users", icon: Users },
  { path: "/community", label: "Community Moderation", icon: MessageSquare },
  { path: "/chatbot-reports", label: "Chatbot Reports", icon: MessageSquareWarning },
  { path: "/articles", label: "Articles", icon: FileText },
  { path: "/medical-verification", label: "Medical Verification", icon: ShieldCheck },
  { path: "/menstrual-logs", label: "Menstrual Logs", icon: BookOpen },
  { path: "/notifications", label: "Notifications", icon: Bell },
  { path: "/analytics", label: "Analytics", icon: BarChart3 },
  { path: "/archive", label: "Moon Archive", icon: Archive },
  { path: "/system-info", label: "System Info", icon: Info },
];

export function Sidebar({ collapsed, onToggle }) {
  const location = useLocation();

  return (
    <>
      {/* Mobile Overlay */}
      {!collapsed && (
        <div
          className="fixed inset-0 z-40 bg-black/30 lg:hidden"
          onClick={onToggle}
        />
      )}

      <aside
        className={`fixed lg:relative z-50 flex h-full flex-col border-r border-[var(--color-sidebar-border)] bg-[var(--color-sidebar)] transition-smooth overflow-hidden ${collapsed ? "-translate-x-full lg:translate-x-0 w-0 lg:w-20" : "translate-x-0 w-64"
          }`}
      >
        {/* Logo */}
        <div className="flex h-16 items-center justify-between border-b border-[var(--color-sidebar-border)] px-6 min-w-max">
          {!collapsed && (
            <div className="flex items-center gap-2">
              <img
                src={logoImg}
                alt="MoonSync Logo"
                className="h-8 w-8 object-contain rounded-lg"
              />
              <span className="text-lg font-semibold text-[var(--color-sidebar-foreground)]">MoonSync</span>
            </div>
          )}
          {collapsed && (
            <div className="mx-auto flex justify-center w-full">
              <img
                src={logoImg}
                alt="MoonSync Logo"
                className="h-8 w-8 object-contain rounded-lg"
              />
            </div>
          )}

          {/* Mobile Close Button */}
          {!collapsed && (
            <button
              onClick={onToggle}
              className="lg:hidden rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
            >
              <X className="h-5 w-5 text-[var(--color-muted-foreground)]" />
            </button>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 overflow-x-hidden min-w-max">
          <ul className="space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;

              return (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    onClick={() => {
                      if (window.innerWidth < 1024 && !collapsed) {
                        onToggle();
                      }
                    }}
                    className={`group flex items-center gap-3 rounded-lg px-3 py-2.5 transition-smooth border-none outline-none ring-0 ${isActive
                        ? "bg-[var(--color-primary)]/10 text-[var(--color-primary)]"
                        : "text-[var(--color-muted-foreground)] hover:bg-[var(--color-primary)]/5 hover:text-[var(--color-foreground)]"
                      }`}
                  >
                    <Icon className={`h-5 w-5 flex-shrink-0 ${isActive ? "text-[var(--color-primary)]" : ""}`} />
                    {!collapsed && (
                      <span className="text-sm font-medium">{item.label}</span>
                    )}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Toggle Button */}
        <button
          onClick={onToggle}
          className="m-3 flex items-center justify-center rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] p-2 hover:bg-[var(--color-muted)] transition-smooth min-w-max"
        >
          {collapsed ? (
            <ChevronRight className="h-4 w-4 text-[var(--color-muted-foreground)]" />
          ) : (
            <ChevronLeft className="h-4 w-4 text-[var(--color-muted-foreground)]" />
          )}
        </button>
      </aside>
    </>
  );
}
