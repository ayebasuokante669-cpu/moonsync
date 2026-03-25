import { useState, useEffect, useRef } from "react";

const API_URL = import.meta.env.VITE_API_URL || "https://moonsync-production.up.railway.app";
import { Search, CheckCircle, XCircle, Shield, Mail, User, FileText, Clock, Filter, ChevronDown, AlertCircle, Upload, Trash2, Eye } from "lucide-react";
import { StatusBadge } from "../components/admin/StatusBadge";
import { Modal } from "../components/admin/Modal";
import { motion, AnimatePresence } from "motion/react";
import { toast } from "sonner";
import { useNavigate, useLocation, useSearchParams } from "react-router";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, DialogClose } from "../components/ui/dialog";
import { ActionButton } from "../components/admin/ActionButton";

const mockRequests = [
  {
    id: 1,
    fullName: "Dr. Sarah Johnson",
    email: "sarah.johnson@healthmail.com",
    profession: "Gynecologist",
    credentials: "MD, Board Certified OB/GYN",
    licenseNumber: "GYN-12345-CA",
    requestDate: "2024-02-15",
    status: "pending",
    documents: ["Medical License", "Board Certification"],
  },
  {
    id: 2,
    fullName: "Emily Rodriguez, RN",
    email: "emily.rodriguez@nursecare.com",
    profession: "Registered Nurse",
    credentials: "RN, BSN",
    licenseNumber: "RN-67890-NY",
    requestDate: "2024-02-14",
    status: "pending",
    documents: ["Nursing License", "BSN Diploma"],
  },
  {
    id: 3,
    fullName: "Dr. Michael Chen",
    email: "m.chen@medcenter.org",
    profession: "Endocrinologist",
    credentials: "MD, Endocrinology Specialist",
    licenseNumber: "ENDO-54321-TX",
    requestDate: "2024-02-13",
    status: "approved",
    documents: ["Medical License", "Specialty Certification"],
  },
];

