import { Background, ReactFlow, type Edge } from "@xyflow/react";
import "@xyflow/react/dist/style.css";

import type {
  AndDTO,
  DiscountPolicyDTO,
  EndDateDTO,
  MaxAmountDTO,
  MaxDTO,
  MinAmountDTO,
  NullableDiscountPolicyDTO,
  OrDTO,
  StartDateDTO,
  SumDTO,
} from "../../DTOs/DiscountPolicyDTO";

import { useState } from "react";
import "./CSS/PurchasePolicyTree.css";
import { DiscountPolicyNode, type PolicyPath } from "./DiscountPolicyNode";

type Props = {
  policy: NullableDiscountPolicyDTO;
  onSave: (policy: NullableDiscountPolicyDTO) => void;
};

type PolicyEdge = Edge;

const HORIZONTAL_GAP = 550;
const VERTICAL_GAP = 220;

function isCompositePolicy(
  policy: DiscountPolicyDTO,
): policy is AndDTO | OrDTO | SumDTO | MaxDTO {
  return (
    policy.type === "AND" ||
    policy.type === "OR" ||
    policy.type === "SUM" ||
    policy.type === "MAX"
  );
}

function getSubtreeWidth(policy: DiscountPolicyDTO): number {
  if (!policy) {
    return 1;
  }

  if (isCompositePolicy(policy)) {
    const leftWidth = getSubtreeWidth(policy.left);
    const rightWidth = getSubtreeWidth(policy.right);

    return leftWidth + rightWidth;
  }

  return 1;
}

function buildTree(
  policy: DiscountPolicyDTO,
  centerX: number,
  level: number,
  nodes: DiscountPolicyNode[],
  edges: PolicyEdge[],
  path: PolicyPath,
  onSwap: (path: PolicyPath) => void,
  onChangeGoal: (
    path: PolicyPath,
    newGoal: number | string | null,
    percentage: number,
  ) => void,
  onReplace: (path: PolicyPath, newPolicy: DiscountPolicyDTO) => void,
  onDelete: () => void,
): string {
  const id = crypto.randomUUID();

  const y = level * VERTICAL_GAP;

  if (isCompositePolicy(policy)) {
    nodes.push({
      id,
      position: {
        x: centerX,
        y,
      },
      className: "hierarchy-flow-node",
      type: "policy",
      data: {
        label: policy.type,
        percentage:
          policy.type === "MAX" || policy.type === "SUM"
            ? 0
            : policy.percentage,
        type: policy.type,
        path,
        onSwap,
        onChangeGoal,
        onReplace,
        onDelete,
      },
    });

    const leftWidth = getSubtreeWidth(policy.left);
    const rightWidth = getSubtreeWidth(policy.right);

    const leftCenter = centerX - (rightWidth * HORIZONTAL_GAP) / 2;

    const rightCenter = centerX + (leftWidth * HORIZONTAL_GAP) / 2;

    const leftId = buildTree(
      policy.left,
      leftCenter,
      level + 1,
      nodes,
      edges,
      [...path, "left"],
      onSwap,
      onChangeGoal,
      onReplace,
      onDelete,
    );

    const rightId = buildTree(
      policy.right,
      rightCenter,
      level + 1,
      nodes,
      edges,
      [...path, "right"],
      onSwap,
      onChangeGoal,
      onReplace,
      onDelete,
    );

    edges.push({
      id: `${id}-${leftId}`,
      source: id,
      target: leftId,
      type: "smoothstep",
    });

    edges.push({
      id: `${id}-${rightId}`,
      source: id,
      target: rightId,
      type: "smoothstep",
    });

    return id;
  }

  let label = "";

  switch (policy?.type) {
    case "MIN_DATE":
      label = `Min Date: ${(policy as StartDateDTO).startDate}`;
      break;

    case "MAX_DATE":
      label = `Max Date: ${(policy as EndDateDTO).endDate}`;
      break;

    case "MIN_TICKETS":
      label = `Min Tickets: ${(policy as MinAmountDTO).minAmount}`;
      break;

    case "MAX_TICKETS":
      label = `Max Tickets: ${(policy as MaxAmountDTO).maxAmount}`;
      break;

    case "SIMPLE":
      label = `Simple Discount`;
      break;

    default:
      label = "Create policy";
  }

  nodes.push({
    id,
    position: {
      x: centerX,
      y,
    },
    className: "hierarchy-flow-node",
    type: "policy",
    data: {
      label,
      type: policy?.type || "NONE",
      path,
      onSwap,
      onChangeGoal,
      onReplace,
      onDelete,
      percentage: policy?.percentage || 0,
    },
  });

  return id;
}

