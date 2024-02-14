package dat.executeWith;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public interface ExecuteWithIF
{
    void executeWith( ExecutorService executorService ) throws IOException;
}