export function MedicalVerification() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterStatus, setFilterStatus] = useState("all");
  const [showFilters, setShowFilters] = useState(false);
  const [actionNotes, setActionNotes] = useState("");
  const token = localStorage.getItem("token") || "test";
  
  // Document Upload State
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [inputLicense, setInputLicense] = useState("");
  const fileInputRef = useRef(null);

  // Admin Actions State
  const [actionModal, setActionModal] = useState({ isOpen: false, type: null, request: null });

  const [searchParams, setSearchParams] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchVerifications = async () => {
      try {
        const res = await fetch(`${API_URL}/admin/medical-verifications`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) throw new Error();
        const data = await res.json();
        const safe = Array.isArray(data) ? data : data.data || [];
        setRequests(safe.map(r => ({
          id: r.id,
          fullName: r.full_name || r.fullName || "Unknown",
          email: r.email || "",
          profession: r.profession || "",
          credentials: r.credentials || "",
          licenseNumber: r.license_number || r.licenseNumber || "",
          requestDate: r.request_date || r.requestDate || "",
          status: r.status || "pending",
          notes: r.notes || "",
          documents: r.documents || [],
        })));
      } catch {
        setRequests([]);
      } finally {
        setLoading(false);
      }
    };
    fetchVerifications();
  }, []);

  useEffect(() => {
    // Intercept new request from Users.jsx Modal
    if (location.state?.newRequest) {
      const newReq = location.state.newRequest;
      const formattedRequest = {
        id: Date.now(),
        fullName: newReq.fullName || newReq.user?.name || "Unknown Applicant",
        email: newReq.user?.email || "pending@example.com",
        profession: newReq.profession || "Not specified",
        credentials: "Pending Verification",
        licenseNumber: "", // Expected to be provided by applicant later
        requestDate: new Date().toISOString().split('T')[0],
        status: "pending",
        documents: [],
      };
      
      setRequests(prev => [formattedRequest, ...prev]);
      // Clear state so it doesn't re-add on refresh
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  useEffect(() => {
    const paramStatus = searchParams.get("status");
    if (paramStatus && ["all", "pending", "approved", "rejected", "under review"].includes(paramStatus)) {
      setFilterStatus(paramStatus);
    }
  }, [searchParams]);

  const handleOpenModal = (request) => {
    setSelectedRequest(request);
    setActionNotes(request.notes || "");
    setInputLicense(request.licenseNumber || "");
    setUploadedFiles(request.documents ? request.documents.map(d => ({ name: d, size: "2.4 MB", type: "application/pdf" })) : []);
    setIsModalOpen(true);
  };

  const handleApprove = async (id) => {
    try {
      await fetch(`${API_URL}/admin/medical-verifications/${id}/approve`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      setRequests(prev => prev.map(req => req.id === id ? { ...req, status: "approved" } : req));
      toast.success("Medical professional verified successfully", {
        description: `${selectedRequest?.fullName} can now post with verified badge`,
      });
    } catch {
      toast.error("Failed to approve verification");
    }
    setIsModalOpen(false);
  };

  const handleSubmitVerification = (id) => {
    // This is for applicant submitting details, we are simulating changing it to under review or submitting docs
    setRequests(requests.map(req =>
      req.id === id ? { ...req, licenseNumber: inputLicense, documents: uploadedFiles.map(f => f.name) } : req
    ));
    toast.success("Documents submitted successfully. Status remains pending until reviewed.");
    setIsModalOpen(false);
  };

  const handleReject = async (id) => {
    if (!actionNotes.trim()) {
      toast.error("Please provide a reason for rejection");
      return;
    }
    try {
      await fetch(`${API_URL}/admin/medical-verifications/${id}/revoke`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      setRequests(prev => prev.map(req => req.id === id ? { ...req, status: "rejected", notes: actionNotes } : req));
      toast.warning("Verification request rejected", {
        description: `${selectedRequest?.fullName} has been notified`,
      });
    } catch {
      toast.error("Failed to reject verification");
    }
    setIsModalOpen(false);
  };
  
  const executeAdminAction = async () => {
    const { type, request } = actionModal;

    try {
      if (type === "revoke") {
        await fetch(`${API_URL}/admin/medical-verifications/${request.id}/revoke`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        });
        setRequests(prev => prev.map(req => req.id === request.id ? { ...req, status: "rejected", notes: "Approval revoked by admin" } : req));
        toast.warning(`Approval revoked for ${request.fullName}`);
      } else if (type === "suspend") {
        setRequests(prev => prev.map(req => req.id === request.id ? { ...req, status: "pending", notes: "Account suspended pending administrative review" } : req));
        toast.warning(`${request.fullName}'s account has been suspended`);
      } else if (type === "delete") {
        setRequests(prev => prev.filter(req => req.id !== request.id));
        toast.error(`Account for ${request.fullName} deleted permanently`);
      }
    } catch {
      toast.error("Action failed");
    }

    setActionModal({ isOpen: false, type: null, request: null });
    setIsModalOpen(false);
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      const newFiles = Array.from(e.target.files).map(file => ({
        name: file.name,
        size: (file.size / 1024 / 1024).toFixed(2) + " MB",
        type: file.type,
        url: URL.createObjectURL(file)
      }));
      setUploadedFiles(prev => [...prev, ...newFiles]);
    }
  };

  const removeFile = (index) => {
    setUploadedFiles(prev => prev.filter((_, i) => i !== index));
  };

  const filteredRequests = requests.filter(req => {
    const matchesSearch =
      req.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      req.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      req.profession.toLowerCase().includes(searchQuery.toLowerCase()) ||
      req.licenseNumber.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filterStatus === "all" || req.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const stats = {
    pending: requests.filter(r => r.status === "pending").length,
    approved: requests.filter(r => r.status === "approved").length,
    rejected: requests.filter(r => r.status === "rejected").length,
  };

  return (
    <div className="space-y-6">
      {/* Page Header with Stats */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Medical Personnel Verification</h1>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
            Review and verify healthcare professionals for community posting
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="text-right">
            <p className="text-2xl font-semibold text-[var(--color-warning)]">{stats.pending}</p>
            <p className="text-xs text-[var(--color-muted-foreground)]">Pending Review</p>
          </div>
          <div className="w-px h-10 bg-[var(--color-border)]" />
          <div className="text-right">
            <p className="text-2xl font-semibold text-[var(--color-success)]">{stats.approved}</p>
            <p className="text-xs text-[var(--color-muted-foreground)]">Verified</p>
          </div>
          <div className="w-px h-10 bg-[var(--color-border)]" />
          <div className="text-right">
            <p className="text-2xl font-semibold text-[var(--color-error)]">{stats.rejected}</p>
            <p className="text-xs text-[var(--color-muted-foreground)]">Rejected</p>
          </div>
        </div>
      </div>

      {/* Search & Filters */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
            <input
              type="text"
              placeholder="Search by name, email, profession, or license number..."
              className="h-10 w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-card)] pl-10 pr-4 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="h-10 px-4 rounded-lg border border-[var(--color-input-border)] bg-[var(--color-card)] text-sm font-medium text-foreground hover:border-[var(--color-primary)] hover:bg-[var(--color-primary-light)] transition-smooth flex items-center gap-2"
          >
            <Filter className="h-4 w-4" />
            Filters
            <ChevronDown className={`h-4 w-4 transition-transform ${showFilters ? "rotate-180" : ""}`} />
          </button>
        </div>

        {showFilters && (
          <motion.div
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-4 flex flex-wrap gap-4"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
          >
            <div className="flex-1 min-w-[200px]">
              <label className="block text-xs font-medium text-[var(--color-muted-foreground)] mb-2">Status</label>
              <div className="flex gap-2">
                {["all", "pending", "approved", "rejected", "under review"].map((status) => (
                  <button
                    key={status}
                    onClick={() => {
                      setFilterStatus(status);
                      setSearchParams({ status });
                    }}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                      filterStatus === status
                        ? "bg-[var(--color-primary)] text-white shadow-soft"
                        : "bg-[var(--color-muted)] text-[var(--color-muted-foreground)] hover:bg-[var(--color-primary-light)]"
                    }`}
                  >
                    {status === "under review" ? "Under Review" : status.charAt(0).toUpperCase() + status.slice(1)}
                  </button>
                ))}
              </div>
            </div>
          </motion.div>
        )}
      </div>

      {/* Requests Table */}
      {loading && <p className="text-sm text-[var(--color-muted-foreground)]">Loading verifications...</p>}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] overflow-hidden">
        <div className="hidden lg:grid bg-[var(--color-secondary-bg)] border-b border-[var(--color-border)] px-6 py-3 grid-cols-12 gap-4 text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wide">
          <div className="col-span-3">Professional</div>
          <div className="col-span-2">Profession</div>
          <div className="col-span-2">License Number</div>
          <div className="col-span-2">Request Date</div>
          <div className="col-span-1">Status</div>
          <div className="col-span-2 text-right">Actions</div>
        </div>

        <div className="divide-y divide-[var(--color-border)]">
          {filteredRequests.length === 0 ? (
            <div className="px-6 py-12 text-center">
              <Shield className="mx-auto h-12 w-12 text-[var(--color-muted-foreground)] mb-4" />
              <h3 className="text-lg font-semibold text-foreground mb-2">No Requests Found</h3>
              <p className="text-sm text-[var(--color-muted-foreground)]">
                {searchQuery || filterStatus !== "all"
                  ? "Try adjusting your search or filters"
                  : "No verification requests at this time"}
              </p>
            </div>
          ) : (
            filteredRequests.map((request) => (
              <motion.div
                key={request.id}
                className="hover:bg-[var(--color-secondary-bg)] transition-colors cursor-pointer"
                onClick={() => handleOpenModal(request)}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                whileHover={{ scale: 1.001 }}
              >
                {/* Desktop Table Row */}
                <div className="hidden lg:grid px-6 py-4 grid-cols-12 gap-4 items-center">
                  <div className="col-span-3 flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] flex items-center justify-center flex-shrink-0">
                      <User className="h-5 w-5 text-white" />
                    </div>
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-foreground truncate">{request.fullName}</p>
                      <p className="text-xs text-[var(--color-muted-foreground)] truncate">{request.email}</p>
                    </div>
                  </div>
                  <div className="col-span-2">
                    <p className="text-sm text-foreground font-medium">{request.profession}</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">{request.credentials}</p>
                  </div>
                  <div className="col-span-2">
                    <div className="flex items-center gap-1.5">
                      <FileText className="h-3.5 w-3.5 text-[var(--color-muted-foreground)]" />
                      <span className="text-sm text-foreground font-mono">{request.licenseNumber || "Pending"}</span>
                    </div>
                  </div>
                  <div className="col-span-2">
                    <div className="flex items-center gap-1.5">
                      <Clock className="h-3.5 w-3.5 text-[var(--color-muted-foreground)]" />
                      <span className="text-sm text-foreground">{request.requestDate}</span>
                    </div>
                  </div>
                  <div className="col-span-1">
                    <StatusBadge status={request.status} size="sm" />
                  </div>
                  <div className="col-span-2 flex justify-end gap-2">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenModal(request);
                      }}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors text-[var(--color-primary)] hover:bg-[var(--color-primary-light)]`}
                    >
                      <Shield className="h-4 w-4" />
                      {request.status === "pending" ? "Submit Docs" : "View"}
                    </button>
                  </div>
                </div>

                {/* Mobile Card View */}
                <div className="lg:hidden p-4 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] flex items-center justify-center flex-shrink-0">
                        <User className="h-6 w-6 text-white" />
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-foreground">{request.fullName}</p>
                        <p className="text-xs text-[var(--color-muted-foreground)] truncate">{request.email}</p>
                      </div>
                    </div>
                    <StatusBadge status={request.status} size="sm" />
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleOpenModal(request);
                    }}
                    className="w-full inline-flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] transition-colors shadow-soft"
                  >
                    <Shield className="h-4 w-4" />
                    {request.status === "pending" ? "Submit Docs" : "View"}
                  </button>
                </div>
              </motion.div>
            ))
          )}
        </div>
      </div>

      {/* Review Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Verification Application Details"
      >
        {selectedRequest && (
          <div className="space-y-6">
            <div className="flex items-start gap-4 p-4 rounded-xl bg-[var(--color-secondary-bg)]">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] flex items-center justify-center flex-shrink-0">
                <User className="h-8 w-8 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-foreground">{selectedRequest.fullName}</h3>
                <div className="mt-2 space-y-1">
                  <div className="flex items-center gap-2 text-sm text-[var(--color-muted-foreground)]">
                    <Mail className="h-4 w-4" />
                    {selectedRequest.email}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-[var(--color-muted-foreground)]">
                    <Shield className="h-4 w-4" />
                    {selectedRequest.profession}
                  </div>
                </div>
              </div>
              <StatusBadge status={selectedRequest.status} />
            </div>

            {/* Application Data */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
               {/* Read-Only Info */}
               <div>
                <label className="block text-xs font-medium text-[var(--color-muted-foreground)] mb-1">Request Date</label>
                <p className="text-sm text-foreground">{selectedRequest.requestDate}</p>
              </div>
              <div>
                <label className="block text-xs font-medium text-[var(--color-muted-foreground)] mb-1">Credentials</label>
                <p className="text-sm text-foreground font-medium">{selectedRequest.credentials}</p>
              </div>

              {/* Editable License Info for Pending Requests OR Display for approved */}
              <div className="md:col-span-2 mt-2">
                <label className="block text-sm font-medium text-foreground mb-2">License Number</label>
                {selectedRequest.status === "pending" || selectedRequest.status === "under review" ? (
                  <input
                    type="text"
                    value={inputLicense}
                    onChange={(e) => setInputLicense(e.target.value)}
                    placeholder="e.g. MD-123456"
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  />
                ) : (
                  <p className="text-sm text-foreground font-mono">{selectedRequest.licenseNumber}</p>
                )}
              </div>
            </div>

            {/* Document Upload Area */}
            <div className="mt-4 border-t border-[var(--color-border)] pt-4">
              <label className="block text-sm font-medium text-foreground mb-3">Supporting Documents</label>
              
              {(selectedRequest.status === "pending" || selectedRequest.status === "under review") && (
                <div className="mb-4">
                  <div 
                    className="border-2 border-dashed border-[var(--color-primary)]/30 rounded-xl p-6 flex flex-col items-center justify-center bg-[var(--color-primary-light)]/20 hover:bg-[var(--color-primary-light)]/40 transition-colors cursor-pointer"
                    onClick={() => fileInputRef.current?.click()}
                  >
                    <Upload className="h-8 w-8 text-[var(--color-primary)] mb-2" />
                    <p className="text-sm font-medium text-foreground mb-1">Click to upload documents</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">Accepted: PDF, JPG, PNG | Max file size: 10MB</p>
                    <input 
                      type="file" 
                      className="hidden" 
                      ref={fileInputRef} 
                      onChange={handleFileChange}
                      accept=".pdf,.jpg,.jpeg,.png"
                      multiple
                    />
                  </div>
                </div>
              )}

              {uploadedFiles.length > 0 && (
                <div className="space-y-2">
                  <AnimatePresence>
                    {uploadedFiles.map((doc, index) => (
                      <motion.div 
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, x: -10 }}
                        key={index} 
                        className="flex items-center justify-between p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] hover:bg-[var(--color-secondary-bg)] transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <FileText className="h-5 w-5 text-[var(--color-primary)]" />
                          <div>
                            <span className="block text-sm font-medium text-foreground truncate max-w-[200px]">{doc.name}</span>
                            <span className="block text-xs text-[var(--color-muted-foreground)]">{doc.size}</span>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <button 
                            className="p-1.5 rounded-md text-[var(--color-primary)] hover:bg-[var(--color-primary-light)] transition-colors"
                            title="Preview File"
                            onClick={(e) => { e.preventDefault(); toast.info(`Previewing ${doc.name}`); }}
                          >
                            <Eye className="h-4 w-4" />
                          </button>
                          {(selectedRequest.status === "pending" || selectedRequest.status === "under review") && (
                            <button 
                              onClick={() => removeFile(index)}
                              className="p-1.5 rounded-md text-[var(--color-error)] hover:bg-[var(--color-error-light)] transition-colors"
                              title="Remove File"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          )}
                        </div>
                      </motion.div>
                    ))}
                  </AnimatePresence>
                </div>
              )}
            </div>

            {/* Admin Notes */}
            {selectedRequest.status === "pending" && (
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Admin Notes (Required for rejection)
                </label>
                <textarea
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  placeholder="Add notes about this verification decision..."
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  rows={2}
                />
              </div>
            )}

            {selectedRequest.notes && selectedRequest.status !== "pending" && (
              <div className="p-4 rounded-lg bg-[var(--color-secondary-bg)] border border-[var(--color-border)]">
                <div className="flex items-start gap-2">
                  <AlertCircle className="h-4 w-4 text-[var(--color-muted-foreground)] mt-0.5" />
                  <div>
                    <p className="text-xs font-medium text-[var(--color-muted-foreground)] mb-1">Admin Notes</p>
                    <p className="text-sm text-foreground">{selectedRequest.notes}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Action Footer */}
            {(selectedRequest.status === "pending" || selectedRequest.status === "under review") && (
              <div className="flex flex-col gap-3 pt-4 border-t border-[var(--color-border)]">
                <button
                  onClick={() => handleSubmitVerification(selectedRequest.id)}
                  className="w-full px-4 py-2.5 rounded-lg border border-[var(--color-primary)] text-[var(--color-primary)] hover:bg-[var(--color-primary-light)] text-sm font-medium transition-smooth flex items-center justify-center gap-2"
                >
                  <Upload className="h-4 w-4" />
                  Submit Verification Documents
                </button>
                <div className="flex gap-3">
                  <button
                    onClick={() => handleReject(selectedRequest.id)}
                    className="flex-1 px-4 py-2.5 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-sm font-medium text-[var(--color-error)] hover:bg-[var(--color-error-light)] transition-smooth flex items-center justify-center gap-2"
                  >
                    <XCircle className="h-4 w-4" />
                    Reject
                  </button>
                  <button
                    onClick={() => handleApprove(selectedRequest.id)}
                    className="flex-1 px-4 py-2.5 rounded-lg bg-[var(--color-primary)] text-sm font-medium text-white hover:bg-[var(--color-primary-hover)] transition-smooth flex items-center justify-center gap-2 shadow-soft"
                  >
                    <CheckCircle className="h-4 w-4" />
                    Approve
                  </button>
                </div>
              </div>
            )}
            
            {/* Admin Management Footer for Approved */}
            {selectedRequest.status === "approved" && (
              <div className="pt-4 border-t border-[var(--color-border)]">
                <h4 className="text-sm font-semibold text-foreground mb-3">Admin Actions</h4>
                <div className="flex flex-col gap-2">
                  <ActionButton 
                    variant="secondary" 
                    icon={XCircle} 
                    className="w-full"
                    onClick={() => setActionModal({ isOpen: true, type: "revoke", request: selectedRequest })}
                  >
                    Revoke Approval
                  </ActionButton>
                  <ActionButton 
                    variant="warning" 
                    icon={Clock} 
                    className="w-full"
                    onClick={() => setActionModal({ isOpen: true, type: "suspend", request: selectedRequest })}
                  >
                    Suspend Account
                  </ActionButton>
                  <ActionButton 
                    variant="error" 
                    icon={Trash2} 
                    className="w-full"
                    onClick={() => setActionModal({ isOpen: true, type: "delete", request: selectedRequest })}
                  >
                    Delete Account
                  </ActionButton>
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Admin Confimration Dialogs */}
      <Dialog open={actionModal.isOpen} onOpenChange={(open) => !open && setActionModal({ isOpen: false, type: null, request: null })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {actionModal.type === "revoke" && "Revoke Approval"}
              {actionModal.type === "suspend" && "Suspend Account"}
              {actionModal.type === "delete" && "Delete Account"}
            </DialogTitle>
            <DialogDescription>
              {actionModal.type === "revoke" && `Are you sure you want to revoke the medical approval for ${actionModal.request?.fullName}? Their status will change to Rejected.`}
              {actionModal.type === "suspend" && `Are you sure you want to suspend medical posting privileges for ${actionModal.request?.fullName}? Their status will change to Pending.`}
              {actionModal.type === "delete" && `Are you sure you want to permanently delete the application and records for ${actionModal.request?.fullName}? This cannot be undone.`}
            </DialogDescription>
          </DialogHeader>

          <DialogFooter className="mt-4">
            <DialogClose asChild>
              <ActionButton variant="secondary">Cancel</ActionButton>
            </DialogClose>
            <ActionButton 
              variant={actionModal.type === "delete" ? "error" : actionModal.type === "suspend" ? "warning" : "primary"} 
              onClick={executeAdminAction}
            >
              Confirm
            </ActionButton>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}