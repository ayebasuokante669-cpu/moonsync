import { useState } from "react";
import { Search, Filter, MoreVertical, X, Ban, Clock, Key, Bell, ShieldCheck, UserCheck } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { ActionButton } from "../components/admin/ActionButton";
import { FilterBar } from "../components/admin/FilterBar";
import { MedicalPersonnelModal } from "../components/modals/MedicalPersonnelModal";
import { useNavigate } from "react-router";
import { motion, AnimatePresence } from "motion/react";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, DialogClose } from "../components/ui/dialog";

export function Users() {
  const [selectedUser, setSelectedUser] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [isMedicalModalOpen, setIsMedicalModalOpen] = useState(false);
  
  // Action Modals State
  const [actionModal, setActionModal] = useState({ isOpen: false, type: null, user: null });
  const [notificationMessage, setNotificationMessage] = useState("");
  
  const [filters, setFilters] = useState({ status: "", role: "" });
  
  const navigate = useNavigate();

  const filteredUsers = mockUsers.filter((user) => {
    const matchesSearch = user.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          user.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filters.status ? user.status === filters.status : true;
    const matchesRole = filters.role ? (filters.role === "Medical Personnel" ? user.isMedical : !user.isMedical) : true;
    
    return matchesSearch && matchesStatus && matchesRole;
  });

  const filterOptions = [
    {
      label: "All Statuses",
      value: filters.status,
      onChange: (val) => setFilters(f => ({ ...f, status: val })),
      options: ["active", "inactive", "suspended", "banned"]
    },
    {
      label: "All Roles",
      value: filters.role,
      onChange: (val) => setFilters(f => ({ ...f, role: val })),
      options: ["User", "Medical Personnel"]
    }
  ];

  const handleMedicalVerification = () => {
    setIsMedicalModalOpen(true);
  };

  const handleModalSubmit = (formData) => {
    navigate("/medical-verification", { state: { newRequest: { ...formData, user: selectedUser } } });
    setSelectedUser(null);
  };

  const executeAction = () => {
    const { type, user } = actionModal;
    
    switch (type) {
      case "notify":
        if (!notificationMessage.trim()) {
          toast.error("Message cannot be empty");
          return;
        }
        toast.success(`Notification sent to ${user.name}`);
        setNotificationMessage("");
        break;
      case "reset":
        toast.success(`Password reset email sent to ${user.email}`);
        break;
      case "suspend":
        toast.warning(`${user.name}'s account has been suspended`);
        break;
      case "ban":
        toast.error(`${user.name} has been permanently banned`);
        break;
    }
    
    setActionModal({ isOpen: false, type: null, user: null });
    // optionally close the user drawer
    // setSelectedUser(null);
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">User Management</h1>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
            Manage user accounts, permissions, and activity
          </p>
        </div>
        <div className="text-right">
          <p className="text-2xl font-semibold text-foreground">{mockUsers.length}</p>
          <p className="text-xs text-[var(--color-muted-foreground)]">Total Users</p>
        </div>
      </div>

      {/* Search and Filters */}
      <FilterBar
        searchPlaceholder="Search users by name or email..."
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        filters={filterOptions}
      />

      {/* Users Table */}
      {filteredUsers.length === 0 ? (
        <div className="flex flex-col items-center justify-center p-12 text-center bg-[var(--color-card)] rounded-xl border border-[var(--color-border)]">
          <div className="h-16 w-16 rounded-full bg-[var(--color-secondary-bg)] flex items-center justify-center mb-4">
            <Search className="h-8 w-8 text-[var(--color-muted-foreground)]" />
          </div>
          <h3 className="text-lg font-semibold text-foreground mb-1">No users found</h3>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-6 max-w-sm">
            Try adjusting your search query or filters to find what you're looking for.
          </p>
          <ActionButton 
            variant="secondary" 
            onClick={() => { 
              setSearchQuery(""); 
              setFilterRole(""); 
              setFilterStatus(""); 
              setFilterVerification(""); 
            }}
          >
            Clear Filters
          </ActionButton>
        </div>
      ) : (
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] overflow-hidden">
          <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-[var(--color-secondary-bg)] border-b border-[var(--color-border)]">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider">User</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider">Email</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider">Registered</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider">Last Active</th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--color-border)]">
              {filteredUsers.map((user) => (
                <motion.tr
                  key={user.id}
                  className="hover:bg-[var(--color-secondary-bg)] transition-smooth cursor-pointer"
                  onClick={() => setSelectedUser(user)}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  whileHover={{ scale: 1.001 }}
                >
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="relative">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] text-sm font-semibold text-white">
                          {user.avatar}
                        </div>
                        {user.isMedical && (
                          <div className="absolute -bottom-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-[var(--color-success)] border-2 border-white">
                            <ShieldCheck className="h-3 w-3 text-white" />
                          </div>
                        )}
                      </div>
                      <div>
                        <span className="text-sm font-medium text-foreground">{user.name}</span>
                        {user.isMedical && (
                          <div className="flex items-center gap-1 mt-0.5">
                            <span className="text-xs text-[var(--color-success)]">Verified Medical</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-[var(--color-muted-foreground)]">{user.email}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <StatusBadge status={user.status} size="sm" />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-[var(--color-muted-foreground)]">{user.registered}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-[var(--color-muted-foreground)]">{user.lastActive}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setSelectedUser(user);
                      }}
                      className="rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
                    >
                      <MoreVertical className="h-4 w-4 text-[var(--color-muted-foreground)]" />
                    </button>
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between border-t border-[var(--color-border)] px-6 py-4">
          <p className="text-sm text-[var(--color-muted-foreground)]">
            Showing <span className="font-medium">1</span> to <span className="font-medium">{filteredUsers.length}</span> of{" "}
            <span className="font-medium">{mockUsers.length}</span> users
          </p>
          <div className="flex gap-2">
            <button className="rounded-lg border border-[var(--color-border)] px-3 py-1.5 text-sm font-medium text-[var(--color-muted-foreground)] hover:bg-[var(--color-muted)] transition-smooth">
              Previous
            </button>
            <button className="rounded-lg bg-[var(--color-primary)] px-3 py-1.5 text-sm font-medium text-white hover:bg-[var(--color-primary-hover)] transition-smooth">
              Next
            </button>
          </div>
        </div>
        </div>
      )}

      {/* User Detail Drawer */}
      {selectedUser && (
        <motion.div
          className="fixed inset-0 z-50 flex items-center justify-end bg-black/30"
          onClick={() => setSelectedUser(null)}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <motion.div
            className="h-full w-full max-w-md bg-[var(--color-card)] shadow-2xl overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 200 }}
          >
            {/* Drawer Header */}
            <div className="sticky top-0 flex items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-card)] px-6 py-4 z-10">
              <h3 className="text-lg font-semibold text-foreground">User Details</h3>
              <button
                onClick={() => setSelectedUser(null)}
                className="rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
              >
                <X className="h-5 w-5 text-[var(--color-muted-foreground)]" />
              </button>
            </div>

            {/* Drawer Content */}
            <div className="p-6 space-y-6">
              {/* Profile Summary */}
              <div className="flex flex-col items-center text-center">
                <div className="relative">
                  <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] text-2xl font-semibold text-white">
                    {selectedUser.avatar}
                  </div>
                  {selectedUser.isMedical && (
                    <div className="absolute -bottom-1 -right-1 flex h-7 w-7 items-center justify-center rounded-full bg-[var(--color-success)] border-2 border-white">
                      <ShieldCheck className="h-4 w-4 text-white" />
                    </div>
                  )}
                </div>
                <h3 className="mt-4 text-xl font-semibold text-foreground">{selectedUser.name}</h3>
                <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">{selectedUser.email}</p>
                <div className="mt-3 flex items-center gap-2">
                  <StatusBadge status={selectedUser.status} />
                  {selectedUser.isMedical && (
                    <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium bg-[var(--color-success-light)] text-[var(--color-success)]">
                      <ShieldCheck className="h-3 w-3" />
                      Verified Medical
                    </span>
                  )}
                </div>
              </div>

              {/* User Info */}
              <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-secondary-bg)] p-4">
                <h4 className="text-sm font-semibold text-foreground mb-3">Account Information</h4>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-sm text-[var(--color-muted-foreground)]">Registered</span>
                    <span className="text-sm font-medium text-foreground">{selectedUser.registered}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-[var(--color-muted-foreground)]">Last Active</span>
                    <span className="text-sm font-medium text-foreground">{selectedUser.lastActive}</span>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="space-y-2">
                <h4 className="text-sm font-semibold text-foreground mb-3">Actions</h4>
                {!selectedUser.isMedical && (
                  <ActionButton variant="primary" icon={UserCheck} className="w-full" onClick={handleMedicalVerification}>
                    Medical Personnel Approval
                  </ActionButton>
                )}
                <ActionButton variant="secondary" icon={Bell} className="w-full" onClick={() => setActionModal({ isOpen: true, type: "notify", user: selectedUser })}>
                  Send Notification
                </ActionButton>
                <ActionButton variant="secondary" icon={Key} className="w-full" onClick={() => setActionModal({ isOpen: true, type: "reset", user: selectedUser })}>
                  Reset Password
                </ActionButton>
                <ActionButton variant="warning" icon={Clock} className="w-full" onClick={() => setActionModal({ isOpen: true, type: "suspend", user: selectedUser })}>
                  Suspend Account
                </ActionButton>
                <ActionButton variant="error" icon={Ban} className="w-full" onClick={() => setActionModal({ isOpen: true, type: "ban", user: selectedUser })}>
                  Ban User
                </ActionButton>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}

      <MedicalPersonnelModal 
        isOpen={isMedicalModalOpen} 
        onClose={() => setIsMedicalModalOpen(false)} 
        onSubmitSuccess={handleModalSubmit} 
      />

      {/* Action Modals */}
      <Dialog open={actionModal.isOpen} onOpenChange={(open) => !open && setActionModal({ isOpen: false, type: null, user: null })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {actionModal.type === "notify" && "Send Notification"}
              {actionModal.type === "reset" && "Reset Password"}
              {actionModal.type === "suspend" && "Suspend Account"}
              {actionModal.type === "ban" && "Ban User"}
            </DialogTitle>
            <DialogDescription>
              {actionModal.type === "notify" && `Send a direct notification to ${actionModal.user?.name}.`}
              {actionModal.type === "reset" && `Are you sure you want to send a password reset link to ${actionModal.user?.email}?`}
              {actionModal.type === "suspend" && `Are you sure you want to temporarily suspend ${actionModal.user?.name}'s account? They will not be able to log in until the suspension is lifted.`}
              {actionModal.type === "ban" && `Are you sure you want to permanently ban ${actionModal.user?.name}? This action cannot be undone and will delete all their associated data in 30 days.`}
            </DialogDescription>
          </DialogHeader>

          {actionModal.type === "notify" && (
            <div className="py-4">
              <label className="block text-sm font-medium text-foreground mb-2">Message</label>
              <textarea
                value={notificationMessage}
                onChange={(e) => setNotificationMessage(e.target.value)}
                placeholder="Type your message here..."
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth min-h-[100px]"
              />
            </div>
          )}

          <DialogFooter className="mt-4">
            <DialogClose asChild>
              <ActionButton variant="secondary">Cancel</ActionButton>
            </DialogClose>
            <ActionButton 
              variant={actionModal.type === "ban" ? "error" : actionModal.type === "suspend" ? "warning" : "primary"} 
              onClick={executeAction}
            >
              {actionModal.type === "notify" && "Send"}
              {actionModal.type === "reset" && "Reset Password"}
              {actionModal.type === "suspend" && "Suspend"}
              {actionModal.type === "ban" && "Ban User"}
            </ActionButton>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}