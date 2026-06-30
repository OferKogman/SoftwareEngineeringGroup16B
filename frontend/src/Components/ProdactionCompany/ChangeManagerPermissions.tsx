import { useState } from "react";
import type { ManagerPermissions } from "../../DTOs/ProductionCompanyDTO";

type ChangeManagerPermissionsProps = {
  onSubmit: (
    targetID: string,
    companyID: number,
    newPermissions: Set<ManagerPermissions>,
  ) => void | Promise<void>;
};

const MANAGER_PERMISSIONS: { value: ManagerPermissions; label: string }[] = [
  { value: "EVENT_INVENTORY", label: "Event Inventory" },
  { value: "VENUE_CONFIGURATION", label: "Venue Configuration" },
  { value: "PURCHASE_POLICY", label: "Purchase Policy" },
  { value: "CUSTOMER_SUPPORT", label: "Customer Support" },
  { value: "VIEW_PURCHASE_HISTORY", label: "View Purchase History" },
  { value: "SALES_REPORT", label: "Sales Report" },
];

export default function ChangeManagerPermissions({
  onSubmit,
}: ChangeManagerPermissionsProps) {
  const [targetID, setTargetID] = useState("");
  const [companyID, setCompanyID] = useState("");
  const [permissions, setPermissions] = useState<Set<ManagerPermissions>>(
    new Set<ManagerPermissions>(),
  );
  const [permissionsError, setPermissionsError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function closePopup() {
    setMessage("");
    setSubmitError("");
  }

  function togglePermission(perm: ManagerPermissions) {
    setPermissions((prev) => {
      const next = new Set(prev);
      if (next.has(perm)) {
        next.delete(perm);
      } else {
        next.add(perm);
      }
      return next;
    });
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPermissionsError("");
    setSubmitError("");
    setMessage("");

    if (permissions.size === 0) {
      setPermissionsError("At least one permission must be selected.");
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(targetID, Number(companyID), permissions);
      setTargetID("");
      setCompanyID("");
      setPermissions(new Set<ManagerPermissions>());
      setMessage("Permissions updated successfully.");
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Change Manager Permissions</h2>

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {submitError && (
        <div className="settings-alert">
          <p>{submitError}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

      <label>
        Target User
        <input
          type="text"
          required
          value={targetID}
          onChange={(e) => setTargetID(e.target.value)}
          placeholder="Target User"
        />
      </label>

      <fieldset>
        <legend>New Permissions</legend>
        {MANAGER_PERMISSIONS.map(({ value, label }) => (
          <label className="perms" key={value}>
            <input
              type="checkbox"
              checked={permissions.has(value)}
              onChange={() => togglePermission(value)}
            />
            {label}
          </label>
        ))}
      </fieldset>
      {permissionsError && <p className="form-error">{permissionsError}</p>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Saving..." : "Update Permissions"}
      </button>
    </form>
  );
}
