package com.catch42.friend_computer;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream; 

import org.apache.commons.lang3.StringUtils;

import net.dv8tion.jda.client.events.relationship.FriendRequestReceivedEvent;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MyListener extends ListenerAdapter 
{
	private static final String friendComputerDiceEmote = "<:friendcomputer:531511997171499048>";
	private static final Long maxDice = 30L;
	
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) {return; }
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping"))
        {
            this.handlePing(event, content);
        }
        else if(StringUtils.startsWith(content, "!roll")) {
        	this.handleRoll(event, content);
        }
    }
    
    @Override
    public void onFriendRequestReceived(FriendRequestReceivedEvent event) {
    	event.getFriendRequest().accept();
    }
    
    private void handlePing(MessageReceivedEvent event, String content) {
    	this.sendMessage(event, "Pong!");
    }
    
    private void handleRoll(MessageReceivedEvent event, String content) {
    	String contentWithoutPrefix = StringUtils.replace(content, "!roll", "").trim();
    	String[] splitStrings = StringUtils.split(contentWithoutPrefix, " ");
    	String numberString = splitStrings[0];
    	String notes = null;
    	if(splitStrings.length > 1) {
    		notes = StringUtils.join(Arrays.copyOfRange(splitStrings, 1, splitStrings.length), " ");
    	}
    	NumberFormat nf = NumberFormat.getInstance();
    	Number numberOfDice = null;
    	try {
    		try {
			numberOfDice = nf.parse(numberString);
    		} catch(ParseException e) {
    			throw new ParseException("dice number not valid.  Received: " + content, 0);
    		}
			if(numberOfDice.longValue() > maxDice) {
				throw new ParseException(String.format("Greater than maxDice.  Currently only %s or fewer dice is supported", maxDice.toString()), 0);
			}
			
			List<Integer> normalDiceResult = null;
			if(numberOfDice.intValue() > 0) { 
				normalDiceResult = IntStream.range(0, numberOfDice.intValue()).mapToObj(i -> this.rollDice(6)).collect(Collectors.toList());
			} else if (numberOfDice.intValue() <= 0) { //allows for negative or zero dice values
				normalDiceResult = new ArrayList<Integer>();
			}
			Integer computerDiceResult = this.rollDice(6);
			
			//sum all results greater than or equal to 5
			Integer sumOfSuccesses = normalDiceResult.stream().mapToInt(i -> i >= 5 ? 1 : 0).sum(); 
			
			//determine if the computer dice was a success, or the computer sided die
			boolean computerDiceFound = false;
			if(computerDiceResult == 6) {
				computerDiceFound = true;
			} else if (computerDiceResult == 5) {
				sumOfSuccesses++;
			}
			
			//time to build result string
			String numberOfDiceString = numberOfDice.intValue() > 0 ? (new Integer(numberOfDice.intValue() + 1)).toString() : "1";
			String successes = sumOfSuccesses + " " + (computerDiceFound ? "[" + friendComputerDiceEmote + "]": "");
			String notesString = notes != null ? "Notes: " + notes : "";
			
			//converts the list of int results to one str.  example: 1, 2, 2, 3, 5
			String diceResults = normalDiceResult.stream()
					.map(s -> String.valueOf(s))
					.collect(Collectors.toList()).stream ().map (i -> i.toString ()).collect (Collectors.joining (", ")) 
					+ (numberOfDice.intValue() > 0 ? ", " : " ") + (computerDiceFound ? "[" + friendComputerDiceEmote +"]" : "[" + computerDiceResult.toString() + "]");
			String result = String.format("Successes: %s.  Rolled %s dice.  Dice: %s.  %s", 
											successes, numberOfDiceString, diceResults, notesString);
			
			this.sendMessage(event, result);
			return;
			
		} catch (ParseException e) {
			this.sendMessage(event, e.getMessage());
			return;
		}
    }
    
    protected int rollDice(int size) {
    	Random random = new Random();
    	int initialResult = random.nextInt(size);
    	int realResult = initialResult + 1; //random generates [0, size) with size being exclusive.  To get desired result, add 1
    	
    	return realResult;
    }
    
    protected void sendMessage(MessageReceivedEvent event, String message) {
    	MessageChannel channel = event.getChannel();
        channel.sendMessage(message).queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
    }
   
}