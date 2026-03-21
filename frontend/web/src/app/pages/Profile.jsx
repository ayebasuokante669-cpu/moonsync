import { useState } from "react";
import { User, Mail, Shield, Key, Bell, Save, Camera } from "lucide-react";
import { ActionButton } from "../components/admin/ActionButton";
import { motion } from "motion/react";
import { toast } from "sonner";

export function Profile() {
  const [profileData, setProfileData] = useState({
    fullName: "Super Admin",
    email: "admin@moonsync.app",
    role: "Super Administrator",
    phone: "+1 (555) 123-4567",
    timezone: "America/New_York",
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [notificationSettings, setNotificationSettings] = useState({
    emailNotifications: true,
    pushNotifications: true,
    reportAlerts: true,
    systemUpdates: false,
  });

  const handleProfileSave = () => {
    toast.success("Profile updated successfully", {
      description: "Your profile information has been saved",
    });
  };

  const handlePasswordChange = () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error("Passwords don't match", {
        description: "Please make sure your passwords match",
      });
      return;
    }
    if (passwordData.newPassword.length < 8) {
      toast.error("Password too short", {
        description: "Password must be at least 8 characters",
      });
      return;
    }
    toast.success("Password changed successfully", {
      description: "Your password has been updated",
    });
    setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" });
  };

  const handleNotificationSave = () => {
    toast.success("Notification preferences saved", {
      description: "Your notification settings have been updated",
    });
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Profile Settings</h1>
        <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
          Manage your account settings and preferences
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Profile Card */}
        <div className="lg:col-span-1">
          <motion.div
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
          >
            <div className="flex flex-col items-center text-center">
              {/* Avatar */}
              <div className="relative">
                <div className="w-24 h-24 rounded-full bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)] flex items-center justify-center text-2xl font-semibold text-white">
                  SA
                </div>
                <button className="absolute bottom-0 right-0 w-8 h-8 rounded-full bg-[var(--color-card)] border-2 border-[var(--color-border)] flex items-center justify-center hover:bg-[var(--color-secondary-bg)] transition-smooth shadow-soft">
                  <Camera className="h-4 w-4 text-[var(--color-muted-foreground)]" />
                </button>
              </div>

              {/* User Info */}
              <h2 className="mt-4 text-xl font-semibold text-foreground">{profileData.fullName}</h2>
              <p className="text-sm text-[var(--color-muted-foreground)] mt-1">{profileData.email}</p>
              
              {/* Role Badge */}
              <div className="mt-4 inline-flex items-center gap-2 px-4 py-2 rounded-full bg-[var(--color-primary-light)] border border-[var(--color-primary)]">
                <Shield className="h-4 w-4 text-[var(--color-primary)]" />
                <span className="text-sm font-medium text-[var(--color-primary)]">{profileData.role}</span>
              </div>

              {/* Stats */}
              <div className="w-full mt-6 pt-6 border-t border-[var(--color-border)]">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-2xl font-semibold text-foreground">127</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">Actions Today</p>
                  </div>
                  <div>
                    <p className="text-2xl font-semibold text-foreground">3,890</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">Total Users</p>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Settings Forms */}
        <div className="lg:col-span-2 space-y-6">
          {/* Profile Information */}
          <motion.div
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            <div className="flex items-center gap-2 mb-6">
              <User className="h-5 w-5 text-[var(--color-primary)]" />
              <h3 className="text-lg font-semibold text-foreground">Profile Information</h3>
            </div>

            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">
                    Full Name
                  </label>
                  <input
                    type="text"
                    value={profileData.fullName}
                    onChange={(e) => setProfileData({ ...profileData, fullName: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">
                    Email Address
                  </label>
                  <input
                    type="email"
                    value={profileData.email}
                    onChange={(e) => setProfileData({ ...profileData, email: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">
                    Phone Number
                  </label>
                  <input
                    type="tel"
                    value={profileData.phone}
                    onChange={(e) => setProfileData({ ...profileData, phone: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">
                    Timezone
                  </label>
                  <select
                    value={profileData.timezone}
                    onChange={(e) => setProfileData({ ...profileData, timezone: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  >
                    <option value="America/New_York">Eastern Time (ET)</option>
                    <option value="America/Chicago">Central Time (CT)</option>
                    <option value="America/Denver">Mountain Time (MT)</option>
                    <option value="America/Los_Angeles">Pacific Time (PT)</option>
                  </select>
                </div>
              </div>

              <div className="pt-4">
                <ActionButton variant="primary" icon={Save} onClick={handleProfileSave}>
                  Save Profile Changes
                </ActionButton>
              </div>
            </div>
          </motion.div>

          {/* Security Settings */}
          <motion.div
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <div className="flex items-center gap-2 mb-6">
              <Key className="h-5 w-5 text-[var(--color-primary)]" />
              <h3 className="text-lg font-semibold text-foreground">Security Settings</h3>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Current Password
                </label>
                <input
                  type="password"
                  value={passwordData.currentPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                  placeholder="Enter current password"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">
                    New Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.newPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                    placeholder="Enter new password"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">
                    Confirm Password
                  </label>
                  <input
                    type="password"
                    value={passwordData.confirmPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                    className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                    placeholder="Confirm new password"
                  />
                </div>
              </div>

              <p className="text-xs text-[var(--color-muted-foreground)]">
                Password must be at least 8 characters long and include uppercase, lowercase, and numbers
              </p>

              <div className="pt-4">
                <ActionButton variant="secondary" icon={Key} onClick={handlePasswordChange}>
                  Update Password
                </ActionButton>
              </div>
            </div>
          </motion.div>

          {/* Notification Preferences */}
          <motion.div
            className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <div className="flex items-center gap-2 mb-6">
              <Bell className="h-5 w-5 text-[var(--color-primary)]" />
              <h3 className="text-lg font-semibold text-foreground">Notification Preferences</h3>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 rounded-lg bg-[var(--color-secondary-bg)]">
                <div>
                  <p className="text-sm font-medium text-foreground">Email Notifications</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Receive updates via email</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={notificationSettings.emailNotifications}
                    onChange={(e) => setNotificationSettings({ ...notificationSettings, emailNotifications: e.target.checked })}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-[var(--color-muted)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--color-primary)]/20 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--color-primary)]"></div>
                </label>
              </div>

              <div className="flex items-center justify-between p-4 rounded-lg bg-[var(--color-secondary-bg)]">
                <div>
                  <p className="text-sm font-medium text-foreground">Push Notifications</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Get desktop push notifications</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={notificationSettings.pushNotifications}
                    onChange={(e) => setNotificationSettings({ ...notificationSettings, pushNotifications: e.target.checked })}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-[var(--color-muted)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--color-primary)]/20 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--color-primary)]"></div>
                </label>
              </div>

              <div className="flex items-center justify-between p-4 rounded-lg bg-[var(--color-secondary-bg)]">
                <div>
                  <p className="text-sm font-medium text-foreground">Report Alerts</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Immediate alerts for urgent reports</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={notificationSettings.reportAlerts}
                    onChange={(e) => setNotificationSettings({ ...notificationSettings, reportAlerts: e.target.checked })}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-[var(--color-muted)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--color-primary)]/20 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--color-primary)]"></div>
                </label>
              </div>

              <div className="flex items-center justify-between p-4 rounded-lg bg-[var(--color-secondary-bg)]">
                <div>
                  <p className="text-sm font-medium text-foreground">System Updates</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Platform updates and maintenance</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={notificationSettings.systemUpdates}
                    onChange={(e) => setNotificationSettings({ ...notificationSettings, systemUpdates: e.target.checked })}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-[var(--color-muted)] peer-focus:outline-none peer-focus:ring-2 focus:ring-[var(--color-primary)]/20 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--color-primary)]"></div>
                </label>
              </div>

              <div className="pt-4">
                <ActionButton variant="primary" icon={Save} onClick={handleNotificationSave}>
                  Save Preferences
                </ActionButton>
              </div>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
