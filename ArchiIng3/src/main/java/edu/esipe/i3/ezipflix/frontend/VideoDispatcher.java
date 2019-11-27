package edu.esipe.i3.ezipflix.frontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.esipe.i3.ezipflix.frontend.data.services.VideoConversion;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.CopyStatus;
import com.microsoft.azure.storage.file.FileRange;
import com.microsoft.azure.storage.file.ListFileItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Created by Gilles GIRAUD gil on 11/4/17.
 */

@SpringBootApplication
@RestController
@EnableWebSocket
@CrossOrigin
@ComponentScan(basePackages = {"edu.esipe.i3.ezipflix.frontend"})
public class VideoDispatcher implements WebSocketConfigurer {

    // rabbitmqadmin -H localhost -u ezip -p pize -V ezip delete queue name=video-conversion-queue
    // rabbitmqadmin -H localhost -u ezip -p pize -V ezip delete exchange name=video-conversion-exchange
    // sudo rabbitmqadmin -u ezip -p pize -V ezip declare exchange name=video-conversion-exchange type=direct
    // sudo rabbitmqadmin -u ezip -p pize -V ezip declare queue name=video-conversion-queue durable=true
    // sudo rabbitmqadmin -u ezip -p pize -V ezip declare binding source="video-conversion-exchange" destination_type="queue" destination="video-conversion-queue" routing_key="video-conversion-queue"
    // MONGO : db.video_conversions.remove({})

    //sudo rabbitmq-server start
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoDispatcher.class);

/*    @Value("${rabbitmq-server.credentials.username}") private String username;
    @Value("${rabbitmq-server.credentials.password}") private String password;
    @Value("${rabbitmq-server.credentials.vhost}") private String vhost;
    @Value("${rabbitmq-server.server}") private String host;
    @Value("${rabbitmq-server.port}") private String port;
    @Value("${conversion.messaging.rabbitmq.conversion-queue}") public  String conversionQueue;
    @Value("${conversion.messaging.rabbitmq.conversion-exchange}") public  String conversionExchange;
*/
    @Autowired VideoConversion videoConversion;
    //@Autowired PubSubTemplate pubSubTemplate;
    public static void main(String[] args) throws Exception {
        SpringApplication.run(VideoDispatcher.class, args);
    }

    // ┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
    // │ REST Resources                                                                                                │
    // └───────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
    @RequestMapping(method = RequestMethod.POST,
                    value = "/convert",
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversionResponse requestConversion(@RequestBody ConversionRequest request) throws JsonProcessingException {
        LOGGER.info("File = {}", request.getPath());
        final ConversionResponse response = new ConversionResponse();
        LOGGER.info("UUID = {}", response.getUuid().toString());
        videoConversion.save(request, response);
	//pubSubTemplate.publish("my-topic",request.getPath());
	System.out.println("request Post convert");
        return response;
    }
    @RequestMapping(method = RequestMethod.GET,
                value = "/directories")
    public String requestiFilesList() throws JsonProcessingException {
        ArrayList<String> liste = new ArrayList<>();
        String json = "";
        try{
            CloudFileClient fileClient = FileClientProvider.getFileClientReference();
            CloudFileShare share = fileClient.getShareReference("archidistriconverter");
            CloudFileDirectory rootDir = share.getRootDirectoryReference();
            for ( ListFileItem fileItem : rootDir.listFilesAndDirectories() ) {
                System.out.println(fileItem.getUri());
                //si c'est un repertoir
                if(fileItem.getClass() == CloudFileDirectory.class)
                {

                }else{
                    String[] arrayOfString = fileItem.getUri().toString().split("/");
                    String name = arrayOfString[arrayOfString.length-1];
                    liste.add(name);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                json = objectMapper.writeValueAsString(liste);
            }
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }
        return json;

    }
    


/*  @Bean
    ConnectionFactory connectionFactory() {
        final CachingConnectionFactory c = new CachingConnectionFactory(host, Integer.parseInt(port));
        c.setVirtualHost(vhost);
        c.setUsername(username);
        c.setPassword(password);
        return c;
    }*/

    @Bean
    public WebSocketHandler videoStatusHandler() {
        return new VideoStatusHandler();
    }
    @Bean
    public WebSocketHandler conversionStatusHandler() {
        return new ConversionStatusHandler();
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(videoStatusHandler(), "/video_status").setAllowedOrigins("*");
        webSocketHandlerRegistry.addHandler(conversionStatusHandler(), "/conversion_status").setAllowedOrigins("*");
    }


//    @Bean
//    AmqpAdmin amqpAdmin() {
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
//        q = rabbitAdmin.declareQueue(new Queue(conversionQueue));
//        rabbitAdmin.declareExchange(new DirectExchange(conversionExchange));
//        Binding binding = BindingBuilder.bind(new Queue(conversionQueue)).to(new DirectExchange(conversionExchange))
//                .with(COMMANDS_QUEUE);
//        rabbitAdmin.declareBinding(binding);
//
//        rabbitAdmin.setAutoStartup(true);
//        return rabbitAdmin;
//    }

//    @Bean(name="video-conversion-template")
//    public RabbitTemplate getVideoConversionTemplate() {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory());
//
//        template.setExchange(conversionExchange);
//        template.setRoutingKey(conversionQueue);
//        template.setQueue(conversionQueue);
//        return template;
//    }

    private static void enumerateDirectoryContents(CloudFileDirectory rootDir) throws StorageException {

        Iterable<ListFileItem> results = rootDir.listFilesAndDirectories();
        for (Iterator<ListFileItem> itr = results.iterator(); itr.hasNext(); ) {
            ListFileItem item = itr.next();
            boolean isDirectory = item.getClass() == CloudFileDirectory.class;
            System.out.println(String.format("\t\t%s: %s", isDirectory ? "Directory " : "File      ", item.getUri().toString()));
            if (isDirectory == true) {
            	enumerateDirectoryContents((CloudFileDirectory) item);
            }
        }
    }

}
