import { useState } from "react";
import { Search, Shield, Calendar, AlertTriangle, Eye, Filter, ChevronDown } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { LogDetailDrawer } from "../components/admin/LogDetailDrawer";
import { motion } from "motion/react";

const mockFlaggedLogs = [
  {
    id: 1,
    user: "Anonymous User 1",
    userId: "ANON-001",
    date: "2024-02-10",
    snippet: "Experiencing unusual symptoms...",
    flagReason: "Medical concern flagged by AI",
    status: "pending",
    severity: "medium",
    timestamp: "2024-02-13 09:15",
  },
  {
    id: 2,
    user: "Anonymous User 2",
    userId: "ANON-002",
    date: "2024-02-09",
    snippet: "Severe pain levels reported...",
    flagReason: "High pain levels - potential health risk",
    status: "pending",
    severity: "high",
    timestamp: "2024-02-12 16:42",
  },
  {
    id: 3,
    user: "Anonymous User 3",
    userId: "ANON-003",
    date: "2024-02-08",
    snippet: "Irregular cycle pattern detected...",
    flagReason: "Pattern anomaly detected",
    status: "resolved",
    severity: "low",
    timestamp: "2024-02-11 11:20",
  },
  {
    id: 4,
    user: "Anonymous User 4",
    userId: "ANON-004",
    date: "2024-02-07",
    snippet: "Extended bleeding duration...",
    flagReason: "Prolonged cycle duration exceeds normal range",
    status: "pending",
    severity: "medium",
    timestamp: "2024-02-10 14:05",
  },
  {
    id: 5,
    user: "Anonymous User 5",
    userId: "ANON-005",
    date: "2024-02-06",
    snippet: "Multiple symptoms reported simultaneously...",
    flagReason: "Complex symptom combination requires review",
    status: "resolved",
    severity: "high",
    timestamp: "2024-02-09 10:30",
  },
];

