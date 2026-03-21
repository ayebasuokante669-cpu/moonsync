import { useState, useRef, useEffect } from "react";
import { User, Settings, LogOut, ChevronDown } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { useNavigate } from "react-router";
import { toast } from "sonner";

export function UserMenu({ 
  userName = "Super Admin", 
  userEmail = "admin@moonsync.app",
  userInitials = "SA" 
}) {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef(null);
  const navigate = useNavigate();

  // Close menu when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [isOpen]);

  const handleLogout = () => {
    setIsOpen(false);
    toast.success("Logged out successfully", {
      description: "Redirecting to login page...",
    });
    setTimeout(() => {
      navigate("/auth");
    }, 1000);
  };

  const handleProfileClick = () => {
    setIsOpen(false);
    navigate("/profile");
  };

  const handleSettingsClick = () => {
    setIsOpen(false);
    navigate("/settings");
  };

  return (
    <div className="relative" ref={menuRef}>
      {/* Trigger Button */}
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 rounded-lg px-2 sm:px-3 py-2 hover:bg-[var(--color-muted)] transition-smooth"
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] text-xs font-semibold text-white">
          {userInitials}
        </div>
        <div className="hidden md:flex flex-col items-start">
          <span className="text-sm font-medium text-foreground">{userName}</span>
          <span className="text-xs text-[var(--color-muted-foreground)]">{userEmail}</span>
        </div>
        <ChevronDown 
          className={`hidden sm:block h-4 w-4 text-[var(--color-muted-foreground)] transition-transform ${
            isOpen ? "rotate-180" : ""
          }`} 
        />
      </button>

      {/* Dropdown Menu */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            className="absolute right-0 mt-2 w-64 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-2xl z-50 overflow-hidden"
            initial={{ opacity: 0, y: -10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -10, scale: 0.95 }}
            transition={{ type: "spring", damping: 25, stiffness: 300 }}
          >
            {/* User Info Header */}
            <div className="p-4 border-b border-[var(--color-border)] bg-[var(--color-secondary-bg)]">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] text-sm font-semibold text-white">
                  {userInitials}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-foreground truncate">{userName}</p>
                  <p className="text-xs text-[var(--color-muted-foreground)] truncate">{userEmail}</p>
                </div>
              </div>
            </div>

            {/* Menu Items */}
            <div className="p-2">
              <button
                onClick={handleProfileClick}
                className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-[var(--color-secondary-bg)] transition-smooth text-left group"
              >
                <User className="h-4 w-4 text-[var(--color-muted-foreground)] group-hover:text-[var(--color-primary)]" />
                <div>
                  <p className="text-sm font-medium text-foreground">Profile</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">View and edit your profile</p>
                </div>
              </button>

              <button
                onClick={handleSettingsClick}
                className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-[var(--color-secondary-bg)] transition-smooth text-left group"
              >
                <Settings className="h-4 w-4 text-[var(--color-muted-foreground)] group-hover:text-[var(--color-primary)]" />
                <div>
                  <p className="text-sm font-medium text-foreground">Settings</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Manage app settings</p>
                </div>
              </button>

              <div className="my-2 h-px bg-[var(--color-border)]" />

              <button
                onClick={handleLogout}
                className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-[var(--color-error-light)] transition-smooth text-left group"
              >
                <LogOut className="h-4 w-4 text-[var(--color-muted-foreground)] group-hover:text-[var(--color-error)]" />
                <div>
                  <p className="text-sm font-medium text-foreground group-hover:text-[var(--color-error)]">Logout</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Sign out of your account</p>
                </div>
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
