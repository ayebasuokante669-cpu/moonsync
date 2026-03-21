import { useState } from "react";
import { Search, MessageSquare, CheckCircle, FileText } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { ActionButton } from "../components/admin/ActionButton";
import { FilterBar } from "../components/admin/FilterBar";
import { toast } from "sonner";

const mockChatbotReports = [
  {
    id: 1,
    user: "Emma Johnson",
    flagReason: "Inappropriate response",
    conversation: [
      { role: "user", content: "I'm experiencing severe cramps, what should I do?" },
      { role: "bot", content: "I'm here to help! Severe cramps can be managed with heat therapy, gentle exercise, and over-the-counter pain relief. However, if the pain is unbearable, please consult a healthcare professional." },
    ],
    timestamp: "2 hours ago",
    status: "pending",
  },
  {
    id: 2,
    user: "Sarah Williams",
    flagReason: "Medical misinformation",
    conversation: [
      { role: "user", content: "Can I use birth control to regulate my period?" },
      { role: "bot", content: "Birth control can be used to regulate menstrual cycles, but this is a decision that should be made with your doctor. I recommend discussing your options with a healthcare provider." },
    ],
    timestamp: "5 hours ago",
    status: "pending",
  },
  {
    id: 3,
    user: "Olivia Brown",
    flagReason: "Unhelpful response",
    conversation: [
      { role: "user", content: "When is my next ovulation day?" },
      { role: "bot", content: "Based on your cycle data, your next ovulation is estimated to be in 5 days. Remember that this is an estimate and actual timing may vary." },
    ],
    timestamp: "1 day ago",
    status: "resolved",
  },
];

