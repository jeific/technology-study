package com.broadtech.qp.solr.pertest;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.*;
import com.broadtech.qp.index.status.RuntimeStatus;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by jeifi on 2017/8/10.
 */
public class SolrRESTBuilder implements Runnable {
    private final static Logger logger = Logger.getLogger(SolrRESTBuilder.class);
    private static final String BASIC_AUTH = "basicauth";
    private final RuntimeStatus status;
    private final FileQueue fileQueue;
    private final RichCtlConfig ctlConfig;
    private final String[] fileds;
    private final String collectionUrl;
    private final String collection;
    private final HttpURLConnection urlc;
    private boolean stopped = false;
    private CountDownLatch cdl = null;

    public SolrRESTBuilder(String baseURL, RichCtlConfig ctl, String collection, FileQueue fileQueue, RuntimeStatus status) {
        this.ctlConfig = ctl;
        this.fileQueue = fileQueue;
        this.status = status;
        this.collection = collection;
        this.fileds = ctl.getCtlConfig().fieldNames.toArray(new String[ctl.getCtlConfig().fieldNames.size()]);
        collectionUrl = baseURL.replace("##", collection);
        urlc = openConnect(collectionUrl, "application/json");
    }

    public void start(int processorSeq) {
        String name = "Build_" + processorSeq + "-" + collection;
        new Thread(this, name).start();
    }

    public void stop(CountDownLatch stopCdl) {
        this.cdl = stopCdl;
        this.stopped = true;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        logger.info("solr索引构建处理器 " + name + " 已启动");
        Path file;
        TimeCounter timeCounter = new TimeCounter();
        byte[] buffer = new byte[64 * 1024];
        List<byte[]> linesCache = new ArrayList<>();
        status.incBusiness(ctlConfig.getCtlConfig());
        while (!stopped) {
            try {
                file = fileQueue.dequeue(ctlConfig);
                if (file == null) {
                    ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                    break;
                }
                timeCounter.reset();
                postFile(file, buffer, linesCache);
                logger.info("POSTing file " + file.toString() + " " + UnitHelper.getHumanSize(file.toFile().length())
                        + " to [URL] " + collectionUrl + ", cost time: " + timeCounter.humanCost());
            } catch (Throwable e) {
                logger.error(name + " 索引处理器遇到异常", e);
            }
        }
        if (urlc != null) urlc.disconnect();
        if (cdl != null) cdl.countDown();
        status.decBusiness(ctlConfig.getCtlConfig());
        logger.info("solr索引构建处理器 " + name + " 已停止");
    }

