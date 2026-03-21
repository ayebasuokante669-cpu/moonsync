import { useState } from "react";
import { Outlet } from "react-router";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";

export function RootLayout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(true);

  const handleToggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed);
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-background">
      <Sidebar collapsed={sidebarCollapsed} onToggle={handleToggleSidebar} />
      
      <div className="flex flex-1 flex-col overflow-hidden">
        <Topbar onMenuClick={handleToggleSidebar} />
        
        <main className="flex-1 overflow-y-auto">
          <div className="mx-auto max-w-[1280px] px-4 sm:px-6 lg:px-8 py-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
