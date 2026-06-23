import { Background, ReactFlow, type Edge } from "@xyflow/react";
import "@xyflow/react/dist/style.css";

import type {
  MaxAgeDTO,
  MaxTicketsDTO,
  MinAgeDTO,
  MinTicketsDTO,
  NullablePurchasePolicyDTO,
  PurchasePolicyDTO,
} from "../../DTOs/PurchasePolicyDTO";

import { useState } from "react";
import "./CSS/PurchasePolicyTree.css";
import { PolicyNode, type PolicyPath } from "./PolicyNode";

type Props = {
  policy: PurchasePolicyDTO;
};

type PolicyEdge = Edge;

const HORIZONTAL_GAP = 550;
const VERTICAL_GAP = 220;

function getSubtreeWidth(policy: PurchasePolicyDTO): number {
  if (!policy) {
    return 1;
  }

  if (policy.type === "AND" || policy.type === "OR") {
    const leftWidth = getSubtreeWidth(policy.left);
    const rightWidth = getSubtreeWidth(policy.right);

    return leftWidth + rightWidth;
  }

  return 1;
}

function buildTree(
  policy: PurchasePolicyDTO,
  centerX: number,
  level: number,
  nodes: PolicyNode[],
  edges: PolicyEdge[],
  path: PolicyPath,
  onSwap: (path: PolicyPath) => void,
  onChangeAge: (path: PolicyPath, newAge: number) => void,
  onChangeTickets: (path: PolicyPath, newAmount: number) => void,
): string {
  const id = crypto.randomUUID();

  const y = level * VERTICAL_GAP;

  if (policy?.type === "AND" || policy?.type === "OR") {
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
        type: policy.type,
        path,
        onSwap,
        onChangeAge,
        onChangeTickets,
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
      onChangeAge,
      onChangeTickets,
    );

    const rightId = buildTree(
      policy.right,
      rightCenter,
      level + 1,
      nodes,
      edges,
      [...path, "right"],
      onSwap,
      onChangeAge,
      onChangeTickets,
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
    case "MIN_AGE":
      label = `Min Age: ${(policy as MinAgeDTO).minAge}`;
      break;

    case "MAX_AGE":
      label = `Max Age: ${(policy as MaxAgeDTO).maxAge}`;
      break;

    case "MIN_TICKETS":
      label = `Min Tickets: ${(policy as MinTicketsDTO).minTickets}`;
      break;

    case "MAX_TICKETS":
      label = `Max Tickets: ${(policy as MaxTicketsDTO).maxTickets}`;
      break;

    default:
      label = "Create a policy";
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
      onChangeAge,
      onChangeTickets,
    },
  });

  return id;
}

function createFlow(
  policy: NullablePurchasePolicyDTO,
  onSwap: (path: PolicyPath) => void,
  onChangeAge: (path: PolicyPath, newAge: number) => void,
  onChangeTickets: (path: PolicyPath, newAmount: number) => void,
) {
  const nodes: PolicyNode[] = [];
  const edges: PolicyEdge[] = [];

  if (!policy) {
    return { nodes, edges };
  }

  buildTree(
    policy,
    0,
    0,
    nodes,
    edges,
    [],
    onSwap,
    onChangeAge,
    onChangeTickets,
  );

  return { nodes, edges };
}

function swapPolicyAtPath(
  policy: NullablePurchasePolicyDTO,
  path: PolicyPath,
): NullablePurchasePolicyDTO {
  if (!policy) {
    return null;
  }

  if (path.length === 0) {
    if (policy.type !== "AND" && policy.type !== "OR") {
      return policy;
    }

    return {
      ...policy,
      type: policy.type === "AND" ? "OR" : "AND",
    };
  }

  if (policy.type !== "AND" && policy.type !== "OR") {
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

function changeAgeAtPath(
  policy: PurchasePolicyDTO,
  path: PolicyPath,
  newAge: number,
): PurchasePolicyDTO {
  if (path.length === 0) {
    if (policy.type === "MIN_AGE") {
      return {
        ...policy,
        minAge: newAge,
      };
    }

    if (policy.type === "MAX_AGE") {
      return {
        ...policy,
        maxAge: newAge,
      };
    }

    return policy;
  }

  if (policy.type !== "AND" && policy.type !== "OR") {
    return policy;
  }

  const [head, ...rest] = path;

  const updatedChild =
    head === "left"
      ? changeAgeAtPath(policy.left, rest, newAge)
      : changeAgeAtPath(policy.right, rest, newAge);

  return {
    ...policy,
    ...(head === "left" ? { left: updatedChild } : { right: updatedChild }),
  };
}

function changeAmountAtPath(
  policy: PurchasePolicyDTO,
  path: PolicyPath,
  newAmount: number,
): PurchasePolicyDTO {
  if (path.length === 0) {
    if (policy.type === "MIN_TICKETS") {
      return {
        ...policy,
        minTickets: newAmount,
      };
    }

    if (policy.type === "MAX_TICKETS") {
      return {
        ...policy,
        maxTickets: newAmount,
      };
    }

    return policy;
  }

  if (policy.type !== "AND" && policy.type !== "OR") {
    return policy;
  }

  const [head, ...rest] = path;

  const updatedChild =
    head === "left"
      ? changeAmountAtPath(policy.left, rest, newAmount)
      : changeAmountAtPath(policy.right, rest, newAmount);

  return {
    ...policy,
    ...(head === "left" ? { left: updatedChild } : { right: updatedChild }),
  };
}

export default function PurchasePolicyTree({ policy }: Props) {
  const [currentPolicy, setCurrentPolicy] =
    useState<NullablePurchasePolicyDTO>(policy);
  const nodeTypes = {
    policy: PolicyNode,
  };

  function swapPolicy(path: PolicyPath) {
    setCurrentPolicy((prevPolicy) => {
      if (!prevPolicy) return prevPolicy;
      return swapPolicyAtPath(prevPolicy, path);
    });
  }

  function changeAge(path: PolicyPath, age: number) {
    setCurrentPolicy((prev) => {
      if (!prev) return prev;

      return changeAgeAtPath(prev, path, age);
    });
  }

  function changeAmount(path: PolicyPath, amount: number) {
    setCurrentPolicy((prev) => {
      if (!prev) return prev;

      return changeAmountAtPath(prev, path, amount);
    });
  }

  const { nodes, edges } = createFlow(
    currentPolicy,
    swapPolicy,
    changeAge,
    changeAmount,
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
    </div>
  );
}
