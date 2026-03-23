import { createBrowserRouter } from "react-router";
import { RootLayout } from "./components/layout/RootLayout";
import { Dashboard } from "./pages/Dashboard";
import { Users } from "./pages/Users";
import { CommunityModeration } from "./pages/CommunityModeration";
import { ChatbotReports } from "./pages/ChatbotReports";
import { Articles } from "./pages/Articles";
import { MenstrualLogs } from "./pages/MenstrualLogs";
import { Notifications } from "./pages/Notifications";
import { Analytics } from "./pages/Analytics";
import { Settings } from "./pages/Settings";
import { SystemInfo } from "./pages/SystemInfo";
import { Auth } from "./pages/Auth";
import { MedicalVerification } from "./pages/MedicalVerification";
import { Profile } from "./pages/Profile";
import { MoonArchive } from "./pages/MoonArchive";

export const router = createBrowserRouter([
  {
    path: "/auth",
    Component: Auth,
  },
  {
    path: "/",
    Component: RootLayout,
    children: [
      { index: true, Component: Dashboard },
      { path: "users", Component: Users },
      { path: "community", Component: CommunityModeration },
      { path: "chatbot-reports", Component: ChatbotReports },
      { path: "articles", Component: Articles },
      { path: "medical-verification", Component: MedicalVerification },
      { path: "menstrual-logs", Component: MenstrualLogs },
      { path: "notifications", Component: Notifications },
      { path: "analytics", Component: Analytics },
      { path: "profile", Component: Profile },
      { path: "settings", Component: Settings },
      { path: "system-info", Component: SystemInfo },
      { path: "archive", Component: MoonArchive },
    ],
  },
]);
