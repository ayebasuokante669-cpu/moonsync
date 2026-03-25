import { useState, useEffect } from "react";

const API_URL = import.meta.env.VITE_API_URL || "https://moonsync-production.up.railway.app";
import { Search, AlertTriangle, Check, Trash2, MessageCircle } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { ActionButton } from "../components/admin/ActionButton";
import { FilterBar } from "../components/admin/FilterBar";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, DialogClose } from "../components/ui/dialog";
import { toast } from "sonner";

const mockReports = [
  {
    id: 1,
    postContent: "Just started tracking my cycle with MoonSync! Any tips for beginners? 🌙",
    author: "Emma Johnson",
    reportedBy: "Sarah W.",
    reason: "Spam",
    severity: "low",
    timestamp: "2 hours ago",
    status: "pending",
  },
  {
    id: 2,
    postContent: "Does anyone else experience severe cramping? Looking for natural remedies...",
    author: "Olivia Brown",
    reportedBy: "Multiple users",
    reason: "Medical misinformation",
    severity: "high",
    timestamp: "4 hours ago",
    status: "flagged",
  },
  {
    id: 3,
    postContent: "Check out this product link for period pain relief! [spam link]",
    author: "Mia Wilson",
    reportedBy: "Ava D.",
    reason: "Inappropriate content",
    severity: "medium",
    timestamp: "1 day ago",
    status: "pending",
  },
];

