package sample;


/**
 * Created by Julee on 08.04.2017.
 */
public abstract class WpsTask implements Runnable {

    protected abstract void doTask();

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {

        }
    }
}
