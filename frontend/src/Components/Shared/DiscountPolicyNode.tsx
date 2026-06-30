import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useState } from "react";
import type {
  DiscountPolicyDTO,
  DiscountPolicyTypes,
  SimpleDiscountPolicyTypes,
} from "../../DTOs/DiscountPolicyDTO";
import "./CSS/PolicyNode.css";

type DiscountPolicyNodeData = {
  label: string;
  percentage: number;
  type: DiscountPolicyTypes | "NONE";
  path: PolicyPath;
  onSwap: (path: PolicyPath) => void;
  onChangeGoal: (
    path: PolicyPath,
    newGoal: number | string | null,
    percentageValues: number,
  ) => void;
  onReplace: (path: PolicyPath, newPolicy: DiscountPolicyDTO) => void;
  onDelete: () => void;
};

export type PolicyPath = ("left" | "right")[];

export type DiscountPolicyNode = Node<DiscountPolicyNodeData>;

function createSimplePolicy(
  type: SimpleDiscountPolicyTypes,
  value: number | string,
  percentage: number,
): DiscountPolicyDTO {
  switch (type) {
    case "SIMPLE":
      return {
        type,
        percentage,
      };
    case "MIN_DATE":
      return {
        type,
        startDate: String(value),
        percentage,
      };

    case "MAX_DATE":
      return {
        type,
        endDate: String(value),
        percentage,
      };

    case "MIN_TICKETS":
      return {
        type,
        minAmount: Number(value),
        percentage,
      };

    case "MAX_TICKETS":
      return {
        type,
        maxAmount: Number(value),
        percentage,
      };

    default:
      throw new Error("Not simple policy");
  }
}

const simpleDiscountPolicyTypes: DiscountPolicyTypes[] = [
  "SIMPLE",
  "MIN_DATE",
  "MAX_DATE",
  "MIN_TICKETS",
  "MAX_TICKETS",
];

function isComplexPolicy(type: DiscountPolicyTypes) {
  return type === "AND" || type === "OR" || type === "SUM" || type === "MAX";
}

