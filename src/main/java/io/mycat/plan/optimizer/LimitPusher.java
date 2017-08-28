package io.mycat.plan.optimizer;

import io.mycat.plan.PlanNode;
import io.mycat.plan.node.JoinNode;
import io.mycat.plan.node.MergeNode;
import io.mycat.plan.node.QueryNode;

public final class LimitPusher {
    private LimitPusher() {
    }

    public static PlanNode optimize(PlanNode qtn) {
        qtn = findChild(qtn);
        return qtn;
    }

    private static PlanNode findChild(PlanNode qtn) {
        if (qtn instanceof MergeNode) {
            // optimizer limit
            // union: push down limit to children
            // union all:push down limit to children and add distinct
            MergeNode node = (MergeNode) qtn;
            long limitFrom = node.getLimitFrom();
            long limitTo = node.getLimitTo();
            if (limitFrom != -1 && limitTo != -1) {
                for (PlanNode child : node.getChildren()) {
                    pushLimit(child, limitFrom, limitTo, node.isUnion());
                }
            }

        } else if ((qtn instanceof JoinNode) || (qtn instanceof QueryNode)) {
            for (PlanNode child : qtn.getChildren()) {
                findChild(child);
            }
        }
        return qtn;
    }

    private static void pushLimit(PlanNode node, long limitFrom, long limitTo, boolean isUnion) {
        if (isUnion) {
            node.setDistinct(true);
        }
        if (node.getLimitFrom() == -1 && node.getLimitTo() == -1) {
            node.setLimitFrom(0);
            node.setLimitTo(limitFrom + limitTo);
        }
    }

}
