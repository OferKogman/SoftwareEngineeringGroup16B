import { useEffect, useState } from "react";
import { NavLink, Navigate, useOutlet } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import "../../CSS/Management.css";
import type { ActiveOrderDTO } from "../../DTOs/ActiveOrderDTO";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";

type InviteDTO = {
  assignerID: string;
  companyName: string;
};

export default function UserManagement() {
  const outlet = useOutlet();
  const isLoggedIn = useLoggedIn();
  const apiFetch = useApiFetch();

  const [activeOrder, setActiveOrder] = useState<ActiveOrderDTO | null>(null);
  const [invites, setInvites] = useState<InviteDTO[] | null>(null);
  const [, setSecondsLeft] = useState<number | null>(null);

  // ACTIVE ORDER
  useEffect(() => {
    async function loadActiveOrder() {
      try {
        const response = await apiFetch(
          "http://localhost:8080/api/user/me/active-order",
          { method: "GET" },
        );

        if (!response.ok) {
          setActiveOrder(null);
          return;
        }

        const order: ActiveOrderDTO = await response.json();
        setActiveOrder(order);
      } catch {
        setActiveOrder(null);
      }
    }

    void loadActiveOrder();
  }, [apiFetch]);

  // INVITES
  useEffect(() => {
    async function loadInvites() {
      try {
        const response = await apiFetch(
          "http://localhost:8080/api/user/me/company-invites",
          { method: "GET" },
        );

        if (!response.ok) {
          setInvites([]);
          return;
        }

        const data: InviteDTO[] = await response.json();
        setInvites(data);
      } catch {
        setInvites([]);
      }
    }

    void loadInvites();
  }, [apiFetch]);

  // TIMER
  useEffect(() => {
    if (!activeOrder) return;

    const updateTimer = () => {
      const endTime = activeOrder.orderStartTime + 10 * 60 * 1000;
      setSecondsLeft(Math.max(0, Math.floor((endTime - Date.now()) / 1000)));
    };

    updateTimer();
    const intervalId = window.setInterval(updateTimer, 1000);
    return () => window.clearInterval(intervalId);
  }, [activeOrder]);

  if (!isLoggedIn.loggedIn) {
    return (
      <div className="management-page">
        <div className="management-header">
          <h1>User Management</h1>
        </div>

        <div className="management-body">
          <div className="management-content">
            <h2>Access Denied</h2>
            <p>You must be logged in to access this page.</p>
          </div>
        </div>
      </div>
    );
    return <Navigate to="/" replace />;
  }

  const hasInvites = invites && invites.length > 0;

  return (
    <div className="management-page">
      <div className="management-header">
        <h1>User Management</h1>
      </div>

      <div className="management-body">
        <aside className="management-sidebar">
          <NavLink to="change-password">Change Password</NavLink>
          <NavLink to="purchase-history">Purchase History</NavLink>
          <NavLink to="companies">My Production Companies</NavLink>

          {hasInvites && (
            <NavLink to="invites">Invites ({invites!.length})</NavLink>
          )}

          {activeOrder && <NavLink to="active-order">Active Order</NavLink>}
        </aside>

        <main className="management-content">
          {outlet ?? (
            <div className="management-default-content">
              <h2>User Management</h2>
              <p>Select an option from the sidebar.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
