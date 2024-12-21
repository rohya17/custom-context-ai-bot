package com.ai.context.custom.bot;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/custom")
@SpringBootApplication
public class CustomContextAiBotApplication {

	@Autowired 
	private ChatClient.Builder chatCLientBuilder;
	private ChatClient chatClient;
	private final String defaultContext = "You are helpful ai friend of user, your name is optimus prime, "
			+ "you talk with user to lift his mood up, tell him jokes, have nice conversation with user.";
	
	public static void main(String[] args) {
		SpringApplication.run(CustomContextAiBotApplication.class, args);
	}
	
	@PostConstruct
	private void constructChatClient() {
		chatClient = chatCLientBuilder.defaultSystem(defaultContext)
				.defaultAdvisors(
						// Chat memory helps us keep context when using the chat bot for up to 100 previous messages.
						new MessageChatMemoryAdvisor(new InMemoryChatMemory(), "USER", 100),
						new SimpleLoggerAdvisor())
				.build();
	}

	@GetMapping("/chat")
	public String chatWithMe( @RequestParam String prompt ) {
		return chatClient.prompt(prompt).call().content();
	}
	
	@PostMapping("/changeContext")
	public ResponseEntity<String> changeChatContext( @RequestParam(defaultValue = defaultContext) String context ){
		
		chatClient = null;
		
		if( context.length() < 100 ) {
			return ResponseEntity.ok("Context length should be 100 characters minimum.");
		}
		
		chatClient = chatCLientBuilder.defaultSystem(context)
				.defaultAdvisors(
						// Chat memory helps us keep context when using the chat bot for up to 100 previous messages.
						new MessageChatMemoryAdvisor(new InMemoryChatMemory(), "USER", 100),
						new SimpleLoggerAdvisor())
				.build();
		
		return ResponseEntity.ok("Chat context has been changed.");
	}
	
	@Bean
	public OpenAPI defineOpenApi() {
	   Server server = new Server();
	   server.setUrl("http://localhost:8080/");
	   server.setDescription("dev");

	   Contact myContact = new Contact();
	   myContact.setName("AI to do list");
	   myContact.setEmail("rohitthorave17895@gmail.com");

	   Info information = new Info()
	           .title( "API's documentation")
			   .version("1.0")
			   .description("This API exposes endpoints to related services." )
	           .contact(myContact);

	   return new OpenAPI()
			   .info(information)
			   .servers(List.of(server))
			   .components(new io.swagger.v3.oas.models.Components());
	}
}