export function ChatbotReports() {
  const [selectedReport, setSelectedReport] = useState(null);
  const [internalNote, setInternalNote] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [filterStatus, setFilterStatus] = useState("");

  const filteredReports = mockChatbotReports.filter((report) => {
    const matchesSearch = report.user.toLowerCase().includes(searchQuery.toLowerCase()) ||
      report.flagReason.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filterStatus ? report.status === filterStatus : true;
    return matchesSearch && matchesStatus;
  });

  const filterOptions = [
    {
      label: "All Statuses",
      value: filterStatus,
      onChange: setFilterStatus,
      options: ["pending", "resolved"]
    }
  ];

  const handleResolve = () => {
    if (!internalNote.trim()) {
      toast.error("Please enter resolution notes before marking as resolved.");
      return;
    }
    toast.success(`Report for ${selectedReport.user} marked as resolved.`);
    setSelectedReport(null);
    setInternalNote("");
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Chatbot Reports</h1>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
            Review flagged AI chatbot conversations and responses
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
      {filteredReports.length === 0 ? (
        <div className="flex flex-col items-center justify-center p-12 text-center bg-[var(--color-card)] rounded-xl border border-[var(--color-border)]">
          <div className="h-16 w-16 rounded-full bg-[var(--color-secondary-bg)] flex items-center justify-center mb-4">
            <Search className="h-8 w-8 text-[var(--color-muted-foreground)]" />
          </div>
          <h3 className="text-lg font-semibold text-foreground mb-1">No chatbot reports found</h3>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-6 max-w-sm">
            There are no reports matching your search or filter criteria.
          </p>
          <ActionButton 
            variant="secondary" 
            onClick={() => { 
              setSearchQuery(""); 
              setFilterStatus(""); 
            }}
          >
            Clear Filters
          </ActionButton>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredReports.map((report) => (
          <div
            key={report.id}
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6 transition-smooth hover:shadow-soft-lg cursor-pointer"
            onClick={() => { setSelectedReport(report); setInternalNote(""); }}
          >
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1">
                {/* Header */}
                <div className="flex items-center gap-3 mb-4">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[var(--color-primary-light)]">
                    <MessageSquare className="h-5 w-5 text-[var(--color-primary)]" />
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h4 className="font-semibold text-foreground">{report.user}</h4>
                      <StatusBadge status={report.status} size="sm" />
                    </div>
                    <p className="text-sm text-[var(--color-muted-foreground)]">
                      Flagged: {report.flagReason} • {report.timestamp}
                    </p>
                  </div>
                </div>

                {/* Conversation Preview */}
                <div className="space-y-3">
                  {report.conversation.map((message, idx) => (
                    <div
                      key={idx}
                      className={`rounded-lg p-3 ${message.role === "user"
                          ? "bg-[var(--color-secondary-bg)] ml-0 mr-12"
                          : "bg-[var(--color-primary-light)] ml-12 mr-0"
                        }`}
                    >
                      <p className="text-xs font-semibold text-[var(--color-muted-foreground)] uppercase mb-1">
                        {message.role === "user" ? "User" : "AI Assistant"}
                      </p>
                      <p className="text-sm text-foreground">{message.content}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* Actions */}
              {report.status === "pending" && (
                <div className="flex flex-col gap-2">
                  <ActionButton
                    variant="success"
                    icon={CheckCircle}
                    size="sm"
                    onClick={(e) => { e.stopPropagation(); setSelectedReport(report); setInternalNote(""); }}
                  >
                    Resolve
                  </ActionButton>
                  <ActionButton
                    variant="secondary"
                    icon={FileText}
                    size="sm"
                    onClick={(e) => { e.stopPropagation(); setSelectedReport(report); setInternalNote(""); }}
                  >
                    Add Note
                  </ActionButton>
                </div>
              )}
            </div>
          </div>
        ))}
        </div>
      )}

      {/* Detail Modal */}
      {selectedReport && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4" onClick={() => setSelectedReport(null)}>
          <div
            className="w-full max-w-2xl rounded-xl bg-[var(--color-card)] shadow-xl overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="border-b border-[var(--color-border)] bg-[var(--color-secondary-bg)] px-6 py-4">
              <h3 className="text-lg font-semibold text-foreground">Report Details</h3>
              <p className="text-sm text-[var(--color-muted-foreground)]">Review and resolve this flagged conversation</p>
            </div>

            {/* Modal Content */}
            <div className="p-6 space-y-4 max-h-[60vh] overflow-y-auto">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] text-sm font-semibold text-white">
                  {selectedReport.user.split(" ").map(n => n[0]).join("")}
                </div>
                <div>
                  <h4 className="font-semibold text-foreground">{selectedReport.user}</h4>
                  <p className="text-sm text-[var(--color-muted-foreground)]">{selectedReport.timestamp}</p>
                </div>
              </div>

              <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-error-light)] p-4">
                <p className="text-sm font-medium text-foreground">
                  <strong>Flag Reason:</strong> {selectedReport.flagReason}
                </p>
              </div>

              {/* Full Conversation */}
              <div className="space-y-3">
                {selectedReport.conversation.map((message, idx) => (
                  <div
                    key={idx}
                    className={`rounded-lg p-4 ${message.role === "user"
                        ? "bg-[var(--color-secondary-bg)]"
                        : "bg-[var(--color-primary-light)]"
                      }`}
                  >
                    <p className="text-xs font-semibold text-[var(--color-muted-foreground)] uppercase mb-2">
                      {message.role === "user" ? "User" : "AI Assistant"}
                    </p>
                    <p className="text-sm text-foreground">{message.content}</p>
                  </div>
                ))}
              </div>

              {/* Internal Note */}
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Internal Note
                </label>
                <textarea
                  value={internalNote}
                  onChange={(e) => setInternalNote(e.target.value)}
                  placeholder="Add notes for internal tracking..."
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  rows={3}
                />
              </div>
            </div>

            {/* Modal Footer */}
            <div className="flex items-center justify-end gap-3 border-t border-[var(--color-border)] bg-[var(--color-secondary-bg)] px-6 py-4">
              <ActionButton variant="ghost" onClick={() => setSelectedReport(null)}>
                Cancel
              </ActionButton>
              {selectedReport.status === "pending" && (
                <ActionButton variant="success" icon={CheckCircle} onClick={handleResolve}>
                  Mark Resolved
                </ActionButton>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