export function DiscountPolicyNode(props: NodeProps<DiscountPolicyNode>) {
  const [showPopup, setShowPopup] = useState(false);
  const [changeValueInput, setChangeValueInput] = useState<string | number>("");
  const [action, setAction] = useState<"CHANGE" | "REPLACE" | null>(null);
  const [replaceInputValue, setReplaceInputValue] =
    useState<DiscountPolicyTypes>("SIMPLE");
  const [replaceGoalInput, setReplaceGoalInput] = useState<string>("");
  const [percentageValue, setPercentageValue] = useState(0);
  const [leftPolicy, setLeftPolicy] = useState<DiscountPolicyDTO | null>(null);
  const [rightPolicy, setRightPolicy] = useState<DiscountPolicyDTO | null>(
    null,
  );

  function getDefaultPolicyValue(
    type: SimpleDiscountPolicyTypes,
  ): number | string {
    if (type === "MIN_DATE" || type === "MAX_DATE") {
      return "DD-MM-YYTHH:MM";
    }

    return 1;
  }

  function renderReplace() {
    if (action !== "REPLACE") return null;

    return isComplexPolicy(replaceInputValue)
      ? renderComplexReplace()
      : renderSimpleReplace();
  }

  function renderSimpleReplace() {
    return (
      <>
        {renderPolicyTypeSelect()}

        {replaceInputValue !== "SIMPLE" && renderGoalInput()}

        {props.data.percentage !== 0 && renderPercentageInput()}

        <button onClick={replaceSimplePolicy}>Replace</button>
      </>
    );
  }

  function renderComplexReplace() {
    return (
      <>
        {renderPolicyTypeSelect()}

        {(replaceInputValue === "AND" || replaceInputValue === "OR") &&
          renderPercentageInput()}

        {renderPolicySideSelector("left")}

        {renderPolicySideSelector("right")}

        <button
          disabled={!leftPolicy || !rightPolicy}
          onClick={replaceComplexPolicy}
        >
          Replace
        </button>
      </>
    );
  }

  function renderPolicyTypeSelect() {
    return (
      <select
        value={replaceInputValue}
        onChange={(e) => {
          const value = e.target.value as DiscountPolicyTypes;

          setReplaceInputValue(value);

          setChangeValueInput("");

          setLeftPolicy(null);
          setRightPolicy(null);
          setReplaceGoalInput("");
        }}
      >
        {simpleDiscountPolicyTypes.map((type) => (
          <option key={type} value={type}>
            {type.replace("_", " ")}
          </option>
        ))}

        <option value="AND">AND</option>
        <option value="OR">OR</option>
        <option value="SUM">SUM</option>
        <option value="MAX">MAX</option>
      </select>
    );
  }

  function replaceSimplePolicy() {
    let newPolicy: DiscountPolicyDTO;

    if (replaceInputValue === "SIMPLE") {
      newPolicy = {
        type: "SIMPLE",
        percentage: percentageValue,
      };
    } else if (
      replaceInputValue === "MIN_DATE" ||
      replaceInputValue === "MAX_DATE"
    ) {
      const value = String(replaceGoalInput);

      if (!value || value.trim() === "") {
        alert("Enter valid value");
        return;
      }

      newPolicy = createSimplePolicy(replaceInputValue, value, percentageValue);
    } else {
      const value = Number(replaceGoalInput);

      if (!value || value <= 0) {
        alert("Enter valid value");
        return;
      }

      newPolicy = createSimplePolicy(
        replaceInputValue as SimpleDiscountPolicyTypes,
        value,
        percentageValue,
      );
    }

    closeReplacePopup();

    props.data.onReplace(props.data.path, newPolicy);
  }

  function replaceComplexPolicy() {
    if (!leftPolicy || !rightPolicy) {
      return;
    }

    let newPolicy: DiscountPolicyDTO;

    if (replaceInputValue === "SUM" || replaceInputValue === "MAX") {
      newPolicy = {
        type: replaceInputValue,
        left: leftPolicy,
        right: rightPolicy,
      };
    } else {
      if (replaceInputValue !== "AND" && replaceInputValue !== "OR") {
        return;
      }

      newPolicy = {
        type: replaceInputValue,
        left: leftPolicy,
        right: rightPolicy,
        percentage: percentageValue,
      };
    }

    closeReplacePopup();

    props.data.onReplace(props.data.path, newPolicy);
  }

  function renderGoalInput(type: DiscountPolicyTypes = replaceInputValue) {
    return (
      <input
        type={
          type === "MIN_DATE" || type === "MAX_DATE"
            ? "datetime-local"
            : "number"
        }
        min={type === "MIN_TICKETS" || type === "MAX_TICKETS" ? 1 : undefined}
        max={
          type === "MIN_DATE" || type === "MAX_DATE"
            ? "9999-12-31T23:59"
            : undefined
        }
        value={action === "CHANGE" ? changeValueInput : replaceGoalInput}
        onChange={(e) => {
          action === "CHANGE"
            ? setChangeValueInput(e.target.value)
            : setReplaceGoalInput(e.target.value);
        }}
        placeholder={
          replaceInputValue === "MIN_DATE" || replaceInputValue === "MAX_DATE"
            ? "DD-MM-YYTHH:MM"
            : "Ticket amount"
        }
      />
    );
  }

  function renderPercentageInput() {
    return (
      <div style={{ position: "relative" }}>
        <input
          type="number"
          min={0}
          max={100}
          value={percentageValue}
          onChange={(e) => setPercentageValue(Number(e.target.value))}
          style={{ paddingRight: "20px" }}
        />
        <span
          style={{
            position: "absolute",
            right: "8px",
            top: "50%",
            transform: "translateY(-50%)",
          }}
        >
          %
        </span>
      </div>
    );
  }

  function renderPolicySideSelector(side: "left" | "right") {
    const policy = side === "left" ? leftPolicy : rightPolicy;

    return (
      <select
        value={policy?.type ?? ""}
        onChange={(e) => {
          const type = e.target.value as SimpleDiscountPolicyTypes;

          if (!type) return;

          const newPolicy = createSimplePolicy(
            type,
            getDefaultPolicyValue(type),
            0,
          );

          side === "left"
            ? setLeftPolicy(newPolicy)
            : setRightPolicy(newPolicy);
        }}
      >
        <option value="">Choose {side} policy</option>

        {simpleDiscountPolicyTypes.map((type) => (
          <option key={type} value={type}>
            {type.replace("_", " ")}
          </option>
        ))}
      </select>
    );
  }

  function closeReplacePopup() {
    setLeftPolicy(null);
    setRightPolicy(null);
    setReplaceGoalInput("");
    setAction(null);
    setShowPopup(false);
  }

  function openReplacePopup() {
    setLeftPolicy(null);
    setRightPolicy(null);
    setReplaceGoalInput("");
    setAction("REPLACE");
  }

  function renderActions() {
    return (
      <>
        {props.data.path.length === 0 && props.data.type !== "NONE" && (
          <button
            onClick={() => {
              props.data.onDelete();
              setShowPopup(false);
            }}
          >
            Delete policy
          </button>
        )}

        {(() => {
          switch (props.data.type) {
            case "AND":
            case "OR":
              return (
                <>
                  {action === "CHANGE" ? (
                    <>
                      {renderPercentageInput()}

                      <button
                        onClick={() => {
                          if (percentageValue < 0 || percentageValue > 100) {
                            alert("Please enter a valid percentage (0-100)");
                            return;
                          }
                          props.data.onChangeGoal(
                            props.data.path,
                            null,
                            percentageValue,
                          );

                          setPercentageValue(0);
                          setAction(null);
                          setShowPopup(false);
                        }}
                      >
                        Change value
                      </button>
                    </>
                  ) : (
                    <button
                      onClick={() => {
                        setAction("CHANGE");
                      }}
                    >
                      Change value
                    </button>
                  )}
                  <button
                    onClick={() => {
                      props.data.onSwap(props.data.path);
                      setShowPopup(false);
                    }}
                  >
                    Change {props.data.type === "AND" ? "to OR" : "to AND"}
                  </button>

                  <button onClick={() => openReplacePopup()}>
                    Replace policy
                  </button>

                  {renderReplace()}
                </>
              );

            case "SUM":
            case "MAX":
              return (
                <>
                  <button
                    onClick={() => {
                      props.data.onSwap(props.data.path);
                      setShowPopup(false);
                    }}
                  >
                    Change {props.data.type === "SUM" ? "to MAX" : "to SUM"}
                  </button>

                  <button onClick={openReplacePopup}>Replace policy</button>

                  {renderReplace()}
                </>
              );

            case "SIMPLE":
            case "MIN_DATE":
            case "MAX_DATE":
            case "MIN_TICKETS":
            case "MAX_TICKETS":
              return (
                <>
                  {action === "CHANGE" ? (
                    <>
                      {props.data.type !== "SIMPLE" &&
                        renderGoalInput(props.data.type)}
                      {props.data.percentage !== 0 && renderPercentageInput()}

                      <button
                        onClick={() => {
                          if (percentageValue < 0 || percentageValue > 100) {
                            alert("Please enter a valid percentage (0-100)");
                            return;
                          }
                          if (props.data.type === "SIMPLE") {
                            props.data.onChangeGoal(
                              props.data.path,
                              null,
                              percentageValue,
                            );

                            setPercentageValue(0);
                            setChangeValueInput("");
                            setAction(null);
                            setShowPopup(false);
                          } else if (
                            props.data.type === "MIN_DATE" ||
                            props.data.type === "MAX_DATE"
                          ) {
                            const dateValue = new Date(
                              changeValueInput as string,
                            );
                            if (isNaN(dateValue.getTime())) {
                              alert("Please enter a valid date");
                              return;
                            }
                            props.data.onChangeGoal(
                              props.data.path,
                              changeValueInput as string,
                              percentageValue,
                            );

                            setPercentageValue(0);
                            setChangeValueInput("");
                            setAction(null);
                            setShowPopup(false);
                          } else {
                            const goalVal = Number(changeValueInput);

                            if (!goalVal || goalVal <= 0) {
                              alert("Please enter a valid value");
                              return;
                            }
                            props.data.onChangeGoal(
                              props.data.path,
                              goalVal,
                              percentageValue,
                            );

                            setPercentageValue(0);
                            setChangeValueInput("");
                            setAction(null);
                            setShowPopup(false);
                          }
                        }}
                      >
                        Change value
                      </button>
                    </>
                  ) : (
                    <button
                      onClick={() => {
                        setAction("CHANGE");
                      }}
                    >
                      Change value
                    </button>
                  )}

                  <button onClick={() => openReplacePopup()}>
                    Replace policy
                  </button>

                  {renderReplace()}
                </>
              );

            case "NONE":
              return (
                <>
                  {action === "REPLACE" ? (
                    renderReplace()
                  ) : (
                    <button
                      onClick={() => {
                        setAction("REPLACE");
                      }}
                    >
                      Create policy
                    </button>
                  )}
                </>
              );
          }
        })()}
      </>
    );
  }

  return (
    <div className="policy-node-wrapper">
      <div
        className="policy-node"
        onClick={() => setShowPopup((prev) => !prev)}
      >
        <label>{props.data.label}</label>
        {props.data.percentage > 0 &&
          props.data.type !== "NONE" &&
          props.data.type !== "SUM" &&
          props.data.type !== "MAX" && (
            <label>
              {" "}
              <br />
              Percentage: {props.data.percentage}%
            </label>
          )}

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
