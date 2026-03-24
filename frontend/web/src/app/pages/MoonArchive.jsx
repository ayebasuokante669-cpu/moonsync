import { useState } from "react";
import { motion } from "motion/react";
import { Archive, Download, Settings as SettingsIcon } from "lucide-react";
import { FilterBar } from "../components/admin/FilterBar";
import { StatusBadge } from "../components/admin/StatusBadge";
import { useNavigate } from "react-router";


export function MoonArchive() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [filterType, setFilterType] = useState("");
  const [retentionPeriod, setRetentionPeriod] = useState("30"); // Defaults to 30 days
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  // Filter the logs
  const filteredLogs = mockArchiveData.filter((log) => {
    const matchesSearch = log.message.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          log.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          log.admin.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesType = filterType ? log.type === filterType : true;
    return matchesSearch && matchesType;
  });

  const generateReport = () => {
    // In a real app, this would trigger a CSV/PDF download of the filtered logs
    alert("Downloading Archive Report CSV...");
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-foreground flex items-center gap-2">
            <Archive className="h-6 w-6 text-[var(--color-primary)]" />
            Moon Archive
          </h1>
          <p className="text-sm text-[var(--color-muted-foreground)]">
            Centralized history of all system events and administrative actions.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button 
            onClick={() => setIsSettingsOpen(!isSettingsOpen)}
            className="flex items-center justify-center h-10 w-10 rounded-lg border border-[var(--color-input-border)] hover:bg-[var(--color-secondary-bg)] transition-colors text-[var(--color-muted-foreground)] hover:text-foreground"
            title="Retention Settings"
          >
            <SettingsIcon className="h-5 w-5" />
          </button>
          <button 
            onClick={generateReport}
            className="flex items-center gap-2 rounded-lg bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-white hover:bg-[var(--color-primary-hover)] transition-smooth"
          >
            <Download className="h-4 w-4" />
            <span className="hidden sm:inline">Export Logs</span>
          </button>
        </div>
      </div>

      {/* Retention Settings Panel */}
      {isSettingsOpen && (
        <motion.div 
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: "auto" }}
          exit={{ opacity: 0, height: 0 }}
          className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-5 shadow-sm"
        >
          <h3 className="font-semibold text-foreground mb-2">Data Retention Policy</h3>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-4">
            Configure how long administrative logs should be stored before being automatically deleted.
          </p>
          <div className="flex flex-wrap gap-3">
            {[
              { label: "30 Days (Default)", value: "30" },
              { label: "90 Days", value: "90" },
              { label: "1 Year", value: "365" },
              { label: "Indefinite", value: "infinite" }
            ].map(option => (
              <label 
                key={option.value}
                className={`flex cursor-pointer items-center gap-2 rounded-lg border p-3 transition-colors ${
                  retentionPeriod === option.value 
                    ? "border-[var(--color-primary)] bg-[var(--color-primary-light)]/20" 
                    : "border-[var(--color-border)] hover:bg-[var(--color-secondary-bg)]"
                }`}
              >
                <input 
                  type="radio" 
                  name="retention" 
                  value={option.value}
                  checked={retentionPeriod === option.value}
                  onChange={(e) => setRetentionPeriod(e.target.value)}
                  className="text-[var(--color-primary)] focus:ring-[var(--color-primary)]" 
                />
                <span className="text-sm font-medium text-[var(--color-foreground)]">{option.label}</span>
              </label>
            ))}
          </div>
        </motion.div>
      )}

      {/* Filters */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-4 shadow-sm">
        <FilterBar
          searchPlaceholder="Search logs by ID, message, or admin..."
          searchValue={searchQuery}
          onSearchChange={setSearchQuery}
          filters={[
            {
              label: "All Activity Types",
              value: filterType,
              onChange: setFilterType,
              options: ["User", "Verification", "Moderation", "Articles", "Notifications", "Logs", "Reports", "System"],
            }
          ]}
        />
      </div>

      {/* Log Feed */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-sm overflow-hidden">
        <div className="overflow-x-auto custom-scrollbar">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead className="bg-[var(--color-secondary-bg)] text-[var(--color-muted-foreground)]">
              <tr>
                <th className="px-6 py-4 font-medium">Log ID</th>
                <th className="px-6 py-4 font-medium min-w-[300px]">Description</th>
                <th className="px-6 py-4 font-medium">Type</th>
                <th className="px-6 py-4 font-medium">Actor</th>
                <th className="px-6 py-4 font-medium">Status</th>
                <th className="px-6 py-4 font-medium">Date & Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--color-border)] text-[var(--color-foreground)] bg-transparent">
              {filteredLogs.length > 0 ? (
                filteredLogs.map((log) => (
                  <tr 
                    key={log.id} 
                    className="hover:bg-[var(--color-secondary-bg)]/50 transition-colors cursor-pointer group"
                    onClick={() => navigate(log.route)}
                  >
                    <td className="px-6 py-4 font-mono text-xs text-[var(--color-muted-foreground)]">{log.id}</td>
                    <td className="px-6 py-4">
                      <span className="font-medium group-hover:text-[var(--color-primary)] transition-colors">
                        {log.message}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center rounded-md bg-[var(--color-input)] px-2 py-1 text-xs font-medium text-[var(--color-foreground)]">
                        {log.type}
                      </span>
                    </td>
                    <td className="px-6 py-4">{log.admin}</td>
                    <td className="px-6 py-4">
                      <StatusBadge status={log.status} />
                    </td>
                    <td className="px-6 py-4 text-[var(--color-muted-foreground)]">
                      {new Date(log.timestamp).toLocaleString(undefined, {
                        dateStyle: 'medium',
                        timeStyle: 'short'
                      })}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center text-[var(--color-muted-foreground)]">
                    <Archive className="mx-auto h-12 w-12 opacity-20 mb-4" />
                    <p className="text-lg font-medium text-[var(--color-foreground)]">No logs found</p>
                    <p className="text-sm mt-1">Try adjusting your filters or search query.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