    private void postData() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 23);
        map.put("name", "jeific");
        map.put("price", 29.63);
        map.put("price2", 29.63f);
        map.put("enjoy", true);
        send(urlc, toJSON(map));
    }

    private void postFile(Path file, byte[] buffer, List<byte[]> linesCache) {
        byte[] lineSeq = ctlConfig.getCtlConfig().lineSep;
        byte[] fieldSeq = ctlConfig.getCtlConfig().fieldSep;
        StringBuilder docs = new StringBuilder();
        try (InputStream ins = new FileInputStream(file.toFile())) {
            int count, offset = 0;
            while ((count = ins.read(buffer, offset, buffer.length - offset)) != -1) {
                linesCache.clear();
                docs.delete(0, docs.length());
                docs.append("[");
                offset = TokenUtils.tokens(buffer, offset + count, lineSeq, linesCache);
                for (byte[] line : linesCache) {
                    transToDocument(docs, line, fieldSeq);
                    docs.append(",");
                }
                docs.replace(docs.length() - 1, docs.length(), "]");
                send(urlc, docs.toString()); // 发送post包
                status.addLine(ctlConfig.getCtlConfig(), count, linesCache.size());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void send(HttpURLConnection urlc, String docs) {
        try (final OutputStream out = urlc.getOutputStream()) {
            out.write(docs.getBytes());
            out.flush();
        } catch (IOException e) {
            logger.error("IOException while posting data: " + e);
        }
        try {
            boolean checkResponse = checkResponseCode(urlc);
            logger.info("checkResponse: " + checkResponse + " => " + urlc.getContentType() + "," + urlc.getContent());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try (final InputStream in = urlc.getInputStream()) {
            byte[] data = new byte[in.available()];
            in.read(data);
            String msg = new String(data);
            logger.info("Response_Input: " + msg);
        } catch (IOException e) {
            logger.warn("IOException while reading response: " + e);
        } catch (Throwable e) {
            logger.error("Looks like Solr is secured and would not let us in. Try with another user in '-u' parameter", e);
        }
    }

    private void transToDocument(StringBuilder docs, byte[] line, byte[] fieldSeq) {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, fieldSeq, ctlConfig.getCtlConfig().fieldNames.size());
        Object fieldValue;
        docs.append("{");
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctlConfig.getConcreteFieldValue(fields.get(i), (short) i);
                if (fieldValue != null)
                    addField(docs, ctlConfig.getCtlConfig().fieldNames.get(i), fieldValue);
            } catch (Exception e) {
                GreatLogger.error(GreatLogger.Level.plain, SolrIndexBuilder.class, "LineTransToDocument", "字段类型化失败", fields.get(i), e);
            }
        }
        docs.replace(docs.length() - 1, docs.length(), "}");
    }

    public static String toJSON(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder("[{");
        map.forEach((k, v) -> {
            addField(builder, k, v);
        });
        builder.replace(builder.length() - 1, builder.length(), "}]");
        return builder.toString();
    }

    private static void addField(StringBuilder builder, String field, Object value) {
        builder.append("\"").append(field).append("\":");
        switch (value.getClass().getSimpleName()) {
            case "Integer":
            case "Byte":
            case "Short":
            case "Long":
            case "Float":
            case "Double":
            case "Boolean":
                builder.append(value);
                break;
            case "Date":
            case "Timestamp":
                builder.append(((Date) value).getTime());
                break;
            case "BigDecimal":
                builder.append(((BigDecimal) value).toBigInteger());
                break;
            default:
                builder.append("\"").append(value).append("\"");
                break;
        }
        builder.append(",");
    }

    private HttpURLConnection openConnect(String collectionUrl, String type) {
        HttpURLConnection urlc = null;
        try {
            URL url = new URL(collectionUrl);
            urlc = (HttpURLConnection) url.openConnection();
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                logger.error("Shouldn't happen: HttpURLConnection doesn't support POST??" + e);
                System.exit(-1);
            }
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", type);
            basicAuth(urlc);
//          urlc.setFixedLengthStreamingMode(length);
            urlc.setChunkedStreamingMode(-1);//use JDK default chunkLen, 4k in Java 8.
            urlc.connect();
            logger.info("已打开" + collectionUrl + " 连接");
        } catch (IOException e) {
            logger.error("Connection error (is Solr running at " + collectionUrl + " ?): " + e);
            System.exit(-1);
        } catch (Exception e) {
            logger.error("POST failed with error " + e.getMessage());
            System.exit(-1);
        }
        return urlc;
    }

    private void basicAuth(HttpURLConnection urlc) throws Exception {
        if (urlc.getURL().getUserInfo() != null) {
            String encoding = Base64.getEncoder().encodeToString(urlc.getURL().getUserInfo().getBytes(US_ASCII));
            urlc.setRequestProperty("Authorization", "Basic " + encoding);
        } else if (System.getProperty(BASIC_AUTH) != null) {
            String basicAuth = System.getProperty(BASIC_AUTH).trim();
            if (!basicAuth.contains(":")) {
                throw new Exception("System property '" + BASIC_AUTH + "' must be of format user:pass");
            }
            urlc.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes(UTF_8)));
        }
    }

    private boolean checkResponseCode(HttpURLConnection urlc) throws IOException, GeneralSecurityException {
        if (urlc.getResponseCode() >= 400) {
            logger.warn("Solr returned an error #" + urlc.getResponseCode() +
                    " (" + urlc.getResponseMessage() + ") for url: " + urlc.getURL());
            Charset charset = StandardCharsets.ISO_8859_1;
            final String contentType = urlc.getContentType();
            // code cloned from ContentStreamBase, but post.jar should be standalone!
            if (contentType != null) {
                int idx = contentType.toLowerCase(Locale.ROOT).indexOf("charset=");
                if (idx > 0) {
                    charset = Charset.forName(contentType.substring(idx + "charset=".length()).trim());
                }
            }
            // Print the response returned by Solr
            try (InputStream errStream = urlc.getErrorStream()) {
                if (errStream != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(errStream, charset));
                    final StringBuilder response = new StringBuilder("Response: ");
                    int ch;
                    while ((ch = br.read()) != -1) {
                        response.append((char) ch);
                    }
                    logger.warn(response.toString().trim());
                }
            }
            if (urlc.getResponseCode() == 401) {
                throw new GeneralSecurityException("Solr requires authentication (response 401). Please try again with '-u' option");
            }
            if (urlc.getResponseCode() == 403) {
                throw new GeneralSecurityException("You are not authorized to perform this action against Solr. (response 403)");
            }
            return false;
        }
        return true;
    }
}
