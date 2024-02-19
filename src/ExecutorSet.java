
import java.util.concurrent.Callable;

public class ExecutorSet implements Callable<Integer> {
    private UserClass userClass;
    private boolean argAgain;

    public ExecutorSet( UserClass userClass , boolean argAgain ) {
        this.userClass = userClass;
        this.argAgain = argAgain;
    }

    @Override
    public Integer call() throws Exception {
        return ( userClass.turn(argAgain) );
    }
}
