package rest.bef.demo.model.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobThreadPool {

    private static final JobThreadPool instance = new JobThreadPool();
    private static final Logger logger = LogManager.getLogger();

    private ThreadPoolExecutor executor;

    private JobThreadPool() {
        this.executor = new ThreadPoolExecutor(500, 500, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>()) {
            @Override
            public Future<?> submit(Runnable task) {
                logger.debug("executor [active:{}][core:{}][pool:{}][max:{}][queue:{}]",
                        this.getActiveCount(),
                        this.getCorePoolSize(),
                        this.getPoolSize(),
                        this.getMaximumPoolSize(),
                        this.getQueue().size());

                return super.submit(task);
            }
        };
    }

    public static JobThreadPool getInstance() {
        return instance;
    }

    public void submit(Runnable job) {
        executor.submit(job);
    }
}