export function CommunityModeration() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedReport, setSelectedReport] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterStatus, setFilterStatus] = useState("");

  const [actionModal, setActionModal] = useState({ isOpen: false, type: null, report: null });
  const [warningMessage, setWarningMessage] = useState("");
  const token = localStorage.getItem("token") || "test";

  useEffect(() => {
    const fetchIssues = async () => {
      try {
        const res = await fetch(`${API_URL}/admin/issues`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) throw new Error();
        const data = await res.json();
        const safe = Array.isArray(data) ? data : data.data || [];
        setReports(safe.map(r => ({
          id: r.id,
          postContent: r.content || r.post_content || r.postContent || "",
          author: r.author || r.author_name || "Unknown",
          reportedBy: r.reported_by || r.reportedBy || "Unknown",
          status: r.status || "pending",
          severity: r.severity || "low",
          reason: r.reason || "",
          timestamp: r.created_at || r.timestamp || "",
        })));
      } catch {
        setReports([]);
      } finally {
        setLoading(false);
      }
    };
    fetchIssues();
  }, []);

  const filteredReports = reports.filter((report) => {
    const matchesSearch = report.postContent.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          report.author.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filterStatus ? filterStatus === "resolved" ? report.status === "resolved" : report.status !== "resolved" : true;
    return matchesSearch && matchesStatus;
  });

  const filterOptions = [
    {
      label: "All Statuses",
      value: filterStatus,
      onChange: setFilterStatus,
      options: ["pending", "flagged", "resolved"]
    }
  ];

  const executeAction = async () => {
    const { type, report } = actionModal;

    if (type === "warn" && !warningMessage.trim()) {
      toast.error("Warning message cannot be empty");
      return;
    }

    try {
      const statusMap = { approve: "resolved", delete: "deleted", warn: "warned" };
      await fetch(`${API_URL}/admin/issues/${report.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          status: statusMap[type],
          ...(type === "warn" && { message: warningMessage }),
        }),
      });

      if (type === "approve") {
        setReports(prev => prev.map(r => r.id === report.id ? { ...r, status: "resolved" } : r));
        toast.success(`Post by ${report.author} has been approved.`);
      } else if (type === "delete") {
        setReports(prev => prev.filter(r => r.id !== report.id));
        toast.error(`Post by ${report.author} has been deleted.`);
      } else if (type === "warn") {
        toast.success(`Warning sent to ${report.author}`);
        setWarningMessage("");
      }
    } catch {
      toast.error("Action failed");
    }

    setActionModal({ isOpen: false, type: null, report: null });
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Community Moderation</h1>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
            Review and moderate reported community posts
          </p>
        </div>
      </div>

      {/* Search and Filters */}
      <FilterBar
        searchPlaceholder="Search reports..."
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        filters={filterOptions}
      />

      {/* Reports List */}
      {loading && <p className="text-sm text-[var(--color-muted-foreground)]">Loading reports...</p>}
      {filteredReports.length === 0 ? (
        <div className="flex flex-col items-center justify-center p-12 text-center bg-[var(--color-card)] rounded-xl border border-[var(--color-border)]">
          <div className="h-16 w-16 rounded-full bg-[var(--color-secondary-bg)] flex items-center justify-center mb-4">
            <Search className="h-8 w-8 text-[var(--color-muted-foreground)]" />
          </div>
          <h3 className="text-lg font-semibold text-foreground mb-1">No moderation reports found</h3>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-6 max-w-sm">
            There are no reports matching your search or filter criteria.
          </p>
          <ActionButton 
            variant="secondary" 
            onClick={() => { 
              setSearchQuery(""); 
              setFilterStatus(""); 
              setFilterSeverity(""); 
            }}
          >
            Clear Filters
          </ActionButton>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredReports.map((report) => {
            const severityColors = {
              low: "bg-[var(--color-success-light)] text-[var(--color-success)]",
              medium: "bg-[var(--color-warning-light)] text-[var(--color-warning)]",
              high: "bg-[var(--color-error-light)] text-[var(--color-error)]",
            };

            return (
              <div
                key={report.id}
                className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6 transition-smooth hover:shadow-soft-lg"
              >
                <div className="flex items-start justify-between gap-4">
                  {/* Post Content */}
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-3">
                      <StatusBadge status={report.status} size="sm" />
                      <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${severityColors[report.severity]}`}>
                        {report.severity.charAt(0).toUpperCase() + report.severity.slice(1)} Severity
                      </span>
                    </div>
                    
                    <div className="rounded-lg bg-[var(--color-secondary-bg)] p-4 mb-4">
                      <p className="text-sm text-foreground">{report.postContent}</p>
                    </div>

                    <div className="flex items-center gap-4 text-sm text-[var(--color-muted-foreground)]">
                      <span><strong>Author:</strong> {report.author}</span>
                      <span>•</span>
                      <span><strong>Reported by:</strong> {report.reportedBy}</span>
                      <span>•</span>
                      <span>{report.timestamp}</span>
                    </div>

                    <div className="mt-3 flex items-center gap-2">
                      <AlertTriangle className="h-4 w-4 text-[var(--color-warning)]" />
                      <span className="text-sm font-medium text-foreground">Reason: {report.reason}</span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex flex-col gap-2">
                    <ActionButton variant="success" icon={Check} size="sm" onClick={() => setActionModal({ isOpen: true, type: "approve", report })}>
                      Approve
                    </ActionButton>
                    <ActionButton variant="error" icon={Trash2} size="sm" onClick={() => setActionModal({ isOpen: true, type: "delete", report })}>
                      Delete
                    </ActionButton>
                    <ActionButton variant="secondary" icon={MessageCircle} size="sm" onClick={() => setActionModal({ isOpen: true, type: "warn", report })}>
                      Warn User
                    </ActionButton>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
      
      {/* Action Modals */}
      <Dialog open={actionModal.isOpen} onOpenChange={(open) => !open && setActionModal({ isOpen: false, type: null, report: null })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {actionModal.type === "warn" && "Warn User"}
              {actionModal.type === "approve" && "Approve Content"}
              {actionModal.type === "delete" && "Delete Content"}
            </DialogTitle>
            <DialogDescription>
              {actionModal.type === "warn" && `Send a direct warning to ${actionModal.report?.author} regarding their post.`}
              {actionModal.type === "approve" && "Are you sure you want to approve this post? It will be marked as resolved and remain visible in the community."}
              {actionModal.type === "delete" && "Are you sure you want to delete this post? This action cannot be undone."}
            </DialogDescription>
          </DialogHeader>

          {actionModal.type === "warn" && (
            <div className="py-4">
              <label className="block text-sm font-medium text-foreground mb-2">Warning Message</label>
              <textarea
                value={warningMessage}
                onChange={(e) => setWarningMessage(e.target.value)}
                placeholder="Briefly explain why their post violated community guidelines..."
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth min-h-[100px]"
              />
            </div>
          )}

          <DialogFooter className="mt-4">
            <DialogClose asChild>
              <ActionButton variant="secondary">Cancel</ActionButton>
            </DialogClose>
            <ActionButton 
              variant={actionModal.type === "delete" ? "error" : actionModal.type === "warn" ? "warning" : "primary"} 
              onClick={executeAction}
            >
              {actionModal.type === "warn" && "Send Warning"}
              {actionModal.type === "approve" && "Confirm Approval"}
              {actionModal.type === "delete" && "Delete Post"}
            </ActionButton>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