export function MenstrualLogs() {
  const [logs, setLogs] = useState(mockFlaggedLogs);
  const [selectedLog, setSelectedLog] = useState(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterStatus, setFilterStatus] = useState("all");
  const [filterSeverity, setFilterSeverity] = useState("all");
  const [showFilters, setShowFilters] = useState(false);

  // Modal states for Contact User
  const [isContactModalOpen, setIsContactModalOpen] = useState(false);
  const [contactMessage, setContactMessage] = useState("");
  const [contactSubject, setContactSubject] = useState("");
  const [userToContact, setUserToContact] = useState(null);

  const handleOpenDrawer = (log) => {
    setSelectedLog(log);
    setIsDrawerOpen(true);
  };

  const handleResolve = (id) => {
    setLogs(logs.map(log => 
      log.id === id ? { ...log, status: "resolved" } : log
    ));
    setIsDrawerOpen(false);
  };

  const handleOpenContactModal = (id) => {
    const log = logs.find((l) => l.id === id);
    if (log) {
      setUserToContact(log);
      setIsContactModalOpen(true);
      setContactSubject(`Response regarding your recent flagged activity`);
    }
  };

  const handleSendNotification = () => {
    if (!contactMessage.trim()) {
      alert("Please enter a message to send.");
      return;
    }
    // Simulate API request
    alert(`Notification sent to ${userToContact.user}`);
    setIsContactModalOpen(false);
    setContactMessage("");
    setContactSubject("");
    setUserToContact(null);
  };

  // Filter logs
  const filteredLogs = logs.filter(log => {
    const matchesSearch = 
      log.user.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.snippet.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.flagReason.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = filterStatus === "all" || log.status === filterStatus;
    const matchesSeverity = filterSeverity === "all" || log.severity === filterSeverity;
    
    return matchesSearch && matchesStatus && matchesSeverity;
  });

  const severityConfig = {
    low: {
      color: "text-[var(--color-success)]",
      bg: "bg-[var(--color-success-light)]",
      border: "border-[var(--color-success)]",
    },
    medium: {
      color: "text-[var(--color-warning)]",
      bg: "bg-[var(--color-warning-light)]",
      border: "border-[var(--color-warning)]",
    },
    high: {
      color: "text-[var(--color-error)]",
      bg: "bg-[var(--color-error-light)]",
      border: "border-[var(--color-error)]",
    },
  };

  return (
    <div className="space-y-6">
      {/* Privacy Notice */}
      <motion.div
        className="rounded-xl border border-[var(--color-primary)] bg-[var(--color-primary-light)] p-5"
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="flex items-start gap-3">
          <Shield className="h-6 w-6 text-[var(--color-primary)] flex-shrink-0 mt-0.5" />
          <div>
            <h3 className="font-semibold text-foreground">Privacy & Medical Safety Protocol</h3>
            <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
              <strong>Important:</strong> This section displays ONLY AI-flagged entries that indicate potential medical concerns requiring professional attention. 
              Regular menstrual logs remain completely private and inaccessible to admins. All data shown here is anonymized. 
              The purpose is solely to identify users who may benefit from medical guidance, not to view personal health information.
            </p>
            <div className="mt-3 p-3 rounded-lg bg-[var(--color-card)]/50 border border-[var(--color-primary)]/30">
              <p className="text-xs text-[var(--color-muted-foreground)]">
                <strong>What triggers a flag:</strong> AI detects severe pain levels (8+/10), prolonged bleeding (&gt;10 days), 
                multiple concerning symptoms, or irregular patterns outside medical norms. Admins can only see the flag reason and 
                anonymized summary - never the full personal log entry.
              </p>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Menstrual Log Monitoring</h1>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
            Review flagged menstrual health logs for potential medical concerns
          </p>
        </div>
        
        {/* Stats */}
        <div className="flex items-center gap-4">
          <div className="text-right">
            <p className="text-2xl font-semibold text-foreground">
              {logs.filter(l => l.status === "pending").length}
            </p>
            <p className="text-xs text-[var(--color-muted-foreground)]">Pending Review</p>
          </div>
          <div className="w-px h-10 bg-[var(--color-border)]" />
          <div className="text-right">
            <p className="text-2xl font-semibold text-[var(--color-success)]">
              {logs.filter(l => l.status === "resolved").length}
            </p>
            <p className="text-xs text-[var(--color-muted-foreground)]">Resolved</p>
          </div>
        </div>
      </div>

      {/* Search & Filters */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row gap-3">
          {/* Search */}
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
            <input
              type="text"
              placeholder="Search by user, snippet, or flag reason..."
              className="h-10 w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-card)] pl-10 pr-4 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          {/* Filter Toggle */}
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="h-10 px-4 rounded-lg border border-[var(--color-input-border)] bg-[var(--color-card)] text-sm font-medium text-foreground hover:border-[var(--color-primary)] hover:bg-[var(--color-primary-light)] transition-smooth flex items-center gap-2"
          >
            <Filter className="h-4 w-4" />
            Filters
            <ChevronDown className={`h-4 w-4 transition-transform ${showFilters ? "rotate-180" : ""}`} />
          </button>
        </div>
        
        {/* Filter Options */}
        {showFilters && (
          <motion.div
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-4 flex flex-wrap gap-4"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
          >
            {/* Status Filter */}
            <div className="flex-1 min-w-[200px]">
              <label className="block text-xs font-medium text-[var(--color-muted-foreground)] mb-2">
                Status
              </label>
              <div className="flex gap-2">
                {["all", "pending", "resolved"].map((status) => (
                  <button
                    key={status}
                    onClick={() => setFilterStatus(status)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                      filterStatus === status
                        ? "bg-[var(--color-primary)] text-white shadow-soft"
                        : "bg-[var(--color-muted)] text-[var(--color-muted-foreground)] hover:bg-[var(--color-primary-light)]"
                    }`}
                  >
                    {status.charAt(0).toUpperCase() + status.slice(1)}
                  </button>
                ))}
              </div>
            </div>
            
            {/* Severity Filter */}
            <div className="flex-1 min-w-[200px]">
              <label className="block text-xs font-medium text-[var(--color-muted-foreground)] mb-2">
                Severity
              </label>
              <div className="flex gap-2">
                {["all", "high", "medium", "low"].map((severity) => (
                  <button
                    key={severity}
                    onClick={() => setFilterSeverity(severity)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                      filterSeverity === severity
                        ? "bg-[var(--color-primary)] text-white shadow-soft"
                        : "bg-[var(--color-muted)] text-[var(--color-muted-foreground)] hover:bg-[var(--color-primary-light)]"
                    }`}
                  >
                    {severity.charAt(0).toUpperCase() + severity.slice(1)}
                  </button>
                ))}
              </div>
            </div>
          </motion.div>
        )}
      </div>

      {/* Logs Table */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] overflow-hidden">
        {/* Table Header - Hidden on mobile */}
        <div className="hidden lg:grid bg-[var(--color-secondary-bg)] border-b border-[var(--color-border)] px-6 py-3 grid-cols-12 gap-4 text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wide">
          <div className="col-span-2">User</div>
          <div className="col-span-1">Date</div>
          <div className="col-span-3">Log Snippet</div>
          <div className="col-span-2">Flag Reason</div>
          <div className="col-span-1">Severity</div>
          <div className="col-span-1">Status</div>
          <div className="col-span-2 text-right">Actions</div>
        </div>
        
        {/* Table Body */}
        <div className="divide-y divide-[var(--color-border)]">
          {filteredLogs.length === 0 ? (
            <div className="px-6 py-12 text-center">
              <Shield className="mx-auto h-12 w-12 text-[var(--color-muted-foreground)] mb-4" />
              <h3 className="text-lg font-semibold text-foreground mb-2">No Logs Found</h3>
              <p className="text-sm text-[var(--color-muted-foreground)]">
                {searchQuery || filterStatus !== "all" || filterSeverity !== "all"
                  ? "Try adjusting your search or filters"
                  : "All menstrual logs are within normal parameters"}
              </p>
            </div>
          ) : (
            filteredLogs.map((log) => {
              const config = severityConfig[log.severity];
              
              return (
                <motion.div
                  key={log.id}
                  className="hover:bg-[var(--color-secondary-bg)] transition-colors cursor-pointer"
                  onClick={() => handleOpenDrawer(log)}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  whileHover={{ scale: 1.001 }}
                >
                  {/* Desktop Table Row */}
                  <div className="hidden lg:grid px-6 py-4 grid-cols-12 gap-4 items-center">
                    {/* User */}
                    <div className="col-span-2 flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-gradient-to-br from-[var(--color-muted)] to-[var(--color-border)] flex items-center justify-center flex-shrink-0">
                        <Shield className="h-4 w-4 text-[var(--color-muted-foreground)]" />
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-foreground truncate">{log.user}</p>
                        <p className="text-xs text-[var(--color-muted-foreground)]">{log.userId}</p>
                      </div>
                    </div>
                    
                    {/* Date */}
                    <div className="col-span-1">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="h-3.5 w-3.5 text-[var(--color-muted-foreground)]" />
                        <span className="text-sm text-foreground">{log.date}</span>
                      </div>
                    </div>
                    
                    {/* Snippet */}
                    <div className="col-span-3">
                      <p className="text-sm text-foreground line-clamp-2">{log.snippet}</p>
                    </div>
                    
                    {/* Flag Reason */}
                    <div className="col-span-2">
                      <div className="flex items-start gap-1.5">
                        <AlertTriangle className="h-3.5 w-3.5 text-[var(--color-warning)] flex-shrink-0 mt-0.5" />
                        <p className="text-sm text-foreground line-clamp-2">{log.flagReason}</p>
                      </div>
                    </div>
                    
                    {/* Severity */}
                    <div className="col-span-1">
                      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${config.bg} ${config.color}`}>
                        {log.severity.charAt(0).toUpperCase() + log.severity.slice(1)}
                      </span>
                    </div>
                    
                    {/* Status */}
                    <div className="col-span-1">
                      <StatusBadge status={log.status} size="sm" />
                    </div>
                    
                    {/* Actions */}
                    <div className="col-span-2 flex justify-end">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleOpenDrawer(log);
                        }}
                        className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium text-[var(--color-primary)] hover:bg-[var(--color-primary-light)] transition-colors"
                      >
                        <Eye className="h-4 w-4" />
                        Review
                      </button>
                    </div>
                  </div>

                  {/* Mobile Card View */}
                  <div className="lg:hidden p-4 space-y-3">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-[var(--color-muted)] to-[var(--color-border)] flex items-center justify-center flex-shrink-0">
                          <Shield className="h-5 w-5 text-[var(--color-muted-foreground)]" />
                        </div>
                        <div>
                          <p className="text-sm font-medium text-foreground">{log.user}</p>
                          <p className="text-xs text-[var(--color-muted-foreground)]">{log.userId}</p>
                        </div>
                      </div>
                      <StatusBadge status={log.status} size="sm" />
                    </div>
                    
                    <div className="space-y-2">
                      <div className="flex items-center gap-2 text-xs text-[var(--color-muted-foreground)]">
                        <Calendar className="h-3.5 w-3.5" />
                        {log.date}
                        <span className={`ml-auto inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${config.bg} ${config.color}`}>
                          {log.severity.charAt(0).toUpperCase() + log.severity.slice(1)}
                        </span>
                      </div>
                      
                      <p className="text-sm text-foreground">{log.snippet}</p>
                      
                      <div className="flex items-start gap-1.5 p-2 rounded-lg bg-[var(--color-warning-light)]">
                        <AlertTriangle className="h-3.5 w-3.5 text-[var(--color-warning)] flex-shrink-0 mt-0.5" />
                        <p className="text-xs text-[var(--color-muted-foreground)]">{log.flagReason}</p>
                      </div>
                    </div>
                    
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenDrawer(log);
                      }}
                      className="w-full inline-flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-[var(--color-primary)] bg-[var(--color-primary-light)] hover:bg-[var(--color-primary)] hover:text-white transition-colors"
                    >
                      <Eye className="h-4 w-4" />
                      Review Details
                    </button>
                  </div>
                </motion.div>
              );
            })
          )}
        </div>
      </div>

      {/* Detail Drawer */}
      <LogDetailDrawer
        log={selectedLog}
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onResolve={handleResolve}
        onContact={handleOpenContactModal}
      />

      {/* Send Notification Modal */}
      {isContactModalOpen && userToContact && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={() => setIsContactModalOpen(false)}></div>
          <div className="relative w-full max-w-md rounded-2xl bg-[var(--color-card)] p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-[var(--color-foreground)]">Send Notification to User</h3>
              <button 
                onClick={() => setIsContactModalOpen(false)}
                className="rounded-lg p-1 text-[var(--color-muted-foreground)] hover:bg-[var(--color-muted)] transition-colors"
              >
                <AlertTriangle className="h-4 w-4 hidden" />
                <span className="sr-only">Close</span>
              </button>
            </div>
            
            <p className="mb-4 text-sm text-[var(--color-muted-foreground)]">
              Sending a notification to <strong>{userToContact.user}</strong>.
            </p>

            <div className="space-y-4">
              <div>
                <label className="mb-1.5 block text-sm font-medium text-[var(--color-foreground)]">Subject (Optional)</label>
                <input
                  type="text"
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-2.5 text-sm text-[var(--color-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20"
                  placeholder="Notification Subject"
                  value={contactSubject}
                  onChange={(e) => setContactSubject(e.target.value)}
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-[var(--color-foreground)]">Message Content</label>
                <textarea
                  className="w-full h-32 resize-none rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-2.5 text-sm text-[var(--color-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 custom-scrollbar"
                  placeholder="Type your message here..."
                  value={contactMessage}
                  onChange={(e) => setContactMessage(e.target.value)}
                ></textarea>
              </div>
            </div>

            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setIsContactModalOpen(false)}
                className="rounded-lg px-4 py-2 font-medium text-[var(--color-muted-foreground)] hover:bg-[var(--color-muted)] hover:text-[var(--color-foreground)] transition-colors text-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleSendNotification}
                className="rounded-lg bg-[var(--color-primary)] px-5 py-2 font-medium text-white hover:bg-[var(--color-primary-hover)] shadow-soft transition-smooth text-sm"
              >
                Send
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
