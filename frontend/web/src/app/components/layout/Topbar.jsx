import { Search, Bell, Menu, User, FileText, Layout, X, Sun, Moon } from "lucide-react";
import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router";
import { motion, AnimatePresence } from "motion/react";
import { NotificationDropdown } from "./NotificationDropdown";
import { UserMenu } from "./UserMenu";
import { useTheme } from "next-themes";

const globalSearchIndex = [
  { id: "p1", type: "page", title: "Dashboard Overview", route: "/", icon: Layout },
  { id: "p2", type: "page", title: "User Management", route: "/users", icon: Layout },
  { id: "p3", type: "page", title: "Content Articles", route: "/articles", icon: Layout },
  { id: "p4", type: "page", title: "Community Moderation", route: "/community", icon: Layout },
  { id: "p5", type: "page", title: "Menstrual Logs", route: "/menstrual-logs", icon: Layout },
  { id: "p6", type: "page", title: "Medical Verification", route: "/medical-verification", icon: Layout },
  { id: "p7", type: "page", title: "Moon Archive", route: "/archive", icon: Layout },
  { id: "a1", type: "article", title: "Understanding Hormonal Changes", route: "/articles", icon: FileText },
  { id: "a2", type: "article", title: "Nutrition for Your Cycle", route: "/articles", icon: FileText },
  { id: "a3", type: "article", title: "Managing Endometriosis", route: "/articles", icon: FileText },
  { id: "u1", type: "user", title: "Dr. Sarah Johnson", route: "/users", icon: User },
  { id: "u2", type: "user", title: "Emma Williams", route: "/users", icon: User },
  { id: "u3", type: "user", title: "Olivia Brown", route: "/users", icon: User },
];

export function Topbar({ onMenuClick }) {
  const [isNotificationDropdownOpen, setIsNotificationDropdownOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const unreadCount = notifications.filter((n) => !n.read).length;

  const [searchQuery, setSearchQuery] = useState("");
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const searchRef = useRef(null);
  const navigate = useNavigate();
  const { theme, setTheme } = useTheme();

  // Handle clicking outside search dropdown
  useEffect(() => {
    function handleClickOutside(event) {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setIsSearchOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const filteredSearch = searchQuery.trim() === "" 
    ? [] 
    : globalSearchIndex.filter(item => {
        const searchWords = searchQuery.toLowerCase().split(/\s+/).filter(word => word.length > 0);
        const itemText = `${item.title} ${item.type}`.toLowerCase();
        // Return true only if ALL search words are found in the item's text
        return searchWords.every(word => itemText.includes(word));
      });

  const handleSearchResultClick = (route) => {
    navigate(route);
    setIsSearchOpen(false);
    setSearchQuery("");
  };
  return (
    <header className="flex h-16 items-center justify-between border-b border-[var(--color-sidebar-border)] bg-[var(--color-background)] px-4 sm:px-8">
      {/* Mobile Menu Button */}
      <button
        onClick={onMenuClick}
        className="lg:hidden rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
      >
        <Menu className="h-5 w-5 text-[var(--color-muted-foreground)]" />
      </button>

      {/* Search Bar */}
      <div className="hidden sm:flex flex-1 max-w-md ml-4">
        <div className="relative w-full" ref={searchRef}>
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
          <input
            type="text"
            placeholder="Search users, reports, articles..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setIsSearchOpen(true);
            }}
            onFocus={() => setIsSearchOpen(true)}
            className="h-10 w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] pl-10 pr-10 text-sm text-[var(--color-foreground)] placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
          />
          {searchQuery && (
            <button 
              onClick={() => { setSearchQuery(""); setIsSearchOpen(false); }}
              className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] transition-colors"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          )}

          {/* Search Results Dropdown */}
          <AnimatePresence>
            {isSearchOpen && searchQuery.trim() !== "" && (
              <motion.div
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 5 }}
                transition={{ duration: 0.15 }}
                className="absolute top-full left-0 right-0 mt-2 bg-[var(--color-card)] rounded-xl shadow-xl border border-[var(--color-border)] z-50 overflow-hidden"
              >
                {filteredSearch.length > 0 ? (
                  <div className="py-2 max-h-[60vh] overflow-y-auto custom-scrollbar">
                    {filteredSearch.map((result) => {
                      const Icon = result.icon;
                      return (
                        <button
                          key={result.id}
                          onClick={() => handleSearchResultClick(result.route)}
                          className="w-full text-left px-4 py-2.5 flex items-center gap-3 hover:bg-[var(--color-secondary-bg)] transition-colors"
                        >
                          <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-[var(--color-primary-light)]/30 flex items-center justify-center text-[var(--color-primary)]">
                            <Icon className="h-4 w-4" />
                          </div>
                          <div>
                            <p className="text-sm font-medium text-[var(--color-foreground)] line-clamp-1">{result.title}</p>
                            <p className="text-xs text-[var(--color-muted-foreground)] capitalize">{result.type}</p>
                          </div>
                        </button>
                      );
                    })}
                  </div>
                ) : (
                  <div className="p-4 text-center">
                    <p className="text-sm text-[var(--color-muted-foreground)]">No results found for "{searchQuery}"</p>
                  </div>
                )}
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>

      {/* Right Side Actions */}
      <div className="flex items-center gap-2 sm:gap-4 ml-auto">
        {/* Theme Toggle */}
        <button
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          className="relative rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
          aria-label="Toggle theme"
        >
          {theme === "dark" ? (
             <Sun className="h-5 w-5 text-[var(--color-muted-foreground)]" />
          ) : (
             <Moon className="h-5 w-5 text-[var(--color-muted-foreground)]" />
          )}
        </button>

        {/* Notifications Dropdown */}
        <div className="relative">
          <button 
            onClick={() => setIsNotificationDropdownOpen(!isNotificationDropdownOpen)}
            className="relative rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
          >
            <Bell className="h-5 w-5 text-[var(--color-muted-foreground)]" />
            {unreadCount > 0 && (
              <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-[var(--color-primary)] text-[10px] font-semibold text-[white]">
                {unreadCount}
              </span>
            )}
          </button>

          <NotificationDropdown 
            isOpen={isNotificationDropdownOpen}
            onClose={() => setIsNotificationDropdownOpen(false)}
            notifications={notifications}
            setNotifications={setNotifications}
          />
        </div>

        {/* Admin Profile with Dropdown */}
        <UserMenu />
      </div>
    </header>
  );
}
