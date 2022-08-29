import java.io.IOException;
import java.net.InetAddress;

public class TestMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=====start======");

        InetAddress defaultNetAddress = InetAddress.getLocalHost();
        System.out.println(defaultNetAddress);

//        try (ServerSocket serverSocket = new ServerSocket(0)) {
//            int freePort = serverSocket.getLocalPort();
//            System.out.println(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int count = 1;
//        while (!Thread.currentThread().isInterrupted()) {
//            TimeUnit.SECONDS.sleep(2);
//            System.out.println(System.currentTimeMillis());
//            if (count++ >= 5) {
//                Thread.currentThread().interrupt();
//            }
//        }

//        URL url = new URL("http://master01:7180");
//        URLConnection conn = url.openConnection();
//        System.out.println(conn.getContent());
//        conn.
//        try (InputStream out = conn.getInputStream()){
//            byte[] data = new byte[1024];
//            while (out.read(data)!=-1){
//                System.out.println(new String(data));
//            }
//        }
//        List<Integer> suc = new ArrayList<>();
//        List<Integer> fail = new ArrayList<>();
//        for (int i = 0; i < 65536; i++) {
//            try (ServerSocket serverSocket = new ServerSocket(i)) {
//                int freePort = serverSocket.getLocalPort();
//                suc.add(i);
//            } catch (IOException e) {
//                fail.add(i);
//            }
//        }
//        System.out.println("suc:" + suc.size() + "\t" + suc.stream().map(String::valueOf).collect(Collectors.joining(",")));
//        System.out.println("fail:" + fail.size() + "\t" + fail.stream().map(String::valueOf).collect(Collectors.joining(",")));
//        System.out.println("suc:" + suc.size() + "\t" + (suc.size() > 0 ? suc.get(0) : -1));
//        System.out.println("fail:" + fail.size() + "\t" + (fail.size() > 0 ? fail.get(0) : -1));

        System.out.println("END");
    }
}
