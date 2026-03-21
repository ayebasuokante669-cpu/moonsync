import { useState } from "react";
import { X, Shield, Calendar, FileText, AlertTriangle, User, CheckCircle2, Bell } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

export function LogDetailDrawer({ log, isOpen, onClose, onResolve, onContact }) {
  const [internalNote, setInternalNote] = useState("");
  
  if (!log) return null;
  
  const severityConfig = {
    low: {
      color: "text-[var(--color-success)]",
      bg: "bg-[var(--color-success-light)]",
      label: "Low Priority"
    },
    medium: {
      color: "text-[var(--color-warning)]",
      bg: "bg-[var(--color-warning-light)]",
      label: "Medium Priority"
    },
    high: {
      color: "text-[var(--color-error)]",
      bg: "bg-[var(--color-error-light)]",
      label: "High Priority"
    }
  };
  
  const config = severityConfig[log.severity] || severityConfig.low;

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            className="fixed inset-0 bg-black/20 backdrop-blur-sm z-40"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          
          {/* Drawer */}
          <motion.div
            className="fixed right-0 top-0 bottom-0 w-full max-w-2xl bg-[var(--color-card)] shadow-2xl z-50 overflow-y-auto"
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 200 }}
          >
            {/* Header */}
            <div className="sticky top-0 bg-[var(--color-card)] border-b border-[var(--color-border)] px-6 py-4 flex items-center justify-between z-10">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl ${config.bg} flex items-center justify-center`}>
                  <Shield className={`h-5 w-5 ${config.color}`} />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-foreground">Log Review</h2>
                  <p className="text-sm text-[var(--color-muted-foreground)]">Review flagged health data</p>
                </div>
              </div>
              
              <button
                onClick={onClose}
                className="w-9 h-9 rounded-lg hover:bg-[var(--color-muted)] transition-colors flex items-center justify-center"
              >
                <X className="h-5 w-5 text-[var(--color-muted-foreground)]" />
              </button>
            </div>
            
            {/* Content */}
            <div className="p-6 space-y-6">
              {/* Privacy Badge */}
              <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-[var(--color-primary-light)] border border-[var(--color-primary)]/30">
                <Shield className="h-4 w-4 text-[var(--color-primary)] flex-shrink-0" />
                <span className="text-sm font-medium text-foreground">Sensitive Health Content</span>
              </div>
              
              {/* Status & Severity */}
              <div className="flex items-center gap-3">
                <span className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium ${config.bg} ${config.color}`}>
                  <AlertTriangle className="h-4 w-4" />
                  {config.label}
                </span>
                
                {log.status === "resolved" && (
                  <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium bg-[var(--color-success-light)] text-[var(--color-success)]">
                    <CheckCircle2 className="h-4 w-4" />
                    Resolved
                  </span>
                )}
              </div>
              
              {/* User Info */}
              <div className="rounded-xl border border-[var(--color-border)] p-5 space-y-4">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-full bg-gradient-to-br from-[var(--color-muted)] to-[var(--color-border)] flex items-center justify-center">
                    <User className="h-6 w-6 text-[var(--color-muted-foreground)]" />
                  </div>
                  <div>
                    <h4 className="font-semibold text-foreground">{log.user}</h4>
                    <p className="text-sm text-[var(--color-muted-foreground)]">User ID: {log.userId || "ANON-" + log.id}</p>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4 pt-4 border-t border-[var(--color-border)]">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 text-[var(--color-muted-foreground)]" />
                    <div>
                      <p className="text-xs text-[var(--color-muted-foreground)]">Log Date</p>
                      <p className="text-sm font-medium text-foreground">{log.date}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <FileText className="h-4 w-4 text-[var(--color-muted-foreground)]" />
                    <div>
                      <p className="text-xs text-[var(--color-muted-foreground)]">Flagged At</p>
                      <p className="text-sm font-medium text-foreground">{log.timestamp || "2024-02-13 14:30"}</p>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* Flag Reason */}
              <div className="rounded-xl bg-[var(--color-warning-light)] border border-[var(--color-warning)] p-5">
                <h4 className="text-sm font-semibold text-foreground mb-2 flex items-center gap-2">
                  <AlertTriangle className="h-4 w-4 text-[var(--color-warning)]" />
                  Flag Reason
                </h4>
                <p className="text-sm text-foreground">{log.flagReason}</p>
              </div>
              
              {/* Full Log Content */}
              <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-secondary-bg)] p-5">
                <h4 className="text-sm font-semibold text-foreground mb-3">Full Log Content</h4>
                <div className="prose prose-sm max-w-none">
                  <p className="text-sm text-foreground leading-relaxed">
                    {log.fullContent || log.snippet + " Additional details about symptoms, cycle patterns, and health observations would appear here. This is sensitive health data requiring professional review and careful handling."}
                  </p>
                </div>
              </div>
              
              {/* Internal Notes */}
              <div className="rounded-xl border border-[var(--color-border)] p-5">
                <h4 className="text-sm font-semibold text-foreground mb-3">Internal Notes (Private)</h4>
                <textarea
                  className="w-full h-24 px-4 py-3 rounded-lg border border-[var(--color-input-border)] bg-[var(--color-card)] text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 resize-none transition-smooth"
                  placeholder="Add internal notes for the medical review team..."
                  value={internalNote}
                  onChange={(e) => setInternalNote(e.target.value)}
                />
              </div>
              
              {/* Actions */}
              {log.status === "pending" && (
                <div className="flex flex-col sm:flex-row gap-3 pt-4 border-t border-[var(--color-border)]">
                  <button
                    onClick={() => onResolve(log.id)}
                    className="flex-1 h-11 px-6 rounded-xl font-medium text-sm bg-[var(--color-success)] text-white hover:bg-[var(--color-success)]/90 shadow-soft hover:shadow-soft-lg transition-all flex items-center justify-center gap-2"
                  >
                    <CheckCircle2 className="h-4 w-4" />
                    Mark as Resolved
                  </button>
                  
                  <button
                    onClick={() => onContact(log.id)}
                    className="flex-1 h-11 px-6 rounded-xl font-medium text-sm bg-[var(--color-card)] border-2 border-[var(--color-border)] text-foreground hover:border-[var(--color-primary)] hover:bg-[var(--color-primary-light)] transition-all flex items-center justify-center gap-2"
                  >
                    <Bell className="h-4 w-4" />
                    Contact User
                  </button>
                </div>
              )}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
