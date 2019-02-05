package com.catch42.friend_computer;


import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

@SpringBootApplication
public class App 
{
	public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
	
	@Value("${token}")
	public String token;
	
	@Bean
	public JDA discordBot() throws LoginException {
		JDA api = new JDABuilder(token)
				.setGame(Game.watching("Alpha Complex"))
				.setStatus(OnlineStatus.ONLINE)
				.addEventListener(new MyListener())
				.build();
		return api;
	}
	
/*    @Bean
    public CommandLineRunner demo() {
        return (args) -> {
            
        };
    }*/
}
