package nio;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steven on 2015/11/21 0021.
 * <p>
 * 异步和阻塞的操作实例
 * 在Cassandra 中要查询的数据通常会向多个数据节点发送查询命令，但是要检查每个字节点返回的数据完整性，就需要一个异步查询同步结果的应用擦场景；
 * 部分代码展示
 */
public class AsyncResult /*implements IasyncResult*/ {
    private byte[] result_;
    private AtomicBoolean done_ = new AtomicBoolean(false);
    private Lock lock_ = new ReentrantLock();
    private Condition condition_;
    private long startTime_;

    public AsyncResult() {
        condition_ = lock_.newCondition(); // 创建一个锁；
        startTime_ = System.currentTimeMillis();
    }

    /**
     * 检查需要的数据是否已经返回，如果没有返回，阻塞
     */
    public byte[] get() {
        lock_.lock();
        try {
            if (!done_.get()) {
                condition_.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock_.unlock();
        }
        return result_;
    }

    /**
     * 检查需要的数据是否已经返回
     */
    public boolean isDone() {
        return done_.get();
    }

    /**
     * 检查在指定的时间内需要的数据是否已经返回，如果没有返回，抛出超时异常
     */
    public byte[] get(long timeout, TimeUnit tu) throws TimeoutException {
        lock_.lock();
        try {
            boolean bVal = true;
            try {
                if (!done_.get()) {
                    long overall_timeout = timeout - (System.currentTimeMillis() - startTime_);
                    if (overall_timeout > 0) { // 设置超时等待时间
                        bVal = condition_.await(overall_timeout, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            if (!bVal && !done_.get()) { // 抛出超时异常
                throw new TimeoutException("Operation timed out");
            }
        } finally {
            lock_.unlock();
        }
        return result_;
    }

    /**
     * 该函数提供另外一个线程设置要返回的数，并唤醒在阻塞的线程
     */
    public void result(Message response) {
        try {
            lock_.lock();
            if (!done_.get()) {
                result_ = response.getMessagetBody();
                done_.set(true);
                condition_.signal();//唤醒阻塞的线程
            }
        }finally {
            lock_.unlock();
        }
    }


}
