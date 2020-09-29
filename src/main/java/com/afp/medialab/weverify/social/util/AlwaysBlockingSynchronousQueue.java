package com.afp.medialab.weverify.social.util;

import java.util.concurrent.SynchronousQueue;

/**
 * A SynchronousQueue in which offer delegates to put. ThreadPoolExecutor uses
 * offer to run a new task. Using put instead means that when all the threads
 * in the pool are occupied, execute will wait for one of them to become free,
 * rather than failing to submit the task.
 */
public class AlwaysBlockingSynchronousQueue
        extends
        SynchronousQueue<Runnable> {
  /**
   * Yes, I know this technically breaks the contract of BlockingQueue, but it
   * works for this case.
   */
  public boolean offer(Runnable task) {
    try {
      put(task);
    } catch(InterruptedException e) {
      return false;
    }
    return true;
  }
}
