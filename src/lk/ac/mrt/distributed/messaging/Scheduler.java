package lk.ac.mrt.distributed.messaging;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Scheduler {

	
	private static final List<ExecutorService> threadsToBeDestroyed = new ArrayList<>();
	
	public static final ThreadFactory factory = new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable runnable) {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		}
	};
	
	public static ExecutorService startThread(Runnable r)
	{
		ExecutorService executor = Executors.newSingleThreadExecutor(factory);
		executor.submit(wrapRunnableWithExceptionHandling(r));
		return executor;
	}

	public static ExecutorService schedule(Runnable r, int seconds)
	{
	    return schedule(r, seconds, seconds);
	}
	
	public static ExecutorService schedule(Runnable r, int initialDelay, int seconds)
	{
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, factory);
	    executor.scheduleWithFixedDelay(wrapRunnableWithExceptionHandling(r), initialDelay, seconds, TimeUnit.SECONDS);
	    return executor;
	}
	
	private static Runnable wrapRunnableWithExceptionHandling(final Runnable r)
	{
		return new Runnable() {
			
			@Override
			public void run() {
				try{
					r.run();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		};
	}

	public static void executeIn(int i, Runnable runnable) {
		schedule(runnable, i, Integer.MAX_VALUE);
	}
}