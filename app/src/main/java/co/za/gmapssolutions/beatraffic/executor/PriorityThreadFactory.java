package co.za.gmapssolutions.beatraffic.executor;

import android.os.Process;

import java.util.concurrent.ThreadFactory;


public class PriorityThreadFactory implements ThreadFactory {
    private final int threadPriority;
    public PriorityThreadFactory(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(threadPriority);
                    runnable.run();
                }catch (Throwable t){
                    //Toast.makeText(,"Unable to set thread priority",Toast.LENGTH_LONG);
                }

            }
        };
        return new Thread(wrapperRunnable);
    }
}
