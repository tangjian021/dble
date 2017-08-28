package io.mycat.config.loader.zkprocess.zktoxml.listen;

import static io.mycat.util.KVPathUtil.BINLOG_PAUSE_STATUS;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.MycatServer;
import io.mycat.config.loader.zkprocess.comm.NotifyService;
import io.mycat.config.loader.zkprocess.comm.ZkConfig;
import io.mycat.config.loader.zkprocess.comm.ZkParamCfg;
import io.mycat.config.loader.zkprocess.comm.ZookeeperProcessListen;
import io.mycat.config.loader.zkprocess.zookeeper.DiretoryInf;
import io.mycat.config.loader.zkprocess.zookeeper.process.BinlogPause;
import io.mycat.config.loader.zkprocess.zookeeper.process.ZkDataImpl;
import io.mycat.config.loader.zkprocess.zookeeper.process.ZkDirectoryImpl;
import io.mycat.config.loader.zkprocess.zookeeper.process.ZkMultLoader;
import io.mycat.manager.response.ShowBinlogStatus;
import io.mycat.util.KVPathUtil;
import io.mycat.util.ZKUtils;

/**
 * Created by huqing.yan on 2017/5/25.
 */
public class BinlogPauseStatusListener extends ZkMultLoader implements NotifyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinlogPauseStatusListener.class);
    private final String currZkPath;

    public BinlogPauseStatusListener(ZookeeperProcessListen zookeeperListen, CuratorFramework curator) {
        this.setCurator(curator);
        currZkPath = KVPathUtil.getBinlogPauseStatus();
        zookeeperListen.addWatch(currZkPath, this);
    }

    @Override
    public boolean notifyProcess() throws Exception {
        DiretoryInf statusDirectory = new ZkDirectoryImpl(currZkPath, null);
        this.getTreeDirectory(currZkPath, BINLOG_PAUSE_STATUS, statusDirectory);
        ZkDataImpl zkDdata = (ZkDataImpl) statusDirectory.getSubordinateInfo().get(0);
        String strPauseInfo = zkDdata.getDataValue();
        LOGGER.info("BinlogPauseStatusListener notifyProcess zk to object  :" + strPauseInfo);

        BinlogPause pauseInfo = new BinlogPause(strPauseInfo);
        if (pauseInfo.getFrom().equals(ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID))) {
            return true; //self node
        }
        String instancePath = ZKPaths.makePath(KVPathUtil.getBinlogPauseInstance(), ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID));
        if (pauseInfo.getStatus() == BinlogPause.BinlogPauseStatus.ON) {
            MycatServer.getInstance().getBackupLocked().compareAndSet(false, true);
            if (ShowBinlogStatus.waitAllSession(pauseInfo.getFrom())) {
                try {
                    ZKUtils.createTempNode(instancePath);
                } catch (Exception e) {
                    LOGGER.warn("create binlogPause instance failed", e);
                }
            } else {
                cleanResource(instancePath);
            }
        } else if (pauseInfo.getStatus() == BinlogPause.BinlogPauseStatus.TIMEOUT) {
            LOGGER.warn("BinlogPauseStatusListener received timeout");
            ShowBinlogStatus.setWaiting(false);

        } else if (pauseInfo.getStatus() == BinlogPause.BinlogPauseStatus.OFF) {
            cleanResource(instancePath);

        }


        return true;
    }

    private synchronized void cleanResource(String instancePath) {
        LOGGER.info("BinlogPauseStatusListener cleanResource");
        while (ShowBinlogStatus.isWaiting()) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
        }
        try {
            if (this.getCurator().checkExists().forPath(instancePath) != null) {
                this.getCurator().delete().forPath(instancePath);
            }
        } catch (Exception e) {
            LOGGER.warn("delete binlogPause instance failed", e);
        } finally {
            MycatServer.getInstance().getBackupLocked().compareAndSet(true, false);
        }
    }
}
