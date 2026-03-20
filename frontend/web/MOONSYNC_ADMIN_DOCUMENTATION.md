# MoonSync Admin Portal - Complete Documentation

## 🌙 Overview

MoonSync Admin Portal is a production-ready, enterprise-grade admin dashboard for managing the MoonSync menstrual wellness Android application. Built with React, TypeScript, and Tailwind CSS v4.

### Design Philosophy

- **Minimal & Calm**: Clean interfaces with intentional white space
- **Healthcare-Grade Clarity**: High readability and accessibility
- **Professional Feminine**: Soft lavender palette that's elegant and sophisticated
- **Responsive & Adaptive**: Seamless experience across all devices

---

## 🎨 Design System

### Color Palette

```css
Primary (Soft Lavender):    #9b87e8
Primary Hover:              #8a75db
Primary Light:              #e8e3fc

Success (Soft Sage):        #86b896
Warning (Muted Amber):      #e6b17e
Error (Soft Rose):          #e88b9d

Background:                 #ffffff
Secondary Background:       #f6f4ff
Muted:                      #f5f5f7
```

### Typography

- **Font Family**: Inter (Google Fonts)
- **Scale**: 
  - H1: 1.5rem (24px) - Page titles
  - H2: 1.25rem (20px) - Section headers
  - H3: 1.125rem (18px) - Card headers
  - Body: 1rem (16px) - Regular text
  - Small: 0.875rem (14px) - UI elements
  - XS: 0.75rem (12px) - Labels

### Spacing System

Built on an 8px grid system:
- 4px, 8px, 12px, 16px, 24px, 32px, 48px, 64px

### Border Radius

- Small: 8px
- Medium: 12px (default)
- Large: 16px

### Shadows

Subtle, soft floating shadows:
- `shadow-soft`: Light elevation
- `shadow-soft-lg`: Medium elevation

---

## 📁 Project Structure

```
/src
├── /app
│   ├── App.tsx                    # Main application entry
│   ├── routes.tsx                 # React Router configuration
│   │
│   ├── /components
│   │   ├── /layout
│   │   │   ├── RootLayout.tsx     # Main layout wrapper
│   │   │   ├── Sidebar.tsx        # Collapsible navigation
│   │   │   └── Topbar.tsx         # Top navigation bar
│   │   │
│   │   └── /admin                 # Reusable admin components
│   │       ├── StatsCard.tsx      # Metric display cards
│   │       ├── StatusBadge.tsx    # Status indicators
│   │       ├── ActionButton.tsx   # Button component
│   │       ├── DataTable.tsx      # Table component
│   │       ├── Modal.tsx          # Modal dialogs
│   │       ├── Alert.tsx          # Alert messages
│   │       ├── Card.tsx           # Container card
│   │       ├── Tabs.tsx           # Tab navigation
│   │       ├── FilterBar.tsx      # Search & filters
│   │       ├── PageHeader.tsx     # Page title header
│   │       ├── EmptyState.tsx     # Empty state UI
│   │       └── LoadingSpinner.tsx # Loading states
│   │
│   └── /pages                     # Application screens
│       ├── Dashboard.tsx          # Main dashboard
│       ├── Users.tsx              # User management
│       ├── CommunityModeration.tsx # Community reports
│       ├── ChatbotReports.tsx     # AI chatbot flags
│       ├── Articles.tsx           # Content management
│       ├── MenstrualLogs.tsx      # Health log monitoring
│       ├── Notifications.tsx      # Push notification manager
│       ├── Analytics.tsx          # Data visualization
│       ├── Settings.tsx           # System configuration
│       └── SystemInfo.tsx         # Design system showcase
│
└── /styles
    ├── theme.css                  # Design tokens & variables
    ├── fonts.css                  # Font imports
    └── index.css                  # Global styles
```

---

## 🧩 Component Library

### 1. StatsCard
Display key metrics with trend indicators.

```tsx
<StatsCard
  title="Total Users"
  value="3,890"
  icon={Users}
  trend={{ value: "12.5%", isPositive: true }}
  description="from last month"
/>
```

### 2. StatusBadge
Visual status indicators.

```tsx
<StatusBadge status="active" size="md" />
// Options: active, inactive, pending, banned, resolved, flagged, draft, published
```

### 3. ActionButton
Flexible button component with variants.

```tsx
<ActionButton variant="primary" icon={Plus} size="md">
  Create Article
</ActionButton>
// Variants: primary, secondary, success, warning, error, ghost
```

### 4. Modal
Reusable modal dialog.

```tsx
<Modal
  isOpen={showModal}
  onClose={() => setShowModal(false)}
  title="Modal Title"
  description="Description text"
  size="md"
>
  {/* Content */}
</Modal>
```

### 5. Alert
Notification alerts with variants.

```tsx
<Alert
  variant="success"
  title="Success!"
  message="Operation completed successfully."
/>
// Variants: success, error, warning, info
```

### 6. Card
Container component for content sections.

```tsx
<Card padding="md" hover>
  {/* Content */}
</Card>
```

### 7. DataTable
Flexible table component for data display.

