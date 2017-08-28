package io.mycat.route.function;

import java.io.Serializable;

import io.mycat.config.model.TableConfig;
import io.mycat.config.model.rule.RuleAlgorithm;

/**
 * AbstractPartitionAlgorithm
 *
 * @author lxy
 */
public abstract class AbstractPartitionAlgorithm implements RuleAlgorithm, Serializable {
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private static final long serialVersionUID = -532594213256935577L;

    @Override
    public void init() {
    }

    /**
     * return the index of node
     * retrun an empty array means router to all node
     * return null if no node matches
     */
    @Override
    public abstract Integer[] calculateRange(String beginValue, String endValue);

    /**
     * valid the consistency between table's node size and rule's node size
     *
     * @param tableConf
     * @return -1 if table datanode size < rule function partition size
     * 0 if table datanode size == rule function partition size
     * 1 if table datanode size > rule function partition size
     */
    public final int suitableFor(TableConfig tableConf) {
        int nPartition = getPartitionNum();
        if (nPartition > 0) {
            int dnSize = tableConf.getDataNodes().size();
            if (dnSize < nPartition) {
                return -1;
            } else if (dnSize > nPartition) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * getPartitionNum, return -1 means no limit
     *
     * @return
     */
    public int getPartitionNum() {
        return -1; // no limit
    }

}
