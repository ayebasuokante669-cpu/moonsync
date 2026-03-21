import { useState } from "react";
import { X, ChevronDown } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { toast } from "sonner";
import { ActionButton } from "../admin/ActionButton";

export function MedicalPersonnelModal({ isOpen, onClose, onSubmitSuccess }) {
  const [formData, setFormData] = useState({
    fullName: "",
    phoneNumber: "",
    profession: "",
    hospitalName: "",
    yearsOfExperience: "",
    country: "",
    stateRegion: "",
  });

  const [errors, setErrors] = useState({});

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.fullName.trim()) newErrors.fullName = "Full Name is required";
    if (!formData.phoneNumber.trim()) newErrors.phoneNumber = "Phone Number is required";
    if (!formData.profession) newErrors.profession = "Profession is required";
    if (!formData.hospitalName.trim()) newErrors.hospitalName = "Hospital name is required";
    if (!formData.yearsOfExperience) newErrors.yearsOfExperience = "Years of experience is required";
    if (!formData.country.trim()) newErrors.country = "Country is required";
    if (!formData.stateRegion.trim()) newErrors.stateRegion = "State/Region is required";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      toast.success("Your medical personnel verification request has been submitted and is pending review.");
      onSubmitSuccess(formData);
      onClose();
      // Reset form
      setFormData({
        fullName: "",
        phoneNumber: "",
        profession: "",
        hospitalName: "",
        yearsOfExperience: "",
        country: "",
        stateRegion: "",
      });
    }
  };

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="absolute inset-0 bg-black/60 backdrop-blur-sm"
          onClick={onClose}
        />
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          className="relative w-full max-w-2xl bg-[var(--color-card)] rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]"
        >
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b border-[var(--color-border)]">
            <div>
              <h2 className="text-xl font-semibold text-foreground">Medical Personnel Verification</h2>
              <p className="text-sm text-[var(--color-muted-foreground)] mt-1">Submit applicant details for review.</p>
            </div>
            <button
              onClick={onClose}
              className="p-2 -mr-2 text-[var(--color-muted-foreground)] hover:text-foreground hover:bg-[var(--color-muted)] rounded-lg transition-smooth"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Body */}
          <div className="p-6 overflow-y-auto custom-scrollbar">
            <form id="medical-form" onSubmit={handleSubmit} className="space-y-6">
              {/* Personal Information */}
              <div>
                <h3 className="text-sm font-semibold text-foreground uppercase tracking-wider mb-4">Personal Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1">Full Name</label>
                    <input
                      type="text"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleChange}
                      className={`w-full rounded-lg border ${errors.fullName ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                      placeholder="e.g. Dr. Jane Doe"
                    />
                    {errors.fullName && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.fullName}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1">Phone Number</label>
                    <input
                      type="tel"
                      name="phoneNumber"
                      value={formData.phoneNumber}
                      onChange={handleChange}
                      className={`w-full rounded-lg border ${errors.phoneNumber ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                      placeholder="+1 (555) 000-0000"
                    />
                    {errors.phoneNumber && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.phoneNumber}</p>}
                  </div>
                </div>
              </div>

              {/* Professional Information */}
              <div>
                <h3 className="text-sm font-semibold text-foreground uppercase tracking-wider mb-4 pb-2 border-t border-[var(--color-border)] pt-4">Professional Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div className="relative">
                    <label className="block text-sm font-medium text-foreground mb-1">Profession</label>
                    <select
                      name="profession"
                      value={formData.profession}
                      onChange={handleChange}
                      className={`w-full rounded-lg border appearance-none ${errors.profession ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                    >
                      <option value="">Select a profession</option>
                      <option value="Doctor">Doctor</option>
                      <option value="Nurse">Nurse</option>
                      <option value="Pharmacist">Pharmacist</option>
                      <option value="Lab Technician">Lab Technician</option>
                      <option value="Medical Assistant">Medical Assistant</option>
                      <option value="Other">Other</option>
                    </select>
                    <ChevronDown className="absolute right-4 top-[38px] h-4 w-4 text-[var(--color-muted-foreground)] pointer-events-none" />
                    {errors.profession && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.profession}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1">Years of Experience</label>
                    <input
                      type="number"
                      name="yearsOfExperience"
                      value={formData.yearsOfExperience}
                      onChange={handleChange}
                      min="0"
                      className={`w-full rounded-lg border ${errors.yearsOfExperience ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                      placeholder="e.g. 5"
                    />
                    {errors.yearsOfExperience && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.yearsOfExperience}</p>}
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-foreground mb-1">Hospital / Institution Name</label>
                    <input
                      type="text"
                      name="hospitalName"
                      value={formData.hospitalName}
                      onChange={handleChange}
                      className={`w-full rounded-lg border ${errors.hospitalName ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                      placeholder="Enter hospital or clinic name"
                    />
                    {errors.hospitalName && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.hospitalName}</p>}
                  </div>
                </div>
              </div>

              {/* Location */}
              <div>
                <h3 className="text-sm font-semibold text-foreground uppercase tracking-wider mb-4 pb-2 border-t border-[var(--color-border)] pt-4">Location</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1">Country</label>
                    <input
                      type="text"
                      name="country"
                      value={formData.country}
                      onChange={handleChange}
                      className={`w-full rounded-lg border ${errors.country ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                      placeholder="Country"
                    />
                    {errors.country && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.country}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1">State / Region</label>
                    <input
                      type="text"
                      name="stateRegion"
                      value={formData.stateRegion}
                      onChange={handleChange}
                      className={`w-full rounded-lg border ${errors.stateRegion ? "border-[var(--color-error)]" : "border-[var(--color-input-border)]"} bg-[var(--color-input)] px-4 py-2.5 text-sm focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20`}
                      placeholder="State or Region"
                    />
                    {errors.stateRegion && <p className="mt-1 text-xs text-[var(--color-error)]">{errors.stateRegion}</p>}
                  </div>
                </div>
              </div>
            </form>
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 p-6 border-t border-[var(--color-border)] bg-[var(--color-secondary-bg)]">
            <ActionButton variant="secondary" onClick={onClose}>
              Cancel
            </ActionButton>
            <ActionButton type="submit" variant="primary" form="medical-form">
              Submit Application
            </ActionButton>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
