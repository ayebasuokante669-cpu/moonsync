import { useState, useEffect, useRef } from "react";
import { Users, Activity, MessageSquare, FileText, Plus, AlertTriangle, UserCheck, ChevronRight } from "lucide-react";
import { useNavigate, useLocation } from "react-router";
import { StatsCard } from "../components/admin/StatsCard";
import { ActionButton } from "../components/admin/ActionButton";
import { StatusBadge } from "../components/admin/StatusBadge";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { api } from "../lib/api";

const chartData = [
  { id: "jan", name: "Jan", users: 1200, engagement: 850 },
  { id: "feb", name: "Feb", users: 1890, engagement: 1240 },
  { id: "mar", name: "Mar", users: 2100, engagement: 1450 },
  { id: "apr", name: "Apr", users: 2780, engagement: 1890 },
  { id: "may", name: "May", users: 3200, engagement: 2240 },
  { id: "jun", name: "Jun", users: 3890, engagement: 2680 },
];

const initialRecentActivity = [
  { id: 1, type: "user", message: "New user registered: Emma Johnson", time: "2 minutes ago", status: "active", route: "/users" },
  { id: 2, type: "report", message: "Community post reported for review", time: "15 minutes ago", status: "flagged", route: "/community" },
  { id: 3, type: "medical", message: "Medical personnel verification request: Dr. Sarah", time: "45 minutes ago", status: "pending", route: "/medical-verification" },
  { id: 4, type: "article", message: "New article published: Understanding Your Cycle", time: "1 hour ago", status: "published", route: "/articles" },
  { id: 5, type: "report", message: "Chatbot conversation flagged", time: "2 hours ago", status: "pending", route: "/chatbot" },
  { id: 6, type: "user", message: "User account suspended: Spam behavior", time: "3 hours ago", status: "banned", route: "/users" },
  { id: 7, type: "medical", message: "Verification approved: Dr. Michael Chen", time: "5 hours ago", status: "approved", route: "/medical-verification" },
  { id: 8, type: "system", message: "System maintenance completed", time: "2 days ago", status: "active", route: "/" },
];

export function Dashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const activityRef = useRef(null);

  const [activities, setActivities] = useState(initialRecentActivity);
  const [showAllActivity, setShowAllActivity] = useState(false);
  const [stats, setStats] = useState(null);

  useEffect(() => {
    api.get("/admin/stats")
      .then(setStats)
      .catch(() => {});
  }, []);

  // Auto-clear activities older than 24 hours on mount
  useEffect(() => {
    setActivities(current => 
      current.filter(act => !act.time.includes("day") && !act.time.includes("week"))
    );
  }, []);

  // Handle scroll to activity from notifications dropdown
  useEffect(() => {
    if (location.state?.scrollToActivity && activityRef.current) {
      setTimeout(() => {
        activityRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
        // Clean up state so refreshing won't scroll again
        window.history.replaceState({}, document.title);
      }, 100);
    }
  }, [location.state]);

  const displayedActivities = showAllActivity ? activities : activities.slice(0, 5);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Dashboard</h1>
        <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
          Welcome back, Super Admin. Here's what's happening with MoonSync today.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Total Users"
          value={stats ? stats.total_users.toLocaleString() : "—"}
          icon={Users}
          trend={{ value: "12.5%", isPositive: true }}
          description="from last month"
        />
        <StatsCard
          title="Active Users"
          value={stats ? stats.active_users.toLocaleString() : "—"}
          icon={Activity}
          trend={{ value: "8.2%", isPositive: true }}
          description="from last month"
        />
        <StatsCard
          title="Chatbot Conversations"
          value={stats ? stats.chatbot_conversations.toLocaleString() : "—"}
          icon={MessageSquare}
          trend={{ value: "3.1%", isPositive: false }}
          description="today"
        />
        <StatsCard
          title="Community Posts"
          value={stats ? stats.community_posts.toLocaleString() : "—"}
          icon={FileText}
          trend={{ value: "15.3%", isPositive: true }}
          description="today"
        />
      </div>

      {/* Charts and Activity */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* User Growth Chart */}
        <div className="lg:col-span-2">
          <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
            <h3 className="text-lg font-semibold text-foreground">User Growth &amp; Engagement</h3>
            <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">Monthly trends over the last 6 months</p>
            
            <div className="mt-6 h-[300px] min-h-[300px] w-full">
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                  <XAxis dataKey="name" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                  <YAxis stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'white',
                      border: '1px solid var(--color-border)',
                      borderRadius: '8px',
                      fontSize: '12px',
                    }}
                  />
                  <Line
                    key="users-line"
                    type="monotone"
                    dataKey="users"
                    stroke="var(--color-primary)"
                    strokeWidth={2}
                    dot={{ fill: 'var(--color-primary)', r: 4 }}
                    name="Total Users"
                  />
                  <Line
                    key="engagement-line"
                    type="monotone"
                    dataKey="engagement"
                    stroke="var(--color-chart-4)"
                    strokeWidth={2}
                    dot={{ fill: 'var(--color-chart-4)', r: 4 }}
                    name="Engagement"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
          <h3 className="text-lg font-semibold text-foreground">Quick Actions</h3>
          <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">Common admin tasks</p>
          
          <div className="mt-6 space-y-3">
            <ActionButton
              variant="primary"
              icon={Plus}
              className="w-full"
              onClick={() => navigate("/articles")}
            >
              Create Article
            </ActionButton>
            <ActionButton
              variant="secondary"
              icon={AlertTriangle}
              className="w-full"
              onClick={() => navigate("/community")}
            >
              Review Reports
            </ActionButton>
            <ActionButton
              variant="secondary"
              icon={MessageSquare}
              className="w-full"
              onClick={() => navigate("/notifications")}
            >
              Send Notification
            </ActionButton>
            <ActionButton
              variant="ghost"
              icon={UserCheck}
              className="w-full"
              onClick={() => navigate("/users")}
            >
              Manage Users
            </ActionButton>
          </div>
        </div>
      </div>

      {/* Recent Activity Feed */}
      <div ref={activityRef} className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6 scroll-mt-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold text-foreground">Recent Activity</h3>
            <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">Latest events across the platform</p>
          </div>
          <button
            onClick={() => navigate("/archive")}
            className="group flex items-center gap-1 text-sm font-medium text-[var(--color-primary)] hover:underline"
          >
            View All in Archive
            <ChevronRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
          </button>
        </div>

        <div className="mt-6 space-y-4">
          {displayedActivities.length === 0 ? (
            <div className="py-8 text-center text-sm text-[var(--color-muted-foreground)]">
              No recent activity.
            </div>
          ) : (
            displayedActivities.map((activity) => (
              <div
                key={activity.id}
                onClick={() => navigate(activity.route)}
                className="flex cursor-pointer items-start justify-between rounded-lg border border-[var(--color-border)] bg-[var(--color-secondary-bg)] p-4 transition-smooth hover:shadow-soft hover:border-[var(--color-primary)]/30"
              >
                <div className="flex-1">
                  <p className="text-sm font-medium text-foreground">{activity.message}</p>
                  <p className="mt-1 text-xs text-[var(--color-muted-foreground)]">{activity.time}</p>
                </div>
                <StatusBadge status={activity.status} size="sm" />
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}