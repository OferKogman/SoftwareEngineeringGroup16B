import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useState } from "react";
import type { PurchasePolicyTypes } from "../../DTOs/PurchasePolicyDTO";

type PolicyNodeData = {
  label: string;
  type: PurchasePolicyTypes;
  onClick: (type: PurchasePolicyTypes) => void;
};

export type PolicyNode = Node<PolicyNodeData>;

export function PolicyNode(props: NodeProps<PolicyNode>) {
  const [showPopup, setShowPopup] = useState(false);

  function renderActions() {
    switch (props.data.type) {
      case "AND":
      case "OR":
        return (
          <>
            <button>
              Change {props.data.type === "AND" ? "to OR" : "to AND"}
            </button>

            <button>Replace policy tree</button>
          </>
        );

      case "MIN_AGE":
      case "MAX_AGE":
        return (
          <>
            <button>Change age</button>

            <button>Create policy</button>

            <button>Replace policy</button>
          </>
        );

      case "MIN_TICKETS":
      case "MAX_TICKETS":
        return (
          <>
            <button>Change tickets amount</button>

            <button>Create policy</button>

            <button>Replace policy</button>
          </>
        );

      case "NONE":
        return <button>Create policy</button>;
    }
  }

  return (
    <div className="policy-node-wrapper">
      <div
        className="policy-node"
        onClick={() => {
          props.data.onClick(props.data.type);
          setShowPopup(true);
        }}
      >
        <label>{props.data.label}</label>

        <Handle type="target" position={Position.Top} />

        <Handle type="source" position={Position.Bottom} />
      </div>

      {showPopup && (
        <div className="policy-node-popup">
          {renderActions()}

          <button onClick={() => setShowPopup(false)}>Close</button>
        </div>
      )}
    </div>
  );
}
