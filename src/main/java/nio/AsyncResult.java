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
 * �첽�������Ĳ���ʵ��
 * ��Cassandra ��Ҫ��ѯ������ͨ�����������ݽڵ㷢�Ͳ�ѯ�������Ҫ���ÿ���ֽڵ㷵�ص����������ԣ�����Ҫһ���첽��ѯͬ�������Ӧ�ò�������
 * ���ִ���չʾ
 */
public class AsyncResult /*implements IasyncResult*/ {
    private byte[] result_;
    private AtomicBoolean done_ = new AtomicBoolean(false);
    private Lock lock_ = new ReentrantLock();
    private Condition condition_;
    private long startTime_;

    public AsyncResult() {
        condition_ = lock_.newCondition(); // ����һ������
        startTime_ = System.currentTimeMillis();
    }

    /**
     * �����Ҫ�������Ƿ��Ѿ����أ����û�з��أ�����
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
     * �����Ҫ�������Ƿ��Ѿ�����
     */
    public boolean isDone() {
        return done_.get();
    }

    /**
     * �����ָ����ʱ������Ҫ�������Ƿ��Ѿ����أ����û�з��أ��׳���ʱ�쳣
     */
    public byte[] get(long timeout, TimeUnit tu) throws TimeoutException {
        lock_.lock();
        try {
            boolean bVal = true;
            try {
                if (!done_.get()) {
                    long overall_timeout = timeout - (System.currentTimeMillis() - startTime_);
                    if (overall_timeout > 0) { // ���ó�ʱ�ȴ�ʱ��
                        bVal = condition_.await(overall_timeout, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            if (!bVal && !done_.get()) { // �׳���ʱ�쳣
                throw new TimeoutException("Operation timed out");
            }
        } finally {
            lock_.unlock();
        }
        return result_;
    }

    /**
     * �ú����ṩ����һ���߳�����Ҫ���ص��������������������߳�
     */
    public void result(Message response) {
        try {
            lock_.lock();
            if (!done_.get()) {
                result_ = response.getMessagetBody();
                done_.set(true);
                condition_.signal();//�����������߳�
            }
        }finally {
            lock_.unlock();
        }
    }


}
