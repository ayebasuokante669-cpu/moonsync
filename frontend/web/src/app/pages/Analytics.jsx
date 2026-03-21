import { Users, Activity, MessageSquare, TrendingUp } from "lucide-react";
import { StatsCard } from "../components/admin/StatsCard";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";

const userGrowthData = [
  { id: "jul", month: "Jul", users: 980 },
  { id: "aug", month: "Aug", users: 1420 },
  { id: "sep", month: "Sep", users: 1890 },
  { id: "oct", month: "Oct", users: 2340 },
  { id: "nov", month: "Nov", users: 2890 },
  { id: "dec", month: "Dec", users: 3200 },
  { id: "jan", month: "Jan", users: 3650 },
  { id: "feb", month: "Feb", users: 3890 },
];

const engagementData = [
  { id: "mon", day: "Mon", sessions: 2400 },
  { id: "tue", day: "Tue", sessions: 2680 },
  { id: "wed", day: "Wed", sessions: 2340 },
  { id: "thu", day: "Thu", sessions: 2890 },
  { id: "fri", day: "Fri", sessions: 3100 },
  { id: "sat", day: "Sat", sessions: 2650 },
  { id: "sun", day: "Sun", sessions: 2200 },
];

const chatbotUsageData = [
  { id: "h00", hour: "00:00", conversations: 45 },
  { id: "h04", hour: "04:00", conversations: 12 },
  { id: "h08", hour: "08:00", conversations: 156 },
  { id: "h12", hour: "12:00", conversations: 234 },
  { id: "h16", hour: "16:00", conversations: 198 },
  { id: "h20", hour: "20:00", conversations: 267 },
  { id: "h23", hour: "23:00", conversations: 89 },
];

const featureUsageData = [
  { id: "cycle", name: "Cycle Tracking", value: 3456, color: "var(--color-chart-1)" },
  { id: "community", name: "Community", value: 2103, color: "var(--color-chart-2)" },
  { id: "chatbot", name: "AI Chatbot", value: 1876, color: "var(--color-chart-3)" },
  { id: "articles", name: "Articles", value: 1543, color: "var(--color-chart-4)" },
];

export function Analytics() {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Analytics</h1>
        <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
          Comprehensive insights into user behavior and platform performance
        </p>
      </div>

      {/* Overview Stats */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Total Users"
          value="3,890"
          icon={Users}
          trend={{ value: "18.2%", isPositive: true }}
          description="vs last month"
        />
        <StatsCard
          title="Daily Active Users"
          value="2,680"
          icon={Activity}
          trend={{ value: "12.5%", isPositive: true }}
          description="vs last month"
        />
        <StatsCard
          title="Avg Session Time"
          value="8.4 min"
          icon={TrendingUp}
          trend={{ value: "5.3%", isPositive: true }}
          description="vs last month"
        />
        <StatsCard
          title="Community Posts"
          value="1,247"
          icon={MessageSquare}
          trend={{ value: "23.1%", isPositive: true }}
          description="this week"
        />
      </div>

      {/* User Growth Chart */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-lg font-semibold text-foreground">User Growth</h3>
            <p className="text-sm text-[var(--color-muted-foreground)]">Total registered users over time</p>
          </div>
          <select className="rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-3 py-2 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth">
            <option>Last 8 months</option>
            <option>Last 6 months</option>
            <option>Last year</option>
          </select>
        </div>
        
        <div className="h-[320px] min-h-[320px] w-full">
          <ResponsiveContainer width="100%" height={320}>
            <LineChart data={userGrowthData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="month" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
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
                key="users-growth-line"
                type="monotone"
                dataKey="users"
                stroke="var(--color-primary)"
                strokeWidth={3}
                dot={{ fill: 'var(--color-primary)', r: 5 }}
                name="Total Users"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Engagement and Feature Usage */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Daily Engagement */}
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
          <h3 className="text-lg font-semibold text-foreground mb-1">Daily Engagement</h3>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-6">User sessions by day of week</p>
          
          <div className="h-[280px] min-h-[280px] w-full">
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={engagementData}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                <XAxis dataKey="day" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                <YAxis stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'white',
                    border: '1px solid var(--color-border)',
                    borderRadius: '8px',
                    fontSize: '12px',
                  }}
                />
                <Bar key="sessions-bar" dataKey="sessions" fill="var(--color-primary)" radius={[8, 8, 0, 0]} name="Sessions" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Feature Usage */}
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
          <h3 className="text-lg font-semibold text-foreground mb-1">Feature Usage</h3>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-6">Distribution of user activity across features</p>
          
          <div className="h-[280px] min-h-[280px] w-full flex items-center justify-center">
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={featureUsageData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {featureUsageData.map((entry) => (
                    <Cell key={`cell-${entry.id}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'white',
                    border: '1px solid var(--color-border)',
                    borderRadius: '8px',
                    fontSize: '12px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          
          {/* Legend */}
          <div className="grid grid-cols-2 gap-3 mt-4">
            {featureUsageData.map((item) => (
              <div key={`legend-${item.id}`} className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full" style={{ backgroundColor: item.color }}></div>
                <span className="text-sm text-[var(--color-muted-foreground)]">{item.name}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Chatbot Usage */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-lg font-semibold text-foreground">AI Chatbot Usage</h3>
            <p className="text-sm text-[var(--color-muted-foreground)]">Conversation activity throughout the day</p>
          </div>
        </div>
        
        <div className="h-[280px] min-h-[280px] w-full">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={chatbotUsageData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="hour" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
              <YAxis stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid var(--color-border)',
                  borderRadius: '8px',
                  fontSize: '12px',
                }}
              />
              <Bar key="conversations-bar" dataKey="conversations" fill="var(--color-chart-4)" radius={[8, 8, 0, 0]} name="Conversations" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}