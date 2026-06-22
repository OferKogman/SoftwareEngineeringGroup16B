import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useState } from "react";
import type { PurchasePolicyTypes } from "../../DTOs/PurchasePolicyDTO";
import "./CSS/PolicyNode.css";

type PolicyNodeData = {
  label: string;
  type: PurchasePolicyTypes;
  path: PolicyPath;
  onSwap: (path: PolicyPath) => void;
  onChangeAge: (path: PolicyPath, newAge: number) => void;
  onChangeTickets: (path: PolicyPath, newAmount: number) => void;
};

export type PolicyPath = ("left" | "right")[];

export type PolicyNode = Node<PolicyNodeData>;

export function PolicyNode(props: NodeProps<PolicyNode>) {
  const [showPopup, setShowPopup] = useState(false);
  const [inputValue, setInputValue] = useState("");
  const [action, setAction] = useState<"AGE" | "TICKETS" | null>(null);

  function renderActions() {
    switch (props.data.type) {
      case "AND":
      case "OR":
        return (
          <>
            <button
              onClick={() => {
                props.data.onSwap(props.data.path);
                setShowPopup(false);
              }}
            >
              Change {props.data.type === "AND" ? "to OR" : "to AND"}
            </button>
          </>
        );

      case "MIN_AGE":
      case "MAX_AGE":
        return (
          <>
            {action === "AGE" ? (
              <>
                <input
                  type="number"
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  placeholder="New age"
                />

                <button
                  onClick={() => {
                    const age = Number(inputValue);
                    if (!age || age <= 0) {
                      alert("Please enter a valid age");
                      return;
                    }
                    props.data.onChangeAge(props.data.path, age);

                    setInputValue("");
                    setAction(null);
                    setShowPopup(false);
                  }}
                >
                  Change age
                </button>
              </>
            ) : (
              <button
                onClick={() => {
                  setAction("AGE");
                }}
              >
                Change age
              </button>
            )}

            <button>Replace policy</button>
          </>
        );

      case "MIN_TICKETS":
      case "MAX_TICKETS":
        return (
          <>
            {action === "TICKETS" ? (
              <>
                <input
                  type="number"
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  placeholder="New amount"
                />

                <button
                  onClick={() => {
                    const amount = Number(inputValue);
                    if (!amount || amount <= 0) {
                      alert("Please enter a valid amount");
                      return;
                    }
                    props.data.onChangeTickets(props.data.path, amount);

                    setInputValue("");
                    setAction(null);
                    setShowPopup(false);
                  }}
                >
                  Change amount
                </button>
              </>
            ) : (
              <button
                onClick={() => {
                  setAction("TICKETS");
                }}
              >
                Change amount
              </button>
            )}

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
        onClick={() => setShowPopup((prev) => !prev)}
      >
        <label>{props.data.label}</label>

        <Handle type="target" position={Position.Top} />
        <Handle type="source" position={Position.Bottom} />
      </div>

      {showPopup && (
        <div className="policy-node-popup" onClick={(e) => e.stopPropagation()}>
          {renderActions()}

          <button onClick={() => setShowPopup(false)}>Close</button>
        </div>
      )}
    </div>
  );
}
