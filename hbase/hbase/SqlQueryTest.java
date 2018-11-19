package com.broadtech.hbase;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlQueryTest {

    private static Logger logger = Logger.getLogger(SqlQueryTest.class);
    private static ObjectMapper objMapper = new ObjectMapper();

    public static void main(String[] args) {
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            // 客户端socket指定服务器的地址和端口号
            socket = new Socket("132.225.129.71", 9795);
            //socket = new Socket("127.0.0.1", 9791);
            //socket = new Socket("192.168.5.202", 9791);
            //socket.setSendBufferSize(50 * 1024);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            List<Thread> ths = new ArrayList<Thread>();
            Mediator lock = new Mediator(1000);
            for (int i = 0; i < 1; i++) {//1 2 3
                ClientSimulator client = new ClientSimulator(out, null, args, lock);
                ths.add(client);
                client.start();
            }
            ClientSimulator client = new ClientSimulator(null, in, args, lock);
            ths.add(client);
            client.start();
            for (Thread th : ths) {
                th.join();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Mediator {
        int limit;

        public Mediator(int limit) {
            this.limit = limit;
        }
    }

    public static class ClientSimulator extends Thread {
        private OutputStream out = null;
        private InputStream in = null;
        private String[] args;
        private Mediator lock;

        public ClientSimulator(OutputStream out, InputStream in, String[] args, Mediator lock) {
            this.out = out;
            this.in = in;
            this.args = args;
            this.lock = lock;
        }

        public void run() {
            BASE64Encoder encoder = new BASE64Encoder();

            //重庆测试200
            String sql = "select" +
                    //" length,HTTP_WAP_Affair_Status,Procedure_StartTime,Procedure_EndTime,city,cell_id,imsi,imei,msisdn,UL_DATA,DL_DATA,UL_IP_PACKET,DL_IP_PACKET,DL_RATE_DATA,HTTP_LASTACK_DELAY" +
                    " EndTime,CI,IMSI,IMEI,MSISDN" +
                    //" from MLTE_S1ULOG" +

                    " from MLTE_S1ULOG" +//460016800704308 14506804031
                    //" from IOT_MLTE_S5S8Log" +//460014115761918 14554636153
                    //" from IOT_MLTE_GNCPDPLog" +//460011034726502 8614535515056

                    " where endtime >= '2018-01-03 00:20:00' and endtime < '2018-01-03 00:25:00'" +
                    " and ci='218392331'" +  //147387650
                    //" and ci in ('86990401', '87291935')" +
                    //" and msisdn='17697144961'" +
                    //" and msisdn in ('13160459488', '13182512031')" +
                    //" and imsi='460016541009455'" +
                    //" and imsi in ('460016541009455', '460013179096078')" +
                    //" and city='13013'" +
                    " ";
            sql = "select ci,endtime from mlte_s1ulog where endtime>='2018-01-02 22:00:00' and endtime<'2018-01-02 22:05:00' and ci=219234572";//ci=219234572"
            sql = "select ci,endtime from mlte_s1ulog where endtime>='2018-01-03 00:20:00' and endtime<'2018-01-03 00:25:00' and msisdn=18639715786";
            sql = "select ci,endtime from mlte_s1ulog where endtime>='2018-01-08 11:00:00' and endtime<'2018-01-08 11:05:00' and ci=12458";

            byte[] sqlByts = sql.getBytes();
            //int cid = getProcessID();
            long cid = Thread.currentThread().getId();
            //String queryId = java.util.UUID.randomUUID().toString();
            String queryId = "20151010104354130";//"20160809144357310";//"20151010104354130";//"6505A5C3137D944C416A1F768F061E30_2302_1439802393545";
            try {
                if (out != null) {
                    Command reqCommand = Command.MSG_QUERY;//Command.MSG_GETDATA MSG_QUERY MSG_ADDTASK MSG_DELTASK
                    while (true) {
                        String mapString = null;

                        //s1u_http sgw:ffffffffffffffffffffffff6441fd3b,1004,2499
                        //cellId:178121731 imei:355060064279620
                        mapString = "{\"checksum\":0,\"sql\":" +
                                "\"" + encoder.encode(sqlByts) +
                                "\"," +
                                "\"data\":[],\"datanum\":\"6000\",\"errormsg\":\"\",\"exportCfg\":null," +
                                "\"moduleid\":\"2305\",\"msgid\":0," +
                                "\"queryid\":\"" + queryId + "\",\"tasktype\":\"2\",\"totaldatanum\":\"\"}";

                        //filed不匹配
                        byte[] jsons = mapString.getBytes();
                        int jsonLenth = jsons.length;
                        byte[] bytes = new byte[12 + jsonLenth];
                        int num = 0;

                        // 命令
                        BytesUtils.intToByteArray(reqCommand.code, bytes, num);
                        num = num + 4;
                        // 长度
                        logger.info(cid + " " + jsonLenth);
                        BytesUtils.intToByteArray(jsonLenth, bytes, num);
                        num = num + 4;
                        // checksum
                        BytesUtils.intToByteArray(reqCommand.code ^ jsonLenth, bytes, num);
                        num = num + 4;
                        for (int k = 0; k < mapString.length(); k++) {
                            bytes[num] = jsons[k];
                            num++;
                        }

                        logger.info(cid + " out bytes.length:     " + bytes.length);

                        synchronized (lock) {
                            out.write(bytes);
                            out.flush();
                            //lock.wait();//TODO
                        }
                        reqCommand = Command.MSG_GETDATA;
                        break;//TODO
                    }
                }

                if (in != null) {
                    int dataCount = 0;
                    OutputStream out = null;
                    try {
                        //out = new FileOutputStream("/xdata0/xdrmsisdn/1.dat");//TODO
                        byte[] delimiterField = BytesSplitter.hexStringToBytes("7c");//03ff
                        byte[] delimiterLine = BytesSplitter.hexStringToBytes("0d0a");//03fe0d0a
                        while (true) {
                            byte[] buffer = new byte[12];
                            int len = in.read(buffer);
                            if (len <= 0) {
                                logger.info("==========退出==========");
                                break;
                            }
                            // 获取命令4字节
                            int cmdCode = BytesUtils.byteArrayToInt(buffer, 0);
                            int jsonLen = BytesUtils.byteArrayToInt(buffer, 4);
                            //int checkSum = Constant.byteArrayToInt(buffer,8);
                            Command command = Command.get(cmdCode);
                            logger.info(cid + " in cmd=" + command + " jsonLen=" + jsonLen);
                            if (jsonLen > 0) {
                                /*buffer = new byte[jsonLen];
                                len = in.read(buffer);
								if (len <= 0) {
									throw new Exception("读取数据错误");
								}*/
                                buffer = readBuffer(in, jsonLen);
                                String jsonData = new String(buffer);
                                try {
                                    Map<String, Object> map = objMapper.readValue(jsonData, Map.class);
                                    //List<String[]> list = (List<String[]>)map.get("data");
                                    List<List<String>> list = (List<List<String>>) map.get("data");
                                    logger.info(cid + " 客户端收到消息length:" + jsonData.length() + " queryId:" + (String) map.get("queryid") + " errormsg:" + (String) map.get("errormsg"));
                                    if (list != null) {
                                        logger.info("数据条数:" + list.size());
                                        StringBuilder builder = new StringBuilder();
                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        for (List<String> line : list) {
                                            builder.append("\n").append(line.toString())
                                                    .append(dateFormat.format(Long.valueOf(line.get(1))));
                                            if (out == null) {
                                                logger.debug(line);
                                            } else {
                                                String field = null;
                                                for (int i = 0, length = line.size() - 1; i < length; i++) {
                                                    field = line.get(i);
                                                    if (!"".equals(field)) out.write(field.getBytes());
                                                    out.write(delimiterField);
                                                }
                                                field = line.get(line.size() - 1);
                                                if (!"".equals(field)) out.write(field.getBytes());
                                                out.write(delimiterLine);
                                            }
                                        }
                                        logger.info(builder.toString());
                                    }
                                    if (list != null) dataCount += list.size();
                                } catch (Exception e) {
                                    logger.warn(cid + " 客户端收到消息length: " + jsonData.length() + " json:\n" + jsonData, e);
                                    break;
                                }

                            }
                            //if (command == Command.MSG_QUERY_REP) continue;
                            if (command == Command.MSG_END) break;

                            //if (dataCount < lock.limit) continue;
//							dataCount = 0;
//							synchronized(lock) {
//								lock.notify();
//							}
                        }
                    } finally {
                        if (out != null) out.close();
                        logger.debug("************out close");
                    }
                }

            } catch (Exception e) {
                logger.error(cid + e.getMessage(), e);
            } finally {
                logger.info(cid + " client closed");
            }
        }
    }

    public static byte[] readBuffer(InputStream inStream, int bufLength) throws CallException {
        try {
            int off = 0;
            int len = 0;
            byte[] buffer = new byte[bufLength];
            while (bufLength > 0) {
                len = inStream.read(buffer, off, bufLength);
                if (len <= 0) {
                    return null;
                }
                off += len;
                bufLength -= len;
            }
            return buffer;
        } catch (Exception e) {
            throw new CallException(e.getClass().getName());
        }
    }

    public static final int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }

}