```tsx
<DataTable
  columns={columns}
  data={data}
  onRowClick={(row) => handleClick(row)}
/>
```

### 8. FilterBar
Search and filter interface.

```tsx
<FilterBar
  searchPlaceholder="Search..."
  searchValue={query}
  onSearchChange={setQuery}
  filters={filterOptions}
/>
```

---

## 📄 Page Descriptions

### Dashboard (`/`)
Executive overview with:
- 4 key metric cards
- User growth line chart
- Recent activity feed
- Quick action buttons

### Users (`/users`)
User management interface with:
- Searchable user table
- User detail drawer (right slide-in)
- Bulk actions
- Status filtering

### Community Moderation (`/community`)
Review reported community posts:
- Report queue with severity badges
- Post preview cards
- Approve/Delete/Warn actions
- Reason tagging

### Chatbot Reports (`/chatbot-reports`)
AI conversation monitoring:
- Flagged conversation list
- Full chat transcript view
- Internal note system
- Resolution workflow

### Articles (`/articles`)
Content management system:
- Article grid layout
- Rich text editor modal
- Category management
- Draft/Published status

### Menstrual Logs (`/menstrual-logs`)
**Sensitive health data monitoring**:
- Privacy-first design
- Only flagged logs shown
- Anonymized user display
- Medical concern alerts

### Notifications (`/notifications`)
Push notification manager:
- Notification composer
- Audience segmentation
- Schedule system
- Mobile preview

### Analytics (`/analytics`)
Data visualization dashboard:
- User growth trends
- Engagement metrics
- Feature usage pie chart
- Chatbot usage patterns

### Settings (`/settings`)
System configuration:
- Feature toggles
- Community rules editor
- API configuration
- System information

### System Info (`/system-info`)
Design system showcase:
- Component library examples
- Color palette
- Typography scale
- Design principles

---

## 🎯 Key Features

### Responsive Design
- **Desktop**: Full sidebar, expanded tables
- **Tablet**: Collapsible sidebar, adjusted layouts
- **Mobile**: Hamburger menu, stacked cards, scrollable tables

### Navigation
- React Router with Data API
- Active state highlighting
- Smooth transitions
- Auto-close mobile menu on navigation

### Accessibility
- Semantic HTML
- Keyboard navigation support
- Focus management
- High contrast ratios (WCAG AA compliant)

### Performance
- Optimized re-renders
- Lazy loading charts
- Efficient state management
- Smooth 60fps animations

---

## 🚀 Technical Stack

- **Framework**: React 18.3.1
- **Routing**: React Router 7
- **Styling**: Tailwind CSS v4
- **Charts**: Recharts 2.15.2
- **Icons**: Lucide React
- **Build Tool**: Vite 6.3.5
- **Language**: TypeScript

---

## 🎨 Design Tokens Reference

All design tokens are defined in `/src/styles/theme.css`:

```css
/* Primary Colors */
--color-primary: #9b87e8
--color-primary-hover: #8a75db
--color-primary-light: #e8e3fc

/* State Colors */
--color-success: #86b896
--color-warning: #e6b17e
--color-error: #e88b9d

/* Neutral Colors */
--color-background: #ffffff
--color-foreground: #1a1a2e
--color-muted: #f5f5f7
--color-muted-foreground: #6b7280

/* Spacing (8px grid) */
--spacing-1: 0.5rem   (8px)
--spacing-2: 1rem     (16px)
--spacing-3: 1.5rem   (24px)
--spacing-4: 2rem     (32px)
```

---

## 📱 Responsive Breakpoints

```css
Mobile:     < 640px
Tablet:     640px - 1024px
Desktop:    > 1024px
```

---

## 🔐 Security Considerations

1. **Sensitive Data Handling**
   - Menstrual logs are anonymized
   - API keys are masked by default
   - User privacy is paramount

2. **Role-Based Access**
   - Super Admin level access
   - Audit logging ready
   - Action confirmation for destructive operations

---

## 💡 Usage Best Practices

### 1. Creating New Pages
```tsx
import { PageHeader } from "../components/admin";

export function NewPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Page Title"
        description="Description"
      />
      {/* Content */}
    </div>
  );
}
```

### 2. Using the Design System
Always use CSS variables for colors:
```tsx
className="bg-[var(--color-primary)] text-[var(--color-primary-foreground)]"
```

### 3. Maintaining Consistency
- Use existing components first
- Follow the 8px spacing grid
- Use defined color tokens
- Match typography scale

---

## 🌟 Future Enhancements

Potential additions:
- Real-time notifications with WebSocket
- Advanced data export functionality
- Multi-language support (i18n)
- Dark mode toggle
- Keyboard shortcuts panel
- Advanced filtering and sorting

---

## 📝 License & Credits

**Product**: MoonSync Admin Portal  
**Design**: Professional Health-Tech SaaS  
**Build Date**: February 12, 2026  
**Version**: 2.1.0

---

## 🤝 Support

For questions or support:
- Visit `/system-info` for design system documentation
- Check component examples in SystemInfo page
- Review this documentation for implementation details

---

**Built with care for healthcare. 🌙**