function createFlow(
  policy: NullableDiscountPolicyDTO,
  onSwap: (path: PolicyPath) => void,
  onChangeGoal: (
    path: PolicyPath,
    newGoal: number | string | null,
    percentage: number,
  ) => void,
  onReplace: (path: PolicyPath, newPolicy: DiscountPolicyDTO) => void,
  onDelete: () => void,
) {
  const nodes: DiscountPolicyNode[] = [];
  const edges: PolicyEdge[] = [];

  if (!policy) {
    nodes.push({
      id: crypto.randomUUID(),
      position: {
        x: 0,
        y: 0,
      },
      className: "hierarchy-flow-node",
      type: "policy",
      data: {
        label: "Create a policy",
        percentage: 0,
        type: "NONE",
        path: [],
        onSwap,
        onChangeGoal,
        onReplace,
        onDelete,
      },
    });

    return { nodes, edges };
  }
  console.log("Building tree for policy:", policy);

  buildTree(
    policy,
    0,
    0,
    nodes,
    edges,
    [],
    onSwap,
    onChangeGoal,
    onReplace,
    onDelete,
  );

  return { nodes, edges };
}

function swapPolicyAtPath(
  policy: NullableDiscountPolicyDTO,
  path: PolicyPath,
): NullableDiscountPolicyDTO {
  if (!policy) {
    return null;
  }

  if (path.length === 0) {
    if (!isCompositePolicy(policy)) {
      return policy;
    }

    if (policy.type === "AND" || policy.type === "OR") {
      return {
        ...policy,
        type: policy.type === "AND" ? "OR" : "AND",
      };
    } else {
      return {
        ...policy,
        type: policy.type === "SUM" ? "MAX" : "SUM",
      };
    }
  }

  if (!isCompositePolicy(policy)) {
    return policy;
  }

  const [head, ...rest] = path;

  return {
    ...policy,
    [head]:
      head === "left"
        ? swapPolicyAtPath(policy.left, rest)
        : swapPolicyAtPath(policy.right, rest),
  };
}

function changeGoalAtPath(
  policy: DiscountPolicyDTO,
  path: PolicyPath,
  newGoal: number | string | null,
  percentageValues: number,
): DiscountPolicyDTO {
  if (path.length === 0) {
    switch (policy.type) {
      case "SIMPLE":
      case "AND":
      case "OR":
        return { ...policy, percentage: percentageValues };
      case "MIN_DATE":
        return {
          ...policy,
          startDate: String(newGoal),
          percentage: percentageValues,
        };

      case "MAX_DATE":
        return {
          ...policy,
          endDate: String(newGoal),
          percentage: percentageValues,
        };

      case "MIN_TICKETS":
        return {
          ...policy,
          minAmount: Number(newGoal),
          percentage: percentageValues,
        };

      case "MAX_TICKETS":
        return {
          ...policy,
          maxAmount: Number(newGoal),
          percentage: percentageValues,
        };

      default:
        return policy;
    }
  }

  if (!isCompositePolicy(policy)) return policy;

  const [head, ...rest] = path;

  return {
    ...policy,
    [head]:
      head === "left"
        ? changeGoalAtPath(policy.left, rest, newGoal, percentageValues)
        : changeGoalAtPath(policy.right, rest, newGoal, percentageValues),
  };
}

function replacePolicyAtPath(
  policy: DiscountPolicyDTO,
  path: PolicyPath,
  newPolicy: DiscountPolicyDTO,
): DiscountPolicyDTO {
  if (path.length === 0) {
    return newPolicy;
  }

  if (!isCompositePolicy(policy)) {
    return policy;
  }

  const [head, ...rest] = path;

  return {
    ...policy,
    ...(head === "left"
      ? {
          left: replacePolicyAtPath(policy.left, rest, newPolicy),
        }
      : {
          right: replacePolicyAtPath(policy.right, rest, newPolicy),
        }),
  };
}

export default function DiscountPolicyTree({ policy, onSave }: Props) {
  const [currentPolicy, setCurrentPolicy] =
    useState<NullableDiscountPolicyDTO>(policy);
  const nodeTypes = {
    policy: DiscountPolicyNode,
  };

  function swapPolicy(path: PolicyPath) {
    setCurrentPolicy((prevPolicy) => {
      if (!prevPolicy) return prevPolicy;
      return swapPolicyAtPath(prevPolicy, path);
    });
  }

  function changeGoal(
    path: PolicyPath,
    goal: number | string | null,
    percentageValues: number,
  ) {
    setCurrentPolicy((prev) => {
      if (!prev) return prev;

      return changeGoalAtPath(prev, path, goal, percentageValues);
    });
  }

  function replacePolicy(path: PolicyPath, newPolicy: DiscountPolicyDTO) {
    setCurrentPolicy((prev) => {
      if (!prev) {
        return newPolicy;
      }

      return replacePolicyAtPath(prev, path, newPolicy);
    });
  }

  function deletePolicy() {
    setCurrentPolicy(null);
  }

  const { nodes, edges } = createFlow(
    currentPolicy,
    swapPolicy,
    changeGoal,
    replacePolicy,
    deletePolicy,
  );

  return (
    <div style={{ width: "100%", height: "600px" }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        fitView
        proOptions={{ hideAttribution: true }}
        nodeTypes={nodeTypes}
      >
        <Background />
      </ReactFlow>

      <button onClick={() => onSave(currentPolicy)}>Save Changes</button>
    </div>
  );
}
