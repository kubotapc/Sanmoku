import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Executor {
    public int exec(UserClass userClass, boolean argAgain) throws Exception {
        int retValue = -1;
        ExecutorService pool = Executors.newSingleThreadExecutor();
        ExecutorSet executorSet = new ExecutorSet(userClass, argAgain);
        try {
            Future<Integer> future = pool.submit(executorSet);
            retValue = future.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            System.out.println(e.getCause());
            retValue = -997;
            
            System.out.println("throw e.getCause()");
            throw new Exception(e.getCause());
        } catch (TimeoutException e) {
            System.out.println(e);
            retValue = -998;
            throw new TimeoutException();
        } catch (Exception e) {
            System.out.println(e);
            retValue = -999;
        } finally {
            pool.shutdownNow();
        }
        return (retValue);
    }
}
