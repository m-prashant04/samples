package com.sample.telegram_bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * References:
 *     https://github.com/rubenlagus/TelegramBots/wiki/Getting-Started
 *     https://core.telegram.org/bots#6-botfather
 *     
 *
 */
public class App
{
    public static void main(String[] args)
    {
        System.out.println("starting app...");
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            System.out.println("registering bot...");
            botsApi.registerBot(new MyAmazingBot());
            System.out.println("registed bot...");
        }
        catch(TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("done.... ready to play....");
    }
}
