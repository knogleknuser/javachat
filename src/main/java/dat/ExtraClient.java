package dat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtraClient
{

    private static final String IP = "localhost";
    public static void main(String[] args)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        System.out.println("Starting extra ChatClient");
        Client client = new Client(IP, 9090, executorService);
        executorService.submit(client);
    }
}
